package scripts.route

import java.util.concurrent.atomic.AtomicReference
import java.util.zip.GZIPInputStream

import javax.servlet.http.HttpServletRequest

import org.apache.http.Header
import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.HttpRequestRetryHandler
import org.apache.http.client.RedirectStrategy
import org.apache.http.client.config.CookieSpecs
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.conn.HttpClientConnectionManager
import org.apache.http.entity.InputStreamEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.message.BasicHeader
import org.apache.http.protocol.HttpContext

import com.dianping.cat.Cat
import com.dianping.cat.message.Transaction
import com.netflix.config.DynamicIntProperty
import com.netflix.config.DynamicPropertyFactory
import com.yjfei.pgateway.common.Constants
import com.yjfei.pgateway.common.GateException
import com.yjfei.pgateway.common.GateHeaders
import com.yjfei.pgateway.context.RequestContext
import com.yjfei.pgateway.filters.GateFilter
import com.yjfei.pgateway.hystrix.GateRequestCommandForSemaphoreIsolation
import com.yjfei.pgateway.hystrix.GateRequestCommandForThreadIsolation
import com.yjfei.pgateway.util.Debug
import com.yjfei.pgateway.util.HTTPRequestUtils

public class ExecuteRoute extends GateFilter{
	private static final DynamicIntProperty MAX_CONNECTIONS = DynamicPropertyFactory.getInstance().getIntProperty(Constants.GateClientMaxConnections, 500)
	private static final DynamicIntProperty MAX_CONNECTIONS_PER_ROUTE = DynamicPropertyFactory.getInstance().getIntProperty(Constants.GateClientRouteMaxConnections, 20)


	private static final Runnable loader = new Runnable() {
		@Override
		void run() {
			ExecuteRoute.loadClient();
		}
	}

	private static final AtomicReference<CloseableHttpClient> clientRef = new AtomicReference<CloseableHttpClient>(newClient());
	private static final Timer managerTimer = new Timer();

	// cleans expired connections at an interval
	static {
		MAX_CONNECTIONS.addCallback(loader)
		MAX_CONNECTIONS_PER_ROUTE.addCallback(loader)

		managerTimer.schedule(new TimerTask() {
					@Override
					void run() {
						try {
							final CloseableHttpClient hc = clientRef.get();
							if (hc == null) return;
							hc.getConnectionManager().closeExpiredConnections();
						} catch (Throwable t) {
							Cat.logError("error closing expired connections", t);
						}
					}
				}, 30000, 5000)
	}

	private static final HttpClientConnectionManager newConnectionManager() {
		HttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(MAX_CONNECTIONS.get());
		cm.setDefaultMaxPerRoute(MAX_CONNECTIONS_PER_ROUTE.get());
		return cm;
	}

	public static final void loadClient() {
		final CloseableHttpClient oldClient = clientRef.get();
		clientRef.set(newClient())
		if (oldClient != null) {
			managerTimer.schedule(new TimerTask() {
						@Override
						void run() {
							try {
								oldClient.close();
							} catch (Throwable t) {
								Cat.logError("error shutting down old connection manager", t);
							}
						}
					}, 30000);
		}

	}

	private static final CloseableHttpClient newClient() {
		// I could statically cache the connection manager but we will probably want to make some of its properties
		// dynamic in the near future also
		RequestConfig config = RequestConfig.custom()
				.setCookieSpec(CookieSpecs.IGNORE_COOKIES)
				.build();
		HttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(0, false);
		RedirectStrategy redirectStrategy = new RedirectStrategy() {
					@Override
					boolean isRedirected(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) {
						return false
					}

					@Override
					HttpUriRequest getRedirect(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) {
						return null
					}
				};
		CloseableHttpClient httpclient = HttpClients.custom()
				.disableContentCompression()
				.setConnectionManager(newConnectionManager())
				.setDefaultRequestConfig(config)
				.setRetryHandler(retryHandler)
				.setRedirectStrategy(redirectStrategy)
				.disableCookieManagement()
				.build();
		return httpclient;
	}

	@Override
	public String filterType() {
		return "route";
	}

	
	public int filterOrder(){
		return 20;
		
	}

	@Override
	public boolean shouldFilter() {
		RequestContext ctx = RequestContext.getCurrentContext();
		return ctx.getRouteUrl() != null && ctx.sendGateResponse();
	}


	@Override
	public Object run() {
		Transaction tran = Cat.getProducer().newTransaction("ExecuteRoute", "run");
		RequestContext ctx = RequestContext.getCurrentContext();
		HttpServletRequest request = ctx.getRequest();
		String url = ctx.getRouteUrl().toString();
		try{


			HttpClient httpclient = clientRef.get();
			Header[] headers = buildGateRequestHeaders(request)
			InputStream requestEntity = getRequestBody(request)
			int contentLength = request.getContentLength()
			String groupName = ctx.getRouteGroup();
			String routName = ctx.getRouteName();

			if (groupName == null) groupName = Constants.DefaultGroup;
			if (routName == null) routName = Constants.DefaultName;
			RequestConfig requestConfig = buildRequestConfig(routName, groupName)
			String verb = request.getMethod().toUpperCase();

			HttpResponse response = forward(httpclient, requestConfig, verb, url, headers, requestEntity, contentLength, groupName, routName);
			setResponse(response)

		}catch(Throwable t){
			String originUrl = getOriginatingURL()
			String targetUrl = url
			String targetIp = "unknown"
			try {
				targetIp = InetAddress.getByName(ctx.getRouteUrl().getHost()).getHostAddress()
			} catch (Exception ignore) { }

			Exception ex = e
			String errorMsg = "[${ex.class.simpleName}]{${ex.message}}   "
			Throwable cause = null
			while ((cause = ex.getCause()) != null) {
				ex = cause
				errorMsg = "${errorMsg}[${ex.class.simpleName}]{${ex.message}}   "
			}

			Cat.logError("Service Execution Error","OriginUrl: ${originUrl}\nTargetUrl: ${targetUrl}\nTargetIp: ${targetIp}\nCause: ${errorMsg}", t)
			throw new GateException(errorMsg,500,"GateExecutorError")
		}finally{
			tran.complete();
		}


	}

	private String getOriginatingURL() {
		HttpServletRequest request = RequestContext.getCurrentContext().getRequest();

		String protocol = request.getHeader("x-forwarded-proto")
		if (protocol == null) protocol = "http"
		if(request.getLocalPort()==8443) protocol = "https"
		String host = request.getHeader("host")
		String uri = request.getRequestURI();
		def URL = "${protocol}://${host}${uri}"
		if (request.getQueryString() != null) {
			URL += "?${request.getQueryString()}"
		}
		return URL
	}


	void setResponse(HttpResponse response) {
		RequestContext ctx = RequestContext.getCurrentContext();
		ctx.setResponseStatusCode(response.getStatusLine().statusCode);

		boolean isOriginResponseGZipped = false
		String headerName, headerValue;
		response.getAllHeaders()?.each { Header header ->
			headerName = header.name;
			headerValue = header.value;
			ctx.addOriginResponseHeader(headerName, headerValue);
			if (headerName.equalsIgnoreCase(GateHeaders.CONTENT_LENGTH)) {
				ctx.setOriginContentLength(headerValue);
			}
			if (isValidGateResponseHeader(headerName)) {
				ctx.addGateResponseHeader(headerName, headerValue);
			}
			if (headerName.equalsIgnoreCase(GateHeaders.CONTENT_ENCODING)) {
				if (HTTPRequestUtils.isGzipped(headerValue)) {
					isOriginResponseGZipped = true;
				}
			}
			if (Debug.debugRequest()) {
				Debug.addRequestDebug("ORIGIN_RESPONSE:: < ${header.name}, ${header.value}")
			}
		}
		ctx.setResponseGZipped(isOriginResponseGZipped);

		InputStream inputStream = response?.entity?.content;
		if (Debug.debugRequest()) {
			if (inputStream == null) {
				Debug.addRequestDebug("ORIGIN_RESPONSE:: < null ");
			}else{
				byte[] origBytes = inputStream.bytes;
				byte[] contentBytes = origBytes;
				if (isOriginResponseGZipped) {
					contentBytes = new GZIPInputStream(new ByteArrayInputStream(origBytes)).bytes
				}
				String entity = new String(contentBytes);
				Debug.addRequestDebug("ORIGIN_RESPONSE:: < ${entity}");

				inputStream = new ByteArrayInputStream(origBytes);
			}
		}
		ctx.setResponseDataStream(inputStream);
	}

	def Header[] buildGateRequestHeaders(HttpServletRequest request) {
		Map<String, Header> headersMap = new HashMap<>();

		Enumeration headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String name = ((String) headerNames.nextElement()).toLowerCase();
			String value = request.getHeader(name);
			if (isValidGateRequestHeader(name)) {
				headersMap.put(name, new BasicHeader(name, value));
			}
		}

		Map<String, String> gateRequestHeaders = RequestContext.getCurrentContext().getGateRequestHeaders();
		gateRequestHeaders.entrySet().each {
			String name = it.getKey().toLowerCase();
			String value = it.getValue();
			headersMap.put(name, new BasicHeader(name, value));
		}

		if (RequestContext.getCurrentContext().getResponseGZipped()) {
			String name = "accept-encoding";
			String value = "gzip";
			headersMap.put(name, new BasicHeader(name, value));
		}
		return headersMap.values();
	}

	def HttpResponse forward(HttpClient httpclient, RequestConfig requestConfig, String verb, String url, Header[] headers, InputStream requestEntity, int contentLength, String groupName, String routName) {
		requestEntity = debug(verb, url, headers, requestEntity, contentLength);
		HttpUriRequest httpUriRequest;

		switch (verb) {
			case 'POST':
				httpUriRequest = new HttpPost(url);
				((HttpPost)httpUriRequest).setConfig(requestConfig);
				InputStreamEntity entity = new InputStreamEntity(requestEntity, contentLength);
				httpUriRequest.setEntity(entity);
				break;
			case 'PUT':
				httpUriRequest = new HttpPut(url);
				((HttpPut)httpUriRequest).setConfig(requestConfig);
				InputStreamEntity entity = new InputStreamEntity(requestEntity, contentLength);
				httpUriRequest.setEntity(entity)
				break;
			default:
				httpUriRequest = RequestBuilder.create(verb).setUri(url).setConfig(requestConfig).build();
		}
		httpUriRequest.setHeaders(headers)

		String  isolationStrategy = DynamicPropertyFactory.instance.getStringProperty(routName + ".isolation.strategy", null).get();
		if (isolationStrategy == null) {
			isolationStrategy = DynamicPropertyFactory.instance.getStringProperty(groupName + ".isolation.strategy", null).get();
		}
		if (isolationStrategy == null) {
			isolationStrategy = DynamicPropertyFactory.instance.getStringProperty("gate.isolation.strategy.global", "SEMAPHORE").get();
		}

		long start = System.currentTimeMillis();
		try {
			if("THREAD".equalsIgnoreCase(isolationStrategy)){
				return new GateRequestCommandForThreadIsolation(httpclient, httpUriRequest, groupName, routName).execute();
			}else{
				return new GateRequestCommandForSemaphoreIsolation(httpclient, httpUriRequest, groupName, routName).execute();
			}
		} finally {
			RequestContext.getCurrentContext().set("remoteCallCost", System.currentTimeMillis() - start)
		}
	}
	def InputStream debug(String verb, String url, Header[] headers, InputStream requestEntity, int contentLength) {
		if (Debug.debugRequest()) {
			RequestContext.getCurrentContext().addGateResponseHeader("x-target-url", url)
			Debug.addRequestDebug("GATE:: url=${url}")
			headers.each {
				Debug.addRequestDebug("GATE::> ${it.name}  ${it.value}")
			}
			if (requestEntity != null) {
				requestEntity = debugRequestEntity(requestEntity)
			}
		}
		return requestEntity
	}

	InputStream debugRequestEntity(InputStream inputStream) {
		if (Debug.debugRequestHeadersOnly()) return inputStream
		if (inputStream == null) return null
		byte[] entityBytes = inputStream.getBytes();
		String entity = new String(entityBytes);
		Debug.addRequestDebug("GATE::> ${entity}")
		return new ByteArrayInputStream(entityBytes);
	}

	boolean isValidGateRequestHeader(String name) {
		if (name.toLowerCase().contains("content-length")) return false;
		if (!RequestContext.getCurrentContext().getResponseGZipped()) {
			if (name.toLowerCase().contains("accept-encoding")) return false;
		}
		return true;
	}

	boolean isValidGateResponseHeader(String name) {
		switch (name.toLowerCase()) {
			case "connection":
			case "content-length":
			case "content-encoding":
			case "server":
			case "transfer-encoding":
			case "access-control-allow-origin":
			case "access-control-allow-headers":
				return false
			default:
				return true
		}
	}

	def getRequestBody(HttpServletRequest request) {
		Object requestEntity = null;
		try {
			requestEntity = request.getInputStream();
		} catch (IOException e) {
			//no requestBody is ok.
		}
		return requestEntity
	}

	private RequestConfig buildRequestConfig(String routName, String groupName) {

		RequestConfig.Builder builder = RequestConfig.custom();

		int connectTimeout = DynamicPropertyFactory.instance.getIntProperty(routName + ".connect.timeout", 0).get();
		if (connectTimeout == 0) {
			connectTimeout = DynamicPropertyFactory.instance.getIntProperty(groupName + ".connect.timeout", 0).get();
		}
		if (connectTimeout == 0) {
			connectTimeout = DynamicPropertyFactory.instance.getIntProperty("gate.connect.timeout.global", 2000).get();
		}
		builder.setConnectTimeout(connectTimeout)

		int socketTimeout = DynamicPropertyFactory.instance.getIntProperty(routName + ".socket.timeout", 0).get();
		if (socketTimeout == 0) {
			socketTimeout = DynamicPropertyFactory.instance.getIntProperty(groupName + ".socket.timeout", 0).get();
		}
		if (socketTimeout == 0) {
			socketTimeout = DynamicPropertyFactory.instance.getIntProperty("gate.socket.timeout.global", 10000).get();
		}
		builder.setSocketTimeout(socketTimeout)

		int requestConnectionTimeout = DynamicPropertyFactory.instance.getIntProperty(routName + ".request.connection.timeout", 0).get();
		if (requestConnectionTimeout == 0) {
			requestConnectionTimeout = DynamicPropertyFactory.instance.getIntProperty(groupName + ".request.connection.timeout", 0).get();
		}
		if (requestConnectionTimeout == 0) {
			requestConnectionTimeout = DynamicPropertyFactory.instance.getIntProperty("gate.request.connection.timeout.global", 10).get();
		}
		builder.setConnectionRequestTimeout(requestConnectionTimeout)

		return  builder.build();
	}
}

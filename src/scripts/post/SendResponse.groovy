package scripts.post

import javax.servlet.http.HttpServletResponse

import com.dianping.cat.Cat
import com.dianping.cat.message.Transaction
import com.netflix.config.DynamicIntProperty
import com.netflix.config.DynamicPropertyFactory
import com.yjfei.pgateway.common.*
import com.yjfei.pgateway.context.RequestContext
import com.yjfei.pgateway.filters.GateFilter

public class SendResponse extends GateFilter {
	private static final DynamicIntProperty BUFFER_SIZE = DynamicPropertyFactory.getInstance().getIntProperty(Constants.GateInitialStreamBufferSize, 1024);

	@Override
	public String filterType() {
		return 'post';
	}

	@Override
	public int filterOrder() {
		return 100;
	}

	@Override
	public boolean shouldFilter() {
		RequestContext context = RequestContext.getCurrentContext();
		return context.getResponseBody() != null || context.getResponseDataStream() != null;
	}

	@Override
	public Object run() {
		Transaction tran = Cat.getProducer().newTransaction("SendResponse", "run");
		RequestContext context = RequestContext.currentContext;
		
		HttpServletResponse servletResponse = context.getResponse();
		OutputStream outStream = servletResponse.getOutputStream();
		String responseBody = context.getResponseBody();
		InputStream responseDataStream = context.getResponseDataStream();
		
		InputStream inStream = null;
		try{
			if(responseBody != null){							
				inStream = new ByteArrayInputStream(responseBody.bytes);
			}else{
				inStream = responseDataStream;
			}
			
			writeResponse(inStream, outStream);
		}catch(Throwable t){
			Cat.logError(t);
		}finally{
			try {
				inStream.close();
				outStream.flush()
				outStream.close()
			} catch (IOException e) {
			}
			tran.complete();
		}
	}
	
	private void writeResponse(InputStream zin, OutputStream out) {
		long start = System.currentTimeMillis();
		
		long readCost = 0; // store the cost for reading data from server
		long writeCost = 0; // store the cost for sending data to client
		
		long begin = 0;
		long end = 0;
		try {
			byte[] bytes = new byte[BUFFER_SIZE.get()];
			int bytesRead = -1;
			
			begin = System.currentTimeMillis()
			while ((bytesRead = zin.read(bytes)) != -1) {
				end = System.currentTimeMillis()
				readCost += (end-begin)

				begin = end
				try {
					out.write(bytes, 0, bytesRead);
					out.flush();
				} catch (IOException e) {
					Cat.logError(e)
				} finally {
					end = System.currentTimeMillis()
					writeCost += (end-begin)
				}

				// doubles buffer size if previous read filled it
				if (bytesRead == bytes.length) {
					bytes = new byte[bytes.length * 2]
				}

				begin = end
			}
		}finally{
			RequestContext.getCurrentContext().set("sendResponseCost", System.currentTimeMillis() - start)
			RequestContext.getCurrentContext().set("sendResponseCost:read", readCost)
			RequestContext.getCurrentContext().set("sendResponseCost:write", writeCost)
		}
	}
}
package com.ctrip.gateway.client;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ctrip.gateway.common.BusinessResponse;
import com.ctrip.gateway.common.GateRequest;
import com.ctrip.gateway.common.GateResponse;
import com.ctrip.gateway.common.HeartBeatResponse;

public class DefaultGateClient implements GateClient {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultGateClient.class);
	
	private ConcurrentHashMap<String,GateConnectionPool> pools;
	
	public DefaultGateClient(){
		this.pools = new ConcurrentHashMap<String,GateConnectionPool>();
	}
	
    private volatile int connectTimeout = 5000;
    private volatile int readTimeout = 10000;
    public volatile int minResponseLength = 6;
    public volatile int maxResponseLength = 1024*1024*2;
    
	private volatile int minCount;
	private volatile int maxCount;

	public GateResponse request(String host, int port, GateRequest request)throws Exception {
		
		 return request(host, port, request, readTimeout);
	}

	public GateResponse request(String host, int port, GateRequest request,int timeout) throws Exception {

		GateConnectionPool pool = getConnectionPool(host, port);
		SocketGateConnection conn = null;
        try{
            conn = (SocketGateConnection) pool.lease();

            sendRequest(request, conn);
            return receiveResponse(conn);
        }catch (Throwable t){
        	try{
	            if (conn != null) {
	                conn.close();
	            }
        	}finally{
        		pool.releaseAndClose(conn);
        	}
            throw new Exception(t);
        }finally {
            if (conn != null) {
                pool.release(conn);
            }
        }

	}
	
    protected void sendRequest(GateRequest request, SocketGateConnection conn) throws IOException {
        OutputStream out = conn.getOutputStream();
        out.write(request.getLenBytes());
        out.write(request.getDataBytes());
        out.flush();
    }
    
    protected GateResponse receiveResponse(SocketGateConnection conn) throws Exception {
        InputStream in = conn.getInputStream();
        byte[] lenBytes = new byte[8];
        int offset=0;
        int b;
        while (offset < 8 && (b = in.read()) != -1) {
            lenBytes[offset++] = (byte) b;
        }
        if(offset==0) throw new Exception("Connection is closed");
        if(offset!=8) throw new Exception("Failed to read 8 bytes length info.");

        int len = Integer.valueOf(new String(lenBytes).trim());
        if (len < minResponseLength || len > maxResponseLength) {
            throw  new Exception("Response size is illegal.");
        }

        long time = System.currentTimeMillis();
        byte[] dataBytes = new byte[len];
        offset=0;
        while (offset < len && (b = in.read()) != -1) {
            dataBytes[offset++] = (byte) b;
        }
        if(offset!=len) throw new Exception("Failed to read the complete response.");

        GateResponse  response = null;
        if (len == 6) {
        	response = new HeartBeatResponse();
        }else{
        	response = new BusinessResponse();
        }

        response.setCreateTime(time);
        response.setLen(len);
        response.setLenBytes(lenBytes);
        response.setDataBytes(dataBytes);

        return response;
    }
    
    protected GateConnectionPool  getConnectionPool(String host, int port) {
        String key = host+ ":" + port;

        GateConnectionPool pool = pools.get(key);
        if (pool == null) {
        	GateConnectionFactory connectFactory = new GateConnectionFactory(host, port, connectTimeout, readTimeout);
            pool = new GateConnectionPool( minCount, maxCount, connectFactory);
            GateConnectionPool found = pools.putIfAbsent(key, pool);
            if (found != null) {
                pool= found;
            }
        }

        return pool;
    }

    
    public void releaseAllConnections() {
        for (GateConnectionPool pool : pools.values()) {
            pool.close();
        }

    }
}

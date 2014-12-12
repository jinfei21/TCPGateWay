package com.ctrip.gateway.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketGateConnection implements GateConnection {

	private final long createTime = System.currentTimeMillis();

	private boolean closed = false;

	private String host;
	private int port;
	private volatile int connectTimeout;
	private volatile int readTimeout;

	private volatile Socket socket = null;
	private volatile InputStream inputStream;
	private volatile OutputStream outputStream;
	private volatile long connectTime;

	public SocketGateConnection(String host, int port, int connectTimeout,int readTimeout) {
		this.host = host;
		this.port = port;
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
	}

	public InputStream getInputStream() throws IOException {
        ensureSocket();
        return inputStream;
	}

	public OutputStream getOutputStream() throws IOException {
        ensureSocket();
        return outputStream;
	}

	public long getCreateTime() {
	
		return this.createTime;
	}

	public long getConnectTime() {
		return this.connectTime;
	}

	public boolean isClosed() {
        if (socket != null) {
            if (socket.isClosed()) {
                closed = true;
            }
        }
        return closed;
	}

	public void close() {
        closed = true;
        inputStream = null;
        outputStream = null;
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception e) {}
            socket = null;
        }
	}
    private void ensureSocket() throws IOException {
        if (socket == null) {
            socket = new Socket();
            socket.setKeepAlive(true);
            socket.setSoTimeout(readTimeout);
            socket.connect(new InetSocketAddress(host,port), connectTimeout);
            connectTime = System.currentTimeMillis();
        }
        if (outputStream == null) {
            outputStream = new BufferedOutputStream(socket.getOutputStream());
        }
        if (inputStream == null) {
            inputStream = new BufferedInputStream(socket.getInputStream());
        }
    }
    
    public InetAddress getLocalAddress() {
        if (this.socket != null) {
            return this.socket.getLocalAddress();
        } else {
            return null;
        }
    }

    public int getLocalPort() {
        if (this.socket != null) {
            return this.socket.getLocalPort();
        } else {
            return -1;
        }
    }

    public InetAddress getRemoteAddress() {
        if (this.socket != null) {
            return this.socket.getInetAddress();
        } else {
            return null;
        }
    }
}

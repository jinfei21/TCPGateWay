package com.ctrip.gateway.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface GateConnection {
	InputStream getInputStream() throws IOException;

	OutputStream getOutputStream() throws IOException;

	long getCreateTime();

	long getConnectTime();

	boolean isClosed();

	void close();
}

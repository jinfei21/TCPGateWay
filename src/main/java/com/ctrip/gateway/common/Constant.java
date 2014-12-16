package com.ctrip.gateway.common;

public class Constant {

	public static final GateRequest UNKNOW_REQUEST = new UnknowRequest();

	public static int MIN_LENGTH = 6;
	public static int MAX_LENGTH = 1024 * 8;

	public static int DEFAULE_CONNECT_TIMEOUT = 1000;
	public static int DEFAULE_READ_TIMEOUT = 1000 * 2;

	public static final String CHANNEL_CREATE_TIME = "ChannelCreateTime";

	public static final String PREV_ROUTE_PATH = "com.ctrip.gateway.route.pre";
	public static final String PROC_ROUTE_PATH = "com.ctrip.gateway.route.process";
	public static final String POST_ROUTE_PATH = "com.ctrip.gateway.route.post";

	public static final String PREV = "pre";
	public static final String PROC = "proc";
	public static final String POST = "post";
}

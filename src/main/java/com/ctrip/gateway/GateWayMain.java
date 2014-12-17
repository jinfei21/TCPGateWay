package com.ctrip.gateway;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.gateway.util.ShutDownHookManager;

public class GateWayMain {
	
    private static final Logger logger = LoggerFactory.getLogger(GateWayMain.class);

    private static String APPLICATION_ID = "gateway";

	public static void main(String args[]){
		
        System.setProperty("archaius.deployment.applicationId", APPLICATION_ID);

        String environment = System.getProperty("archaius.deployment.environment");
        if(environment==null || environment.equals("")){
            System.setProperty("archaius.deployment.environment", "locale");
        }
		
        printStartupAndShutdownMsg(args);
        
        TCPGateWayServer gateWay = null;
        try {
        	gateWay = new TCPGateWayServer();
        	gateWay.start();

            final TCPGateWayServer finalGatekeeper = gateWay;
            ShutDownHookManager.get().addShutDown(new Runnable() {
            
                public void run() {
                	finalGatekeeper.close();
                }
            },Integer.MAX_VALUE);

        } catch (Exception e) {
            if(gateWay!=null) gateWay.close();
            logger.error("Can not to start the TcpGatekeeper then is going to shutdown", e);
        }
	}
	
    private static void printStartupAndShutdownMsg(String[] args) {
        String host= "Unknown";
        try {
            host = InetAddress.getLocalHost().toString();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        final String hostName = host;
        final String className = GateWayMain.class.getSimpleName();

        logger.info("STARTUP_MSG:\n" +
                        "*******************************************\n" +
                        "\tStarting : {}\n" +
                        "\tHost : {}\n" +
                        "\tArgs : {}\n" +
                        "*******************************************",
                className,hostName, Arrays.toString(args));

        ShutDownHookManager.get().addShutDown(new Runnable() {
            
            public void run() {
            	logger.info("SHUTDOWN_MSG:\n" +
                                "*******************************************\n" +
                                "\tShutting down : {}\n" +
                                "\tHost : {}\n" +
                                "*******************************************",
                        className,hostName);

            }
        }, 1);
    }

}

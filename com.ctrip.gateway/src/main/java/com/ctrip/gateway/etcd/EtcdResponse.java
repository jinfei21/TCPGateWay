package com.ctrip.gateway.etcd;

public class EtcdResponse {

    public String action;
    public EtcdNode node;
    public EtcdNode prevNode;

    @Override
    public String toString() {
        return Json.format(this);
    }
}
package com.ctrip.gateway.common;
public enum ExecuteStatus {
    SUCCESS(1), SKIPPED(-1), DISABLED(-2), FAILED(-3);

    private int status;
    ExecuteStatus(int status) {
        this.status = status;
    }

}
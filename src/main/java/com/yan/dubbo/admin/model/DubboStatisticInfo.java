package com.yan.dubbo.admin.model;

import java.io.Serializable;

public class DubboStatisticInfo implements Serializable {
    private static final long serialVersionUID = 6056446649517777168L;

    private int providerCount;
    private int providerInterfaceCount;
    private int consumerCount;
    private int consumerInterfaceCount;
    private int overrideCount;
    private int overrideInterfaceCount;
    private int routeCount;

    public int getProviderCount() {
        return providerCount;
    }

    public void setProviderCount(int providerCount) {
        this.providerCount = providerCount;
    }

    public int getProviderInterfaceCount() {
        return providerInterfaceCount;
    }

    public void setProviderInterfaceCount(int providerInterfaceCount) {
        this.providerInterfaceCount = providerInterfaceCount;
    }

    public int getConsumerCount() {
        return consumerCount;
    }

    public void setConsumerCount(int consumerCount) {
        this.consumerCount = consumerCount;
    }

    public int getConsumerInterfaceCount() {
        return consumerInterfaceCount;
    }

    public void setConsumerInterfaceCount(int consumerInterfaceCount) {
        this.consumerInterfaceCount = consumerInterfaceCount;
    }

    public int getOverrideCount() {
        return overrideCount;
    }

    public void setOverrideCount(int overrideCount) {
        this.overrideCount = overrideCount;
    }

    public int getOverrideInterfaceCount() {
        return overrideInterfaceCount;
    }

    public void setOverrideInterfaceCount(int overrideInterfaceCount) {
        this.overrideInterfaceCount = overrideInterfaceCount;
    }

    public int getRouteCount() {
        return routeCount;
    }

    public void setRouteCount(int routeCount) {
        this.routeCount = routeCount;
    }

    @Override
    public String toString() {
        return "DubboStatisticInfo{" +
                "providerCount=" + providerCount +
                ", providerInterfaceCount=" + providerInterfaceCount +
                ", consumerCount=" + consumerCount +
                ", consumerInterfaceCount=" + consumerInterfaceCount +
                ", overrideCount=" + overrideCount +
                ", overrideInterfaceCount=" + overrideInterfaceCount +
                ", routeCount=" + routeCount +
                '}';
    }
}

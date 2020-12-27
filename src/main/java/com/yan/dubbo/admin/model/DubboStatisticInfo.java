package com.yan.dubbo.admin.model;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class DubboStatisticInfo implements Serializable {
    private static final long serialVersionUID = 6056446649517777168L;

    private int providerCount;
    private int providerInterfaceCount;
    private Set<String> providerIpSet;
    private Set<String> providerInterfaceSet;

    private int consumerCount;
    private int consumerInterfaceCount;

    private int overrideCount;
    private int overrideInterfaceCount;
    private int routeCount;

    //provider; ip:port,providerStatistic
    private List<DubboProviderStatisticInfo> providerStatisticInfoList;

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

    public Set<String> getProviderIpSet() {
        return providerIpSet;
    }

    public void setProviderIpSet(Set<String> providerIpSet) {
        this.providerIpSet = providerIpSet;
    }

    public Set<String> getProviderInterfaceSet() {
        return providerInterfaceSet;
    }

    public void setProviderInterfaceSet(Set<String> providerInterfaceSet) {
        this.providerInterfaceSet = providerInterfaceSet;
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

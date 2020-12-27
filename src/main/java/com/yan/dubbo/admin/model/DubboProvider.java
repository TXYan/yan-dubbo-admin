package com.yan.dubbo.admin.model;

import com.alibaba.dubbo.common.Constants;

/**
 * http://dubbo.apache.org/zh-cn/docs/user/references/xml/dubbo-provider.html
 */
public class DubboProvider extends DubboInfo {
    private static final long serialVersionUID = -6529408278902920771L;

    private String application;
    private String methods;

    private boolean anyhost;
    private int delay;
    private int retries = 2;
    private String filter;
    private boolean generic;
    private int threads;
    private long timestamp;
    private boolean dynamic;
    private boolean enabled = true;

    private int weight = -1;//权重

    public DubboProvider() {
        setCategory(Constants.PROVIDERS_CATEGORY);
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getMethods() {
        return methods;
    }

    public void setMethods(String methods) {
        this.methods = methods;
    }

    public boolean isAnyhost() {
        return anyhost;
    }

    public void setAnyhost(boolean anyhost) {
        this.anyhost = anyhost;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public boolean isGeneric() {
        return generic;
    }

    public void setGeneric(boolean generic) {
        this.generic = generic;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "DubboProvider{" +
                "application='" + application + '\'' +
                ", methods='" + methods + '\'' +
                ", anyhost=" + anyhost +
                ", delay=" + delay +
                ", retries=" + retries +
                ", filter='" + filter + '\'' +
                ", generic=" + generic +
                ", threads=" + threads +
                ", timestamp=" + timestamp +
                ", dynamic=" + dynamic +
                ", enabled=" + enabled +
                ", weight=" + weight +
                "} " + super.toString();
    }
}

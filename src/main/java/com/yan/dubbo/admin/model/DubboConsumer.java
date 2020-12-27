package com.yan.dubbo.admin.model;

import com.alibaba.dubbo.common.Constants;

/**
 * http://dubbo.apache.org/zh-cn/docs/user/references/xml/dubbo-consumer.html
 */
public class DubboConsumer extends DubboInfo {
    private static final long serialVersionUID = 2743499500899139042L;

    private String application;
    private String methods;

    private boolean check;//check > default.check
    private long pid;
    private String filter;

    public DubboConsumer() {
        setCategory(Constants.CONSUMERS_CATEGORY);
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

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public long getPid() {
        return pid;
    }

    public void setPid(long pid) {
        this.pid = pid;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    @Override
    public String toString() {
        return "DubboConsumer{" +
                "application='" + application + '\'' +
                ", methods='" + methods + '\'' +
                ", check=" + check +
                ", pid=" + pid +
                ", filter='" + filter + '\'' +
                "} " + super.toString();
    }
}

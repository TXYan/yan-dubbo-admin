package com.yan.dubbo.admin.model;

import com.alibaba.dubbo.common.Constants;

/**
 * http://dubbo.apache.org/zh-cn/docs/user/demos/routing-rule-deprecated.html
 */
public class DubboRoute extends DubboInfo {
    private static final long serialVersionUID = -1208664851911618719L;

    private String group;
    private boolean dynamic; //表示该数据为持久数据，当注册方退出时，数据依然保存在注册中心，必填。
    private String rule;
    private boolean enabled = true; //是否生效
    private int priority = 1;
    private boolean force;
    private boolean runtime;

    public DubboRoute() {
        setCategory(Constants.ROUTERS_CATEGORY);
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public boolean isRuntime() {
        return runtime;
    }

    public void setRuntime(boolean runtime) {
        this.runtime = runtime;
    }

    @Override
    public String toString() {
        return "DubboRoute{" +
                "group='" + group + '\'' +
                ", dynamic=" + dynamic +
                ", rule='" + rule + '\'' +
                ", enabled=" + enabled +
                ", priority=" + priority +
                ", force=" + force +
                ", runtime=" + runtime +
                "} " + super.toString();
    }
}

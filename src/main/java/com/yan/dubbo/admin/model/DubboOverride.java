package com.yan.dubbo.admin.model;

import com.alibaba.dubbo.common.Constants;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * http://dubbo.apache.org/zh-cn/docs/user/demos/config-rule-deprecated.html
 */
public class DubboOverride extends DubboInfo {
    private static final long serialVersionUID = -5324578474302788031L;

    private String application;
    private boolean dynamic;//数据是否持久化
    private boolean enabled;//代表该条Override是否生效

    //Constants.DISABLED_KEY,Constants.WEIGHT_KEY,Constants.GROUP_KEY
    private Map<String, String> attributeMap = new ConcurrentHashMap<>();

    public DubboOverride() {
        setCategory(Constants.CONFIGURATORS_CATEGORY);
    }

    @JsonIgnore
    public void initAttribute() {
        if (getUrl() == null) {
            return;
        }
        String disabled = getUrl().getParameter(Constants.DISABLED_KEY, "");
        if (!"".equals(disabled)) {
            attributeMap.put(Constants.DISABLED_KEY, disabled);
        }

        String weight = getUrl().getParameter(Constants.WEIGHT_KEY, "");
        if (!"".equals(weight)) {
            attributeMap.put(Constants.WEIGHT_KEY, weight);
        }
    }

    public void putAllAttr(Map<String, String> attrMap) {
        if (attrMap != null && attrMap.size() > 0) {
            attributeMap.putAll(attrMap);
        }
    }

    public void setAttribute(String attr, String val) {
        attributeMap.put(attr, val);
        if (getUrl() != null) {
        }
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
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

    public Map<String, String> getAttributeMap() {
        return attributeMap;
    }

    @Override
    public String toString() {
        return "DubboOverride{" +
                "application=" + application +
                ", dynamic=" + dynamic +
                ", enabled=" + enabled +
                ", attributeMap=" + attributeMap +
                "} " + super.toString();
    }
}

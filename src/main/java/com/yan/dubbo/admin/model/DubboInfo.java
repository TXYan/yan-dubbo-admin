package com.yan.dubbo.admin.model;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

public class DubboInfo implements Serializable, Cloneable {
    private static final long serialVersionUID = -4448383927730115888L;

    private String category;
    private String protocol;
    private String ip;
    private int port; //consumer 没有port
    private String interfaceName;
    private String version;

    @JsonIgnore
    private URL url;
    private String id;

    @JsonIgnore
    public boolean isRemoved() {
        return Constants.EMPTY_PROTOCOL.equals(protocol);
    }

    public String getCategory() {
        return category;
    }

    void setCategory(String category) {
        this.category = category;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return "DubboInfo{" +
                "category='" + category + '\'' +
                ", protocol='" + protocol + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", interfaceName='" + interfaceName + '\'' +
                ", version='" + version + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}

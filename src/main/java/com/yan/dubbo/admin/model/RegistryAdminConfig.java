package com.yan.dubbo.admin.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * date:2020-04-04
 * Author:Y'an
 */

public class RegistryAdminConfig implements Serializable {
    private static final long serialVersionUID = -3423538415586029526L;

    private String name;
    private String addrName;
    private String group;
    private String addr;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddrName() {
        return addrName;
    }

    public void setAddrName(String addrName) {
        this.addrName = addrName;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistryAdminConfig that = (RegistryAdminConfig) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(addrName, that.addrName) &&
                Objects.equals(group, that.group) &&
                Objects.equals(addr, that.addr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, addrName, group, addr);
    }

    @Override
    public String toString() {
        return "RegistryAdminConfig{" +
                "name='" + name + '\'' +
                ", addrName='" + addrName + '\'' +
                ", group='" + group + '\'' +
                ", addr='" + addr + '\'' +
                '}';
    }
}

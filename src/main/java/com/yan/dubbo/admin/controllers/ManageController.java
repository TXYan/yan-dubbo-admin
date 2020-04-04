package com.yan.dubbo.admin.controllers;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.yan.dubbo.admin.manage.RegistryManager;
import com.yan.dubbo.admin.manage.RegistryOperator;
import com.yan.dubbo.admin.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/manage")
public class ManageController {

    @Autowired
    private RegistryManager registryManager;

    @RequestMapping("/statistic/get")
    public Object getStatistic(String name) {
        DubboStatisticInfo statisticInfo = null;
        RegistryOperator operator = registryManager.getOperator(name);
        if (operator == null) {
            return statisticInfo;
        }

        return operator.getStatisticInfo();
    }

    @RequestMapping("/provider/get")
    public List<? extends DubboInfo> getProvider(String name, @RequestParam(required = false) String ip, @RequestParam(required = false) String interfaceName) {
        RegistryOperator operator = registryManager.getOperator(name);
        if (operator == null) {
            return Collections.emptyList();
        }
        Map<String, String> filterMap =  new HashMap<>();
        if (!StringUtils.isBlank(ip)) {
            filterMap.put(FilterConstants.IP, ip);
        }
        if (!StringUtils.isBlank(interfaceName)) {
            filterMap.put(FilterConstants.INTERFACE_NAME, interfaceName);
        }
        return operator.filterDubboInfo(filterMap, DubboProvider.class);
    }

    @RequestMapping("/override/get")
    public List<? extends DubboInfo> getOverride(String name, @RequestParam(required = false) String ip, @RequestParam(required = false) String interfaceName) {
        RegistryOperator operator = registryManager.getOperator(name);
        if (operator == null) {
            return Collections.emptyList();
        }
        Map<String, String> filterMap =  new HashMap<>();
        if (!StringUtils.isBlank(ip)) {
            filterMap.put(FilterConstants.IP, ip);
        }
        if (!StringUtils.isBlank(interfaceName)) {
            filterMap.put(FilterConstants.INTERFACE_NAME, interfaceName);
        }
        return operator.filterDubboInfo(filterMap, DubboOverride.class);
    }

    @RequestMapping("/override/set")
    public Object addOverride(String name, String ip, int port, String interfaceName, String version, String attr, String val, @RequestParam(required = false, defaultValue = "true") boolean enabled) {
        RegistryOperator operator = registryManager.getOperator(name);
        if (operator == null) {
            return "name:" + name + " can not find";
        }
        DubboOverride override = new DubboOverride();
        override.setIp(ip);
        override.setPort(port);
        override.setInterfaceName(interfaceName);
        override.setVersion(version);
        override.setAttribute(attr, val);
        override.setEnabled(enabled);
        operator.override(override);

        return "SUCCESS";
    }

    @RequestMapping("/override/ip/disabled")
    public String disabled(String name, String ip, int port, @RequestParam(required = false, defaultValue = "true") boolean disabled, @RequestParam(required = false, defaultValue = "-1") int weight) {
        RegistryOperator operator = registryManager.getOperator(name);
        if (operator == null) {
            return "name:" + name + " can not find";
        }
        //查询所有接口
        Map<String, String> filterMap =  new HashMap<>();
        filterMap.put(FilterConstants.IP, ip);
        List<? extends DubboInfo> providerList = operator.filterDubboInfo(filterMap, DubboProvider.class);

        if (providerList.size() == 0) {
            return "can not find any interface";
        }

        providerList.stream().filter(p -> ip.equals(p.getIp()) && port == p.getPort()).forEach(p -> {
            DubboOverride override = new DubboOverride();
            override.setIp(ip);
            override.setPort(port);
            override.setInterfaceName(p.getInterfaceName());
            override.setVersion(p.getVersion());
            override.setAttribute(Constants.DISABLED_KEY, String.valueOf(disabled));
            if (weight > -1) {
                override.setAttribute(Constants.WEIGHT_KEY, String.valueOf(weight));
            }
            //如果权重是默认的那么删除
            if (!disabled && weight == 100) {
                override.setEnabled(false);
            } else {
                override.setEnabled(true);
            }

            operator.override(override);
        });

        return "SUCCESS";
    }
}

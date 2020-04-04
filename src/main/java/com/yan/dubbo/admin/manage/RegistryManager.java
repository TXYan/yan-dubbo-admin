package com.yan.dubbo.admin.manage;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.yan.dubbo.admin.model.RegistryAdminConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Slf4j
@Component
public class RegistryManager implements Serializable {
    private static final long serialVersionUID = 150055856326073516L;

    private Pattern EMPTY_PATTERN = Pattern.compile("\\s+|\\r|\\n");

    private volatile String zkAddrs = "{\"rd\":\"192.168.2.180:2181,192.168.2.181:2181\"}";
    private volatile String registryConfigs = "[{\"name\":\"test\",\"addrName\":\"rd\",\"group\":\"/test\"}]";

    private volatile Map<String, String> zkAddrMap;
    private volatile List<RegistryAdminConfig> registryConfigList;
    private volatile ConcurrentHashMap<String, RegistryOperator> operatorMap = new ConcurrentHashMap<>(2000);

    @PostConstruct
    public void init() {
        //去除空格、换行
        zkAddrs = EMPTY_PATTERN.matcher(zkAddrs).replaceAll("");
        registryConfigs = EMPTY_PATTERN.matcher(registryConfigs).replaceAll("");
        zkAddrMap = JSON.parseObject(zkAddrs, Map.class);
        registryConfigList = JSON.parseArray(registryConfigs, RegistryAdminConfig.class);
        //根据配置的addName补充addr
        fillAddr();
        generateOperator();
    }

    @PreDestroy
    public void destroy() {
        operatorMap.forEach((name, operator) -> {
            operator.destroy();
        });
    }

    public RegistryOperator getOperator(String name) {
        return operatorMap.get(name);
    }

    private void generateOperator() {
        if (CollectionUtils.isEmpty(registryConfigList)) {
            return;
        }
        for (RegistryAdminConfig adminConfig : registryConfigList) {
            RegistryOperator operator = new RegistryOperator(adminConfig);
            operator.afterPropertiesSet();
            operatorMap.put(adminConfig.getName(), operator);
        }
    }

    private void fillAddr() {
        if (CollectionUtils.isEmpty(registryConfigList)) {
            return;
        }
        Iterator<RegistryAdminConfig> iterator = registryConfigList.iterator();
        Set<String> nameSet = new HashSet<>(50);
        while (iterator.hasNext()) {
            RegistryAdminConfig registryConfig = iterator.next();
            //重复的忽略掉
            if (nameSet.contains(registryConfig.getName())) {
                iterator.remove();
                log.warn("RegistryManager registryConfig:{}, name is duplicate, ignore", registryConfig);
                continue;
            }

            String addr = registryConfig.getAddr();
            if (StringUtils.isBlank(addr)) {
                addr = zkAddrMap != null ? zkAddrMap.get(registryConfig.getAddrName()) : null;
            }
            if (StringUtils.isBlank(addr)) {
                iterator.remove();
                log.warn("RegistryManager registryConfig:{}, can not find address", registryConfig);
                continue;
            }
            registryConfig.setAddr(addr);
            nameSet.add(registryConfig.getName());
        }
    }
}

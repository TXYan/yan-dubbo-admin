package com.yan.dubbo.admin.manage;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;
import com.baidu.disconf.client.common.annotations.DisconfUpdateService;
import com.baidu.disconf.client.common.update.IDisconfUpdate;
import com.yan.dubbo.admin.model.RegistryAdminConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
//@DisconfFile(filename = "admin.properties")
//@DisconfUpdateService(classes = {RegistryManager.class})
@PropertySource("classpath:config/dubbo-admin.properties")
public class RegistryManager /*implements IDisconfUpdate*/ {

    private Pattern EMPTY_PATTERN = Pattern.compile("\\s+|\\r|\\n");
    private static final String DEFAULT_ADDRNAME = "default";

    @Value("${zkAddrs}")
    private volatile String zkAddrs;// = "{\"rd\":\"192.168.2.180:2181,192.168.2.181:2181\"}";
    @Value("${registryConfigs}")
    private volatile String registryConfigs;// = "[{\"name\":\"zpJob\",\"addrName\":\"rd\",\"group\":\"zp_dubbo/job\"}]";

    private volatile Map<String, String> zkAddrMap;
    private volatile List<RegistryAdminConfig> registryConfigList;
    private volatile ConcurrentHashMap<String, RegistryOperator> operatorMap = new ConcurrentHashMap<>(100);

    @DisconfFileItem(name = "zkAddrs", associateField = "zkAddrs")
    public String getZkAddrs() {
        return zkAddrs;
    }

    @DisconfFileItem(name = "registryConfigs", associateField = "registryConfigs")
    public String getRegistryConfigs() {
        return registryConfigs;
    }

//    @Override
    @PostConstruct
    public void reload() {
        initTransform();
        //根据配置的addName补充addr
        fillAddr();
        generateOperator();
    }

    private void initTransform() {
        if (StringUtils.isBlank(zkAddrs) || StringUtils.isBlank(registryConfigs)) {
            return;
        }
        //去除空格、换行
        zkAddrs = EMPTY_PATTERN.matcher(zkAddrs).replaceAll("");
        registryConfigs = EMPTY_PATTERN.matcher(registryConfigs).replaceAll("");
        zkAddrMap = JSON.parseObject(zkAddrs, Map.class);
        log.info("registryConfigs: {}", registryConfigs);
        registryConfigList = JSON.parseArray(registryConfigs, RegistryAdminConfig.class);
    }

    @PreDestroy
    public void destroy() {
        operatorMap.forEach((name, operator) -> {
            operator.destroy();
        });
    }

    private void destroyOperators(Collection<RegistryOperator> operators) {
        if (CollectionUtils.isEmpty(operators)) {
            return;
        }
        operators.forEach(RegistryOperator::destroy);
    }

    public RegistryOperator getOperator(String name) {
        return operatorMap.get(name);
    }

    public List<String> getOperatorNameList() {
        if (CollectionUtils.isEmpty(registryConfigList)) {
            return Collections.emptyList();
        }
        return registryConfigList.stream().map(RegistryAdminConfig::getName).collect(Collectors.toList());
    }

    private void generateOperator() {
        if (CollectionUtils.isEmpty(registryConfigList)) {
            destroyOperators(operatorMap.values());
            operatorMap.clear();
            return;
        }
        Set<String> nameSet = registryConfigList.stream().map(RegistryAdminConfig::getName).collect(Collectors.toSet());
        List<RegistryOperator> removeOperatorList = operatorMap.entrySet().stream().filter(entry -> !nameSet.contains(entry.getKey())).map(Map.Entry::getValue).collect(Collectors.toList());
        removeOperatorList.forEach(o -> {
            operatorMap.remove(o.getAdminConfig().getName());
        });

        for (RegistryAdminConfig adminConfig : registryConfigList) {
            //和现有的对比
            RegistryOperator oldOperator = operatorMap.get(adminConfig.getName());
            if (oldOperator == null || !oldOperator.getAdminConfig().equals(adminConfig)) {
                RegistryOperator operator = new RegistryOperator(adminConfig);
                operator.afterPropertiesSet();
                operatorMap.put(adminConfig.getName(), operator);
                if (oldOperator != null) {
                    //释放老的对象
                    removeOperatorList.add(oldOperator);
                }
            }
        }

        destroyOperators(removeOperatorList);
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
                if (StringUtils.isBlank(registryConfig.getAddrName())) {
                    registryConfig.setAddrName(DEFAULT_ADDRNAME);
                }
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

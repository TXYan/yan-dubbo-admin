package com.yan.dubbo.admin.manage;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.RegistryService;
import com.yan.dubbo.admin.model.*;
import com.yan.dubbo.admin.tools.DubboAdminTool;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * date:2020-04-04
 * Author:Y'an
 */

@Slf4j
public class RegistryOperator implements NotifyListener {

    private static final URL SUBSCRIBE = new URL(Constants.ADMIN_PROTOCOL, NetUtils.getLocalHost(), 0, "",
            Constants.INTERFACE_KEY, Constants.ANY_VALUE,
            Constants.GROUP_KEY, Constants.ANY_VALUE,
            Constants.VERSION_KEY, Constants.ANY_VALUE,
            Constants.CLASSIFIER_KEY, Constants.ANY_VALUE,
            Constants.CATEGORY_KEY, Constants.PROVIDERS_CATEGORY + ","
            + Constants.CONSUMERS_CATEGORY + ","
            + Constants.ROUTERS_CATEGORY + ","
            + Constants.CONFIGURATORS_CATEGORY,
            Constants.ENABLED_KEY, Constants.ANY_VALUE,
            Constants.CHECK_KEY, String.valueOf(false));

    private RegistryAdminConfig adminConfig;
    private ReferenceConfig<RegistryService> registryRef;

    //<ip:port:interface:version,DubboProvider>>>>
    private ConcurrentHashMap<String, DubboProvider> providerMap = new ConcurrentHashMap<>(400);
    //<ip:port:interface:version,DubboConsumer>>>>
    private ConcurrentHashMap<String, DubboConsumer> consumerMap = new ConcurrentHashMap<>(400);
    //<ip:port:interface:version,DubboOverride>>>>
    private ConcurrentHashMap<String, DubboOverride> overrideMap = new ConcurrentHashMap<>(100);
    //<ip:port:interface:version,DubboRoute>>>>
    private ConcurrentHashMap<String, DubboRoute> routeMap = new ConcurrentHashMap<>(100);

    public ConcurrentHashMap<String, DubboProvider> getProviderMap() {
        return providerMap;
    }

    public ConcurrentHashMap<String, DubboConsumer> getConsumerMap() {
        return consumerMap;
    }

    public ConcurrentHashMap<String, DubboOverride> getOverrideMap() {
        return overrideMap;
    }

    public ConcurrentHashMap<String, DubboRoute> getRouteMap() {
        return routeMap;
    }

    public RegistryOperator(RegistryAdminConfig config) {
        this.adminConfig = config;
        registryRef = DubboAdminTool.convertZookeeperRegistryServiceRef(adminConfig);
    }

    public void afterPropertiesSet() {
        //初始化 RegistryService实例，并注册监听
        registryRef.get().subscribe(SUBSCRIBE, this);
    }

    public void destroy() {
        registryRef.get().unsubscribe(SUBSCRIBE, this);
        registryRef.destroy();
        adminConfig = null;
        registryRef = null;
    }

    public void override(DubboOverride dubboOverride) {
        if (dubboOverride == null) {
            return;
        }
        //根据设置查看是否已经存在相应设置
        String id = DubboAdminTool.generateUniqId(dubboOverride);
        DubboOverride oldOverride = overrideMap.get(id);
        if (oldOverride == null) {
            //新增 有效的
            if (dubboOverride.isEnabled()) {
                URL url = DubboAdminTool.convertURL(dubboOverride);
                registryRef.get().register(url);
            }
        } else {
            //如果设置项一样 仅仅失效，那么需要移出
            if (!dubboOverride.isEnabled() && oldOverride.isEnabled()) {
                registryRef.get().unregister(oldOverride.getUrl());
            } else {
                oldOverride.putAllAttr(dubboOverride.getAttributeMap());
                oldOverride.setEnabled(dubboOverride.isEnabled());
                //先注册，再取消注册老的，避免中间空当影响
                registryRef.get().register(DubboAdminTool.convertURL(oldOverride));
                registryRef.get().unregister(oldOverride.getUrl());
            }
        }
    }

    public List<? extends DubboInfo> filterDubboInfo(Map<String, String> filterMap, Class clszz) {
        Map<String, ? extends DubboInfo> map = clszz.equals(DubboProvider.class) ? providerMap :
                clszz.equals(DubboConsumer.class) ? consumerMap :
                        clszz.equals(DubboOverride.class) ? overrideMap :
                                clszz.equals(DubboRoute.class) ? routeMap : null;
        if (map == null || map.size() == 0) {
            return Collections.emptyList();
        }
        List<? extends DubboInfo> infList = new ArrayList<>(map.values());
        if (filterMap == null || filterMap.size() <= 0) {
            return infList;
        }

        Iterator<? extends DubboInfo> iterator = infList.iterator();
        while (iterator.hasNext()) {
            DubboInfo dubboInfo = iterator.next();
            String val = filterMap.get(FilterConstants.IP);
            if (val != null && !val.equals(dubboInfo.getIp())) {
                iterator.remove();
                continue;
            }

            val = filterMap.get(FilterConstants.INTERFACE_NAME);
            if (val != null && !val.equals(dubboInfo.getInterfaceName())) {
                iterator.remove();
                continue;
            }
        }
        return infList;
    }

    public DubboStatisticInfo getStatisticInfo() {
        DubboStatisticInfo statisticInfo = new DubboStatisticInfo();
        Set<String> ipSet = DubboAdminTool.newSet(providerMap.size() / 2);
        Set<String> interfaceSet = DubboAdminTool.newSet(providerMap.size() / 2);
        providerMap.values().forEach(p -> {
            ipSet.add(p.getIp());
            interfaceSet.add(p.getInterfaceName());
        });
        statisticInfo.setProviderCount(ipSet.size());
        statisticInfo.setProviderInterfaceCount(interfaceSet.size());

        ipSet.clear();
        interfaceSet.clear();
        consumerMap.values().forEach(c -> {
            ipSet.add(c.getIp());
            interfaceSet.add(c.getInterfaceName());
        });
        statisticInfo.setConsumerCount(ipSet.size());
        statisticInfo.setConsumerInterfaceCount(interfaceSet.size());

        ipSet.clear();
        interfaceSet.clear();
        overrideMap.values().forEach(o -> {
            ipSet.add(o.getIp());
            interfaceSet.add(o.getInterfaceName());
        });
        statisticInfo.setOverrideCount(ipSet.size());
        statisticInfo.setOverrideInterfaceCount(interfaceSet.size());

        statisticInfo.setRouteCount(routeMap.size());

        return statisticInfo;
    }

    //此通知列表一次只会和一个接口有关

    /**
     * empty configurators 通知的ip是操作机器的ip，what？
     * @param urlList
     */
    @Override
    public void notify(List<URL> urlList) {
        if (CollectionUtils.isEmpty(urlList)) {
            return;
        }

        System.out.println(urlList);

        Map<String, DubboProvider> notifyProviderMap = new HashMap<>();
        Map<String, DubboConsumer> notifyConsumerMap = new HashMap<>();
        Map<String, DubboOverride> notifyOverrideMap = new HashMap<>();
        Map<String, DubboRoute> notifyRouteMap = new HashMap<>();

        String interfaceName = urlList.get(0).getServiceInterface();
        for (URL url : urlList) {
            DubboInfo dubboInfo = DubboAdminTool.convertDubboInfo(url);

            if (dubboInfo == null) {
                log.warn("notify url:{}, convert failed", url.toFullString());
                continue;
            }
            if (dubboInfo instanceof DubboProvider) {
                if (!removeEmptyDubboInfo(providerMap, dubboInfo)) {
                    notifyProviderMap.put(DubboAdminTool.generateUniqId(dubboInfo), (DubboProvider) dubboInfo);
                }
            } else if (dubboInfo instanceof DubboConsumer){
                if (!removeEmptyDubboInfo(consumerMap, dubboInfo)) {
                    notifyConsumerMap.put(DubboAdminTool.generateUniqId(dubboInfo), (DubboConsumer) dubboInfo);
                }
            } else if (dubboInfo instanceof DubboOverride) {
                if (!removeEmptyDubboInfo(overrideMap, dubboInfo)) {
                    notifyOverrideMap.put(DubboAdminTool.generateUniqId(dubboInfo), (DubboOverride) dubboInfo);
                }
            } else if (dubboInfo instanceof DubboRoute) {
                if (!removeEmptyDubboInfo(routeMap, dubboInfo)) {
                    notifyRouteMap.put(DubboAdminTool.generateUniqId(dubboInfo), (DubboRoute) dubboInfo);
                }
            }
        }

        removeNotNotifyDubboInfo(interfaceName, providerMap, notifyProviderMap);
        providerMap.putAll(notifyProviderMap);
        removeNotNotifyDubboInfo(interfaceName, consumerMap, notifyConsumerMap);
        consumerMap.putAll(notifyConsumerMap);
        removeNotNotifyDubboInfo(interfaceName, overrideMap, notifyOverrideMap);
        overrideMap.putAll(notifyOverrideMap);
        removeNotNotifyDubboInfo(interfaceName, routeMap, notifyRouteMap);
        routeMap.putAll(notifyRouteMap);

        System.out.println("providerMap:" + providerMap.keySet());
        System.out.println("consumerMap:" + consumerMap.keySet());
        System.out.println("overrideMap:" + overrideMap);
        System.out.println("routeMap:" + routeMap.keySet());
    }

    private boolean removeEmptyDubboInfo(ConcurrentHashMap<String, ? extends DubboInfo> infoMap, DubboInfo dubboInfo) {
        if (dubboInfo == null) {
            return true;
        }
        if (!dubboInfo.isRemoved()) {
            return false;
        }
        //移出通知无效的服务
        infoMap.entrySet().stream().filter(entry -> DubboAdminTool.isMatchRemoved(dubboInfo, entry.getValue()))
                .forEach(entry -> infoMap.remove(entry.getKey()));
        return true;
    }

    private void removeNotNotifyDubboInfo(String interfaceName, ConcurrentHashMap<String, ? extends DubboInfo> dubboInfoMap, Map<String, ? extends DubboInfo> notifyDubboInfoMap) {
        if (notifyDubboInfoMap == null || notifyDubboInfoMap.size() <= 0) {
            return;
        }
        //移除注销但是无感知的老服务
        Set<String> notifyIdSet = notifyDubboInfoMap.keySet();
        dubboInfoMap.values().stream().filter(p -> p.getInterfaceName().equals(interfaceName)).map(DubboAdminTool::generateUniqId)
                .filter(id -> !notifyIdSet.contains(id)).forEach(dubboInfoMap::remove);
    }
}

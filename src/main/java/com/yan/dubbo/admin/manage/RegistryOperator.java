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
import org.apache.commons.lang3.math.NumberUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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

    public RegistryAdminConfig getAdminConfig() {
        return adminConfig;
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

    /**
     * 可能出现的问题：
     * 1. 由于zk通知慢 如果只是notify来维护数据，那么在未完成notify过程中进行二次操作，那么拿到的override还是上次的override，进而导致有部分override没有unregister
     * 2. 如果前端操作过快会导致即时调用了unregister方法，但是zk服务端没有删除，导致该override一直保留
     * @param dubboOverride
     */
    public void override(DubboOverride dubboOverride) {
        if (dubboOverride == null) {
            return;
        }
        //根据设置查看是否已经存在相应设置
        String id = DubboAdminTool.generateUniqId(dubboOverride);
        DubboOverride oldOverride = overrideMap.get(id);
        synchronized (this) {
            URL oldURL = oldOverride != null ? oldOverride.getUrl() : null;
            boolean oldEnabled = oldOverride != null && oldOverride.isEnabled();
            DubboOverride resultOverride = DubboAdminTool.mergeOverride(oldOverride, dubboOverride);
            if (resultOverride.isEnabled() //结果是生效的那么需要更新规则
                    || oldEnabled//老的是生效，新的是失效 需要移除老的配置
            ) {
                URL resultURL = DubboAdminTool.convertURL(resultOverride);
                resultOverride.setUrl(resultURL);
                if (!resultOverride.isEnabled()) {
                    resultOverride.setRemoteUnregister(true);
                }
                overrideMap.put(id, resultOverride);
                if (oldURL != null) {
                    registryRef.get().unregister(oldURL);
                }
                if (resultOverride.isEnabled()) {
                    registryRef.get().register(resultURL);
                }
            }
        }
    }


    public <T extends DubboInfo> List<T> filterDubboInfo(Map<String, String> filterMap, Class<T> clszz) {
        Map<String, T> map = clszz.equals(DubboProvider.class) ? (Map<String, T>) providerMap :
                clszz.equals(DubboConsumer.class) ? (Map<String, T>) consumerMap :
                        clszz.equals(DubboOverride.class) ? (Map<String, T>) overrideMap :
                                clszz.equals(DubboRoute.class) ? (Map<String, T>) routeMap : null;

        if (map == null || map.size() == 0) {
            return Collections.emptyList();
        }
        List<T> infList = new ArrayList<>(map.values());
        if (filterMap == null || filterMap.size() <= 0) {
            return infList;
        }

        List<T> resultList = new ArrayList<>(infList.size() + 1);
        Iterator<T> iterator = infList.iterator();
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
            resultList.add((T) dubboInfo.clone());
        }
        return resultList;
    }

    public List<DubboProviderStatisticInfo> getProviderStatisticInfoList() {
        Map<String, DubboProviderStatisticInfo> statisticInfoMap = DubboAdminTool.newMap(50);

        providerMap.values().forEach(p -> {
            String key = p.getIp() + ":" + p.getPort();
            DubboProviderStatisticInfo statisticInfo = statisticInfoMap.computeIfAbsent(key, f -> new DubboProviderStatisticInfo(p.getIp(), p.getPort()));
            statisticInfo.setInterfaceCount(statisticInfo.getInterfaceCount() + 1);
            statisticInfo.setEnabledInterfaceCount(statisticInfo.getEnabledInterfaceCount() + 1);
        });

        Map<String, Set<String>> disabledInterfaceMap = DubboAdminTool.newMap(statisticInfoMap.size());
        overrideMap.values().forEach(o -> {
            if (!o.isEnabled()) {
                return;
            }
            String key = o.getIp() + ":" + o.getPort();
            DubboProviderStatisticInfo statisticInfo = statisticInfoMap.get(key);
            if (statisticInfo == null) {
                return;
            }
            Set<String> interfaceSet = disabledInterfaceMap.computeIfAbsent(o.getInterfaceName(), f -> DubboAdminTool.newSet(statisticInfo.getInterfaceCount()));
            String disabled = o.getAttributeMap().get(Constants.DISABLED_KEY);
            if ("true".equals(disabled) && !interfaceSet.contains(o.getInterfaceName())) {
                statisticInfo.setDisabledInterfaceCount(statisticInfo.getDisabledInterfaceCount() + 1);
                statisticInfo.setEnabledInterfaceCount(statisticInfo.getEnabledInterfaceCount() - 1);
                interfaceSet.add(o.getInterfaceName());
            }

            int weight = NumberUtils.toInt(o.getAttributeMap().get(Constants.WEIGHT_KEY), -1);
            if (weight >= 0) {
                statisticInfo.addWeightCount(weight);
            }
        });

        return new ArrayList<>(statisticInfoMap.values());
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
        statisticInfo.setProviderIpSet(new HashSet<>(ipSet));
        statisticInfo.setProviderInterfaceSet(new HashSet<>(interfaceSet));

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

    /**
     * 由于机器下线导致override数据一直存在，此方法根据稳定的provider来移除
     */
    public int cleanConfigurators(boolean debug) {
        //将当前稳定的provider的ip收集
        Set<String> servIpSet = providerMap.values().stream().map(DubboProvider::getIp).collect(Collectors.toSet());
        Iterator<Map.Entry<String, DubboOverride>> iterator = overrideMap.entrySet().iterator();
        int cleanCount = 0;
        while (iterator.hasNext()) {
            Map.Entry<String, DubboOverride> entry = iterator.next();
            if (servIpSet.contains(entry.getValue().getIp())) {
                continue;
            }
            if (!debug) {
                registryRef.get().unregister(entry.getValue().getUrl());
                iterator.remove();
                cleanCount++;
            }
            log.info("cleanConfigurators adminConfig:{}, id:{}, debug:{}", adminConfig, entry.getKey(), debug);
        }
        return cleanCount;
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

        Map<String, DubboProvider> notifyProviderMap = new HashMap<>();
        Map<String, DubboConsumer> notifyConsumerMap = new HashMap<>();
        Map<String, DubboOverride> notifyOverrideMap = new HashMap<>();
        Map<String, DubboRoute> notifyRouteMap = new HashMap<>();
        String uuid = UUID.randomUUID().toString();
        String interfaceName = urlList.get(0).getServiceInterface();
        for (URL url : urlList) {
            log.info("notify uuid:{}, adminConfig:{}, interfaceName:{}, url:{}", uuid, adminConfig, interfaceName, url.toFullString());
            DubboInfo dubboInfo = DubboAdminTool.convertDubboInfo(url);

            if (dubboInfo == null) {
                log.warn("notify url:{}, convert failed", url.toFullString());
                continue;
            }
            if (dubboInfo instanceof DubboProvider) {
                if (!removeEmptyDubboInfo(providerMap, dubboInfo)) {
                    notifyProviderMap.put(DubboAdminTool.generateUniqId(dubboInfo), (DubboProvider) dubboInfo);
                }
            } else if (dubboInfo instanceof DubboConsumer) {
                if (!removeEmptyDubboInfo(consumerMap, dubboInfo)) {
                    notifyConsumerMap.put(DubboAdminTool.generateUniqId(dubboInfo), (DubboConsumer) dubboInfo);
                }
            } else if (dubboInfo instanceof DubboOverride) {
                if (notifyOverride((DubboOverride) dubboInfo)) {
                    notifyOverrideMap.put(DubboAdminTool.generateUniqId(dubboInfo), (DubboOverride) dubboInfo);
                }
//                if (!removeEmptyDubboInfo(overrideMap, dubboInfo)) {
//                    notifyOverrideMap.put(DubboAdminTool.generateUniqId(dubboInfo), (DubboOverride) dubboInfo);
//                }
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
//        overrideMap.putAll(notifyOverrideMap);
        removeNotNotifyDubboInfo(interfaceName, routeMap, notifyRouteMap);
        routeMap.putAll(notifyRouteMap);
    }

    private boolean notifyOverride(DubboOverride notifyOverride) {
        if (notifyOverride == null) {
            return false;
        }
        if (notifyOverride.isRemoved()) {
            //移出通知无效的服务
            overrideMap.entrySet().stream().filter(entry -> DubboAdminTool.isMatchRemoved(notifyOverride, entry.getValue()))
                    .forEach(entry -> overrideMap.remove(entry.getKey()));
            return false;
        }

        //如果通知的override的timestamp<已存在的override的timestamp那么返回true，不覆盖现有的
        DubboOverride curOverride = overrideMap.get(notifyOverride.getId());
        log.info("notifyOverride notifyOverrideUrl:{} curOverrideUrl:{}", notifyOverride.getUrl().toFullString(), curOverride != null ? curOverride.getUrl().toFullString() : null);

        if (curOverride != null) {
            long curOverrideTimeStamp = NumberUtils.toLong(curOverride.getAttributeMap().get(Constants.TIMESTAMP_KEY), 0L);
            long notifyOverrideTimeStamp = NumberUtils.toLong(notifyOverride.getAttributeMap().get(Constants.TIMESTAMP_KEY), 0L);
            if (curOverrideTimeStamp > notifyOverrideTimeStamp) {
                log.info("notifyOverride overrideId:{}, curOverrideTimeStamp:{}, notifyOverrideTimeStamp:{}, ignored", notifyOverride.getId(), curOverrideTimeStamp, notifyOverrideTimeStamp);
                registryRef.get().unregister(notifyOverride.getUrl());
                return false;
            }
        }

        overrideMap.put(notifyOverride.getId(), notifyOverride);
        return true;
    }

    private boolean removeEmptyDubboInfo(ConcurrentHashMap<String, ? extends DubboInfo> infoMap, DubboInfo dubboInfo) {
        if (dubboInfo == null) {
            return true;
        }

        if (!dubboInfo.isRemoved()) {
            if (dubboInfo instanceof DubboOverride) {
                DubboOverride notifyOverride = (DubboOverride) dubboInfo;
                //如果通知的override的timestamp<已存在的override的timestamp那么返回true，不覆盖现有的
                DubboOverride curOverride = (DubboOverride) infoMap.get(dubboInfo.getId());
                log.info("notify removeEmptyDubboInfo notifyOverrideUrl:{} curOverrideUrl:{}",
                        notifyOverride.getUrl().toFullString(), curOverride != null ? curOverride.getUrl().toFullString() : null);
                if (curOverride != null) {
                    long curOverrideTimeStamp = NumberUtils.toLong(curOverride.getAttributeMap().get(Constants.TIMESTAMP_KEY), 0L);
                    long notifyOverrideTimeStamp = NumberUtils.toLong(notifyOverride.getAttributeMap().get(Constants.TIMESTAMP_KEY), 0L);
                    if (notifyOverrideTimeStamp <= 0L) {
                        log.info("removeEmptyDubboInfo overrideUrl:{}, not contain timestamp", dubboInfo.getUrl());
                    }
                    if (curOverrideTimeStamp > notifyOverrideTimeStamp) {
                        log.info("removeEmptyDubboInfo overrideId:{}, curOverrideTimeStamp:{}, notifyOverrideTimeStamp:{}, ignored", dubboInfo.getId(), curOverrideTimeStamp, notifyOverrideTimeStamp);
//                        return true;
                    }
                }
            }
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

package com.yan.dubbo.admin.tools;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.registry.RegistryService;
import com.yan.dubbo.admin.model.*;
import org.apache.commons.lang3.StringUtils;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DubboAdminTool {

    private static ApplicationConfig APPLICATIONCONFIG = new ApplicationConfig("dubbo-admin");

    private DubboAdminTool() {
    }

    public static String generateUniqId(String... keys) {
        if (keys == null) {
            return "";
        }
        List<String> keyList = Stream.of(keys).sorted().collect(Collectors.toList());
        //暂时不对字符串做摘要，明文排查问题方便
        return StringUtils.join(keyList, ":");
    }

    public static String generateInterfaceUniqId(DubboInfo dubboInfo) {
        if (dubboInfo == null) {
            return "";
        }
        return generateUniqId(dubboInfo.getIp(), String.valueOf(dubboInfo.getPort()), dubboInfo.getInterfaceName(), dubboInfo.getVersion());
    }

    public static String generateUniqId(DubboInfo dubboInfo) {
        if (dubboInfo == null) {
            return "";
        }

        String id = "";
        if (dubboInfo instanceof DubboProvider || dubboInfo instanceof DubboConsumer) {
            id = generateUniqId(dubboInfo.getIp(), String.valueOf(dubboInfo.getPort()), dubboInfo.getInterfaceName(), dubboInfo.getVersion());
        } else if (dubboInfo instanceof DubboOverride) {
            List<String> keyList = new ArrayList<>(16);
            keyList.addAll(((DubboOverride) dubboInfo).getAttributeMap().keySet());
            keyList.add(dubboInfo.getIp());
            keyList.add(String.valueOf(dubboInfo.getPort()));
            keyList.add(dubboInfo.getInterfaceName());
            keyList.add(dubboInfo.getVersion());
            id = generateUniqId(keyList.toArray(new String[0]));
        } else if (dubboInfo instanceof DubboRoute) {
            List<String> keyList = new ArrayList<>(16);
//            keyList.addAll(((DubboRoute) dubboInfo).getAttributeMap().keySet());
            keyList.add(dubboInfo.getIp());
            keyList.add(String.valueOf(dubboInfo.getPort()));
            keyList.add(dubboInfo.getInterfaceName());
            keyList.add(dubboInfo.getVersion());
            id = generateUniqId(keyList.toArray(new String[0]));
        }
        dubboInfo.setId(id);
        return id;
    }

    public static ReferenceConfig<RegistryService> convertZookeeperRegistryServiceRef(RegistryAdminConfig config) {
        if (config == null) {
            return null;
        }
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(config.getAddr());
        registryConfig.setGroup(config.getGroup());
        registryConfig.setProtocol("zookeeper");
        registryConfig.setCheck(false);
        registryConfig.setFile(System.getProperty("user.home") + "/.dubbo/dubbo-registry-" + config.getName() + ".cache");

        ReferenceConfig<RegistryService> reference = new ReferenceConfig<>();
        reference.setApplication(APPLICATIONCONFIG);
        reference.setRegistry(registryConfig);
        reference.setInterface(RegistryService.class);
        reference.setCheck(false);
        return reference;
    }

    public static DubboProvider convertDubboProvider(URL url) {
        if (url == null) {
            return null;
        }
        DubboProvider dubboProvider = new DubboProvider();
        dubboProvider.setApplication(url.getParameter(Constants.APPLICATION_KEY, ""));
        dubboProvider.setProtocol(url.getProtocol());
        dubboProvider.setIp(url.getIp());
        dubboProvider.setPort(url.getPort());
        dubboProvider.setPort(url.getPort());
        dubboProvider.setInterfaceName(url.getServiceInterface());
        dubboProvider.setVersion(url.getParameter(Constants.VERSION_KEY, ""));
        dubboProvider.setMethods(url.getParameter(Constants.METHODS_KEY, ""));
        dubboProvider.setAnyhost(url.isAnyHost());
        dubboProvider.setDelay(url.getParameter("delay", url.getParameter("default.delay", -1)));
        dubboProvider.setRetries(url.getParameter("retries", url.getParameter("default.retries", -1)));
        dubboProvider.setFilter(url.getParameter(Constants.SERVICE_FILTER_KEY, url.getParameter("default.service.filter", "")));
        dubboProvider.setGeneric(url.getParameter(Constants.GENERIC_KEY, false));
        dubboProvider.setThreads(url.getParameter(Constants.THREADS_KEY, 200));
        dubboProvider.setTimestamp(url.getParameter(Constants.TIMESTAMP_KEY, 0L));
        dubboProvider.setDynamic(url.getParameter(Constants.DYNAMIC_KEY, false));
        dubboProvider.setEnabled(url.getParameter(Constants.ENABLED_KEY, true));

        dubboProvider.setUrl(url);
        generateUniqId(dubboProvider);
        return dubboProvider;
    }

    public static DubboConsumer convertDubboConsumer(URL url) {
        if (url == null) {
            return null;
        }
        DubboConsumer dubboConsumer = new DubboConsumer();
        dubboConsumer.setApplication(url.getParameter(Constants.APPLICATION_KEY, ""));
        dubboConsumer.setProtocol(url.getProtocol());
        dubboConsumer.setIp(url.getIp());
        dubboConsumer.setPort(url.getPort());
        dubboConsumer.setInterfaceName(url.getServiceInterface());
        dubboConsumer.setVersion(url.getParameter(Constants.VERSION_KEY, ""));
        dubboConsumer.setMethods(url.getParameter(Constants.METHODS_KEY, ""));
        dubboConsumer.setCheck(url.getParameter(Constants.CHECK_KEY, url.getParameter("default.check", true)));
        dubboConsumer.setPid(url.getParameter(Constants.PID_KEY, 0L));
        dubboConsumer.setFilter(url.getParameter(Constants.REFERENCE_FILTER_KEY, url.getParameter("default.reference.filter", "")));
        dubboConsumer.setUrl(url);
        generateUniqId(dubboConsumer);
        return dubboConsumer;
    }

    public static DubboOverride convertDubboOverride(URL url) {
        if (url == null) {
            return null;
        }
        DubboOverride dubboOverride = new DubboOverride();
        dubboOverride.setProtocol(url.getProtocol());
        dubboOverride.setIp(url.getIp());
        dubboOverride.setPort(url.getPort());
        dubboOverride.setInterfaceName(url.getServiceInterface());
        dubboOverride.setVersion(url.getParameter(Constants.VERSION_KEY, ""));
        dubboOverride.setDynamic(url.getParameter(Constants.DYNAMIC_KEY, false));
        dubboOverride.setEnabled(url.getParameter(Constants.ENABLED_KEY, true));
        dubboOverride.setApplication(url.getParameter(Constants.APPLICATION_KEY, ""));
        dubboOverride.setUrl(url);
        dubboOverride.initAttribute();
        generateUniqId(dubboOverride);
        return dubboOverride;
    }

    public static DubboRoute convertDubboRoute(URL url) {
        if (url == null) {
            return null;
        }
        DubboRoute dubboRoute = new DubboRoute();
        dubboRoute.setProtocol(url.getProtocol());
        dubboRoute.setIp(url.getIp());
        dubboRoute.setPort(url.getPort());
        dubboRoute.setInterfaceName(url.getServiceInterface());
        dubboRoute.setVersion(url.getParameter(Constants.VERSION_KEY, ""));
        dubboRoute.setGroup(url.getParameter(Constants.GROUP_KEY, ""));
        dubboRoute.setDynamic(url.getParameter(Constants.DYNAMIC_KEY, false));
        dubboRoute.setEnabled(url.getParameter(Constants.ENABLED_KEY, true));
        dubboRoute.setPriority(url.getParameter(Constants.PRIORITY_KEY, 1));
        dubboRoute.setForce(url.getParameter(Constants.FORCE_KEY, false));
        dubboRoute.setRuntime(url.getParameter(Constants.RUNTIME_KEY, false));
        dubboRoute.setRule(url.getParameter(Constants.RULE_KEY, ""));
        dubboRoute.setUrl(url);
        generateUniqId(dubboRoute);
        return dubboRoute;
    }

    public static DubboInfo convertDubboInfo(URL url) {
        if (url == null) {
            return null;
        }
        String category = url.getParameter(Constants.CATEGORY_KEY, Constants.PROVIDERS_CATEGORY);
        if (Constants.PROVIDERS_CATEGORY.equals(category)) {
            return convertDubboProvider(url);
        } else if (Constants.CONSUMERS_CATEGORY.equals(category)) {
            return convertDubboConsumer(url);
        } else if (Constants.CONFIGURATORS_CATEGORY.equals(category)) {
            return convertDubboOverride(url);
        } else if (Constants.ROUTERS_CATEGORY.equals(category)) {
            return convertDubboRoute(url);
        }
        return null;
    }

    public static URL convertURL(DubboOverride dubboOverride) {
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.OVERRIDE_PROTOCOL).append("://");
        if (!com.alibaba.dubbo.common.utils.StringUtils.isBlank(dubboOverride.getIp()) && ! Constants.ANY_VALUE.equals(dubboOverride.getIp())) {
            sb.append(dubboOverride.getIp());
        } else {
            sb.append(Constants.ANYHOST_VALUE);
        }
        if (dubboOverride.getPort() > 0) {
            sb.append(":").append(dubboOverride.getPort());
        }
        sb.append("/").append(dubboOverride.getInterfaceName()).append("?");
        Map<String, String> param = new HashMap<>(dubboOverride.getAttributeMap());
        param.put(Constants.CATEGORY_KEY, Constants.CONFIGURATORS_CATEGORY);
        param.put(Constants.ENABLED_KEY, String.valueOf(dubboOverride.isEnabled()));
        param.put(Constants.DYNAMIC_KEY, String.valueOf(dubboOverride.isDynamic()));
        if (!StringUtils.isBlank(dubboOverride.getApplication()) && !Constants.ANY_VALUE.equals(dubboOverride.getApplication())) {
            param.put(Constants.APPLICATION_KEY, dubboOverride.getApplication());
        }
        if (StringUtils.isNotBlank(dubboOverride.getVersion())) {
            param.put(Constants.VERSION_KEY, dubboOverride.getVersion());
        }
        sb.append(com.alibaba.dubbo.common.utils.StringUtils.toQueryString(param));
        return URL.valueOf(sb.toString());
    }

    public static boolean isMatchRemoved(DubboInfo removedInfo, DubboInfo compareInfo) {
        //configurator empty ip 返回的是当前ip地址，需要测试route什么状况
        if (removedInfo instanceof DubboOverride) {
            return removedInfo.getInterfaceName().equals(compareInfo.getInterfaceName()) &&
                    (Constants.ANY_VALUE.equals(removedInfo.getVersion()) || removedInfo.getVersion().equals(compareInfo.getVersion()));
        }
        return removedInfo.getIp().equals(compareInfo.getIp()) &&
                (removedInfo.getPort() <= 0 || removedInfo.getPort() == compareInfo.getPort()) &&
                removedInfo.getInterfaceName().equals(compareInfo.getInterfaceName()) &&
                (Constants.ANY_VALUE.equals(removedInfo.getVersion()) || removedInfo.getVersion().equals(compareInfo.getVersion()));
    }

    public static <E> Set<E> newSet(int expectSize) {
        return new HashSet<>(capacity(expectSize));
    }

    public static <K, V> Map<K, V> newMap(int expectSize) {
        return new HashMap<>(capacity(expectSize));
    }

    private static int capacity(int expectedSize) {
        if (expectedSize < 3) {
            return expectedSize + 1;
        } else {
            return expectedSize < 1073741824 ? (int) ((float) expectedSize / 0.75F + 1.0F) : 2147483647;
        }
    }

}

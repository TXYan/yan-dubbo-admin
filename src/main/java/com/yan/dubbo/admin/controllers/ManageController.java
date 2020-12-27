package com.yan.dubbo.admin.controllers;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.yan.dubbo.admin.manage.RegistryManager;
import com.yan.dubbo.admin.manage.RegistryOperator;
import com.yan.dubbo.admin.model.*;
import com.yan.dubbo.admin.model.view.ProviderApiVo;
import com.yan.dubbo.admin.model.view.ProviderInfoVo;
import com.yan.dubbo.admin.tools.DubboAdminTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/manage")
public class ManageController {

    @Autowired
    private RegistryManager registryManager;

    @GetMapping("/provider")
    public ModelAndView getProvider() {
        return new ModelAndView("provider.html");
    }

    @GetMapping("/provider/name/list.json")
    public Response getProviderJson() {
        List<String> nameList = registryManager.getOperatorNameList();
        return Response.success(nameList);
    }

    @GetMapping("/provider/ip/list.json")
    public Response getProviderIpJson(String name) {
        Response<List<ProviderInfoVo>> response = Response.success(Collections.emptyList());
        RegistryOperator operator = registryManager.getOperator(name);
        if (operator != null) {
            List<DubboProviderStatisticInfo> providerStatisticInfoList = operator.getProviderStatisticInfoList();
            if (CollectionUtils.isNotEmpty(providerStatisticInfoList)) {
                List<ProviderInfoVo> voList = providerStatisticInfoList.stream().map(info -> {
                    ProviderInfoVo infoVo = new ProviderInfoVo();
                    infoVo.setName(name);
                    infoVo.setIp(info.getIp());
                    infoVo.setPort(info.getPort());
                    infoVo.setTotalApiCount(info.getInterfaceCount());
                    infoVo.setEffectApiCount(info.getEnabledInterfaceCount());
                    infoVo.setUneffectApiCount(info.getDisabledInterfaceCount());
                    infoVo.setWeightInfoStr(info.getWeightInfoStr());
                    return infoVo;
                }).collect(Collectors.toList());
                response.setData(voList);
            }
        }
        return response;
    }

    @GetMapping("/provider/api/list.json")
    public Response getProviderApiJson(String name, String ip) {
        RegistryOperator operator = registryManager.getOperator(name);
        if (operator == null) {
            return Response.fail("未找到提供者");
        }
        Map<String, String> filterMap =  new HashMap<>();
        if (!StringUtils.isBlank(ip)) {
            filterMap.put(FilterConstants.IP, ip);
        }
        List<DubboProvider> providerInfoList = null;
        List<DubboOverride> overrideInfoList = null;
        if (StringUtils.isNotEmpty(ip)) {
            providerInfoList = operator.filterDubboInfo(filterMap, DubboProvider.class);
            overrideInfoList = operator.filterDubboInfo(filterMap, DubboOverride.class);
        }
        overrideProviderAttrs(providerInfoList, overrideInfoList);
        List<ProviderApiVo> apiVos = Collections.emptyList();
        if (CollectionUtils.isNotEmpty(providerInfoList)) {
            apiVos = providerInfoList.stream().map(p -> {
                ProviderApiVo apiVo = new ProviderApiVo();
                apiVo.setName(name);
                apiVo.setIp(ip);
                apiVo.setPort(p.getPort());
                apiVo.setApiName(p.getInterfaceName());
                apiVo.setEffect(p.isEnabled());
                apiVo.setFilters(p.getFilter());
                apiVo.setVersion(p.getVersion());
                apiVo.setThreadCount(p.getThreads());
                apiVo.setWeight(p.getWeight() > -1 ? p.getWeight() : 100);
                return apiVo;
            }).collect(Collectors.toList());
        }
        return Response.success(apiVos);
    }

    private void overrideProviderAttrs(List<DubboProvider> providerList, List<DubboOverride> overrideList) {
        if (CollectionUtils.isEmpty(providerList) || CollectionUtils.isEmpty(overrideList)) {
            return;
        }
        Map<String, List<DubboOverride>> overrideListMap = overrideList.stream().filter(DubboOverride::isEnabled).collect(Collectors.groupingBy(DubboAdminTool::generateInterfaceUniqId));
        providerList.forEach(provider -> {
            List<DubboOverride> overrides = overrideListMap.get(DubboAdminTool.generateInterfaceUniqId(provider));
            if (CollectionUtils.isNotEmpty(overrides)) {
                overrides.forEach(override -> {
                    Map<String, String> attrMap = override.getAttributeMap();
                    boolean disabled = BooleanUtils.toBoolean(attrMap.get(Constants.DISABLED_KEY));
                    if (disabled) {
                        provider.setEnabled(false);
                    }
                    int weight = NumberUtils.toInt(attrMap.get(Constants.WEIGHT_KEY), -1);
                    if (weight > -1) {
                        provider.setWeight(weight);
                    }

                });
            }
        });
    }



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
    public Response addOverride(String name, String ip, int port, String interfaceName, String version, String attr, String val, @RequestParam(required = false, defaultValue = "true") boolean enabled) {
        RegistryOperator operator = registryManager.getOperator(name);
        if (operator == null) {
            return Response.fail("name:" + name + " can not find");
        }

        DubboOverride override = new DubboOverride();
        override.setIp(ip);
        override.setPort(port);
        override.setInterfaceName(interfaceName);
        override.setVersion(version);
        override.setAttribute(attr, val);
        override.setEnabled(enabled);
        operator.override(override);

        return Response.success("SUCCESS");
    }

    @RequestMapping("/override/ip/disabled.json")
    public Response disabledNew(String name, String ip, int port, @RequestParam(required = false, defaultValue = "true") boolean disabled, @RequestParam(required = false, defaultValue = "-1") int weight,
                                @RequestParam(required = false) String interfaceName) {
        if (weight > 200) {
            return Response.fail("weight can not large than 200");
        }
        RegistryOperator operator = registryManager.getOperator(name);
        if (operator == null) {
            return Response.fail("name:" + name + " can not find");
        }
        //查询所有接口
        Map<String, String> filterMap =  new HashMap<>();
        filterMap.put(FilterConstants.IP, ip);
        List<? extends DubboInfo> providerList = operator.filterDubboInfo(filterMap, DubboProvider.class);

        if (providerList.size() == 0) {
            return Response.success("SUCCESS");
        }
        //偶尔不及时 需要根据override进行补充
        List<? extends DubboInfo> overrideList = operator.filterDubboInfo(filterMap, DubboOverride.class);
        //这里有可能key冲突 不处理 暴露问题 然后解决
        Map<String, DubboInfo> interfaceOverrideMap = overrideList.stream().collect(Collectors.toMap(DubboInfo::getId, o -> o));
        providerList.forEach(p -> {
            if (org.apache.commons.lang3.StringUtils.isNotBlank(interfaceName) && !interfaceName.equals(p.getInterfaceName())) {
                return;
            }
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
            override.setEnabled(disabled || weight != 100);
            override.setRemoteUnregister(!disabled && weight == 100);
            operator.override(override);
            interfaceOverrideMap.remove(override.getId());
        });
        log.info("disabledNew name:{}, ip:{}, disabled:{}, providerSize:{}, additionOverrideSize:{}", name, ip, disabled, providerList.size(), interfaceOverrideMap.size());
        interfaceOverrideMap.values().forEach(o -> {
            if (org.apache.commons.lang3.StringUtils.isNotBlank(interfaceName) && !interfaceName.equals(o.getInterfaceName())) {
                return;
            }
            DubboOverride override = (DubboOverride) o;
            override.setAttribute(Constants.DISABLED_KEY, String.valueOf(disabled));
            //这里只处理两个属性，如果不是，那么说明是老的遗留的 不设置weight，然后移除
            if (override.getAttributeMap().containsKey(Constants.WEIGHT_KEY)) {
                if (weight > -1) {
                    override.setAttribute(Constants.WEIGHT_KEY, String.valueOf(weight));
                }
            }
            //如果权重是默认的那么删除
            override.setEnabled(disabled || weight != 100);
            operator.override(override);
        });

        return Response.success("SUCCESS");
    }

    @RequestMapping("/override/interface/timeout")
    public Response overrideTimeout(String name, String interfaceName, int timeout, @RequestParam(required = false, defaultValue = "false") boolean enabled) {
        RegistryOperator operator = registryManager.getOperator(name);
        if (operator == null) {
            return Response.fail("name:" + name + " can not find");
        }

        //查询所有接口
        Map<String, String> filterMap =  new HashMap<>();
        filterMap.put(FilterConstants.INTERFACE_NAME, interfaceName);
        List<? extends DubboInfo> providerList = operator.filterDubboInfo(filterMap, DubboProvider.class);

        if (providerList.size() == 0) {
            return Response.fail("can not any find provider");
        }

        providerList.forEach(p -> {
            DubboOverride override = new DubboOverride();
            override.setIp(p.getIp());
            override.setPort(p.getPort());
            override.setInterfaceName(interfaceName);
            override.setVersion(p.getVersion());
            override.setAttribute(Constants.TIMEOUT_KEY, String.valueOf(timeout));
            //如果权重是默认的那么删除
            override.setEnabled(enabled);
            operator.override(override);
        });

        return Response.success("SUCCESS");
    }

    //清理无用的configurator 比如机器下线或者更换ip
    @RequestMapping("/override/clean")
    public Response cleanConfigurators(String name, @RequestParam(required = false, defaultValue = "true") boolean debug) {
        RegistryOperator operator = registryManager.getOperator(name);
        if (operator == null) {
            return Response.fail("name:" + name + " can not find");
        }

        int cleanCount = operator.cleanConfigurators(debug);
        return Response.success("Done, cleanCount:" + cleanCount);
    }
}

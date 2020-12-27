package com.yan.dubbo.admin.model;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class DubboProviderStatisticInfo implements Serializable {
    private static final long serialVersionUID = -6361883307822055660L;

    private String ip;
    private int port;
    private int interfaceCount;//共有多少个接口
    private int enabledInterfaceCount;//几个启用
    private int disabledInterfaceCount;//几个禁用
    //启用接口的权重分布
    private Map<Integer, Integer> weightCountMap = new HashMap<>();

    public DubboProviderStatisticInfo() {

    }

    public DubboProviderStatisticInfo(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void addWeightCount(int weight) {
        Integer count = weightCountMap.getOrDefault(weight, 0);
        weightCountMap.put(weight, count + 1);
    }

    public String getWeightInfoStr() {
        if (weightCountMap.size() == 0) {
            return "权重:100";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Integer> entry : weightCountMap.entrySet()) {
            sb.append("权重").append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}

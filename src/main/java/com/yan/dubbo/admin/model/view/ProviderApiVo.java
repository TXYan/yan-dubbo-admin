package com.yan.dubbo.admin.model.view;

import lombok.Data;

import java.io.Serializable;

@Data
public class ProviderApiVo implements Serializable {
    private static final long serialVersionUID = -4723904491376495352L;
    //{port:"18010", apiName: "com.yan.test.api.TestApi", isEffect: true, weight: 19, filters: "", version:"1.0.1", threadCount:600 },
    private String name;
    private String ip;
    private int port;
    private String apiName;
    private boolean isEffect;
    private int weight;
    private String filters;
    private String version;
    private int threadCount;
}

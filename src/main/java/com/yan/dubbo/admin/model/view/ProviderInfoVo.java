package com.yan.dubbo.admin.model.view;

import lombok.Data;

import java.io.Serializable;

@Data
public class ProviderInfoVo implements Serializable {
    private static final long serialVersionUID = -962451723340850209L;
    //{ip:"127.0.0.1", port:"18010", totalApiCount: 99, effectApiCount: 80, uneffectApiCount: 19, weightInfo: "权重:100" },
    private String name;
    private String ip;
    private int port;
    private int totalApiCount;
    private int effectApiCount;
    private int uneffectApiCount;
    private String weightInfoStr;

}

package com.zhipin.dubbo.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.zhipin.dubbo"})
public class ZhipinDubboAdmin {
    public static void main(String[] args) {
        SpringApplication.run(ZhipinDubboAdmin.class, args);
    }
}

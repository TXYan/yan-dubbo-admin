package com.yan.dubbo.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * date:2020-04-04
 * Author:Y'an
 */

@SpringBootApplication(scanBasePackages = {"com.yan.dubbo"})
public class YanDubboAdmin {
    public static void main(String[] args) {
        SpringApplication.run(YanDubboAdmin.class, args);
    }
}

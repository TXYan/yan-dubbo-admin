package com.yan.dubbo.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@Slf4j
@ImportResource("classpath*:spring/*.xml")
@SpringBootApplication(scanBasePackages = {"com.yan.dubbo"})
public class YanDubboAdmin {
    public static void main(String[] args) {
        SpringApplication.run(com.yan.dubbo.admin.YanDubboAdmin.class, args);
        log.info("Application Startup Success");
    }
}

package com.yan.dubbo.admin.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LoginController {

    @RequestMapping("/home")
    public String home() {
        return "home";
    }
}

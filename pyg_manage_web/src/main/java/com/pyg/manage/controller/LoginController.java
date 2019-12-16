package com.pyg.manage.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 获取登陆用户名
 */
@RestController
public class LoginController {

    @RequestMapping("loginName")
    public Map loginName(){
        Map map = new HashMap();
        //获取登陆用户名 上下文 环境 域对象
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        map.put("loginName",username);
        return map;
    }
}

package com.pyg.user.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author huyy
 * @Title: LoginController
 * @ProjectName pyg_parent18
 * @Description: TODO
 * @date 2018/12/1018:04
 */
@RestController
public class LoginController {


    @RequestMapping("/getLoginUser")
    public Map getLoginUser(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map result = new HashMap();
        result.put("username",name);
        return result;
    }
}

package com.pyg.user.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义认证类  : 返回授权信息
 */
public class UserDetailsServiceImpl implements UserDetailsService {


    /*
        *@Desc 认证方法  作用：提供用户对象
        *@param username 登陆页输入的用户名 查询数据库 seller
        *@return org.springframework.security.core.userdetails.UserDetails
        **/
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println(username);
        System.out.println("经过授权认证类");
        List<GrantedAuthority> authorities = new ArrayList<>();//权限列表
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_SELLER");//权限对象
        GrantedAuthority authority2 = new SimpleGrantedAuthority("ROLE_USER");//权限对象
        authorities.add(authority);
        authorities.add(authority2);
        return new User(username,"",authorities);

    }
}

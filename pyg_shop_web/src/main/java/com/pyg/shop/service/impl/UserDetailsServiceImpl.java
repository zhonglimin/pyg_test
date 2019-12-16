package com.pyg.shop.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.pojo.TbSeller;
import com.pyg.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义认证类  <user-service>
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Reference
    private SellerService sellerService;

    /*public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }*/

    /*
        *@Desc 认证方法  作用：提供用户对象
        *@param username 登陆页输入的用户名 查询数据库 seller
        *@return org.springframework.security.core.userdetails.UserDetails
        **/
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println(username);
        TbSeller seller = sellerService.findOne(username);
        if(seller!=null&&"1".equals(seller.getStatus())){//商家必须存在，并且状态必须是审核通过的
            List<GrantedAuthority> authorities = new ArrayList<>();//权限列表
            GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_SELLER");//权限对象
            authorities.add(authority);
            return new User(username,seller.getPassword(),authorities);
        }else{
            return null;
        }

    }
}

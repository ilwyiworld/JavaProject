package com.yiworld.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    //设置拦截规则，设置默认登录页面以及登录成功后的跳转页面
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                //设置拦截规则
                .antMatchers("/")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                //开启默认登录页面
                .formLogin()
                //默认登录页面
                .loginPage("/login")
                //默认登录成功跳转页面
                .defaultSuccessUrl("/chat")
                .permitAll()
                .and()
                //设置注销
                .logout()
                .permitAll();
    }

    @Override
    //定义两个用户，设置用户名、用户密码、用户角色等信息
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("yiliang").password("111").roles("USER")
                .and()
                .withUser("root").password("root").roles("USER");
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        //设置静态资源不被拦截
        web.ignoring().antMatchers("/resources/static/**");
    }
}

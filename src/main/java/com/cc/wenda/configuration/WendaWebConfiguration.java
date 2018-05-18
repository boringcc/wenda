package com.cc.wenda.configuration;

import com.cc.wenda.interceptor.LoginRequiredInterceptor;
import com.cc.wenda.interceptor.PassprotIntercepter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * 注册拦截器
 */

@Component
public class WendaWebConfiguration extends WebMvcConfigurerAdapter{

    @Autowired
    PassprotIntercepter passprotIntercepter;

    @Autowired
    LoginRequiredInterceptor loginRequredIntercepter;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(passprotIntercepter);
        registry.addInterceptor(loginRequredIntercepter).addPathPatterns("/user/*");
        super.addInterceptors(registry);
    }
}

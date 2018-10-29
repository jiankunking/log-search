package com.jiankunking.logsearch.interceptor;


import com.jiankunking.logsearch.config.EnvionmentVariables;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author jiankunking.
 * @date：2018/10/11 19:50
 * @description:
 */
@Configuration
public class WebAppConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        super.addResourceHandlers(registry);
        registry.addResourceHandler("/downloads/**").addResourceLocations("file:" + EnvionmentVariables.OFF_LINE_LOG_DOWNLOAD_PATH);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UrlInterceptor()).addPathPatterns("/**");
    }
}

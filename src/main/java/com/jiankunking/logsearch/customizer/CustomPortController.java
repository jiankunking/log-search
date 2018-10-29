package com.jiankunking.logsearch.customizer;


import com.jiankunking.logsearch.config.EnvionmentVariables;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
@RestController
@EnableAutoConfiguration
public class CustomPortController implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
        factory.setPort(EnvionmentVariables.LISTEN_PORT);
    }

}

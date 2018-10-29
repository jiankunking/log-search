package com.jiankunking.logsearch.client;

import com.google.common.net.HostAndPort;
import com.jiankunking.logsearch.config.EnvionmentVariables;
import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author jiankunking.
 * @date：2018/8/17 10:05
 * @description:
 */
public class ConsulClientSingleton {

    private ConsulClientSingleton() {
    }

    public static KeyValueClient getKeyValueClient() {
        return Singleton.INSTANCE.getInstance().keyValueClient();
    }

    /**
     * destroy consul client
     *
     * @throws IOException
     */
    public static void destroyConsulClient() {
        if (Singleton.INSTANCE.getInstance() != null) {
            Singleton.INSTANCE.getInstance().destroy();
        }
    }

    /**
     * consul client 单例
     */
    private enum Singleton {
        INSTANCE;
        private Consul singleton;

        Singleton() {
            HostAndPort hostAndPort = HostAndPort.fromParts(EnvionmentVariables.CONSUL_HOST, EnvionmentVariables.CONSUL_PORT);
            singleton = Consul.builder()
                    .withHostAndPort(hostAndPort)
                    .withReadTimeoutMillis(TimeUnit.SECONDS.toMillis(10 * 1000))
                    .build();
        }

        public Consul getInstance() {
            return singleton;
        }
    }
}

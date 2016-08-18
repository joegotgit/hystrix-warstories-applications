package com.codecentric.hystrix.warstories.shared.configuration;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.boon.etcd.ClientBuilder;
import org.boon.etcd.Etcd;
import org.springframework.context.annotation.Configuration;

import com.netflix.config.*;
import com.netflix.config.source.EtcdConfigurationSource;

/**
 * @author Benjamin Wilms (xd98870)
 */
@Configuration
public class ArchaiusConfiguration {

    private static final Log LOGGER = LogFactory.getLog(ArchaiusConfiguration.class);

    private DynamicStringProperty etcdServerPort =
            DynamicPropertyFactory.getInstance().getStringProperty("server.etcd.baseurl", "http://192.168.178.31:2379");

    public ArchaiusConfiguration() {

        LOGGER.debug(LOGGER.isDebugEnabled() ? "### Archaius init: " + this.toString() : null);

        // Config fallback (config.properties) and Etcd configuration
        ConcurrentCompositeConfiguration compositeConfig = new ConcurrentCompositeConfiguration();

        // File based configuration
        ClasspathPropertiesConfiguration.initialize();

        // CoresOS Etcd service configuration
        DynamicWatchedConfiguration etcdConfiguration = createEtcdConfiguration();

        if (etcdConfiguration != null)
            compositeConfig.addConfiguration(etcdConfiguration, "etcd dynamic override configuration");
        else {
            LOGGER.debug(LOGGER.isDebugEnabled() ? "etcdConfigurartion == null" : null);
        }
        ConfigurationManager.install(compositeConfig);


        LOGGER.debug(LOGGER.isDebugEnabled() ? "is Configuration installed: " + ConfigurationManager.isConfigurationInstalled() :
                null);


    }

    private DynamicWatchedConfiguration createEtcdConfiguration() {
        try {
            Etcd etcd = createEtcdClient();

            LOGGER.debug(LOGGER.isDebugEnabled() ? "Etcd Client created: " + (etcd != null) : null);

            AbstractPollingScheduler scheduler = new FixedDelayPollingScheduler();
            EtcdConfigurationSource etcdConfigurationSource = new EtcdConfigurationSource(etcd, "/hystrix/");
            DynamicWatchedConfiguration etcdConfiguration = new DynamicWatchedConfiguration(etcdConfigurationSource);

            return etcdConfiguration;

        } catch (Exception e) {
            LOGGER.error("CoresOS ETCD Service not reachable...");
            return null;
        }
    }

    /***
     * Create and initials etcd client
     *
     * @return
     */
    private Etcd createEtcdClient() {
        LOGGER.debug(LOGGER.isDebugEnabled() ? "Etcd server baseurl: " + etcdServerPort.get() : null);

        ClientBuilder clientBuilder;
        clientBuilder = ClientBuilder.builder().hosts(URI.create(etcdServerPort.get())).timeOutInMilliseconds(5000);

        return clientBuilder.createClient();

    }
}

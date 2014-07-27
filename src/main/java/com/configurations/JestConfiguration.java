package com.configurations;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.ClientConfig;

public class JestConfiguration {

    public JestClient jestClient(){

        String connectionUrl =
            "https://site:d0dc2b14d8ace7fa75e297b4b10edf5a@dwalin-us-east-1.searchly.com";

        // Configuration
        ClientConfig clientConfig = new ClientConfig.Builder( connectionUrl )
                                                    .multiThreaded(true).build();

        // Construct a new Jest client according to configurations via factory
        JestClientFactory factory = new JestClientFactory();
        factory.setClientConfig(clientConfig);
        return factory.getObject();
    }
}

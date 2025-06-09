package com.reactivelearning.demo.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

@Configuration
public class SslConfig {

    @Bean
    public WebTestClient webTestClient(@Value("classpath:springboot.crt") Resource cert) throws Exception {

        TrustManagerFactory trustManagerFactory = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());

        CertificateFactory certificateFactory = CertificateFactory
                .getInstance("X.509");
        Certificate certificate = certificateFactory.generateCertificate(cert.getInputStream());

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null);
        trustStore.setCertificateEntry("springboot", certificate);

        trustManagerFactory.init(trustStore);

        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(trustManagerFactory)
                .build();

        HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));

        return WebTestClient
                .bindToServer()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl("https://localhost:8080")
                .build();

    }

}

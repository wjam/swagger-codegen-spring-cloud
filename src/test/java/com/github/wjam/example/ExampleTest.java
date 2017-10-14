package com.github.wjam.example;

import feign.Feign;
import feign.codec.StringDecoder;
import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit.core.SimulationSource;
import io.swagger.api.DefaultApi;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cloud.netflix.feign.support.ResponseEntityDecoder;
import org.springframework.cloud.netflix.feign.support.SpringMvcContract;

import static io.specto.hoverfly.junit.dsl.HoverflyDsl.response;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static org.junit.Assert.assertEquals;

public class ExampleTest {

    private Hoverfly hoverfly;

    @Before
    public void setup() {
        hoverfly = new Hoverfly(HoverflyMode.SIMULATE);
        hoverfly.start();
        hoverfly.importSimulation(SimulationSource.dsl(
                service("swagger.example").get("/api/example").willReturn(response().status(201).body("expected"))
        ));
    }

    @After
    public void shutdown() {
        hoverfly.close();
    }

    @Test
    public void example() throws NoSuchMethodException {

        if (DefaultApi.class.getDeclaredMethod("get").isDefault()) {
            System.out.println("feign will run the default method rather than executing the HTTP request");
        }

        final DefaultApi api = Feign.builder()
                .contract(new SpringMvcContract())
                .decoder(new ResponseEntityDecoder(new StringDecoder()))
                .target(DefaultApi.class, "http://swagger.example");

        assertEquals("expected", api.get().getBody());
    }

}

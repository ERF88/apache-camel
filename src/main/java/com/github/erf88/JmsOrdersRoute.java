package com.github.erf88;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.activemq.ActiveMQComponent;
import org.apache.camel.impl.DefaultCamelContext;

public class JmsOrdersRoute {

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.addComponent("activemq", ActiveMQComponent.activeMQComponent("tcp://localhost:61616"));
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("file:pedidos?noop=true")
                .to("activemq:queue:pedidos");
            }
        });

		context.start();
		Thread.sleep(10000);
		context.stop();
    }

}

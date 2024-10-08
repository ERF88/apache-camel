package com.github.erf88;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.activemq.ActiveMQComponent;
import org.apache.camel.component.http.HttpMethods;
import org.apache.camel.impl.DefaultCamelContext;

public class FileOrdersRoute {

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("file:pedidos?delay=5s&noop=true")
                        .split().xpath("/pedido/itens/item")
                        .filter().xpath("/item/formato[text()='EBOOK']")
                        .log("${id} - ${body}")
                        .unmarshal().jacksonXml()
                        .marshal().json()
                        .log("$id} - ${body}")
                        .setHeader(Exchange.FILE_NAME, simple("${file:name.noext}-${header.CamelSplitIndex}.json"))
                .to("file:saida");
            }
        });

		context.start();
		Thread.sleep(10000);
		context.stop();
    }

}

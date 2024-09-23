package com.github.erf88;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.activemq.ActiveMQComponent;
import org.apache.camel.component.http.HttpMethods;
import org.apache.camel.impl.DefaultCamelContext;
import org.xml.sax.SAXParseException;

public class OrdersRoute {

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.addComponent("activemq", ActiveMQComponent.activeMQComponent("tcp://localhost:61616"));
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                errorHandler(deadLetterChannel("activemq:queue:pedidos.DLQ")
                        .logExhaustedMessageHistory(true)
                        .maximumRedeliveries(3)
                        .redeliveryDelay(2000)
                        .onRedelivery(e -> {
                            Object counter = e.getIn().getHeader(Exchange.REDELIVERY_COUNTER);
                            Object max = e.getIn().getHeader(Exchange.REDELIVERY_MAX_COUNTER);
                            System.out.printf("Redelivery %s / %s%n", counter, max);
                        }));

//                onException(SAXParseException.class)
//                        .handled(true)
//                        .maximumRedeliveries(3)
//                        .redeliveryDelay(4000)
//                        .onRedelivery(e -> {
//                            Object counter = e.getIn().getHeader(Exchange.REDELIVERY_COUNTER);
//                            Object max = e.getIn().getHeader(Exchange.REDELIVERY_MAX_COUNTER);
//                            System.out.printf("Redelivery %s / %s%n", counter, max);
//                        });

                from("activemq:queue:pedidos")
                        .routeId("rota-pedidos")
                        .to("validator:pedido.xsd")
                        .multicast()
//                        .parallelProcessing()
                        .to("direct:soap")
                        .to("direct:http");

                from("direct:http")
                        .routeId("rota-http")
                        .setProperty("pedidoId", xpath("/pedido/id/text()"))
                        .setProperty("clienteId", xpath("/pedido/pagamento/email-titular/text()"))
                        .split().xpath("/pedido/itens/item")
                        .filter().xpath("/item/formato[text()='EBOOK']")
                        .setProperty("ebookId", xpath("/item/livro/codigo/text()"))
                        .unmarshal().jacksonXml()
                        .marshal().json()
                        .log("${id} - ${body}")
                        .setHeader(Exchange.HTTP_METHOD, HttpMethods.GET)
                        .setHeader(Exchange.HTTP_QUERY, simple("ebookId=${exchangeProperty.ebookId}&pedidoId=${exchangeProperty.pedidoId}&clienteId=${exchangeProperty.clienteId}"))
                .to("http://localhost:8080/webservices/ebook/item");

                from("direct:soap")
                        .routeId("rota-soap")
                        .to("xslt:pedido-para-soap.xslt")
                        .log("${body}")
                        .setHeader(Exchange.CONTENT_TYPE, constant("text/xml"))
                        .setHeader(Exchange.HTTP_METHOD, HttpMethods.POST)
                .to("http://localhost:8080/webservices/financeiro");
            }
        });

		context.start();
		Thread.sleep(10000);
		context.stop();
    }

}

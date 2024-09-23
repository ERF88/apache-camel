package com.github.erf88;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.erf88.model.Negotiation;
import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpMethods;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.SimpleRegistry;

import javax.sql.DataSource;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class NegotiationsRoute {

    public static void main(String[] args) throws Exception {

        SimpleRegistry simpleRegistry = new SimpleRegistry();
        simpleRegistry.put("mysql", Map.of(DataSource.class, createDataSource()));
        CamelContext context = new DefaultCamelContext(simpleRegistry);
        JacksonDataFormat jacksonDataFormat = new JacksonDataFormat(createXmlMapper(), Negotiation.class);
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("timer://negociacoes?fixedRate=true&delay=1s&period=360s")
                        .setHeader(Exchange.HTTP_METHOD, HttpMethods.POST)
                        .to("http://localhost:8080/negociacoes")
                        .split().xpath("/negociacoes/negociacao")
                        .unmarshal(jacksonDataFormat)
                        .split(body())
                        .process(exchange -> {
                            Negotiation negotiation = exchange.getIn().getBody(Negotiation.class);
                            exchange.setProperty("preco", negotiation.getPrice());
                            exchange.setProperty("quantidade", negotiation.getQuantity());
                            exchange.setProperty("data", negotiation.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                        })
                        .setBody(simple("insert into negotiation(price, quantity, `date`) values (${exchangeProperty.preco}, ${exchangeProperty.quantidade}, '${exchangeProperty.data}')"))
                        .log("${body}")
                        .delay(1000)
                .to("jdbc:mysql");
            }
        });

        context.start();
        Thread.sleep(5000);
        context.stop();
    }

    public static XmlMapper createXmlMapper() {
        XmlMapper xmlMapper = new XmlMapper(new XmlFactory());
        xmlMapper.registerModule(new JavaTimeModule()); // Para suporte a tipos Java 8, se necessário
        xmlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Para formatos ISO-8601, se necessário
        return xmlMapper;
    }

    private static MysqlConnectionPoolDataSource createDataSource() {
        MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();
        dataSource.setDatabaseName("camel");
        dataSource.setServerName("localhost");
        dataSource.setPort(3306);
        dataSource.setUser("root");
        dataSource.setPassword("root");
        return dataSource;
    }

}

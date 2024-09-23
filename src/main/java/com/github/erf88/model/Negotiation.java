package com.github.erf88.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.github.erf88.deserializer.UnixTimestampToLocalDateDeserializer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "negociacao")
public class Negotiation {

    @JacksonXmlProperty(localName = "preco")
    private BigDecimal price;
    @JacksonXmlProperty(localName = "quantidade")
    private Integer quantity;
    @JacksonXmlProperty(localName = "data")
    @JsonDeserialize(using = UnixTimestampToLocalDateDeserializer.class)
    private LocalDate date;

}

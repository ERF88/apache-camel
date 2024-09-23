package com.github.erf88.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public class UnixTimestampToLocalDateDeserializer extends JsonDeserializer<LocalDate> {

    @Override
    public LocalDate deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException, JsonProcessingException {
        return Instant.ofEpochSecond(Long.parseLong(jsonParser.getText()))
                .atZone(ZoneOffset.from(ZoneOffset.UTC))
                .toLocalDate();
    }

}

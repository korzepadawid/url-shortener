package io.github.korzepadawid.urlshortener.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonDateTimeConfig {

  public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

  @Bean
  public SimpleModule module() {
    SimpleModule module = new SimpleModule();
    module
        .addSerializer(LocalDateTime.class,
            new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_PATTERN)));
    module.addDeserializer(LocalDateTime.class,
        new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_PATTERN)));
    return module;
  }
}

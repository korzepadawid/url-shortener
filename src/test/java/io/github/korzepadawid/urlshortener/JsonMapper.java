package io.github.korzepadawid.urlshortener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class JsonMapper {

  public static String toJson(final Object object) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException exception) {
      throw new RuntimeException(exception.getMessage());
    }
  }
}

package io.github.korzepadawid.urlshortener.api.v1.models;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public class RestException {

  private final String message;
  private final HttpStatus status;
  private final LocalDateTime date = LocalDateTime.now();
  private final Object details;
}

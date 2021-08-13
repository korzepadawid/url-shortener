package io.github.korzepadawid.urlshortener.api.v1.models;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UrlReadDto {

  private String longUrl;
  private String shortUrl;
  private LocalDateTime expiringAt;
}

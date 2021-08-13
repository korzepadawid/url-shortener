package io.github.korzepadawid.urlshortener.api.v1.mappers;

import io.github.korzepadawid.urlshortener.api.v1.models.UrlReadDto;
import io.github.korzepadawid.urlshortener.api.v1.models.UrlWriteDto;
import io.github.korzepadawid.urlshortener.models.Url;
import io.github.korzepadawid.urlshortener.services.Base62Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UrlMapperImpl implements UrlMapper {

  private final Base62Service base62Service;

  @Override
  public Url convertUrlWriteDtoToUrl(UrlWriteDto urlWriteDto) {
    if (urlWriteDto == null) {
      return null;
    }
    return Url.builder()
        .url(urlWriteDto.getUrl())
        .expiringAt(urlWriteDto.getExpiringAt())
        .build();
  }

  @Override
  public UrlReadDto convertUrlToUrlReadDto(Url url) {
    if (url == null) {
      return null;
    }
    return UrlReadDto.builder()
        .shortUrl("/" + base62Service.encode(url.getId()))
        .longUrl(url.getUrl())
        .expiringAt(url.getExpiringAt())
        .build();
  }
}

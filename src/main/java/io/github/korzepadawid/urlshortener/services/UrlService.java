package io.github.korzepadawid.urlshortener.services;

import io.github.korzepadawid.urlshortener.api.v1.models.UrlReadDto;
import io.github.korzepadawid.urlshortener.api.v1.models.UrlWriteDto;

public interface UrlService {

  UrlReadDto createUrl(UrlWriteDto urlWriteDto);

  UrlReadDto getUrl(String encodedId);
}

package io.github.korzepadawid.urlshortener.api.v1.mappers;

import io.github.korzepadawid.urlshortener.api.v1.models.UrlReadDto;
import io.github.korzepadawid.urlshortener.api.v1.models.UrlWriteDto;
import io.github.korzepadawid.urlshortener.models.Url;

public interface UrlMapper {

  Url convertUrlWriteDtoToUrl(UrlWriteDto urlWriteDto);

  UrlReadDto convertUrlToUrlReadDto(Url url);
}

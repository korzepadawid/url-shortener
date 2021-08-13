package io.github.korzepadawid.urlshortener.services;

import io.github.korzepadawid.urlshortener.api.v1.mappers.UrlMapper;
import io.github.korzepadawid.urlshortener.api.v1.models.UrlReadDto;
import io.github.korzepadawid.urlshortener.api.v1.models.UrlWriteDto;
import io.github.korzepadawid.urlshortener.exceptions.ResourceNotFoundException;
import io.github.korzepadawid.urlshortener.models.Url;
import io.github.korzepadawid.urlshortener.repositories.UrlRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UrlServiceImpl implements UrlService {

  private final UrlRepository urlRepository;
  private final Base62Service base62Service;
  private final UrlMapper urlMapper;

  @Override
  public UrlReadDto createUrl(UrlWriteDto urlWriteDto) {
    Optional<Url> optionalUrl = urlRepository
        .findFirstByUrlAndExpiringAtIsNull(urlWriteDto.getUrl());

    if (optionalUrl.isPresent() && urlWriteDto.getExpiringAt() == null) {
      return urlMapper.convertUrlToUrlReadDto(optionalUrl.get());
    }

    Url convertedUrl = urlMapper.convertUrlWriteDtoToUrl(urlWriteDto);
    Url savedUrl = urlRepository.save(convertedUrl);

    return urlMapper.convertUrlToUrlReadDto(savedUrl);
  }

  @Override
  public UrlReadDto getUrl(String encodedId) {
    Long decodedId = base62Service.decode(encodedId);
    Url url = urlRepository
        .findExistingNonExpiredUrl(decodedId, LocalDateTime.now())
        .orElseThrow(() -> new ResourceNotFoundException(
            "Invalid encoded id: " + encodedId + ". Url not found."));

    return urlMapper.convertUrlToUrlReadDto(url);
  }
}

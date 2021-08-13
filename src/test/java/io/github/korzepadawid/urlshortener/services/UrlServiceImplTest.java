package io.github.korzepadawid.urlshortener.services;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.github.korzepadawid.urlshortener.api.v1.mappers.UrlMapper;
import io.github.korzepadawid.urlshortener.api.v1.models.UrlReadDto;
import io.github.korzepadawid.urlshortener.api.v1.models.UrlWriteDto;
import io.github.korzepadawid.urlshortener.exceptions.ResourceNotFoundException;
import io.github.korzepadawid.urlshortener.models.Url;
import io.github.korzepadawid.urlshortener.repositories.UrlRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UrlServiceImplTest {

  @Mock
  private UrlRepository urlRepository;

  @Mock
  private Base62Service base62Service;

  @Mock
  private UrlMapper urlMapper;

  @InjectMocks
  private UrlServiceImpl urlService;

  private static final LocalDateTime CREATED_AT = LocalDateTime.now().minusDays(2);
  private static final String BASE62ENCODED_ID = "fhf2";
  private static final String SHORT_URL = "/" + BASE62ENCODED_ID;
  private static final String HTTPS_STACKOVERFLOW_COM = "https://stackoverflow.com/";
  private static final Long DECODED_ID = 3641200L;

  private Url url;
  private UrlWriteDto urlWriteDto;
  private UrlReadDto urlReadDto;

  @BeforeEach
  void setUp() {
    url = Url.builder()
        .id(DECODED_ID)
        .url(HTTPS_STACKOVERFLOW_COM)
        .expiringAt(null)
        .createdAt(CREATED_AT)
        .build();

    urlWriteDto = UrlWriteDto.builder()
        .url(HTTPS_STACKOVERFLOW_COM)
        .expiringAt(null)
        .build();

    urlReadDto = UrlReadDto.builder()
        .shortUrl(SHORT_URL)
        .longUrl(HTTPS_STACKOVERFLOW_COM)
        .expiringAt(null)
        .build();
  }

  @Test
  void createUrl_WhenExistingNeverExpiringUrl_ThenReturnsOldUrlInsteadOfCreatingNew() {
    when(urlRepository
        .findAlreadyExistingUrl(eq(urlWriteDto.getUrl()), eq(urlWriteDto.getExpiringAt())))
        .thenReturn(Optional.of(url));
    when(urlMapper.convertUrlToUrlReadDto(any(Url.class))).thenReturn(urlReadDto);

    UrlReadDto result = urlService.createUrl(urlWriteDto);

    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("shortUrl", SHORT_URL)
        .hasFieldOrPropertyWithValue("longUrl", HTTPS_STACKOVERFLOW_COM)
        .hasFieldOrPropertyWithValue("expiringAt", null);
  }

  @Test
  void createUrl_WhenNonExistingOrExpiredUrl_ThenReturnsCreatedUrl() {
    when(urlRepository
        .findAlreadyExistingUrl(eq(urlWriteDto.getUrl()), eq(urlWriteDto.getExpiringAt())))
        .thenReturn(Optional.empty());
    when(urlMapper.convertUrlWriteDtoToUrl(any(UrlWriteDto.class))).thenReturn(url);
    when(urlRepository.save(any(Url.class))).thenReturn(url);
    when(urlMapper.convertUrlToUrlReadDto(any(Url.class))).thenReturn(urlReadDto);

    UrlReadDto result = urlService.createUrl(urlWriteDto);

    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("shortUrl", SHORT_URL)
        .hasFieldOrPropertyWithValue("longUrl", HTTPS_STACKOVERFLOW_COM)
        .hasFieldOrPropertyWithValue("expiringAt", null);
  }

  @Test
  void getUrl_WhenNonExistingOrExpiredUrl_ThenThrowsResourceNotFoundException() {
    when(base62Service.decode(anyString())).thenReturn(DECODED_ID);
    when(urlRepository.findExistingNonExpiredUrl(eq(DECODED_ID),
        any(LocalDateTime.class))).thenReturn(Optional.empty());

    Throwable exception = catchThrowable(() -> urlService.getUrl(BASE62ENCODED_ID));

    assertThat(exception)
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("not found");
  }

  @Test
  void getUrl_WhenExistingUrl_ThenReturnsUrl() {
    when(base62Service.decode(anyString())).thenReturn(DECODED_ID);
    when(urlRepository.findExistingNonExpiredUrl(eq(DECODED_ID),
        any(LocalDateTime.class))).thenReturn(Optional.of(url));
    when(urlMapper.convertUrlToUrlReadDto(any(Url.class))).thenReturn(urlReadDto);

    UrlReadDto result = urlService.getUrl(BASE62ENCODED_ID);

    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("shortUrl", SHORT_URL)
        .hasFieldOrPropertyWithValue("longUrl", HTTPS_STACKOVERFLOW_COM)
        .hasFieldOrPropertyWithValue("expiringAt", null);
  }
}
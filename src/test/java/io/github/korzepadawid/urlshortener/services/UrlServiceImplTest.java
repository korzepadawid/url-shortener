package io.github.korzepadawid.urlshortener.services;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.korzepadawid.urlshortener.api.v1.mappers.UrlMapper;
import io.github.korzepadawid.urlshortener.api.v1.models.UrlReadDto;
import io.github.korzepadawid.urlshortener.api.v1.models.UrlWriteDto;
import io.github.korzepadawid.urlshortener.exceptions.ResourceNotFoundException;
import io.github.korzepadawid.urlshortener.models.Url;
import io.github.korzepadawid.urlshortener.repositories.UrlRepository;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
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

  private static final String HTTPS_STACKOVERFLOW_COM = "https://stackoverflow.com/";
  private static final String BASE62ENCODED_ID = "fhf2";
  private static final String SHORT_URL = "/" + BASE62ENCODED_ID;
  private static final Long DECODED_ID = 3641200L;

  private Set<Url> urls;
  private Url url;
  private UrlWriteDto urlWriteDto;
  private UrlReadDto urlReadDto;

  @BeforeEach
  void setUp() {
    urls = new HashSet<>();

    urlWriteDto = UrlWriteDto.builder()
        .url(HTTPS_STACKOVERFLOW_COM)
        .build();

    urlReadDto = UrlReadDto.builder()
        .longUrl(HTTPS_STACKOVERFLOW_COM)
        .shortUrl(SHORT_URL)
        .build();

    url = Url.builder()
        .id(DECODED_ID)
        .url(HTTPS_STACKOVERFLOW_COM)
        .build();

  }

  @Test
  void isNotExpiredUrl_WhenNull_ThenReturnsFalseInsteadOfNullPointerException() {
    boolean result = urlService.isNotExpiredUrl(null);
    assertThat(result).isFalse();
  }

  @Test
  void isNotExpiredUrl_WhenExpiringAtIsNull_ThenReturnsTrue() {
    boolean result = urlService.isNotExpiredUrl(Url.builder().expiringAt(null).build());
    assertThat(result).isTrue();
  }

  @Test
  void isNotExpiredUrl_WhenNotExpiredUrl_ThenReturnsTrue() {
    final LocalDateTime expiringAt = LocalDateTime.now().plusSeconds(10);
    boolean result = urlService.isNotExpiredUrl(Url.builder().expiringAt(expiringAt).build());
    assertThat(result).isTrue();
  }

  @Test
  void isNotExpiredUrl_WhenExpiredUrl_ThenReturnsFalse() {
    final LocalDateTime expiringAt = LocalDateTime.now().minusSeconds(10);
    boolean result = urlService.isNotExpiredUrl(Url.builder().expiringAt(expiringAt).build());
    assertThat(result).isFalse();
  }

  @Test
  void createUrl_WhenAlreadyExistingUrlAndExpiringAtIsNull_ThenReturnsExistingUrl() {
    urls.add(url);

    when(urlRepository.findByUrl(anyString())).thenReturn(urls);
    when(urlMapper.convertUrlToUrlReadDto(any(Url.class))).thenReturn(urlReadDto);

    UrlReadDto result = urlService.createUrl(urlWriteDto);

    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("longUrl", urlReadDto.getLongUrl())
        .hasFieldOrPropertyWithValue("shortUrl", urlReadDto.getShortUrl())
        .hasFieldOrPropertyWithValue("expiringAt", null);

    verify(urlRepository, times(1)).findByUrl(anyString());
    verify(urlRepository, times(0)).save(any());
  }

  @Test
  void createUrl_WhenExistingAndValidUrl_ThenReturnsExistingUrl() {
    final LocalDateTime expiringAt = LocalDateTime.now().plusSeconds(55);
    url.setExpiringAt(expiringAt);
    urlWriteDto.setExpiringAt(expiringAt);
    urlReadDto.setExpiringAt(expiringAt);
    urls.add(url);

    when(urlRepository.findByUrl(anyString())).thenReturn(urls);
    when(urlMapper.convertUrlToUrlReadDto(any(Url.class))).thenReturn(urlReadDto);

    UrlReadDto result = urlService.createUrl(urlWriteDto);

    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("longUrl", urlReadDto.getLongUrl())
        .hasFieldOrPropertyWithValue("shortUrl", urlReadDto.getShortUrl())
        .hasFieldOrPropertyWithValue("expiringAt", expiringAt);

    verify(urlRepository, times(1)).findByUrl(anyString());
    verify(urlRepository, times(0)).save(any());
  }


  @Test
  void createUrl_WhenNoMatch_ThenCreatesAndReturnsNewUrl() {
    final LocalDateTime expiringAt = LocalDateTime.now().plusSeconds(55);
    urlWriteDto.setExpiringAt(expiringAt);
    urlReadDto.setExpiringAt(expiringAt);
    urls.add(url);

    when(urlRepository.findByUrl(anyString())).thenReturn(urls);
    when(urlMapper.convertUrlWriteDtoToUrl(any(UrlWriteDto.class))).thenReturn(url);
    when(urlRepository.save(any(Url.class))).thenReturn(url);
    when(urlMapper.convertUrlToUrlReadDto(any(Url.class))).thenReturn(urlReadDto);

    UrlReadDto result = urlService.createUrl(urlWriteDto);

    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("longUrl", urlReadDto.getLongUrl())
        .hasFieldOrPropertyWithValue("shortUrl", urlReadDto.getShortUrl())
        .hasFieldOrPropertyWithValue("expiringAt", expiringAt);

    verify(urlRepository, times(1)).findByUrl(anyString());
    verify(urlRepository, times(1)).save(any());
  }

  @Test
  void getUrl_WhenNoMatch_ThenThrowsResourceNotFoundException() {
    when(base62Service.decode(anyString())).thenReturn(DECODED_ID);
    when(urlRepository.findById(anyLong())).thenReturn(Optional.empty());

    Throwable exception = catchThrowable(() -> urlService.getUrl(BASE62ENCODED_ID));

    assertThat(exception)
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("not found");
  }

  @Test
  void getUrl_WhenMatchButUrlHasAlreadyExpired_ThenThrowsResourceNotFoundException() {
    url.setExpiringAt(LocalDateTime.now().minusSeconds(1));
    when(base62Service.decode(anyString())).thenReturn(DECODED_ID);
    when(urlRepository.findById(anyLong())).thenReturn(Optional.of(url));

    Throwable exception = catchThrowable(() -> urlService.getUrl(BASE62ENCODED_ID));

    assertThat(exception)
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("not found");
  }

  @Test
  void getUrl_WhenMatchAndExpiringAtIsNull_ThenReturnsUrl() {
    when(base62Service.decode(anyString())).thenReturn(DECODED_ID);
    when(urlRepository.findById(anyLong())).thenReturn(Optional.of(url));
    when(urlMapper.convertUrlToUrlReadDto(any(Url.class))).thenReturn(urlReadDto);

    UrlReadDto result = urlService.getUrl(BASE62ENCODED_ID);

    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("longUrl", urlReadDto.getLongUrl())
        .hasFieldOrPropertyWithValue("shortUrl", urlReadDto.getShortUrl())
        .hasFieldOrPropertyWithValue("expiringAt", urlReadDto.getExpiringAt());
  }

  @Test
  void getUrl_WhenMatchAndExpiringAtValid_ThenReturnsUrl() {
    final LocalDateTime expiringAt = LocalDateTime.now().plusSeconds(10);
    url.setExpiringAt(expiringAt);
    urlReadDto.setExpiringAt(expiringAt);
    when(base62Service.decode(anyString())).thenReturn(DECODED_ID);
    when(urlRepository.findById(anyLong())).thenReturn(Optional.of(url));
    when(urlMapper.convertUrlToUrlReadDto(any(Url.class))).thenReturn(urlReadDto);

    UrlReadDto result = urlService.getUrl(BASE62ENCODED_ID);

    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("longUrl", urlReadDto.getLongUrl())
        .hasFieldOrPropertyWithValue("shortUrl", urlReadDto.getShortUrl())
        .hasFieldOrPropertyWithValue("expiringAt", expiringAt);
  }
}
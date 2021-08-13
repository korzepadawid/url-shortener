package io.github.korzepadawid.urlshortener.api.v1.mappers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import io.github.korzepadawid.urlshortener.api.v1.models.UrlReadDto;
import io.github.korzepadawid.urlshortener.api.v1.models.UrlWriteDto;
import io.github.korzepadawid.urlshortener.models.Url;
import io.github.korzepadawid.urlshortener.services.Base62Service;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UrlMapperImplTest {

  @Mock
  private Base62Service base62Service;

  @InjectMocks
  private UrlMapperImpl urlMapper;

  private static final String HTTPS_STACKOVERFLOW_COM = "https://stackoverflow.com/";
  private static final LocalDateTime EXPIRING_AT = LocalDateTime.now().plusWeeks(2);
  private static final LocalDateTime CREATED_AT = LocalDateTime.now().minusDays(2);
  private static final String BASE62ENCODED_ID = "j2Jp";
  private static final Long DECODED_ID = 4538735L;

  private UrlWriteDto urlWriteDto;
  private Url url;

  @BeforeEach
  void setUp() {
    urlWriteDto = UrlWriteDto.builder()
        .url(HTTPS_STACKOVERFLOW_COM)
        .expiringAt(null)
        .build();

    url = Url.builder()
        .id(DECODED_ID)
        .url(HTTPS_STACKOVERFLOW_COM)
        .createdAt(CREATED_AT)
        .expiringAt(null)
        .build();
  }

  @Test
  void convertUrlWriteDtoToUrl_WhenNull_ThenReturnsNull() {
    Url result = urlMapper.convertUrlWriteDtoToUrl(null);

    assertThat(result).isNull();
  }

  @Test
  void convertUrlWriteDtoToUrl_WhenExpiringAtIsNull_ThenCorrect() {
    Url result = urlMapper.convertUrlWriteDtoToUrl(urlWriteDto);

    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("url", HTTPS_STACKOVERFLOW_COM)
        .hasFieldOrPropertyWithValue("expiringAt", null)
        .hasFieldOrPropertyWithValue("id", null)
        .hasFieldOrPropertyWithValue("createdAt", null);
  }

  @Test
  void convertUrlWriteDtoToUrl_WhenExpiringAtIsNotNull_ThenCorrect() {
    urlWriteDto.setExpiringAt(EXPIRING_AT);

    Url result = urlMapper.convertUrlWriteDtoToUrl(urlWriteDto);

    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("url", HTTPS_STACKOVERFLOW_COM)
        .hasFieldOrPropertyWithValue("expiringAt", EXPIRING_AT)
        .hasFieldOrPropertyWithValue("id", null)
        .hasFieldOrPropertyWithValue("createdAt", null);
  }

  @Test
  void convertUrlToUrlReadDto_WhenNull_ThenReturnsNull() {
    UrlReadDto result = urlMapper.convertUrlToUrlReadDto(null);

    assertThat(result).isNull();
  }

  @Test
  void convertUrlToUrlReadDto_WhenValidObject_ThenCorrect() {
    when(base62Service.encode(anyLong())).thenReturn(BASE62ENCODED_ID);

    UrlReadDto result = urlMapper.convertUrlToUrlReadDto(url);

    assertThat(result)
        .isNotNull()
        .hasFieldOrPropertyWithValue("shortUrl", "/" + BASE62ENCODED_ID)
        .hasFieldOrPropertyWithValue("longUrl", HTTPS_STACKOVERFLOW_COM)
        .hasFieldOrPropertyWithValue("expiringAt", null);
  }
}
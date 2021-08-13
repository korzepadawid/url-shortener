package io.github.korzepadawid.urlshortener.api.v1.controllers;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.korzepadawid.urlshortener.JsonMapper;
import io.github.korzepadawid.urlshortener.api.v1.models.UrlReadDto;
import io.github.korzepadawid.urlshortener.api.v1.models.UrlWriteDto;
import io.github.korzepadawid.urlshortener.exceptions.ResourceNotFoundException;
import io.github.korzepadawid.urlshortener.services.UrlService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class UrlControllerTest {

  @Mock
  private UrlService urlService;

  @InjectMocks
  private UrlController urlController;

  private MockMvc mockMvc;

  private static final String HTTPS_STACKOVERFLOW_COM = "https://stackoverflow.com/";
  private static final String BASE62ENCODED_ID = "fhf2";
  private static final String INVALID_URL = "invalid url";
  private static final String URL_ERROR_MESSAGE = "Invalid url.";
  private static final String EXPIRING_AT_ERROR_MESSAGE = "The expiring date must be from the future.";
  private static final String SHORT_URL = "/" + BASE62ENCODED_ID;
  private static final LocalDateTime PAST_DATE = LocalDateTime.now().minusDays(22);

  private UrlWriteDto urlWriteDto;
  private UrlReadDto urlReadDto;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(urlController)
        .setControllerAdvice(RestControllerExceptionHandler.class)
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
  void createNew_WhenHttpPostAndInvalidUrl_ThenReturnsErrorDetailsAndStatus400() throws Exception {
    urlWriteDto.setUrl(INVALID_URL);

    mockMvc.perform(post(UrlController.BASE_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(JsonMapper.toJson(urlWriteDto)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.details.url", is(URL_ERROR_MESSAGE)));
  }

  @Test
  void createNew_WhenHttpPostAndExpiringAtFromThePast_ThenReturnsErrorDetailsAndStatus400()
      throws Exception {
    urlWriteDto.setExpiringAt(PAST_DATE);

    mockMvc.perform(post(UrlController.BASE_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(JsonMapper.toJson(urlWriteDto)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.details.expiringAt", is(EXPIRING_AT_ERROR_MESSAGE)));
  }

  @Test
  void createNew_WhenHttpPostAllFieldsWithErrors_ThenReturnsErrorDetailsAndStatus400()
      throws Exception {
    urlWriteDto.setUrl(INVALID_URL);
    urlWriteDto.setExpiringAt(PAST_DATE);

    mockMvc.perform(post(UrlController.BASE_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(JsonMapper.toJson(urlWriteDto)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.details.expiringAt", is(EXPIRING_AT_ERROR_MESSAGE)))
        .andExpect(jsonPath("$.details.url", is(URL_ERROR_MESSAGE)));
  }

  @Test
  void createNew_WhenHttpPostWithValidUrlWriteDto_ThenReturnsUrlReadDtoWithNewUrlAndStatus201()
      throws Exception {
    when(urlService.createUrl(any(UrlWriteDto.class))).thenReturn(urlReadDto);

    mockMvc.perform(post(UrlController.BASE_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(JsonMapper.toJson(urlWriteDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.shortUrl", is(urlReadDto.getShortUrl())))
        .andExpect(jsonPath("$.longUrl", is(urlReadDto.getLongUrl())))
        .andExpect(jsonPath("$.expiringAt", is(urlReadDto.getExpiringAt())));
  }

  @Test
  void getOne_WhenHttpGetWithNotExistingOrExpiredUrl_ThenReturnsStatus404() throws Exception {
    final String errorMessage = "Not found";
    when(urlService.getUrl(anyString())).thenThrow(new ResourceNotFoundException(errorMessage));

    mockMvc.perform(get(UrlController.BASE_URL + "/ds2"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message", is(errorMessage)));
  }

  @Test
  void getOne_WhenHttpGetWithExistingUrl_ThenReturnsUrlReadDtoAndStatus200() throws Exception {
    when(urlService.getUrl(anyString())).thenReturn(urlReadDto);

    mockMvc.perform(get(UrlController.BASE_URL + "/ds2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.shortUrl", is(urlReadDto.getShortUrl())))
        .andExpect(jsonPath("$.longUrl", is(urlReadDto.getLongUrl())))
        .andExpect(jsonPath("$.expiringAt", is(urlReadDto.getExpiringAt())));
  }
}
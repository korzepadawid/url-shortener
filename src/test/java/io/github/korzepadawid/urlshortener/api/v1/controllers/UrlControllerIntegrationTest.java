package io.github.korzepadawid.urlshortener.api.v1.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.korzepadawid.urlshortener.JsonMapper;
import io.github.korzepadawid.urlshortener.api.v1.models.UrlWriteDto;
import io.github.korzepadawid.urlshortener.config.JacksonDateTimeConfig;
import io.github.korzepadawid.urlshortener.models.Url;
import io.github.korzepadawid.urlshortener.repositories.UrlRepository;
import io.github.korzepadawid.urlshortener.services.Base62Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UrlControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private Base62Service base62Service;

  @Autowired
  private UrlRepository urlRepository;

  private static final String HTTPS_STACKOVERFLOW_COM = "https://stackoverflow.com/";

  @Test
  void httpGet_WhenExistingUrl_ThenReturnsGivenUrlAndStatus200() throws Exception {
    Url savedUrl = urlRepository.save(urlEntityWithExpiringAt(null));
    final String shortUrl = "/" + base62Service.encode(savedUrl.getId());

    mockMvc.perform(get(UrlController.BASE_URL + shortUrl))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.longUrl", is(HTTPS_STACKOVERFLOW_COM)))
        .andExpect(jsonPath("$.shortUrl", is(shortUrl)))
        .andExpect(jsonPath("$.expiringAt", nullValue()));
  }

  @Test
  void httpGet_WhenNonExistingUrl_ThenReturnsErrorAndStatus404() throws Exception {
    mockMvc.perform(get(UrlController.BASE_URL + "/sdad"))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message", containsString("not found")));
  }

  @Test
  void httpGet_WhenExpiredUrl_ThenReturnsErrorAndStatus404() throws Exception {
    Url url = urlEntityWithExpiringAt(LocalDateTime.now().minusSeconds(1));
    Url savedUrl = urlRepository.save(url);
    final String shortUrl = "/" + base62Service.encode(savedUrl.getId());

    mockMvc.perform(get(UrlController.BASE_URL + shortUrl))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message", containsString("not found")));
  }

  @Test
  void httpGet_WhenTwoEqualUrlButWithDifferentExpiringDate_ThenReturnsGiveUrl() throws Exception {
    Url url = urlEntityWithExpiringAt(LocalDateTime.now().plusMinutes(2));
    urlRepository.save(urlEntityWithExpiringAt(null));
    Url savedUrl = urlRepository.save(url);
    final String shortUrl = "/" + base62Service.encode(savedUrl.getId());

    mockMvc.perform(get(UrlController.BASE_URL + shortUrl))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.longUrl", is(HTTPS_STACKOVERFLOW_COM)))
        .andExpect(jsonPath("$.shortUrl", is(shortUrl)))
        .andExpect(jsonPath("$.expiringAt", is(url.getExpiringAt()
            .format(DateTimeFormatter.ofPattern(JacksonDateTimeConfig.DATE_PATTERN)))));
  }

  @Test
  void httpPost_WhenDateInvalidPattern_ThenReturnsJsonParseErrorAndStatus400() throws Exception {
    mockMvc.perform(post(UrlController.BASE_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(
            "{ \"url\": \"https://start.spring.io/\", \"expiringAt\": \"20-JUN-2025 08:03\" }"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message", is("JSON Parse error.")));
  }

  @Test
  void httpPost_WhenExpiringATFromThePast_ThenReturnsValidationErrorAndStatus400()
      throws Exception {
    UrlWriteDto urlWriteDto = UrlWriteDto.builder()
        .url(HTTPS_STACKOVERFLOW_COM)
        .expiringAt(LocalDateTime.now().minusSeconds(1))
        .build();

    mockMvc.perform(post(UrlController.BASE_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(JsonMapper.toJson(urlWriteDto)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.details.expiringAt", containsString("must be from the future")));
  }

  @Test
  void httpPost_WhenInvalidUrl_ThenReturnsValidationErrorAndStatus400() throws Exception {
    UrlWriteDto urlWriteDto = UrlWriteDto.builder()
        .url("httXps://github.com/korzepadawid/url-shortener")
        .build();

    mockMvc.perform(post(UrlController.BASE_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(JsonMapper.toJson(urlWriteDto)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.details.url", containsString("Invalid url.")));
  }

  @Test
  void httpPost_WhenExistingUrlWithTheSameExpiringAt_ThenReturnsOldInsteadOfNew() throws Exception {
    Url url = urlEntityWithExpiringAt(null);
    UrlWriteDto urlWriteDto = UrlWriteDto.builder().url(url.getUrl()).expiringAt(null).build();
    Url savedUrl = urlRepository.save(url);
    final String shortUrl = "/" + base62Service.encode(savedUrl.getId());

    mockMvc.perform(post(UrlController.BASE_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(JsonMapper.toJson(urlWriteDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.shortUrl", is(shortUrl)))
        .andExpect(jsonPath("$.longUrl", is(urlWriteDto.getUrl())))
        .andExpect(jsonPath("$.expiringAt", nullValue()));
  }

  Url urlEntityWithExpiringAt(LocalDateTime expiringAt) {
    return Url.builder()
        .url(HTTPS_STACKOVERFLOW_COM)
        .expiringAt(expiringAt)
        .build();
  }
}

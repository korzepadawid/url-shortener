package io.github.korzepadawid.urlshortener.api.v1.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

  Url urlEntityWithExpiringAt(LocalDateTime expiringAt) {
    return Url.builder()
        .url(HTTPS_STACKOVERFLOW_COM)
        .expiringAt(expiringAt)
        .build();
  }
}

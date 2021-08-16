package io.github.korzepadawid.urlshortener.api.v1.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.korzepadawid.urlshortener.models.Url;
import io.github.korzepadawid.urlshortener.repositories.UrlRepository;
import io.github.korzepadawid.urlshortener.services.Base62Service;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class IndexControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private Base62Service base62Service;

  @Autowired
  private UrlRepository urlRepository;

  private static final String HTTPS_STACKOVERFLOW_COM = "https://stackoverflow.com/";

  @Test
  void httpGet_WhenNotExistingUrl_ThenReturns404() throws Exception {
    mockMvc.perform(get(IndexController.BASE_URL + "/asdf"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message", containsString("not found")));
  }

  @Test
  void httpGet_WhenExpiredUrl_ThenReturns404() throws Exception {
    Url savedUrl = urlRepository.save(urlEntityWithExpiringAt(LocalDateTime.now().minusSeconds(1)));
    final String shortUrl = "/" + base62Service.encode(savedUrl.getId());

    mockMvc.perform(get(IndexController.BASE_URL + shortUrl))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message", containsString("not found")));
  }

  @Test
  void httpGet_WhenExistingUrl_ThenRedirectsAndReturnsStatus3XX() throws Exception {
    Url savedUrl = urlRepository.save(urlEntityWithExpiringAt(LocalDateTime.now().plusWeeks(1)));
    final String shortUrl = "/" + base62Service.encode(savedUrl.getId());

    mockMvc.perform(get(IndexController.BASE_URL + shortUrl))
        .andExpect(status().is3xxRedirection());
  }

  Url urlEntityWithExpiringAt(LocalDateTime expiringAt) {
    return Url.builder()
        .url(HTTPS_STACKOVERFLOW_COM)
        .expiringAt(expiringAt)
        .build();
  }
}

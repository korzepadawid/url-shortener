package io.github.korzepadawid.urlshortener.api.v1.controllers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.github.korzepadawid.urlshortener.api.v1.models.RestException;
import io.github.korzepadawid.urlshortener.api.v1.models.UrlReadDto;
import io.github.korzepadawid.urlshortener.api.v1.models.UrlWriteDto;
import io.github.korzepadawid.urlshortener.models.Url;
import io.github.korzepadawid.urlshortener.repositories.UrlRepository;
import io.github.korzepadawid.urlshortener.services.Base62Service;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UrlControllerE2ETest {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private UrlRepository urlRepository;

  @Autowired
  private Base62Service base62Service;

  private static final String HTTPS_STACKOVERFLOW_COM = "https://stackoverflow.com/";

  @Test
  void httpGet_WhenExistingUrl_ThenReturnsGivenUrl() {
    Url savedUrl = urlRepository.save(Url.builder().url(HTTPS_STACKOVERFLOW_COM).build());
    final String shortUrl = "/" + base62Service.encode(savedUrl.getId());

    UrlReadDto restResponse = restTemplate.getForObject(getAddress() + shortUrl, UrlReadDto.class);

    assertThat(restResponse)
        .isNotNull()
        .hasFieldOrPropertyWithValue("shortUrl", shortUrl)
        .hasFieldOrPropertyWithValue("longUrl", savedUrl.getUrl())
        .hasFieldOrPropertyWithValue("expiringAt", savedUrl.getExpiringAt());
  }

  @Test
  void httpGet_WhenNonExistingOrExpiredUrl_ThenReturnsErrorResponse() {
    RestException restResponse = restTemplate
        .getForObject(getAddress() + "/1", RestException.class);

    assertThat(restResponse)
        .isNotNull()
        .hasFieldOrPropertyWithValue("message", "Invalid encoded id: 1. Url not found.")
        .hasFieldOrPropertyWithValue("status", 404);
  }

  @Test
  void httpPost_WhenNonExistingOrExpiredUrl_ThenCreatesNewAndReturns() {
    UrlWriteDto urlWriteDto = urlWriteDtoWithExpiringAt(LocalDateTime.now().plusMinutes(5));
    HttpEntity<UrlWriteDto> request = new HttpEntity<>(urlWriteDto, new HttpHeaders());

    UrlReadDto response = restTemplate.postForObject(getAddress(), request, UrlReadDto.class);

    assertThat(response)
        .isNotNull()
        .hasFieldOrPropertyWithValue("shortUrl", "/1")
        .hasFieldOrPropertyWithValue("longUrl", urlWriteDto.getUrl())
        .hasFieldOrProperty("expiringAt");
  }

  @Test
  void httpPost_WhenDateFromThePast_ThenReturnsError() {
    UrlWriteDto urlWriteDto = urlWriteDtoWithExpiringAt(LocalDateTime.now().minusSeconds(1));
    HttpEntity<UrlWriteDto> request = new HttpEntity<>(urlWriteDto, new HttpHeaders());

    RestException response = restTemplate.postForObject(getAddress(), request, RestException.class);

    assertThat(response)
        .isNotNull()
        .hasFieldOrPropertyWithValue("message", "Validation error.")
        .hasFieldOrPropertyWithValue("status", 400);
  }

  String getAddress() {
    return "http://localhost:" + port + UrlController.BASE_URL;
  }

  UrlWriteDto urlWriteDtoWithExpiringAt(LocalDateTime expiringAt) {
    return UrlWriteDto.builder()
        .url(HTTPS_STACKOVERFLOW_COM)
        .expiringAt(expiringAt)
        .build();
  }
}

package io.github.korzepadawid.urlshortener.api.v1.controllers;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.korzepadawid.urlshortener.api.v1.models.UrlReadDto;
import io.github.korzepadawid.urlshortener.exceptions.ResourceNotFoundException;
import io.github.korzepadawid.urlshortener.services.UrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class IndexControllerTest {

  @Mock
  private UrlService urlService;

  @InjectMocks
  private IndexController indexController;

  private MockMvc mockMvc;

  private final static String ERROR_MESSAGE = "Not found";
  private static final String HTTPS_STACKOVERFLOW_COM = "https://stackoverflow.com/";
  private static final String BASE62ENCODED_ID = "fhf2";
  private static final String SHORT_URL = "/" + BASE62ENCODED_ID;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(indexController)
        .setControllerAdvice(RestControllerExceptionHandler.class)
        .build();
  }

  @Test
  void redirectToOriginalUrl_WhenNotExistingOrExpiredUrl_ThenReturnsStatus404() throws Exception {
    when(urlService.getUrl(anyString())).thenThrow(new ResourceNotFoundException(ERROR_MESSAGE));

    mockMvc.perform(get(IndexController.BASE_URL + "/"  + BASE62ENCODED_ID))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message", is(ERROR_MESSAGE)));
  }

  @Test
  void redirectToOriginalUrl_WhenExistingUrl_ThenReturnsStatus301AndRedirectsToOriginalPage()
      throws Exception {
    UrlReadDto urlReadDto = UrlReadDto.builder()
        .longUrl(HTTPS_STACKOVERFLOW_COM)
        .shortUrl(SHORT_URL)
        .build();
    when(urlService.getUrl(anyString())).thenReturn(urlReadDto);

    mockMvc.perform(get(IndexController.BASE_URL + "/" + BASE62ENCODED_ID))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(urlReadDto.getLongUrl()));
  }
}
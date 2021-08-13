package io.github.korzepadawid.urlshortener.api.v1.controllers;

import io.github.korzepadawid.urlshortener.api.v1.models.UrlReadDto;
import io.github.korzepadawid.urlshortener.services.UrlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping(IndexController.BASE_URL)
public class IndexController {

  public static final String BASE_URL = "/";

  private final UrlService urlService;

  @GetMapping("{encodedId}")
  public String redirectToOriginalUrl(@PathVariable String encodedId) {
    UrlReadDto urlReadDto = urlService.getUrl(encodedId);
    String originalUrl = urlReadDto.getLongUrl();
    log.info("Redirecting to " + originalUrl);
    return "redirect:" + originalUrl;
  }
}

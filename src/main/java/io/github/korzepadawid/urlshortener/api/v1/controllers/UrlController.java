package io.github.korzepadawid.urlshortener.api.v1.controllers;

import io.github.korzepadawid.urlshortener.api.v1.models.UrlReadDto;
import io.github.korzepadawid.urlshortener.api.v1.models.UrlWriteDto;
import io.github.korzepadawid.urlshortener.services.UrlService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping(UrlController.BASE_URL)
public class UrlController {

  public static final String BASE_URL = "/api/v1/urls";

  private final UrlService urlService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UrlReadDto createNew(@Valid @RequestBody UrlWriteDto urlWriteDto) {
    return urlService.createUrl(urlWriteDto);
  }

  @GetMapping("/{encodedId}")
  @ResponseStatus(HttpStatus.OK)
  public UrlReadDto getOne(@PathVariable String encodedId) {
    return urlService.getUrl(encodedId);
  }
}

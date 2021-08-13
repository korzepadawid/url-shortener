package io.github.korzepadawid.urlshortener.repositories;

import io.github.korzepadawid.urlshortener.models.Url;
import java.time.LocalDateTime;
import java.util.Optional;

public interface UrlRepository {

  Url save(Url url);

  Optional<Url> findExistingNonExpiredUrl(Long id,
      LocalDateTime expiringAt);

  Optional<Url> findAlreadyExistingUrl(String url, LocalDateTime expiringAt);
}

package io.github.korzepadawid.urlshortener.repositories;

import io.github.korzepadawid.urlshortener.models.Url;
import java.time.LocalDateTime;
import java.util.Optional;

public interface UrlRepository {

  Url save(Url url);

  Optional<Url> findFirstByUrlAndExpiringAtIsNull(String url);

  Optional<Url> findFirstByIdAndExpiringAtAfterOrExpiringAtIsNull(Long id,
      LocalDateTime expiringAt);
}

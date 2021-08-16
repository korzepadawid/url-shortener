package io.github.korzepadawid.urlshortener.repositories;

import io.github.korzepadawid.urlshortener.models.Url;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

public interface UrlRepository {

  Url save(Url url);

  Set<Url> findByUrl(String url);

  Optional<Url> findById(Long id);

  void deleteUrlsByExpiringAtBefore(LocalDateTime expiringAt);
}

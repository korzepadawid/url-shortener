package io.github.korzepadawid.urlshortener.repositories;

import io.github.korzepadawid.urlshortener.models.Url;
import java.time.LocalDateTime;
import java.util.Set;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlRepositorySql extends UrlRepository, CrudRepository<Url, Long> {

  Set<Url> findByUrl(String url);

  void deleteUrlsByExpiringAtBefore(LocalDateTime expiringAt);
}

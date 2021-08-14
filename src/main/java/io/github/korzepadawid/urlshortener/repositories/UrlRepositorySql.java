package io.github.korzepadawid.urlshortener.repositories;

import io.github.korzepadawid.urlshortener.models.Url;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlRepositorySql extends UrlRepository, CrudRepository<Url, Long> {

  @Query("from Url u where u.id=:id and (u.expiringAt > :expiringAt or u.expiringAt is null)")
  Optional<Url> findExistingNonExpiredUrl(Long id,
      LocalDateTime expiringAt);

  @Query("from Url u where u.url=:url and (u.expiringAt=:expiringAt or (:expiringAt is null and u.expiringAt is null ) )")
  Optional<Url> findAlreadyExistingUrl(String url, LocalDateTime expiringAt);

  void deleteUrlsByExpiringAtBefore(LocalDateTime expiringAt);
}

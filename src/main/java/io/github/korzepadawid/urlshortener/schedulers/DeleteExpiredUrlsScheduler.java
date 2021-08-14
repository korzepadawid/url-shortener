package io.github.korzepadawid.urlshortener.schedulers;

import io.github.korzepadawid.urlshortener.repositories.UrlRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class DeleteExpiredUrlsScheduler {

  private final UrlRepository urlRepository;

  @Transactional
  @Scheduled(cron = "0 * * * * *") // Every minute.
  public void deleteExpiredUrls() {
    log.warn("I'm going to remove expired URLs.");
    urlRepository.deleteUrlsByExpiringAtBefore(LocalDateTime.now());
  }
}

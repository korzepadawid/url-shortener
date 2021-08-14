package io.github.korzepadawid.urlshortener.api.v1.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UrlWriteDto {

  @URL(message = "Invalid url.")
  @NotBlank(message = "Url can't be blank.")
  @Length(min = 3, max = 255, message = "Url must be between 3-255 characters.")
  private String url;

  @Future(message = "The expiring date must be from the future.")
  private LocalDateTime expiringAt;
}

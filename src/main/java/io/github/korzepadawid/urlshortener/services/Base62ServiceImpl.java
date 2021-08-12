package io.github.korzepadawid.urlshortener.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class Base62ServiceImpl implements Base62Service {

  private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

  @Override
  public String encode(Long id) {
    if (id < 0) {
      throw new IllegalArgumentException(
          "Invalid parameter: " + id + ". Number must be greater than or equal to zero.");
    }

    if (id == 0) {
      return String.valueOf(BASE62.charAt(0));
    }

    StringBuilder resultBuilder = new StringBuilder();
    long number = id;

    while (number > 0) {
      resultBuilder.insert(0, BASE62.charAt((int) (number % 62)));
      number /= 62;
    }

    return resultBuilder.toString();
  }

  @Override
  public Long decode(String string) {
    Pattern pattern = Pattern.compile("^[0-9a-zA-Z]+$");
    Matcher matcher = pattern.matcher(string);

    if (!matcher.matches()) {
      throw new IllegalArgumentException(
          "Invalid parameter: " + string + ". It doesn't match pattern ^[0-9a-zA-Z]+$");
    }

    long result = 0L;

    for (int i = 0; i < string.length(); i++) {
      int exponent = string.length() - i - 1;
      result += (long) BASE62.indexOf(string.charAt(i)) * (Math.pow(62, exponent));
    }

    return result;
  }
}

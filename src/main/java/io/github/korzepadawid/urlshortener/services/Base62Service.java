package io.github.korzepadawid.urlshortener.services;

public interface Base62Service {

  String encode(Long number);

  Long decode(String string);
}

package io.github.korzepadawid.urlshortener.services;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Base62ServiceImplTest {

  private Base62Service base62Service;

  @BeforeEach
  void setUp() {
    base62Service = new Base62ServiceImpl();
  }

  @Test
  void encode_WhenNegativeNumber_ThenThrowsIllegalArgumentException() {
    final Long negativeNumber = -3L;

    Throwable exception = catchThrowable(() -> base62Service.encode(negativeNumber));

    assertThat(exception)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("greater than or equal to zero");
  }

  @Test
  void encode_WhenZero_ThenReturnsBase62RepresentationOfZero() {
    final Long zero = 0L;

    String result = base62Service.encode(zero);

    assertThat(result).isEqualTo("0");
  }

  @Test
  void encode_WhenCorrect_ThenReturnsBase62RepresentationOfNumber() {
    final Long number = 4538735L;

    String result = base62Service.encode(number);

    assertThat(result).isEqualTo("j2Jp");
  }

  @Test
  void decode_WhenBlankString_ThenThrowsIllegalArgumentException() {
    final String blankString = "";

    Throwable exception = catchThrowable(() -> base62Service.decode(blankString));

    assertThat(exception)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("");
  }

  @Test
  void decode_WhenStringWithSpecialCharacters_ThenThrowsIllegalArgumentException() {
    final String specialCharactersString = "k@f2";

    Throwable exception = catchThrowable(() -> base62Service.decode(specialCharactersString));

    assertThat(exception)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("doesn't match pattern");
  }

  @Test
  void decode_WhenStringWithDiacritics_ThenThrowsIllegalArgumentException() {
    final String diacriticsString = "tęst";

    Throwable exception = catchThrowable(() -> base62Service.decode(diacriticsString));

    assertThat(exception)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("doesn't match pattern");
  }

  @Test
  void decode_WhenStringWithDiacriticsAndSpecialCharacters_ThenThrowsIllegalArgumentException() {
    final String diacriticsAndSpecialCharactersString = "tę$t";

    Throwable exception = catchThrowable(
        () -> base62Service.decode(diacriticsAndSpecialCharactersString));

    assertThat(exception)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("doesn't match pattern");
  }

  @Test
  void decode_WhenZeroAsAString_ThenReturnsEncodedZero() {
    final String oneCharacterString = "0";

    Long result = base62Service.decode(oneCharacterString);

    assertThat(result).isEqualTo(0);
  }

  @Test
  void decode_WhenOneCharacterString_ThenReturnsEncodedNumber() {
    final String oneCharacterString = "b";

    Long result = base62Service.decode(oneCharacterString);

    assertThat(result).isEqualTo(11);
  }

  @Test
  void decode_WhenCorrect_ThenReturnsEncodedNumber() {
    final String string = "fdDf12";

    Long result = base62Service.decode(string);

    assertThat(result).isEqualTo(13943437364L);
  }
}
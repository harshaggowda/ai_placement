package com.careeros.auth.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StrongPasswordValidatorTest {

    private final StrongPasswordValidator validator = new StrongPasswordValidator();

    @Test
    void acceptsPasswordWithLetterAndDigitAndMinLength() {
        assertThat(validator.isValid("password1", null)).isTrue();
    }

    @Test
    void rejectsTooShort() {
        assertThat(validator.isValid("pass1", null)).isFalse();
    }

    @Test
    void rejectsWithoutDigit() {
        assertThat(validator.isValid("onlyletters", null)).isFalse();
    }

    @Test
    void rejectsWithoutLetter() {
        assertThat(validator.isValid("12345678", null)).isFalse();
    }

    @Test
    void rejectsNull() {
        assertThat(validator.isValid(null, null)).isFalse();
    }
}

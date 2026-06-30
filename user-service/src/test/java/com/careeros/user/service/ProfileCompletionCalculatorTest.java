package com.careeros.user.service;

import com.careeros.user.entity.ExperienceLevel;
import com.careeros.user.entity.UserProfile;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ProfileCompletionCalculator}. No Spring context required.
 */
class ProfileCompletionCalculatorTest {

    private final ProfileCompletionCalculator calculator = new ProfileCompletionCalculator();

    @Test
    void emptyProfileIsZeroPercent() {
        assertThat(calculator.calculate(new UserProfile())).isZero();
    }

    @Test
    void fullyPopulatedProfileIs100Percent() {
        UserProfile p = new UserProfile();
        p.setHeadline("h");
        p.setBio("b");
        p.setLocation("l");
        p.setAvatarUrl("a");
        p.setExperienceLevel(ExperienceLevel.STUDENT);
        p.setTargetRole("SWE");
        p.setTargetCompany("ACME");
        p.setGraduationYear(2026);
        p.setPreferredLanguages(List.of("Java"));
        p.setPreferredTechStack(List.of("Spring"));
        p.setEducation(List.of("BSc CS"));
        p.setGithubUrl("gh");
        p.setLinkedinUrl("li");

        assertThat(calculator.calculate(p)).isEqualTo(100);
    }

    @Test
    void partiallyPopulatedProfileIsBetween() {
        UserProfile p = new UserProfile();
        p.setHeadline("h");
        p.setBio("b");

        int pct = calculator.calculate(p);
        assertThat(pct).isGreaterThan(0).isLessThan(100);
    }

    @Test
    void blankHeadlineCountsAsMissing() {
        UserProfile p = new UserProfile();
        p.setHeadline("   "); // blank
        assertThat(calculator.calculate(p)).isZero();
    }

    @Test
    void emptyListCountsAsMissing() {
        UserProfile p = new UserProfile();
        p.setPreferredLanguages(List.of());
        assertThat(calculator.calculate(p)).isZero();
    }

    @Test
    void nullListCountsAsMissing() {
        UserProfile p = new UserProfile();
        p.setPreferredLanguages(null);
        assertThat(calculator.calculate(p)).isZero();
    }

    @Test
    void singleFieldFilledIs8Percent() {
        // 1 of 13 fields = 7.69 -> rounds to 8
        UserProfile p = new UserProfile();
        p.setHeadline("Only headline");
        assertThat(calculator.calculate(p)).isEqualTo(8);
    }

    @Test
    void sevenFieldsFilledIs54Percent() {
        // 7/13 = 53.84... -> rounds to 54
        UserProfile p = new UserProfile();
        p.setHeadline("X");
        p.setBio("Y");
        p.setLocation("Z");
        p.setAvatarUrl("url");
        p.setExperienceLevel(ExperienceLevel.JUNIOR);
        p.setTargetRole("Dev");
        p.setTargetCompany("Corp");
        assertThat(calculator.calculate(p)).isEqualTo(54);
    }

    @Test
    void experienceLevelNullCountsAsMissing() {
        UserProfile p = new UserProfile();
        p.setExperienceLevel(null);
        p.setHeadline("X"); // only 1 field set
        assertThat(calculator.calculate(p)).isEqualTo(8);
    }
}

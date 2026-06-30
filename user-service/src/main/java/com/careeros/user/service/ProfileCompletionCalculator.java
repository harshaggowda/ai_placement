package com.careeros.user.service;

import com.careeros.user.entity.UserProfile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Computes a profile's completion percentage from a fixed set of weighted-equally key fields.
 *
 * <p>Isolated as a component so the rule is unit-testable and reused by every read path. Adjusting
 * the definition of "complete" is a one-place change.
 */
@Component
public class ProfileCompletionCalculator {

    /** Percentage (0–100) of key fields that are populated. */
    public int calculate(UserProfile p) {
        boolean[] fields = {
                hasText(p.getHeadline()),
                hasText(p.getBio()),
                hasText(p.getLocation()),
                hasText(p.getAvatarUrl()),
                p.getExperienceLevel() != null,
                hasText(p.getTargetRole()),
                hasText(p.getTargetCompany()),
                p.getGraduationYear() != null,
                isNotEmpty(p.getPreferredLanguages()),
                isNotEmpty(p.getPreferredTechStack()),
                isNotEmpty(p.getEducation()),
                hasText(p.getGithubUrl()),
                hasText(p.getLinkedinUrl())
        };
        int filled = 0;
        for (boolean present : fields) {
            if (present) {
                filled++;
            }
        }
        return (int) Math.round(filled * 100.0 / fields.length);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private boolean isNotEmpty(List<?> value) {
        return value != null && !value.isEmpty();
    }
}

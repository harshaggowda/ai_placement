package com.careeros.user.service.impl;

import com.careeros.user.dto.NotificationPreferenceDto;
import com.careeros.user.dto.NotificationPreferenceResponseDto;
import com.careeros.user.entity.NotificationPreference;
import com.careeros.user.mapper.NotificationPreferenceMapper;
import com.careeros.user.repository.NotificationPreferenceRepository;
import com.careeros.user.service.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {

    private final NotificationPreferenceRepository repository;
    private final NotificationPreferenceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public NotificationPreferenceResponseDto get(UUID userId) {
        return mapper.toResponse(getOrCreate(userId));
    }

    @Override
    public NotificationPreferenceResponseDto upsert(UUID userId, NotificationPreferenceDto request) {
        NotificationPreference pref = getOrCreate(userId);
        mapper.updateEntity(request, pref);
        return mapper.toResponse(pref);
    }

    private NotificationPreference getOrCreate(UUID userId) {
        return repository.findByUserId(userId).orElseGet(() -> {
            NotificationPreference p = new NotificationPreference();
            p.setUserId(userId);
            return repository.save(p);
        });
    }
}

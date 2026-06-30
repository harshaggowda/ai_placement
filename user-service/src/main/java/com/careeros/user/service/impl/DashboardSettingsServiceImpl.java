package com.careeros.user.service.impl;

import com.careeros.user.dto.DashboardSettingsDto;
import com.careeros.user.dto.DashboardSettingsResponseDto;
import com.careeros.user.entity.DashboardSettings;
import com.careeros.user.mapper.DashboardSettingsMapper;
import com.careeros.user.repository.DashboardSettingsRepository;
import com.careeros.user.service.DashboardSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class DashboardSettingsServiceImpl implements DashboardSettingsService {

    private final DashboardSettingsRepository repository;
    private final DashboardSettingsMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public DashboardSettingsResponseDto get(UUID userId) {
        return mapper.toResponse(getOrCreate(userId));
    }

    @Override
    public DashboardSettingsResponseDto upsert(UUID userId, DashboardSettingsDto request) {
        DashboardSettings settings = getOrCreate(userId);
        mapper.updateEntity(request, settings);
        return mapper.toResponse(settings);
    }

    private DashboardSettings getOrCreate(UUID userId) {
        return repository.findByUserId(userId).orElseGet(() -> {
            DashboardSettings s = new DashboardSettings();
            s.setUserId(userId);
            return repository.save(s);
        });
    }
}

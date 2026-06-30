package com.careeros.career.service.impl;

import com.careeros.career.dto.ProgressSnapshotDto;
import com.careeros.career.dto.ProgressSnapshotResponseDto;
import com.careeros.career.entity.ProgressSnapshot;
import com.careeros.career.mapper.ProgressSnapshotMapper;
import com.careeros.career.repository.ProgressSnapshotRepository;
import com.careeros.career.service.ProgressSnapshotService;
import com.careeros.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgressSnapshotServiceImpl implements ProgressSnapshotService {

    private final ProgressSnapshotRepository snapshotRepository;
    private final ProgressSnapshotMapper snapshotMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProgressSnapshotResponseDto> getAllSnapshots(UUID userId) {
        return snapshotRepository.findAllByUserIdOrderBySnapshotDateDesc(userId).stream()
                .map(snapshotMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProgressSnapshotResponseDto getSnapshot(UUID userId, UUID snapshotId) {
        ProgressSnapshot snapshot = getSnapshotEntity(userId, snapshotId);
        return snapshotMapper.toResponse(snapshot);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ProgressSnapshotResponseDto getSnapshotByDate(UUID userId, LocalDate date) {
        ProgressSnapshot snapshot = snapshotRepository.findBySnapshotDateAndUserId(date, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Snapshot not found for date: " + date));
        return snapshotMapper.toResponse(snapshot);
    }

    @Override
    @Transactional
    public ProgressSnapshotResponseDto createSnapshot(UUID userId, ProgressSnapshotDto dto) {
        if (snapshotRepository.findBySnapshotDateAndUserId(dto.snapshotDate(), userId).isPresent()) {
            throw new IllegalArgumentException("A snapshot for this date already exists");
        }
        
        ProgressSnapshot snapshot = snapshotMapper.toEntity(dto);
        snapshot.setUserId(userId);

        ProgressSnapshot saved = snapshotRepository.save(snapshot);
        return snapshotMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProgressSnapshotResponseDto updateSnapshot(UUID userId, UUID snapshotId, ProgressSnapshotDto dto) {
        ProgressSnapshot snapshot = getSnapshotEntity(userId, snapshotId);
        
        if (!snapshot.getSnapshotDate().equals(dto.snapshotDate()) && 
            snapshotRepository.findBySnapshotDateAndUserId(dto.snapshotDate(), userId).isPresent()) {
            throw new IllegalArgumentException("A snapshot for this new date already exists");
        }
        
        snapshotMapper.updateEntity(dto, snapshot);

        ProgressSnapshot updated = snapshotRepository.save(snapshot);
        return snapshotMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteSnapshot(UUID userId, UUID snapshotId) {
        ProgressSnapshot snapshot = getSnapshotEntity(userId, snapshotId);
        snapshotRepository.delete(snapshot);
    }

    private ProgressSnapshot getSnapshotEntity(UUID userId, UUID snapshotId) {
        return snapshotRepository.findByIdAndUserId(snapshotId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Snapshot not found"));
    }
}

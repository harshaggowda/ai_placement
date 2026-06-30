package com.careeros.career.service;

import com.careeros.career.dto.ProgressSnapshotDto;
import com.careeros.career.dto.ProgressSnapshotResponseDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ProgressSnapshotService {

    List<ProgressSnapshotResponseDto> getAllSnapshots(UUID userId);

    ProgressSnapshotResponseDto getSnapshot(UUID userId, UUID snapshotId);
    
    ProgressSnapshotResponseDto getSnapshotByDate(UUID userId, LocalDate date);

    ProgressSnapshotResponseDto createSnapshot(UUID userId, ProgressSnapshotDto dto);

    ProgressSnapshotResponseDto updateSnapshot(UUID userId, UUID snapshotId, ProgressSnapshotDto dto);

    void deleteSnapshot(UUID userId, UUID snapshotId);
}

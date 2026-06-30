package com.careeros.career.service.impl;

import com.careeros.career.dto.ProgressSnapshotDto;
import com.careeros.career.dto.ProgressSnapshotResponseDto;
import com.careeros.career.entity.ProgressSnapshot;
import com.careeros.career.mapper.ProgressSnapshotMapper;
import com.careeros.career.repository.ProgressSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgressSnapshotServiceImplTest {

    @Mock ProgressSnapshotRepository snapshotRepository;
    @Mock ProgressSnapshotMapper snapshotMapper;

    @InjectMocks ProgressSnapshotServiceImpl snapshotService;

    private final UUID userId = UUID.randomUUID();
    private final UUID snapshotId = UUID.randomUUID();

    @Test
    void createSnapshotSucceeds() {
        LocalDate date = LocalDate.now();
        ProgressSnapshotDto dto = new ProgressSnapshotDto(date, 5, 2, 1, 0, 3, null);
        
        ProgressSnapshot snapshot = new ProgressSnapshot();
        snapshot.setId(snapshotId);

        when(snapshotRepository.findBySnapshotDateAndUserId(date, userId)).thenReturn(Optional.empty());
        when(snapshotMapper.toEntity(dto)).thenReturn(snapshot);
        when(snapshotRepository.save(snapshot)).thenReturn(snapshot);
        
        ProgressSnapshotResponseDto responseMock = new ProgressSnapshotResponseDto(snapshotId, userId, date, 5, 2, 1, 0, 3, null, null, null);
        when(snapshotMapper.toResponse(snapshot)).thenReturn(responseMock);

        ProgressSnapshotResponseDto result = snapshotService.createSnapshot(userId, dto);

        verify(snapshotRepository).save(snapshot);
        assertThat(result.applicationsSent()).isEqualTo(5);
    }

    @Test
    void createSnapshotThrowsWhenDateExists() {
        LocalDate date = LocalDate.now();
        ProgressSnapshotDto dto = new ProgressSnapshotDto(date, 5, 2, 1, 0, 3, null);
        
        when(snapshotRepository.findBySnapshotDateAndUserId(date, userId)).thenReturn(Optional.of(new ProgressSnapshot()));

        assertThatThrownBy(() -> snapshotService.createSnapshot(userId, dto))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

package com.wpits.customerbill.entities;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimePeriod {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
}

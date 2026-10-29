package com.wpits.accountmanagement.entities.common;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** TMF666 TimePeriod - a deadline (endDateTime only), a startDateTime only, or both. */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimePeriod {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
}

package com.wpits.accountmanagement.dtos.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimePeriodDto {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime startDateTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime endDateTime;
}

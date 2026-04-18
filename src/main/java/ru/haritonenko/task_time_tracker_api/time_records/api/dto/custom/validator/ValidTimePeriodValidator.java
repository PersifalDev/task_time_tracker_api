package ru.haritonenko.task_time_tracker_api.time_records.api.dto.custom.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import ru.haritonenko.task_time_tracker_api.time_records.api.dto.TimeRecordCreateRequestDto;
import ru.haritonenko.task_time_tracker_api.time_records.api.dto.custom.annotation.ValidTimePeriod;
import ru.haritonenko.task_time_tracker_api.time_records.api.dto.filter.TimeRecordRequestFilterDto;

@Component
public class ValidTimePeriodValidator implements ConstraintValidator<ValidTimePeriod, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        if (value instanceof TimeRecordCreateRequestDto dto) {
            if (dto.startTime() == null || dto.endTime() == null) {
                return true;
            }
            return !dto.endTime().isBefore(dto.startTime());
        }

        if (value instanceof TimeRecordRequestFilterDto dto) {
            if (dto.startTime() == null || dto.endTime() == null) {
                return true;
            }
            return !dto.endTime().isBefore(dto.startTime());
        }

        return true;
    }
}
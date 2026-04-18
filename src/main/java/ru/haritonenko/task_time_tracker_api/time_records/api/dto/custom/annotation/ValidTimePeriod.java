package ru.haritonenko.task_time_tracker_api.time_records.api.dto.custom.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.haritonenko.task_time_tracker_api.time_records.api.dto.custom.validator.ValidTimePeriodValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidTimePeriodValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTimePeriod {

    String message() default "End time must be after or equal to start time";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
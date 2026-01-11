package uz.fido.pfexchange.dto;

import java.time.LocalDate;

public record PersonActivityDto(
    LocalDate beginDate,
    LocalDate endDate,
    String code,
    String stuffFlag
) {}

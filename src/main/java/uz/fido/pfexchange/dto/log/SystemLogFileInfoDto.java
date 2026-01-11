package uz.fido.pfexchange.dto.log;


public record SystemLogFileInfoDto(String path, long size, boolean archived) {}

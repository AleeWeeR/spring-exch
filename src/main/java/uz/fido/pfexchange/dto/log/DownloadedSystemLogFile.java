package uz.fido.pfexchange.dto.log;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

public record DownloadedSystemLogFile(
        Resource resource, MediaType mediaType, String fileName, long contentLength) {}

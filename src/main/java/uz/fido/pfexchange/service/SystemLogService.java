package uz.fido.pfexchange.service;

import uz.fido.pfexchange.dto.log.DownloadedSystemLogFile;
import uz.fido.pfexchange.dto.log.SystemLogDto;
import uz.fido.pfexchange.dto.log.SystemLogFileInfoDto;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface SystemLogService {

    SystemLogDto getLatestSystemLogs(String file, int lines);

    List<SystemLogFileInfoDto> listLogFiles();

    Path getLogFilePath(String fileName);

    DownloadedSystemLogFile prepareDownload(String file) throws IOException;
}

package uz.fido.pfexchange.service.impl;

import lombok.RequiredArgsConstructor;

import org.apache.commons.io.input.ReversedLinesFileReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import uz.fido.pfexchange.dto.log.DownloadedSystemLogFile;
import uz.fido.pfexchange.dto.log.SystemLogDto;
import uz.fido.pfexchange.dto.log.SystemLogFileInfoDto;
import uz.fido.pfexchange.service.SystemLogService;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

@Service
@RequiredArgsConstructor
public class SystemLogServiceImpl implements SystemLogService {

    @Value("${LOG_PATH:/var/log/app}")
    private String logPath;

    @Value("${LOG_FILE:pfexchange}")
    private String logFileName;

    @Override
    public SystemLogDto getLatestSystemLogs(String file, int lines) {
        try {
            Path baseDir = Path.of(logPath).normalize();
            Path logFile = baseDir.resolve(file).normalize();

            if (!logFile.startsWith(baseDir)) {
                throw new IOException("Invalid log file path");
            }

            if (!Files.exists(logFile)) {
                throw new IOException("Log file not found");
            }

            List<String> logs = readLastNLines(logFile, lines);

            return new SystemLogDto(file, logs, logs.size());
        } catch (IOException e) {
            return new SystemLogDto(file, List.of("Error: " + e.getMessage()), 0);
        }
    }

    @Override
    public List<SystemLogFileInfoDto> listLogFiles() {
        Path baseDir = Path.of(logPath);
    
        if (!Files.isDirectory(baseDir)) {
            return List.of();
        }
    
        List<SystemLogFileInfoDto> result = new ArrayList<>();
    
        collectLogFiles(baseDir, baseDir, result);
    
        Path archiveDir = baseDir.resolve("archive");
        if (Files.isDirectory(archiveDir)) {
            collectLogFiles(baseDir, archiveDir, result);
        }
    
        return result;
    }


    @Override
    public Path getLogFilePath(String fileName) {
        Path baseDir = Path.of(logPath).toAbsolutePath().normalize();
        Path filePath = baseDir.resolve(fileName).normalize();

        if (!filePath.startsWith(baseDir)) {
            throw new IllegalArgumentException("Invalid log file path");
        }

        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("Log file not found");
        }

        return filePath;
    }

    @Override
    public DownloadedSystemLogFile prepareDownload(String file) throws IOException {
        Path logFile = getLogFilePath(file);

        if (!Files.exists(logFile)) {
            throw new FileNotFoundException("Log file not found");
        }

        if (file.endsWith(".gz")) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            try (InputStream is = Files.newInputStream(logFile);
                    GZIPInputStream gis = new GZIPInputStream(is)) {
                gis.transferTo(baos);
            }

            byte[] content = baos.toByteArray();

            return new DownloadedSystemLogFile(
                    new ByteArrayResource(content),
                    MediaType.TEXT_PLAIN,
                    file.replaceAll("\\.gz$", ".txt"),
                    content.length);
        }

        Resource resource = new FileSystemResource(logFile);

        return new DownloadedSystemLogFile(
                resource,
                MediaType.TEXT_PLAIN,
                logFile.getFileName().toString(),
                Files.size(logFile));
    }

    private void collectLogFiles(Path baseDir, Path dir, List<SystemLogFileInfoDto> target) {
        try (Stream<Path> stream = Files.list(dir)) {
            stream
                .filter(Files::isRegularFile)
                .filter(this::isLogFile)
                .map(p -> toDto(baseDir, p))
                .forEach(target::add);
        } catch (IOException ignored) {
        }
    }


    private boolean isLogFile(Path p) {
        String name = p.getFileName().toString();
        return name.endsWith(".log") || name.endsWith(".log.gz");
    }

    private SystemLogFileInfoDto toDto(Path baseDir, Path file) {
        String relativePath = baseDir.relativize(file).toString();

        return new SystemLogFileInfoDto(
                relativePath, getFileSizeSafe(file), relativePath.endsWith(".gz"));
    }

    private List<String> readLastNLines(Path path, int n) throws IOException {
        List<String> lines = new ArrayList<>();

        if (path.toString().endsWith(".gz")) {
            readFromGzip(path, n, lines);
        } else {
            readFromPlainFile(path, n, lines);
        }

        Collections.reverse(lines);
        return lines;
    }

    private void readFromPlainFile(Path path, int n, List<String> lines) throws IOException {

        try (ReversedLinesFileReader reader =
                ReversedLinesFileReader.builder()
                        .setPath(path)
                        .setCharset(StandardCharsets.UTF_8)
                        .get()) {

            String line;
            int count = 0;

            while ((line = reader.readLine()) != null && count < n) {
                lines.add(line);
                count++;
            }
        }
    }

    private void readFromGzip(Path path, int n, List<String> result) throws IOException {

        Deque<String> buffer = new ArrayDeque<>(n);

        try (InputStream is = Files.newInputStream(path);
                GZIPInputStream gis = new GZIPInputStream(is);
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(gis, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (buffer.size() == n) {
                    buffer.removeFirst();
                }
                buffer.addLast(line);
            }
        }

        result.addAll(buffer);
    }

    private long getFileSizeSafe(Path path) {
        try {
            return Files.size(path);
        } catch (IOException e) {
            return 0;
        }
    }
}

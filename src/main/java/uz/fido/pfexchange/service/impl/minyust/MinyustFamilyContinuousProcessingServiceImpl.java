package uz.fido.pfexchange.service.impl.minyust;

import jakarta.annotation.PreDestroy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import uz.fido.pfexchange.dto.minyust.ProcessingState;
import uz.fido.pfexchange.enums.MinyustState;
import uz.fido.pfexchange.service.minyust.MinyustFamilyBatchRequestProcessor;
import uz.fido.pfexchange.service.minyust.MinyustFamilyContinuousProcessingService;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinyustFamilyContinuousProcessingServiceImpl implements MinyustFamilyContinuousProcessingService {

    private final MinyustFamilyBatchRequestProcessor processor;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicReference<Thread> processingThread = new AtomicReference<>();

    @Override
    public boolean start() {
        if (!isRunning.compareAndSet(false, true)) {
            log.debug("Continuous processing already running, ignoring start request");
            return false;
        }

        Thread thread = new Thread(this::runProcessing, "minyust-continuous-processor");
        thread.setUncaughtExceptionHandler(this::handleUncaughtException);
        processingThread.set(thread);
        thread.start();

        log.info("Continuous processing started on thread: {}", thread.getName());
        return true;
    }

    @Override
    public boolean stop() {
        Thread thread = processingThread.get();

        if (thread != null && thread.isAlive()) {
            log.info("Interrupting continuous processing thread: {}", thread.getName());
            thread.interrupt();
            return true;
        }

        log.debug("No active processing to stop");
        return false;
    }

    @Override
    public ProcessingState getState() {
        Thread thread = processingThread.get();
        boolean running = isRunning.get();

        if (running && thread != null && thread.isAlive()) {
            return new ProcessingState(MinyustState.RUNNING, thread.getName());
        }

        if (running) {
            // Thread died unexpectedly, clean up state
            cleanup();
            return new ProcessingState(MinyustState.STOPPED_UNEXPECTEDLY, null);
        }

        return new ProcessingState(MinyustState.IDLE, null);
    }

    @Override
    public boolean isRunning() {
        return isRunning.get()
                && processingThread.get() != null
                && processingThread.get().isAlive();
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down continuous processing service");
        stop();
    }

    private void runProcessing() {
        log.info("Starting batch processing loop");
        try {
            processor.processAllPendingRequests();
            log.info("Batch processing completed successfully");
        } catch (Exception e) {
            log.error("Batch processing failed with exception", e);
        } finally {
            cleanup();
        }
    }

    private void handleUncaughtException(Thread t, Throwable e) {
        log.error("Uncaught exception in thread {}: {}", t.getName(), e.getMessage(), e);
        cleanup();
    }

    private void cleanup() {
        isRunning.set(false);
        processingThread.set(null);
        log.debug("Processing state cleaned up");
    }
}

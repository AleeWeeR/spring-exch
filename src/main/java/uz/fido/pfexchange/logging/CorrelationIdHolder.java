package uz.fido.pfexchange.logging;

public class CorrelationIdHolder {

    private static final ThreadLocal<String> CORRELATION_ID = new ThreadLocal<>();

    public static void set(String correlationId) {
        CORRELATION_ID.set(correlationId);
    }

    public static String get() {
        return CORRELATION_ID.get();
    }

    public static void clear() {
        CORRELATION_ID.remove();
    }
}

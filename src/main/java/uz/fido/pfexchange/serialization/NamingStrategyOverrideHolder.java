package uz.fido.pfexchange.serialization;

import uz.fido.pfexchange.config.properties.SerializationProperties.NamingType;

public final class NamingStrategyOverrideHolder {

    private static final ThreadLocal<NamingType> OVERRIDE = new ThreadLocal<>();

    private NamingStrategyOverrideHolder() {}

    public static void set(NamingType type) {
        OVERRIDE.set(type);
    }

    public static NamingType get() {
        return OVERRIDE.get();
    }

    public static void clear() {
        OVERRIDE.remove();
    }

    public static boolean hasOverride() {
        return OVERRIDE.get() != null;
    }
}
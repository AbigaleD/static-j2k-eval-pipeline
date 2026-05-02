package cases;

import java.util.Locale;

public final class StaticInitializersAndConstants {
    public static final String DEFAULT_REGION = "us-east";
    public static final int BASE_TIMEOUT_SECONDS = 30;
    public static final int MAX_RETRIES = 3;
    public static final String NORMALIZED_REGION;
    public static final int MAX_TOTAL_WAIT_SECONDS;

    private static final String PREFIX;

    static {
        NORMALIZED_REGION = DEFAULT_REGION.toUpperCase(Locale.ROOT);
        MAX_TOTAL_WAIT_SECONDS = BASE_TIMEOUT_SECONDS * MAX_RETRIES;
        PREFIX = "region-" + NORMALIZED_REGION.toLowerCase(Locale.ROOT);
    }

    private StaticInitializersAndConstants() {
    }

    public static String cacheKey(String serviceName) {
        if (serviceName == null || serviceName.trim().isEmpty()) {
            throw new IllegalArgumentException("serviceName must not be blank");
        }
        return PREFIX + ":" + serviceName.trim().toLowerCase(Locale.ROOT);
    }
}

package apps.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.lang.reflect.Field;

public class AppsUtil {
    /*
     * Use reflection to reset the apps.AppsBase instance
     */
    public static void resetAppsBase() {
        assertDoesNotThrow( () -> {
            Class<?> c = apps.AppsBase.class;
            Field f = c.getDeclaredField("preInit");
            f.setAccessible(true);
            f.set(null, false);
        }, "Failed to reset apps.AppsBase static preInit field");
    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AppsUtil.class);

}

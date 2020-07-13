package apps.util;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import java.lang.reflect.Field;

public class AppsUtil {
    /*
     * Use reflection to reset the apps.AppsBase instance
     */
    public static void resetAppsBase() {
        try {
            Class<?> c = apps.AppsBase.class;
            Field f = c.getDeclaredField("preInit");
            f.setAccessible(true);
            f.set(null, false);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException x) {
            log.error("Failed to reset apps.AppsBase static preInit field", x);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(AppsUtil.class);

}

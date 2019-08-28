package jmri.util;

import java.lang.reflect.Field;

/**
 * Static methods using reflection.
 *
 * @author Daniel Bergqvist 2019
 */
public final class ReflectionUtilScaffold {

    // This class should never be instanciated.
    private ReflectionUtilScaffold() {
    }
    
    public static void setField(Object object, String fieldName, Object newValue)
            throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {
        
        Field f1 = object.getClass().getDeclaredField(fieldName);
        f1.setAccessible(true);
        f1.set(object, newValue);
    }
    
}

package jmri.swing;

import jmri.Permission;

/**
 * Permission Swing tools.
 *
 * @author Daniel Bergqvist 2024
 */
public final class PermissionSwingTools {

    // Private constructor to ensure this class never get instanciated.
    private PermissionSwingTools() {
    }

    /**
     * Find the name of the adapter class for an object.
     *
     * @param o object of a configurable type
     * @return class name of adapter
     */
    public static String adapterNameForObject(Object o) {
        return adapterNameForClass(o.getClass());
    }

    /**
     * Find the name of the adapter class for an object.
     *
     * @param c class of a configurable type
     * @return class name of adapter
     */
    public static String adapterNameForClass(Class<?> c) {
        String className = c.getName();
        log.trace("handle object of class {}", className);
        int lastDot = className.lastIndexOf(".");
        if (lastDot > 0) {
            // found package-class boundary OK
            int firstDollarSign = className.indexOf("$");
            String result;
            if (firstDollarSign > 0) {
                // found inner-class boundary OK
                result = className.substring(0, lastDot)
                        + ".swing."
                        + className.substring(lastDot + 1, firstDollarSign)
                        + "Swing$"
                        + className.substring(firstDollarSign + 1)
                        + "Swing";
            } else {
                result = className.substring(0, lastDot)
                        + ".swing."
                        + className.substring(lastDot + 1, className.length())
                        + "Swing";
            }
            log.trace("adapter class name is {}", result);
            return result;
        } else {
            // No last dot found! This should not be possible in Java.
            log.error("No package name found, which is not yet handled!");
            throw new RuntimeException("No package name found, which is not yet handled!");
        }
    }

    /**
     * Get a SwingConfiguratorInterface for a class
     * @param permission The permission to get a PermissionSwing of
     * @return a PermissionSwing object
     */
    static public PermissionSwing getPermissionSwingForClass(Permission permission) {
        Class<?> clazz = permission.getClass();
        PermissionSwing permissionSwing;
        try {
            permissionSwing = (PermissionSwing) Class.forName(adapterNameForClass(clazz)).getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException
                    | NoSuchMethodException | java.lang.reflect.InvocationTargetException ex) {
            log.error("Cannot load PermissionSwing for {}", clazz.getName(), ex);
            return null;
        }
        return permissionSwing;
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PermissionSwingTools.class);

}

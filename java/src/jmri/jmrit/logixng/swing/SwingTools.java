package jmri.jmrit.logixng.swing;

/**
 * LogixNG Swing tools.
 *
 * @author Daniel Bergqvist 2019
 */
public final class SwingTools {

    // Private constructor to ensure this class never get instanciated.
    private SwingTools() {
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
            String result = className.substring(0, lastDot)
                    + ".swing."
                    + className.substring(lastDot + 1, className.length())
                    + "Swing";
            log.trace("adapter class name is {}", result);
            return result;
        } else {
            // No last dot found! This should not be possible in Java.
            log.error("No package name found, which is not yet handled!");
            throw new RuntimeException("No package name found, which is not yet handled!");
        }
    }

    /**
     * Get a SwingConfiguratorInterface for an object
     * @param object The object to get a SwingConfiguratorInterface of
     * @return a SwingConfiguratorInterface object
     */
    static public SwingConfiguratorInterface getSwingConfiguratorForObject(Object object) {
        SwingConfiguratorInterface adapter = null;
        try {
            adapter = (SwingConfiguratorInterface) Class.forName(adapterNameForObject(object)).getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException
                    | NoSuchMethodException | java.lang.reflect.InvocationTargetException ex) {
            log.error("Cannot load SwingConfiguratorInterface adapter for {}", object.getClass().getName(), ex);
        }
        if (adapter != null) {
            return adapter;
        } else {
            log.error("Cannot load SwingConfiguratorInterface for {}", object.getClass().getName());
            return null;
        }
    }

    /**
     * Get a SwingConfiguratorInterface for a class
     * @param clazz The class to get a SwingConfiguratorInterface of
     * @return a SwingConfiguratorInterface object
     */
    static public SwingConfiguratorInterface getSwingConfiguratorForClass(Class<?> clazz) {
        SwingConfiguratorInterface adapter = null;
        try {
            adapter = (SwingConfiguratorInterface) Class.forName(adapterNameForClass(clazz)).getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException
                    | NoSuchMethodException | java.lang.reflect.InvocationTargetException ex) {
            log.error("Cannot load SwingConfiguratorInterface adapter for {}", clazz.getName(), ex);
        }
        if (adapter != null) {
            return adapter;
        } else {
            log.error("Cannot load SwingConfiguratorInterface for {}", clazz.getName());
            return null;
        }
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SwingTools.class);

}

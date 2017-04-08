package jmri.jmrix;

/**
 * Lightweight class to check if a system is active.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 * @deprecated since 4.5.1
 */
@Deprecated
abstract public class ActiveSystemFlag {

    /**
     * Check if a particular package name is active.
     *
     * @param name A package name similar to "jmri.jmrix.loconet"
     * @return true if active; false otherwise
     * @throws java.lang.ClassNotFoundException            if class "ActiveFlag"
     *                                                     in package name
     *                                                     cannot be found
     * @throws java.lang.NoSuchMethodException             if "ActiveFlag" does
     *                                                     not implement the
     *                                                     method "isActive"
     * @throws java.lang.IllegalAccessException            if the method
     *                                                     "isActive" is not
     *                                                     public
     * @throws java.lang.reflect.InvocationTargetException if method "isActive"
     *                                                     cannot be invoked
     */
    static public boolean isActive(String name)
            throws ClassNotFoundException,
            NoSuchMethodException,
            IllegalAccessException,
            java.lang.reflect.InvocationTargetException {
        String classname = name + ".ActiveFlag";
        Class<?> c = Class.forName(classname);
        java.lang.reflect.Method m = c.getMethod("isActive", (Class[]) null);
        Object b = m.invoke(null, (Object[]) null); // static object, no args
        return ((Boolean) b);
    }
}

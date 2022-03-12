package jmri.configurexml;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Abstract class to provide basic error handling for XmlAdapter
 *
 * @author Bob Jacobsen Copyright (c) 2009
 * @see XmlAdapter
 */
public abstract class AbstractXmlAdapter implements XmlAdapter {

    private ErrorHandler errorHandler = XmlAdapter.getDefaultExceptionHandler();

    /** {@inheritDoc} */
    @Override
    public void handleException(
            String description,
            String operation,
            String systemName,
            String userName,
            Exception exception) {
        if (errorHandler != null) {
            this.errorHandler.handle(new ErrorMemo(this, operation, description, systemName, userName, exception));
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean load(Element e) throws JmriConfigureXmlException {
        throw new UnsupportedOperationException("One of the other load methods must be implemented.");
    }

    /** {@inheritDoc} */
    @Override
    public void load(Element e, Object o) throws JmriConfigureXmlException {
        log.error("Invalid method called");
    }

    /** {@inheritDoc} */
    @Override
    public boolean load(@Nonnull Element shared, Element perNode) throws JmriConfigureXmlException { // may not need exception
        return this.load(shared);
    }

    /** {@inheritDoc} */
    @Override
    public void load(@Nonnull Element shared, Element perNode, Object o) throws JmriConfigureXmlException { // may not need exception
        this.load(shared, o);
    }

    /**
     * Determine if this set of configured objects should be loaded after basic
     * GUI construction is completed.
     * <p>
     * Default behavior is to load when requested. Classes that should wait
     * until basic GUI is constructed should override this method and return
     * true
     *
     * @return true to defer loading
     * @see jmri.configurexml.XmlAdapter#loadDeferred()
     * @since 2.11.2
     */
    @Override
    public boolean loadDeferred() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int loadOrder() {
        return 50;
    }

    /** {@inheritDoc} */
    @Override
    public Element store(@Nonnull Object o, boolean shared) {
        if (shared) {
            return this.store(o);
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void setExceptionHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    /** {@inheritDoc} */
    @Override
    public ErrorHandler getExceptionHandler() {
        return this.errorHandler;
    }

    /**
     * Service method to handle attribute input of
     * boolean  (true/yes vs false/no) values.  Not being present
     * is not an error. Not parsing (which shouldn't happen due to
     * the XML Schema checks) invokes the default error handler.
     * @param element the element to parse.
     * @param name element attribute name.
     * @param def default value if name not present in element.
     * @return boolean value of attribute, else default if not present or error.
     */
    final public boolean getAttributeBooleanValue(@Nonnull Element element, @Nonnull String name, boolean def) {
        Attribute a;
        String val = null;
        try {
            a = element.getAttribute(name);
            if (a == null) return def;
            val = a.getValue();
            if ( val.equals("yes") || val.equals("true") ) return true;  // non-externalized strings
            if ( val.equals("no") || val.equals("false") ) return false;
            return def;
        } catch (Exception ex) {
            log.debug("caught exception", ex);
            ErrorMemo em = new ErrorMemo(this,
                                            "getAttributeBooleanValue threw exception",
                                            "element: "+element.getName(),
                                            "attribute: "+name,
                                            "value: "+val,
                                            ex);
            getExceptionHandler().handle(em);
            return def;
        }
    }

    /**
     * Service method to handle attribute input of
     * integer values.  Not being present
     * is not an error. Not parsing (which shouldn't happen due to
     * the XML Schema checks) invokes the default error handler.
     * @param element the element to parse.
     * @param name element attribute name.
     * @param def default value if name not present in element.
     * @return integer value of attribute, else default if not present or error.
     */
    final public int getAttributeIntegerValue(@Nonnull Element element, @Nonnull String name, int def) {
        Attribute a;
        String val = null;
        try {
            a = element.getAttribute(name);
            if (a == null) return def;
            val = a.getValue();
            return a.getIntValue();
        } catch (Exception ex) {
            log.debug("caught exception", ex);
            ErrorMemo em = new ErrorMemo(this,
                                            "getAttributeIntegerValue threw exception",
                                            "element: "+element.getName(),
                                            "attribute: "+name,
                                            "value: "+val,
                                            ex);
            getExceptionHandler().handle(em);
            return def;
        }
    }

    /**
     * Service method to handle attribute input of
     * double values.  Not being present
     * is not an error. Not parsing (which shouldn't happen due to
     * the XML Schema checks) invokes the default error handler.
     * @param element the element to parse.
     * @param name element attribute name.
     * @param def default value if name not present in element.
     * @return double value of attribute, else default if not present or error.
     */
    final public double getAttributeDoubleValue(@Nonnull Element element, @Nonnull String name, double def) {
        Attribute a;
        String val = null;
        try {
            a = element.getAttribute(name);
            if (a == null) return def;
            val = a.getValue();
            return a.getDoubleValue();
        } catch (Exception ex) {
            log.debug("caught exception", ex);
            ErrorMemo em = new ErrorMemo(this,
                                            "getAttributeDoubleValue threw exception",
                                            "element: "+element.getName(),
                                            "attribute: "+name,
                                            "value: "+val,
                                            ex);
            getExceptionHandler().handle(em);
            return def;
        }
    }

    /**
     * Service method to handle attribute input of
     * float values.  Not being present
     * is not an error. Not parsing (which shouldn't happen due to
     * the XML Schema checks) invokes the default error handler.
     * 
     * @param element the element to parse.
     * @param name element attribute name.
     * @param def default value if name not present in element.
     * @return float value of attribute, else default if not present or error.
     */
    final public float getAttributeFloatValue(@Nonnull Element element, @Nonnull String name, float def) {
        Attribute a;
        String val = null;
        try {
            a = element.getAttribute(name);
            if (a == null) return def;
            val = a.getValue();
            return a.getFloatValue();
        } catch (Exception ex) {
            log.debug("caught exception", ex);
            ErrorMemo em = new ErrorMemo(this,
                                            "getAttributeFloatValue threw exception",
                                            "element: "+element.getName(),
                                            "attribute: "+name,
                                            "value: "+val,
                                            ex);
            getExceptionHandler().handle(em);
            return def;
        }
    }

    /**
     * Base for support of Enum load/store to XML files.
     */
    public static abstract class EnumIO <T extends Enum<T>> { // public to be usable by adapters in other configXML packages

        /**
         * Convert an enum value to a String for storage in an XML file.
         * @param e enum value.
         * @return storage string.
         */
        @Nonnull
        abstract public String outputFromEnum(@Nonnull T e);
        
        /**
         * Convert a String value from an XML file to an enum value.
         * @param s storage string
         * @return enum value.
         */
        @Nonnull
        abstract public T inputFromString(@CheckForNull String s);

        /**
         * Convert a JDOM Attribute from an XML file to an enum value
         * @param a JDOM attribute.
         * @return enum value.
         */
        @Nonnull
        public T inputFromAttribute(@Nonnull Attribute a) {
            return inputFromString(a.getValue());
        }
    }
    
    /**
     * Support for Enum I/O to XML using the enum's ordinal numbers in String form.<p>
     * String or mapped I/LO should he preferred.<p>
     * This converts to and from ordinal numbers
     * so the order of definitions in the enum has to 
     * match up with the (former) constant values.
     * @param <T> generic Enum class.
     */
    public static class EnumIoOrdinals <T extends Enum<T>> extends EnumIO<T> { // public to be usable by adapters in other configXML packages
    
        public EnumIoOrdinals(@Nonnull Class<T> clazz) {
            this.clazz = clazz;
        }
        final Class<T> clazz;

        /** {@inheritDoc} */
        @Override
        @Nonnull
        public String outputFromEnum(@Nonnull T e) {
            int ordinal = e.ordinal();
            return Integer.toString(ordinal);
        }
        
        /** {@inheritDoc} */
        @Override
        @Nonnull
        public T inputFromString(@CheckForNull String s) {
            if (s == null) {
                log.error("from String null get {} for {}", clazz.getEnumConstants()[0].name(), clazz);
                return clazz.getEnumConstants()[0];
            }
            
            try {
                int content = Integer.parseInt(s);
                return clazz.getEnumConstants()[content];
            } catch (RuntimeException e) {
                log.error("from String {} get {} for {}", s, clazz.getEnumConstants()[0].name(), clazz, e);
                return clazz.getEnumConstants()[0];
            }
        }

    }

    /**
     * Support for Enum I/O to XML using the enum's element names.
     * @param <T> generic enum class.
     */
    public static class EnumIoNames <T extends Enum<T>> extends EnumIO<T> { // public to be usable by adapters in other configXML packages
    
        /**
         * This constructor converts to and from strings
         * using the enum element names.
         * @param clazz enum class.
         */
        public EnumIoNames(@Nonnull Class<T> clazz) {
            this.clazz = clazz;
            
            mapToEnum = new HashMap<>();
            for (T t : clazz.getEnumConstants() ) {
                mapToEnum.put(t.name(), t);
            }
            
        }

        final Class<T> clazz;
        final Map<String, T> mapToEnum;
        
        /** {@inheritDoc} */
        @Override
        @Nonnull
        public String outputFromEnum(@Nonnull T e) {
            String retval = e.name();
            log.trace("from {} make String {} for {}", e, retval, clazz);
            return retval;
        }
        
        /** {@inheritDoc} */
        @Override
        @Nonnull
        public T inputFromString(@CheckForNull String s) {
            if (s == null) {
                log.error("from String null get {} for {}", clazz.getEnumConstants()[0].name(), clazz);
                return clazz.getEnumConstants()[0];
            }
            
            T retval = mapToEnum.get(s);
            if (retval == null) {
                log.error("from String {} get {} for {}", s, clazz.getEnumConstants()[0].name(), clazz);
                return clazz.getEnumConstants()[0];
            } else {
                log.trace("from String {} get {} for {}", s, retval, clazz);
                return retval;
            }
        }
    }

    /**
     * Support for Enum I/O to XML using the enum's element names;
     * for backward compatibility, it will also accept ordinal 
     * numbers when reading.
     * @param <T> generic enum class.
     */
    public static class EnumIoNamesNumbers <T extends Enum<T>> extends EnumIoNames<T> { // public to be usable by adapters in other configXML packages
    
        /**
         * This constructor converts to and from strings
         * using the enum element names and, on read only, ordinal numbers
         * @param clazz enum class type.
         */
        public EnumIoNamesNumbers(@Nonnull Class<T> clazz) {
            super(clazz);
            
            for (T t : clazz.getEnumConstants() ) { // append to existing map
                mapToEnum.put(Integer.toString(t.ordinal()), t);
            }
            
        }
    }

    /**
     * Support for Enum I/O to XML using explicit mapping.<p>
     * This converts to and from ordinal numbers
     * so the order of definitions in the enum has to 
     * match up with the (former) constant values.
     * @param <T> generic enum class.
     */
    public static class EnumIoMapped <T extends Enum<T>> extends EnumIO<T> { // public to be usable by adapters in other configXML packages
    
        /**
         * @param clazz enum class.
         * @param mapToEnum Substitutes an explicit mapping
         * for mapping from Strings to enums; this could allow e.g.
         * accepting both name and number versions. Multiple entries
         * are OK: this can map both "1" and "Foo" to Foo for past-schema support.
         * @param mapFromEnum Substitutes an explicit mapping
         * enum entries to Strings; this determines what will
         * be written out. 
         */
        public EnumIoMapped(@Nonnull Class<T> clazz, @Nonnull Map<String, T> mapToEnum, @Nonnull Map<T, String> mapFromEnum) {
            this.clazz = clazz;
            
            this.mapToEnum = mapToEnum;
            
            this.mapFromEnum = mapFromEnum;
        }

        /**
         * @param clazz enum class.
         * @param mapToEnum Substitutes an explicit mapping
         * for mapping from Strings to enums; this could allow e.g.
         * accepting both name and number versions. Multiple entries
         * are OK: this can map both "1" and "Foo" to Foo for past-schema support.
         * The mapping from enums to Strings uses the enum names.
         */
        public EnumIoMapped(@Nonnull Class<T> clazz, @Nonnull Map<String, T> mapToEnum) {
            this.clazz = clazz;
            
            this.mapToEnum = mapToEnum;
            
            this.mapFromEnum = new HashMap<>();
            for (T t : clazz.getEnumConstants() ) {
                this.mapFromEnum.put(t, t.name());
            }
        }

        final Class<T> clazz;
        final Map<T, String> mapFromEnum;
        final Map<String, T> mapToEnum;
        
        /** {@inheritDoc} */
        @Override
        @Nonnull
        public String outputFromEnum(@Nonnull T e) {
            String retval = mapFromEnum.get(e);
            log.trace("from {} make String {} for {}", e, retval, clazz);
            return retval;
        }
        
        /** {@inheritDoc} */
        @Override
        @Nonnull
        public T inputFromString(@CheckForNull String s) {
            if (s == null) {
                log.error("from String null get {} for {}", clazz.getEnumConstants()[0].name(), clazz);
                return clazz.getEnumConstants()[0];
            }
            
            try {
                T retval = mapToEnum.get(s);
                if (retval == null) {
                    log.error("from String {} get {} for {}", s, clazz.getEnumConstants()[0].name(), clazz);
                    return clazz.getEnumConstants()[0];
                } else {
                    log.trace("from String {} get {} for {}", s, retval, clazz);
                    return retval;
                }
            } catch (RuntimeException e) {
                log.error("from String {} get {} for {}", s, clazz.getEnumConstants()[0].name(), clazz, e);
                return clazz.getEnumConstants()[0];
            }
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractXmlAdapter.class);
}

package jmri.configurexml;

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


    public static abstract class EnumIO <T extends Enum<T>> { // public to be usable by adapters in other configXML packages

        @Nonnull
        abstract public String outputFromEnum(@Nonnull T e);
        
        @Nonnull
        abstract public T inputFromString(@Nonnull String s);

        @Nonnull
        abstract public T inputFromAttribute(@Nonnull Attribute a);
    }
    
    /**
     * Support for Enum I/O to XML using the enum's ordinal numbers in String form.<p>
     * String or mapped I/LO should he preferred.<p>
     * This converts to and from ordinal numbers
     * so the order of definitions in the enum has to 
     * match up with the (former) constant values.
     */
    public static class EnumIoOrdinals <T extends Enum<T>> extends EnumIO<T> { // public to be usable by adapters in other configXML packages
    
        public EnumIoOrdinals(@Nonnull Class<T> clazz) {
            this.clazz = clazz;
        }
        Class<T> clazz;

        @Nonnull
        public String outputFromEnum(@Nonnull T e) {
            int ordinal = e.ordinal();
            return ""+ordinal;
        }
        
        @Nonnull
        public T inputFromString(@Nonnull String s) {
            int content = Integer.parseInt(s);
            return clazz.getEnumConstants()[content];
        }

        @Nonnull
        public T inputFromAttribute(@Nonnull Attribute a) {
            return inputFromString(a.getValue());
        }
    }

    /**
     * Support for Enum I/O to XML using the enum's element names.<p>
     */
    public static class EnumIoNames <T extends Enum<T>> extends EnumIO<T> { // public to be usable by adapters in other configXML packages
    
        /**
         * This constructor converts to and from strings.
         * If both Map arguments are null, a 1-to-1 mapping
         * to the enum element names is used.
         * @param mapToEnum if provided, substitutes an explicit mapping
         * for mapping from Strings to enums; this could allow e.g.
         * accepting both name and number versions.
         * @param mapFromEnum if provided, substitutes an explicit mapping
         * for mapping from enum entries to Strings; this determines what will
         * be written out.
         */
        public EnumIoNames(@Nonnull Class<T> clazz) {
            this.clazz = clazz;
            
            mapToEnum = new HashMap<String, T>();
            for (T t : clazz.getEnumConstants() ) {
                mapToEnum.put(t.toString(), t);
            }
            
        }

        Class<T> clazz;
        final Map<String, T> mapToEnum;
        
        @Nonnull
        public String outputFromEnum(@Nonnull T e) {
                String retval = e.toString();
                log.trace("from {} make String {}} for {}", e, retval, clazz);
                return retval;
        }
        
        @Nonnull
        public T inputFromString(@Nonnull String s) {
                T retval = mapToEnum.get(s);
                log.trace("from String {} get {}} for {}", s, retval, clazz);
                return retval;
        }

        @Nonnull
        public T inputFromAttribute(@Nonnull Attribute a) {
            return inputFromString(a.getValue());
        }
    }

    /**
     * Support for Enum I/O to XML using explicit mapping.<p>
     * This converts to and from ordinal numbers
     * so the order of definitions in the enum has to 
     * match up with the (former) constant values.
     */
    public static class EnumIoMapped <T extends Enum<T>> extends EnumIO<T> { // public to be usable by adapters in other configXML packages
    
        /**
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
         * @param mapToEnum Substitutes an explicit mapping
         * for mapping from Strings to enums; this could allow e.g.
         * accepting both name and number versions. Multiple entries
         * are OK: this can map both "1" and "Foo" to Foo for past-schema support.
         * The mapping from enums to Strings uses the enum names.
         */
        public EnumIoMapped(@Nonnull Class<T> clazz, @Nonnull Map<String, T> mapToEnum) {
            this.clazz = clazz;
            
            this.mapToEnum = mapToEnum;
            
            this.mapFromEnum = new HashMap<T, String>();
            for (T t : clazz.getEnumConstants() ) {
                this.mapFromEnum.put(t, t.toString());
            }
        }

        Class<T> clazz;
        final Map<T, String> mapFromEnum;
        final Map<String, T> mapToEnum;
        
        @Nonnull
        public String outputFromEnum(@Nonnull T e) {
            String retval = mapFromEnum.get(e);
            log.trace("from {} make String {}} for {}", e, retval, clazz);
            return retval;
        }
        
        @Nonnull
        public T inputFromString(@Nonnull String s) {
            T retval = mapToEnum.get(s);
            log.trace("from String {} get {}} for {}", s, retval, clazz);
            return retval;
        }

        @Nonnull
        public T inputFromAttribute(@Nonnull Attribute a) {
            return inputFromString(a.getValue());
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractXmlAdapter.class);
}

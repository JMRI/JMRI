package jmri.configurexml;

import javax.annotation.Nonnull;

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

    /**
     * Support for Enum I/O via XML
     */
    public class EnumIO <T extends Enum<T>> {  // public to be usable in other packages
    
        // this implementation just uses ordinal for now

        public EnumIO(Class<T> clazz) {
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
}

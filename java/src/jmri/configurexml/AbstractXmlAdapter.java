package jmri.configurexml;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.jmrit.display.layoutEditor.LayoutSlip;

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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean load(Element e) throws JmriConfigureXmlException {
        throw new UnsupportedOperationException("One of the other load methods must be implemented.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean load(@Nonnull Element shared, Element perNode) throws JmriConfigureXmlException { // may not need exception
        return this.load(shared);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public int loadOrder() {
        return 50;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Element store(@Nonnull Object o, boolean shared) {
        if (shared) {
            return this.store(o);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExceptionHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ErrorHandler getExceptionHandler() {
        return this.errorHandler;
    }

    /**
     * Support for Enum I/O via XML
     */
    public static class EnumIO<T extends Enum<T>> {  // public to be usable in other packages

        // This implementation just uses ordinal for now,
        // so the order of definitions in the enum has to
        // match up with the (former) constant values.
        // Later, we can add an explicit mapping here (rather
        // than having to define it in the enum itself)
        public EnumIO(Class<T> clazz) {
            this.clazz = clazz;
        }
        Class<T> clazz;

        @Nonnull
        public String outputFromEnum(@Nonnull T e) {
            return e.name();
        }

        @CheckForNull
        public T inputFromString(@CheckForNull String string) {
            T result = null;
            if ((string != null) && !string.isEmpty()) {
                try {
                    //first see if it matches enum name (exactly)
                    result = Enum.valueOf(clazz, string);
                } catch (IllegalArgumentException e) {    //(nope)
                    try {
                        //try to parse it as an integer
                        int ordinal = Integer.parseInt(string);
                        result = clazz.getEnumConstants()[ordinal];
                    } catch (NumberFormatException e1) {  //(nope)
                        //failure
                    }
                }
            }
            return result;
        }

        @CheckForNull
        public T inputFromAttribute(@CheckForNull Attribute a) {
            return ((a == null) ? null : inputFromString(a.getValue()));
        }
    }
}

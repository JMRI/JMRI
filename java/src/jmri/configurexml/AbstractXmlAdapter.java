package jmri.configurexml;

import org.jdom2.Element;

/**
 * Abstract class to provide basic error handling for XmlAdapter
 *
 * @author Bob Jacobsen Copyright (c) 2009
 * @see XmlAdapter
 */
public abstract class AbstractXmlAdapter implements XmlAdapter {

    private ErrorHandler errorHandler = XmlAdapter.getDefaultExceptionHandler();

    @Override
    public void creationErrorEncountered(
            String description,
            String systemName,
            String userName,
            Exception exception) throws JmriConfigureXmlException {
        this.handleException(description, null, systemName, userName, exception);
    }

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

    @Override
    public boolean load(Element e) throws JmriConfigureXmlException {
        throw new UnsupportedOperationException("One of the other load methods must be implemented.");
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException { // may not need exception
        return this.load(shared);
    }

    @Override
    public void load(Element shared, Element perNode, Object o) throws JmriConfigureXmlException { // may not need exception
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
     * Get the order to load items from XML files in.
     *
     * @return the order
     */
    @Override
    public int loadOrder() {
        return 50;
    }

    @Override
    public Element store(Object o, boolean shared) {
        if (shared) {
            return this.store(o);
        }
        return null;
    }

    @Override
    public void setExceptionHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public ErrorHandler getExceptionHandler() {
        return this.errorHandler;
    }
}

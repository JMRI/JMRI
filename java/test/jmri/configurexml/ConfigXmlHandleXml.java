package jmri.configurexml;

import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;
import org.jdom2.Element;

/**
 * Testing dummy class used in jmri.configurexml.ConfigXmlManagerTest
 *
 * Needs to be in the jmri package to drive the auto-class resolution methods
 *
 * @author Bob Jacobsen Copyright 2017
 */
class ConfigXmlHandleXml implements XmlAdapter {

    @Override
    public boolean load(Element e) {
        return true;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        return true;
    }

    @Override
    public boolean loadDeferred() {
        return false;
    }

    @Override
    public void load(Element e, Object o) {
        return;
    }

    @Override
    public void load(Element shared, Element perNode, Object o) {
        return;
    }

    @Override
    public Element store(Object o) {
        return null;
    }

    @Override
    public Element store(Object o, boolean shared) {
        throw new IllegalArgumentException("for testing");
    }

    @Override
    public int loadOrder() {
        return 3;
    }

    @Override
    public void handleException(
            @Nonnull String description,
            @CheckForNull String operation,
            @CheckForNull String systemName,
            @CheckForNull String userName,
            @CheckForNull Exception exception) throws JmriConfigureXmlException {
    }

    @Override
    public void setExceptionHandler(ErrorHandler errorHandler) {
        // empty; does nothing
    }

    @Override
    public ErrorHandler getExceptionHandler() {
        return XmlAdapter.getDefaultExceptionHandler();
    }
}

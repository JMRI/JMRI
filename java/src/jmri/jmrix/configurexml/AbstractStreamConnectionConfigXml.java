package jmri.jmrix.configurexml;

import jmri.jmrix.AbstractStreamPortController;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base (and partial implementation) for classes persisting the status
 * of (non-serial) Stream Port adapters.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @author George Warner Copyright: Copyright (c) 2017
 */
abstract public class AbstractStreamConnectionConfigXml extends AbstractConnectionConfigXml {

    /**
     * constructor
     */
    public AbstractStreamConnectionConfigXml() {
    }

    protected AbstractStreamPortController adapter;

    /**
     * set the stream port adapter
     *
     * @param streamPortAdapter the stream port adapter to set
     */
    protected void setAdapter(AbstractStreamPortController streamPortAdapter) {
        log.debug("setAdapter({})", streamPortAdapter);
        adapter = streamPortAdapter;
    }

    /**
     * get the stream port adapter
     *
     * @return the stream port adapter
     */
    protected AbstractStreamPortController getAdapter() {
        log.debug("getAdapter()");
        return adapter;
    }

    /**
     * get the stream adapter
     *
     * @return the stream port adapter
     */
    public AbstractStreamPortController getStreamAdapter() {
        log.debug("getStreamAdapter()");
        return adapter;
    }

    /**
     * get instance
     *
     * @param object to get the instance of
     */
    protected abstract void getInstance(Object object);

    /**
     * {@inheritDoc}
     */
    @Override
    public Element store(Object object) {
        log.debug("store({})", object);
        getInstance(object);
        Element e = new Element("connection");

        if (adapter == null) {
            log.warn("No adapter found while saving stream port configuration {}", object.toString());
            return null;
        }

        // many of the following are required by the DTD; failing to include
        // them makes the XML file unreadable, but at least the next
        // invocation of the program can then continue.
        storeCommon(e, adapter);

        e.setAttribute("class", this.getClass().getName());

        extendElement(e);

        return e;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void extendElement(Element e) {
        log.debug("extendElement({})", e);
    }

    /**
     * load from xml elements
     *
     * @param shared  element
     * @param perNode element
     * @return boolean true if successful
     */
    @Override
    public boolean load(Element shared, Element perNode) {
        log.debug("load({}, {})", shared, perNode);
        boolean result = true;  // assume success (optimist!)

        getInstance();

        loadCommon(shared, perNode, adapter);

        // register, so can be picked up next time
        register();

        // once all the configure processing has happened, do any
        // extra config
        unpackElement(shared, perNode);
        return result;
    }

    /**
     * Update static data from XML file
     *
     * @param element Top level Element to unpack.
     */
    @Override
    public void load(Element element, Object o) {
        log.debug("load({}, {})", element, o);

        getInstance(o);

        loadCommon(element, element, adapter);

        // register, so can be picked up next time
        register();

        // once all the configure processing has happened, do any
        // extra config
        unpackElement(element, element);
    }

    // initialize logging
    private static final Logger log
            = LoggerFactory.getLogger(AbstractStreamConnectionConfigXml.class);
}

package jmri.jmrix.anyma_dmx.configurexml;

import jmri.jmrix.anyma_dmx.AnymaDMX_Adapter;
import jmri.jmrix.anyma_dmx.AnymaDMX_ConnectionConfig;
import jmri.jmrix.configurexml.AbstractConnectionConfigXml;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistence of layout connections by persisting the
 * AnymaDMX_Adapter. Note this is named as the XML version of a
 * AnymaDMX_ConnectionConfig object, but it's actually persisting the
 * AnymaDMX_Adapter.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Paul Bender Copyright: Copyright (c) 2015
 * @author George Warner Copyright (C) 2017
 * @since       4.9.6
 */
public class AnymaDMX_ConnectionConfigXml extends AbstractConnectionConfigXml {

    private AnymaDMX_Adapter adapter = null;

    public AnymaDMX_ConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        log.info("*getInstance without Parameter called");
        if (adapter == null) {
            adapter = new AnymaDMX_Adapter();
            if (adapter.getDMX_Controller() == null) {
                //try {
                //    this.creationErrorEncountered("Not running on Anyma DMX.", adapter.getSystemPrefix(), adapter.getUserName(), null);
                //} catch (JmriConfigureXmlException ex) {
                //    log.error("Not running on Anyma DMX.", ex);
                //}
            }
        }
    }

    protected void getInstance(Object object) {
        log.info("*getInstance with Parameter called");
        adapter = ((AnymaDMX_ConnectionConfig) object).getAdapter();
    }

    @Override
    protected void register() {
        this.register(new AnymaDMX_ConnectionConfig(adapter));
    }

    /**
     * Default implementation for storing the static contents of the serial port
     * implementation
     *
     * @param o Object to store, of type PositionableLabel
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        getInstance(o);
        Element e = new Element("connection");
        storeCommon(e, adapter);
        e.setAttribute("class", this.getClass().getName());
        return e;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        getInstance();
        loadCommon(shared, perNode, adapter);

        // register, so can be picked up next time
        register();

        adapter.configure();
        return true;
    }

    private final static Logger log = LoggerFactory.getLogger(AnymaDMX_ConnectionConfigXml.class);

}

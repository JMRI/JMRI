package jmri.jmrix.lenz.liusbserver.configurexml;

import jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXml;
import jmri.jmrix.lenz.liusbserver.ConnectionConfig;
import jmri.jmrix.lenz.liusbserver.LIUSBServerAdapter;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistance of layout connections by persistening the LIUSB Server
 * (and connections). Note this is named as the XML version of a
 * ConnectionConfig object, but it's actually persisting the LIUSB Server.
 * <p>
 * NOTE: The LIUSB Server currently has no options, so this class does not store
 * any.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Paul Bender Copyright (C) 2009
 */
public class ConnectionConfigXml extends AbstractNetworkConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    /**
     * An LIUSBServer connection needs no extra information, so we reimplement
     * the superclass method to just write the necessary parts.
     *
     * @return Formatted element containing no attributes except the class name
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
        boolean result = true;
        getInstance();

        loadCommon(shared,perNode,adapter);

        // register, so can be picked up
        register();
     
        if(adapter.getDisabled()){
            unpackElement(shared,perNode);
            return result;
        }
        return result;
    }

    @Override
    protected void getInstance() {
        if (adapter == null) {
            adapter = new LIUSBServerAdapter();
            try { 
                adapter.connect();
                adapter.configure();
            } catch(Exception e){
                log.error("Error connecting or configuring port.");
            }
        }
    }

    @Override
    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig) object).getAdapter();
    }

    @Override
    protected void register() {
        this.register(new ConnectionConfig(adapter));
    }

    private final static Logger log = LoggerFactory.getLogger(ConnectionConfigXml.class);

}

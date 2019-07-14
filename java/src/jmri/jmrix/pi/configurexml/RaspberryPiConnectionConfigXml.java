package jmri.jmrix.pi.configurexml;

import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrix.configurexml.AbstractConnectionConfigXml;
import jmri.jmrix.pi.RaspberryPiAdapter;
import jmri.jmrix.pi.RaspberryPiConnectionConfig;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistence of layout connections by persisting the
 * RaspberryPiAdapter. Note this is named as the XML version of a
 * RaspberryPiConnectionConfig object, but it's actually persisting the
 * RaspberryPiAdapter.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Paul Bender Copyright: Copyright (c) 2015
 */
public class RaspberryPiConnectionConfigXml extends AbstractConnectionConfigXml {

    private RaspberryPiAdapter adapter = null;

    public RaspberryPiConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        log.debug("getInstance without Parameter called");
        if (adapter == null) {
            adapter = new RaspberryPiAdapter();
            if (adapter.getGPIOController() == null) {
                handleException("Not running on Raspberry PI.", null, adapter.getSystemPrefix(), adapter.getUserName(), null);
            }
        }
    }

    protected void getInstance(Object object) {
        log.debug("getInstance with Parameter called");
        adapter = ((RaspberryPiConnectionConfig) object).getAdapter();
    }

    @Override
    protected void register() {
        this.register(new RaspberryPiConnectionConfig(adapter));
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

    private final static Logger log = LoggerFactory.getLogger(RaspberryPiConnectionConfigXml.class);

}

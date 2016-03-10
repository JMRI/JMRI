package jmri.jmrix.pi.configurexml;

import jmri.jmrix.configurexml.AbstractConnectionConfigXml;
import jmri.jmrix.pi.ConnectionConfig;
import jmri.jmrix.pi.RaspberryPiAdapter;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistance of layout connections by persistening
 * the RaspberryPiAdapter. Note this is named as the XML version of
 * a ConnectionConfig object, but it's actually persisting the 
 * RaspberryPiAdapter.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write,
 * as that class is the one actually registered. Reads are brought
 * here directly via the class attribute in the XML.
 *
 * @author Paul Bender Copyright: Copyright (c) 2015
 * @version $Revision$
 */
public class ConnectionConfigXml extends AbstractConnectionConfigXml {

    private RaspberryPiAdapter adapter = null;

    public ConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        log.debug("getInstance without Parameter called");
        if(adapter == null) adapter=new RaspberryPiAdapter();
    }

    protected void getInstance(Object object) {
        log.debug("getInstance with Parameter called");
        adapter=(RaspberryPiAdapter)((ConnectionConfig) object).getAdapter();
    }

    @Override
    protected void register() {
        this.register(new ConnectionConfig(adapter));
    }

    /**
     * Default implementation for storing the static contents of the serial port
     * implementation
     *
     * @param o Object to store, of type PositionableLabel
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o){
       getInstance(o);
       Element e = new Element("connection");
       storeCommon(e,adapter);
       e.setAttribute("class", this.getClass().getName());
       return e;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws Exception {
       getInstance();
       loadCommon(shared, perNode, adapter);

       // register, so can be picked up next time
       register();

       adapter.configure();
       return true;
    }

    private final static Logger log = LoggerFactory.getLogger(ConnectionConfigXml.class.getName());

}

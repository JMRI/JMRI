package jmri.jmrix.loconet.hexfile.configurexml;

import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractConnectionConfigXml;
import jmri.jmrix.loconet.hexfile.ConnectionConfig;
import jmri.jmrix.loconet.hexfile.LnHexFilePort;

import org.jdom.Element;

/**
 * Handle XML persistance of layout connections by persistening
 * the HexFIle LocoNet emuilator (and connections). Note this is
 * named as the XML version of a ConnectionConfig object,
 * but it's actually persisting the HexFile info.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write,
 * as that class is the one actually registered. Reads are brought
 * here directly via the class attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 1.4 $
 */
public class ConnectionConfigXml extends AbstractConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    /**
     * A HexFile connection needs no extra information, so
     * we reimplement the superclass method to just write the
     * necessary parts.
     * @param o
     * @return Formatted element containing no attributes except the class name
     */
    public Element store(Object o) {
        getInstance();

        Element e = new Element("connection");

        e.addAttribute("class", this.getClass().getName());

        return e;
    }

    /**
     * Update static data from XML file
     * @param e Top level Element to unpack.
      */
    public void load(Element e) {
        // hex file has no options in the XML

        // start the "connection"
        jmri.jmrix.loconet.hexfile.HexFileFrame f
                = new jmri.jmrix.loconet.hexfile.HexFileFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            //log.error("starting HexFileFrame exception: "+ex.toString());
        }
        f.pack();
        f.show();

        // register, so can be picked up
        getInstance();
        register();
    }


    protected void getInstance() {
        adapter = LnHexFilePort.instance();
    }

    protected void register() {
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig(adapter));
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ConnectionConfigXml.class.getName());

}
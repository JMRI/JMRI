package jmri.jmrix.loconet.hexfile.configurexml;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.loconet.hexfile.ConnectionConfig;
import jmri.jmrix.loconet.hexfile.LnHexFilePort;
import org.jdom2.Element;

/**
 * Handle XML persistance of layout connections by persistening the HexFIle
 * LocoNet emuilator (and connections). Note this is named as the XML version of
 * a ConnectionConfig object, but it's actually persisting the HexFile info.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision$
 */
public class ConnectionConfigXml extends AbstractSerialConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    /**
     * A HexFile connection needs no extra information, so we reimplement the
     * superclass method to just write the necessary parts.
     *
     * @param o
     * @return Formatted element containing no attributes except the class name
     */
    public Element store(Object o) {
        getInstance(o);

        Element e = new Element("connection");
        if (adapter.getSystemConnectionMemo() != null) {
            e.setAttribute("userName", adapter.getSystemConnectionMemo().getUserName());
            e.setAttribute("systemPrefix", adapter.getSystemConnectionMemo().getSystemPrefix());
        }
        if (adapter.getManufacturer() != null) {
            e.setAttribute("manufacturer", adapter.getManufacturer());
        }
        saveOptions(e, adapter);

        if (adapter.getDisabled()) {
            e.setAttribute("disabled", "yes");
        } else {
            e.setAttribute("disabled", "no");
        }

        e.setAttribute("class", this.getClass().getName());

        return e;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        jmri.jmrix.loconet.hexfile.HexFileFrame f = null;
        jmri.jmrix.loconet.hexfile.HexFileServer hfs = null;

        getInstance();
        // hex file has no options in the XML

        GraphicsEnvironment.getLocalGraphicsEnvironment();
        // create GUI, unless running in headless mode
        if (!GraphicsEnvironment.isHeadless()) {
            f = new jmri.jmrix.loconet.hexfile.HexFileFrame();
            f.setAdapter((LnHexFilePort) adapter);
            try {
                f.initComponents();
            } catch (Exception ex) {
                //log.error("starting HexFileFrame exception: "+ex.toString());
            }
            f.pack();
            f.setVisible(true);
        } else {  // create and configure the headless server 
            hfs = new jmri.jmrix.loconet.hexfile.HexFileServer();
            hfs.setAdapter((LnHexFilePort) adapter);
        }

        if (shared.getAttribute("option1") != null) {
            String option1Setting = shared.getAttribute("option1").getValue();
            adapter.configureOption1(option1Setting);
        }
        if (shared.getAttribute("option2") != null) {
            String option2Setting = shared.getAttribute("option2").getValue();
            adapter.configureOption2(option2Setting);
        }
        if (shared.getAttribute("option3") != null) {
            String option3Setting = shared.getAttribute("option3").getValue();
            adapter.configureOption3(option3Setting);
        }
        if (shared.getAttribute("option4") != null) {
            String option4Setting = shared.getAttribute("option4").getValue();
            adapter.configureOption4(option4Setting);
        }
        loadOptions(shared.getChild("options"), adapter);
        String manufacturer;
        try {
            manufacturer = shared.getAttribute("manufacturer").getValue();
            adapter.setManufacturer(manufacturer);
        } catch (NullPointerException ex) { //Considered normal if not present

        }
        if (adapter.getSystemConnectionMemo() != null) {
            if (shared.getAttribute("userName") != null) {
                adapter.getSystemConnectionMemo().setUserName(shared.getAttribute("userName").getValue());
            }

            if (shared.getAttribute("systemPrefix") != null) {
                adapter.getSystemConnectionMemo().setSystemPrefix(shared.getAttribute("systemPrefix").getValue());
            }
        }
        if (shared.getAttribute("disabled") != null) {
            String yesno = shared.getAttribute("disabled").getValue();
            if ((yesno != null) && (!yesno.equals(""))) {
                if (yesno.equals("no")) {
                    adapter.setDisabled(false);
                } else if (yesno.equals("yes")) {
                    adapter.setDisabled(true);
                }
            }
        }

        // register, so can be picked up
        register();
        if (adapter.getDisabled()) {
            if (!GraphicsEnvironment.isHeadless() && f != null) {
                f.setVisible(false);
            }
            return true;
        }
        if (!GraphicsEnvironment.isHeadless() && f != null) {
            f.configure();
        } else if (hfs != null) {
            hfs.configure();
        }
        return true;
    }

    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig) object).getAdapter();
    }

    protected void getInstance() {
        adapter = new LnHexFilePort();
    }

    @Override
    protected void register() {
        this.register(new ConnectionConfig(adapter));
    }

}

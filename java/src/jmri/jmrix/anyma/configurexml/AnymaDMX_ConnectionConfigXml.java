package jmri.jmrix.anyma.configurexml;

import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.UsbPortAdapter;
import jmri.jmrix.anyma.AnymaDMX_ConnectionConfig;
import jmri.jmrix.anyma.AnymaDMX_UsbPortAdapter;
import jmri.jmrix.configurexml.AbstractUsbConnectionConfigXml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistence of layout connections by persisting the
 * AnymaDMX_UsbPortAdapter. Note this is named as the XML version of a
 * AnymaDMX_ConnectionConfig object, but it's actually persisting the
 * AnymaDMX_UsbPortAdapter.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author George Warner Copyright (c) 2017-2018
 * @since 4.9.6
 */
public class AnymaDMX_ConnectionConfigXml extends AbstractUsbConnectionConfigXml {

    /**
     * constructor
     */
    public AnymaDMX_ConnectionConfigXml() {
        super();
        log.debug("* constructor()");
    }

    /**
     * get instance
     */
    @Override
    protected void getInstance() {
        log.debug("* getInstance()");
        if (getAdapter() == null) {
            setAdapter(new AnymaDMX_UsbPortAdapter());
            //if (adapter.getAnymaDMX_Controller() == null) {
            //    try {
            //        this.creationErrorEncountered("Not running on Anyma DMX.", adapter.getSystemPrefix(), adapter.getUserName(), null);
            //    } catch (JmriConfigureXmlException ex) {
            //        log.error("Not running on Anyma DMX.", ex);
            //    }
            //}
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void getInstance(Object object) {
        setAdapter((UsbPortAdapter) ((ConnectionConfig) object).getAdapter());
    }

    /**
     * register
     */
    @Override
    protected void register() {
        if (adapter instanceof AnymaDMX_UsbPortAdapter) {
            this.register(new AnymaDMX_ConnectionConfig(
                    (AnymaDMX_UsbPortAdapter) adapter));
        }
    }

    private final static Logger log
            = LoggerFactory.getLogger(AnymaDMX_ConnectionConfigXml.class);
}

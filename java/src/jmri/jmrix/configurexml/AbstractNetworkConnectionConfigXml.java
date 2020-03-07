package jmri.jmrix.configurexml;

import jmri.jmrix.NetworkPortAdapter;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base (and partial implementation) for classes persisting the status
 * of Network port adapters.
 *
 * @author Bob Jacobsen, Copyright (c) 2003
 */
abstract public class AbstractNetworkConnectionConfigXml extends AbstractConnectionConfigXml {

    public AbstractNetworkConnectionConfigXml() {
    }

    protected NetworkPortAdapter adapter;

    protected void getInstance(Object object) {
        getInstance(); // over-ridden during migration
    }

    /**
     * Default implementation for storing the static contents of the Network
     * port implementation.
     *
     * @param o Object to store, of type PositionableLabel
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        getInstance(o);

        Element e = new Element("connection");
        // many of the following are required by the DTD; failing to include
        // them makes the XML file unreadable, but at least the next
        // invocation of the program can then continue.

        storeCommon(e, adapter);

        if (adapter.getMdnsConfigure() == true) {
            // if we are using mDNS for configuration, only save
            // the hostname if it was specified.
            if (adapter.getHostName() != null && !adapter.getHostName().equals("")) {
                e.setAttribute("address", adapter.getHostName());
            }

            e.setAttribute("mdnsConfigure", "true");
            if (adapter.getAdvertisementName() != null) {
                e.setAttribute("advertisementName", adapter.getAdvertisementName());
            }
            if (adapter.getServiceType() != null) {
                e.setAttribute("serviceType", adapter.getServiceType());
            }

        } else {
            e.setAttribute("mdnsConfigure", "false");

            // require a value for the address if we are not using mDNS.
            if (adapter.getHostName() != null) {
                e.setAttribute("address", adapter.getHostName());
            } else {
                e.setAttribute("address", Bundle.getMessage("noneSelected"));
            }

            // write the port only if we are not using automatic configuration.
            if (adapter.getPort() != 0) {
                e.setAttribute("port", "" + adapter.getPort());
            } else {
                e.setAttribute("port", Bundle.getMessage("noneSelected"));
            }
        }

        e.setAttribute("class", this.getClass().getName());

        extendElement(e);

        return e;
    }

    /**
     * Customizable method if you need to add anything more.
     *
     * @param e Element being created, update as needed
     */
    @Override
    protected void extendElement(Element e) {
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        boolean result = true;
        getInstance();

        boolean mdnsConfig = false;
        try {
            mdnsConfig = (shared.getAttribute("mdnsConfigure").getValue().equals("true"));
        } catch (NullPointerException ex) {  // considered normal if the attributes are not present
        }
        adapter.setMdnsConfigure(mdnsConfig);

        if (mdnsConfig) {

            // configure host name
            try {
                String hostName = shared.getAttribute("address").getValue();
                // the hostname is optional when mDNS is being used.
                adapter.setHostName(hostName);
            } catch (NullPointerException ex) {  // considered normal if the attributes are not present
            }

            // configure the Service Type
            try {
                String serviceType = shared.getAttribute("serviceType").getValue();
                // the Service Type is optional when mDNS is being used.
                adapter.setServiceType(serviceType);
            } catch (NullPointerException ex) {  // considered normal if the attributes are not present
            }

            // configure the advertisement name
            try {
                String advertisementName = shared.getAttribute("advertisementName").getValue();
                // the Advertisement Name is optional when mDNS is being used.
                adapter.setAdvertisementName(advertisementName);
            } catch (NullPointerException ex) {  // considered normal if the attributes are not present
            }

            // get the host IP and port number
            // via mdns
            adapter.autoConfigure();

        } else {
            // get the host name and port number via parameters.

            // configure host name
            String hostName = null;
            try {
                hostName = shared.getAttribute("address").getValue();
            } catch (NullPointerException ex) {  // considered normal if the attributes are not present
            }
            adapter.setHostName(hostName);

            try {
                int port = shared.getAttribute("port").getIntValue();
                adapter.setPort(port);
            } catch (org.jdom2.DataConversionException ex) {
                log.warn("Could not parse port attribute: {}", shared.getAttribute("port"));
            } catch (NullPointerException ex) {  // considered normal if the attributes are not present
            }
        }

        loadCommon(shared, perNode, adapter);
        // register, so can be picked up next time
        register();

        if (adapter.getDisabled()) {
            unpackElement(shared, perNode);
            return result;
        }
        try {
            adapter.connect();
        } catch (Exception ex) {
            handleException(ex.getMessage(), "opening connection", null, null, ex);
            return false;
        }

        // if successful so far, go ahead and configure
        adapter.configure();

        // once all the configure processing has happened, do any
        // extra config
        unpackElement(shared, perNode);
        return result;
    }

    /**
     * Update static data from XML file.
     *
     * @param element Top level Element to unpack.
     */
    @Override
    public void load(Element element, Object o) {
        log.error("method with two args invoked");
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(AbstractNetworkConnectionConfigXml.class);

}

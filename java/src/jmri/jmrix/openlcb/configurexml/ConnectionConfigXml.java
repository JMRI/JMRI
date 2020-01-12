package jmri.jmrix.openlcb.configurexml;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import jmri.jmrix.PortAdapter;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.ConfigurationManager;

/**
 * This class encapsulates common code for reading and writing per-connection information from/to
 * the XML of the connection profile. It is intended to be called by all conforming Adapter
 * implementations that are the possible choices for an OpenLCB connection.
 * <p>
 * (C) Balazs Racz, 2018.
 */

public class ConnectionConfigXml {
    /**
     * Checks if we are loading an OpenLCB protocol adapter. If no, returns without doing
     * anything. If yes, loads the protocol settings from the XML elements.
     *
     * Must be called after loadOptions is done.
     *
     * @param shared  The &lt;connection&gt; element in the shared profile configuration.
     * @param perNode The &lt;connection&gt; element in the per-node profile configuration.
     * @param adapter The adapter that's in the process of initializing this connection.
     */
    public static void maybeLoadOlcbProfileSettings(Element shared, Element perNode, PortAdapter
            adapter) {
        CanSystemConnectionMemo sc = isOpenLCBProtocol(adapter);
        if (sc == null) return;
        loadSettingsElement(sc, shared);
        loadSettingsElement(sc, perNode);
    }

    private static void loadSettingsElement(CanSystemConnectionMemo sc, Element xmlNode) {
        for (Element n : xmlNode.getChildren("node")) {
            String protocol = n.getAttributeValue("name");
            for (Element p : n.getChildren("parameter")) {
                String optionName = p.getAttributeValue("name");
                String value = p.getTextTrim();
                sc.setProtocolOption(protocol, optionName, value);
            }
        }
    }

    /**
     * Checks if we are saving an OpenLCB protocol connection. If no, does nothing. If yes, saves
     * the protocol options from the SystemConnectionMemo into the XML element.
     * This function needs to be called from the extendElement(Element e) override in a
     * ConnectionConfigXml of an adaptor.
     *
     * @param element &lt;connection&gt; XML node
     * @param adapter Adaptor object that we are trying to save; used to access the system
     *                connection memo.
     */
    public static void maybeSaveOlcbProfileSettings(Element element, PortAdapter adapter) {
        CanSystemConnectionMemo sc = isOpenLCBProtocol(adapter);
        if (sc == null) return;

        for (String protocol : sc.getProtocolsWithOptions()) {
            Element n = new Element("node");
            n.setAttribute("name", protocol);
            element.addContent(n);

            Map<String, String> params = sc.getProtocolAllOptions(protocol);
            for (Map.Entry<String, String> entry : params.entrySet()) {
                Element p = new Element("parameter");
                p.setAttribute("name", entry.getKey());
                p.addContent(entry.getValue());
                n.addContent(p);
            }
        }
    }

    /**
     * Tests whether a CAN adapter is set to openLCB protocol or not.
     *
     * @param adapter CAN adapter 9may be serial, loopback or network).
     * @return null for non-OpenLCB connections; for OpenLCB the connection-associated the
     * CanSystemConnectionMemo.
     */
    public static CanSystemConnectionMemo isOpenLCBProtocol(PortAdapter adapter) {
        CanSystemConnectionMemo sc = (CanSystemConnectionMemo) adapter.getSystemConnectionMemo();
        if (sc == null) {
            log.error("Adapter is expected to have a CanSystemConnectionMemo to be used for " +
                    "OpenLCB protocol.");
            return null;
        }
        if (!ConfigurationManager.OPENLCB.equals(adapter.getOptionState("Protocol"))) {
            log.debug("Skipping OpenLCB protocol properties action, because protocol is not " +
                    "OpenLCB, but {}", adapter.getOptionState("Protocol"));
            return null;
        }
        return sc;
    }

    private static final Logger log = LoggerFactory.getLogger(ConnectionConfigXml.class);
}

package jmri.jmrix.bidib.serialdriver.configurexml;

//import com.pi4j.io.serial.Serial;
import jmri.jmrix.PortAdapter;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.bidib.serialdriver.ConnectionConfig;
import jmri.jmrix.bidib.serialdriver.SerialDriverAdapter;
import org.bidib.jbidibc.messages.utils.ByteUtils;
import org.jdom2.Element;

/**
 * Handle XML persistance of layout connections by persistening the
 * SerialDriverAdapter (and connections). Note this is named as the XML version
 * of a ConnectionConfig object, but it's actually persisting the
 * SerialDriverAdapter.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @author Eckart Meyer Copyright (C) 2019
 */
public class ConnectionConfigXml extends AbstractSerialConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        adapter = new SerialDriverAdapter();
    }

    @Override
    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig) object).getAdapter();
    }

    @Override
    protected void register() {
        this.register(new ConnectionConfig(adapter));
    }

    @Override
    protected void loadCommon(Element shared, Element perNode, PortAdapter adapter) {
        unpackElement1(shared, perNode); //we must have those attributes before opening the line (open is in load after loadCommon but before unpackElement)
        super.loadCommon(shared, perNode, adapter);
    }
    /**
     * Write out the SerialNode objects too
     *
     * @param e Element being extended
     */
    @Override
    protected void extendElement(Element e) {
        SerialDriverAdapter a = (SerialDriverAdapter)adapter;
        if (a.getUseAutoScan()) {
            e.setAttribute("autoScan", "true");
        }
        else {
            e.setAttribute("autoScan", "false");
        }
        if (a.getRootNodeUid() != null) {
            e.setAttribute("rootNodeUID", ByteUtils.formatHexUniqueId(a.getRootNodeUid()));
        }
        if (!a.getPortNameFilter().isEmpty()) {
            e.setAttribute("portNameFilter", a.getPortNameFilter());
        }
    }
    
    /**
     * Same as unpackElement() in super class, but that one is called from load() too late.
     * Get the additional parameters from XML.
     * 
     * @see #unpackElement
     * @param shared selected Element
     * @param perNode from super class
     */
    protected void unpackElement1(Element shared, Element perNode) {
        SerialDriverAdapter a = (SerialDriverAdapter)adapter;
        if (shared.getAttribute("autoScan") != null) {
            a.setUseAutoScan( shared.getAttributeValue("autoScan").equals("true"));
        }
        if (shared.getAttribute("rootNodeUID") != null) {
            a.setRootNodeUid(ByteUtils.parseHexUniqueId(shared.getAttributeValue("rootNodeUID")));
        }
        if (shared.getAttribute("portNameFilter") != null) {
            a.setPortNameFilter(shared.getAttributeValue("portNameFilter"));
        }
    }
}

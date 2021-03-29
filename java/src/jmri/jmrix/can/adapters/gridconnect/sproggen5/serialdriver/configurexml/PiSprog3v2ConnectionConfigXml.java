package jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.configurexml;

import org.jdom2.Element;

import jmri.jmrix.PortAdapter;
import jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.*;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;

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
 * @author Andrew Crosland Copyright (C) 2008, 2019, 2021
 */
public class PiSprog3v2ConnectionConfigXml extends AbstractSerialConnectionConfigXml {

    public PiSprog3v2ConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        adapter = new PiSprog3v2SerialDriverAdapter();
    }

    @Override
    protected void getInstance(Object object) {
        adapter = ((PiSprog3v2ConnectionConfig) object).getAdapter();
    }

    @Override
    protected void register() {
        this.register(new PiSprog3v2ConnectionConfig(adapter));
    }

    @Override
    protected void loadOptions(Element shared, Element perNode, PortAdapter adapter) {
        super.loadOptions(shared, perNode, adapter);

        jmri.jmrix.openlcb.configurexml.ConnectionConfigXml.maybeLoadOlcbProfileSettings(
                shared.getParentElement(), perNode.getParentElement(), adapter);
    }

    @Override
    protected void extendElement(Element e) {
        jmri.jmrix.openlcb.configurexml.ConnectionConfigXml.maybeSaveOlcbProfileSettings(
                e, adapter);
    }
}

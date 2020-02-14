package jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.configurexml;

import org.jdom2.Element;

import jmri.jmrix.PortAdapter;
import jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.PiSprog3ConnectionConfig;
import jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.Sprog3PlusSerialDriverAdapter;
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
 * @author Andrew Crosland 2008
 * @author Andrew Crosland 2019
 */
public class PiSprog3ConnectionConfigXml extends AbstractSerialConnectionConfigXml {

    public PiSprog3ConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        adapter = new Sprog3PlusSerialDriverAdapter();
    }

    @Override
    protected void getInstance(Object object) {
        adapter = ((PiSprog3ConnectionConfig) object).getAdapter();
    }

    @Override
    protected void register() {
        this.register(new PiSprog3ConnectionConfig(adapter));
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

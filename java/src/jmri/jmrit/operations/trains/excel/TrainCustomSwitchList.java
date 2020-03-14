package jmri.jmrit.operations.trains.excel;

import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import org.jdom2.Element;

public class TrainCustomSwitchList extends TrainCustomCommon implements InstanceManagerAutoDefault {

    private String directoryName = "csvSwitchLists"; // NOI18N

    @Override
    public String getDirectoryName() {
        return directoryName;
    }

    @Override
    public void setDirectoryName(String name) {
        directoryName = name;
    }

    @Override
    public void load(Element options) {
        Element mc = options.getChild(Xml.SWITCHLIST_CREATOR);
        super.load(mc);
    }

    @Override
    public void store(Element options) {
        Element mc = new Element(Xml.SWITCHLIST_CREATOR);
        super.store(mc);
        options.addContent(mc);
    }
}

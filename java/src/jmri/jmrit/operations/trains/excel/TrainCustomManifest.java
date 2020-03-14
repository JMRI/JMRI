package jmri.jmrit.operations.trains.excel;

import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import org.jdom2.Element;

public class TrainCustomManifest extends TrainCustomCommon implements InstanceManagerAutoDefault {

    private String directoryName = "csvManifests"; // NOI18N

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
        Element mc = options.getChild(Xml.MANIFEST_CREATOR);
        super.load(mc);
    }

    @Override
    public void store(Element options) {
        Element mc = new Element(Xml.MANIFEST_CREATOR);
        super.store(mc);
        options.addContent(mc);
    }
}

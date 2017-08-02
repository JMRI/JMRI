package jmri.jmrit.operations.trains.excel;

import org.jdom2.Element;

public class TrainCustomManifest extends TrainCustomCommon {
    
    private String directoryName = "csvManifests"; // NOI18N
    
    /**
     * record the single instance *
     */
    private static TrainCustomManifest _instance = null;

    public static synchronized TrainCustomManifest instance() {
        if (_instance == null) {
            // create and load
            _instance = new TrainCustomManifest();
        }
        return _instance;
    }
    
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

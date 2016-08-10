package jmri.jmrit.operations.trains.excel;

import org.jdom2.Element;

public class TrainCustomSwitchList extends TrainCustomCommon {
    
    private String directoryName = "csvSwitchLists"; // NOI18N
    
    /**
     * record the single instance *
     */
    private static TrainCustomSwitchList _instance = null;

    public static synchronized TrainCustomSwitchList instance() {
        if (_instance == null) {
            // create and load
            _instance = new TrainCustomSwitchList();
        }
        return _instance;
    }
    
    public String getDirectoryName() {
        return directoryName;
    }

    public void setDirectoryName(String name) {
        directoryName = name;
    }
    
    public void load(Element options) {
        Element mc = options.getChild(Xml.SWITCHLIST_CREATOR);
        super.load(mc);
    }
    
    public void store(Element options) {
        Element mc = new Element(Xml.SWITCHLIST_CREATOR);
        super.store(mc);
        options.addContent(mc);
    }
}

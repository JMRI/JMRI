package jmri.jmrit.withrottle;

import org.apache.log4j.Logger;
import java.io.File;
import jmri.jmrit.XmlFile;
import org.jdom.Document;
import org.jdom.Element;

/**
 *	@author Brett Hoffman   Copyright (C) 2010
 *	@version $Revision$
 */
abstract public class AbstractWiThrottlePreferences {
    private String fileName;

    public void openFile(String fileName){
        this.fileName = fileName;
        AbstractWiThrottlePreferencesXml prefsXml = new AbstractWiThrottlePreferencesXml();
        File file = new File(this.fileName);
        Element root;
        try {
            root = prefsXml.rootFromFile(file);
        }catch (java.io.FileNotFoundException ea) {
            log.info("Could not find WiThrottle preferences file.  Normal if preferences have not been saved before.");
            root = null;
        }catch (Exception eb) {
            log.error("Exception while loading throttles preferences: " + eb);
            root = null;
        }
        if (root != null){
            load(root.getChild("WiThrottlePreferences"));
        }
    }

    abstract void load(Element child);

    abstract Element store();

    public void save() {
    	if (fileName == null) return;

    	XmlFile xmlFile = new XmlFile(){};
    	xmlFile.makeBackupFile(fileName);
    	File file=new File(fileName);
    	try {
            File parentDir=file.getParentFile();
            if(!parentDir.exists()){
                if (!parentDir.mkdir()) {
                    log.warn("Could not create parent directory for prefs file :"+fileName);
                    return;
                }
            }
            if (file.createNewFile()) log.debug("Creating new WiThrottle prefs file: "+fileName);
    	}catch (Exception ea) {
    		log.error("Could not create WiThrottle preferences file.");
    	}

    	try {
    		Element root = new Element("withrottle-prefs");
    		Document doc = XmlFile.newDocument(root);
    		root.setContent(store());
    		xmlFile.writeXML(file, doc);
    	}catch (Exception eb){
    		log.warn("Exception in storing WiThrottle xml: "+eb);
    	}
    }

    public AbstractWiThrottlePreferences(){}

    public static class AbstractWiThrottlePreferencesXml extends XmlFile{}

    private static Logger log = Logger.getLogger(AbstractWiThrottlePreferences.class.getName());

}

package jmri.web.miniserver;

import java.io.File;
import jmri.jmrit.XmlFile;
import org.jdom.Document;
import org.jdom.Element;

/**
 *	@author Modifications by Steve Todd   Copyright (C) 2011
 *	@version $Revision$
 */
abstract public class AbstractMiniServerPreferences {
    private String fileName;

    public void openFile(String fileName){
        this.fileName = fileName;
        AbstractMiniServerPreferencesXml prefsXml = new AbstractMiniServerPreferencesXml();
        File file = new File(this.fileName);
        Element root;
        try {
            root = prefsXml.rootFromFile(file);
        }catch (java.io.FileNotFoundException ea) {
            log.info("Could not find MiniServer preferences file.  Normal if preferences have not been saved before.");
            root = null;
        }catch (Exception eb) {
            log.error("Exception while loading throttles preferences: " + eb);
            root = null;
        }
        if (root != null){
            load(root.getChild("MiniServerPreferences"));
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
            if (file.createNewFile()) log.debug("Creating new MiniServer prefs file: "+fileName);
    	}catch (Exception ea) {
    		log.error("Could not create MiniServer preferences file.");
    	}

    	try {
    		Element root = new Element("MiniServer-prefs");
    		Document doc = XmlFile.newDocument(root);
    		root.setContent(store());
    		xmlFile.writeXML(file, doc);
    	}catch (Exception eb){
    		log.warn("Exception in storing MiniServer xml: "+eb);
    	}
    }

    public AbstractMiniServerPreferences(){}

    public static class AbstractMiniServerPreferencesXml extends XmlFile{}

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractMiniServerPreferences.class.getName());

}

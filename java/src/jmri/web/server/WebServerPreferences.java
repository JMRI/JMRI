package jmri.web.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import jmri.beans.Bean;
import jmri.jmrit.XmlFile;
import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;

/**
 * @author Randall Wood Copyright (C) 2012
 * @version $Revision$
 */
public class WebServerPreferences extends Bean {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.web.server.WebServerStrings");
    //  Flag that prefs have not been saved:
    private boolean isDirty = false;
    // initial defaults if prefs not found
    private int clickDelay = 1;
    private int refreshDelay = 5;
    private boolean useAjax = true;
    private boolean plain = false;
    private ArrayList<String> disallowedFrames = new ArrayList<String>(Arrays.asList(rb.getString("DefaultDisallowedFrames").split(";")));
    private boolean rebuildIndex = false;
    private String railRoadName = rb.getString("DefaultRailroadName");
    private int port = 12080;
    private static Logger log = Logger.getLogger(WebServerPreferences.class.getName());

    public WebServerPreferences(String fileName) {
        openFile(fileName);
    }

    public WebServerPreferences() {
    }

    public void load(Element child) {
        Attribute a;
        if ((a = child.getAttribute("clickDelay")) != null) {
            try {
                setClickDelay(Integer.valueOf(a.getValue()));
            } catch (NumberFormatException e) {
                log.debug(e);
            }
        }
        if ((a = child.getAttribute("refreshDelay")) != null) {
            try {
                setRefreshDelay(Integer.valueOf(a.getValue()));
            } catch (NumberFormatException e) {
                log.debug(e);
            }
        }
        if ((a = child.getAttribute("useAjax")) != null) {
            setUseAjax(a.getValue().equalsIgnoreCase("true"));
        }
        if ((a = child.getAttribute("simple")) != null) {
            setPlain(a.getValue().equalsIgnoreCase("true"));
        }
        if ((a = child.getAttribute("rebuildIndex")) != null) {
            setRebuildIndex(a.getValue().equalsIgnoreCase("true"));
        }
        if ((a = child.getAttribute("port")) != null) {
            try {
                setPort(a.getIntValue());
            } catch (DataConversionException ex) {
                setPort(12080);
                log.error("Unable to read port. Setting to default value.", ex);
            }
        }
        if ((a = child.getAttribute("railRoadName")) != null) {
            setRailRoadName(a.getValue());
        }
        Element df = child.getChild("disallowedFrames");
        if (df != null) {
            this.disallowedFrames.clear();
            for (Object f : df.getChildren("frame")) {
                this.addDisallowedFrame(((Element) f).getText().trim());
            }
        }
    }

    public boolean compareValuesDifferent(WebServerPreferences prefs) {
        if (getClickDelay() != prefs.getClickDelay()) {
            return true;
        }
        if (getRefreshDelay() != prefs.getRefreshDelay()) {
            return true;
        }
        if (useAjax() != prefs.useAjax()) {
            return true;
        }
        if (!(getDisallowedFrames().equals(prefs.getDisallowedFrames()))) {
            return true;
        }
        if (isRebuildIndex() != prefs.isRebuildIndex()) {
            return true;
        }
        if (getPort() != prefs.getPort()) {
            return true;
        }
        if (!getRailRoadName().equals(prefs.getRailRoadName())) {
            return true;
        }
        return false;
    }

    public void apply(WebServerPreferences prefs) {
        setClickDelay(prefs.getClickDelay());
        setRefreshDelay(prefs.getRefreshDelay());
        setUseAjax(prefs.useAjax());
        setDisallowedFrames((ArrayList<String>) prefs.getDisallowedFrames());
        setRebuildIndex(prefs.isRebuildIndex());
        setPort(prefs.getPort());
        setRailRoadName(prefs.getRailRoadName());
    }

    public Element store() {
        Element prefs = new Element("WebServerPreferences");
        prefs.setAttribute("clickDelay", "" + getClickDelay());
        prefs.setAttribute("refreshDelay", "" + getRefreshDelay());
        prefs.setAttribute("useAjax", "" + useAjax());
        prefs.setAttribute("simple", "" + isPlain());
        prefs.setAttribute("disallowedFrames", "" + getDisallowedFrames());
        prefs.setAttribute("rebuildIndex", "" + isRebuildIndex());
        prefs.setAttribute("port", "" + getPort());
        prefs.setAttribute("railRoadName", getRailRoadName());
        Element df = new Element("disallowedFrames");
        for (String name : getDisallowedFrames()) {
            Element frame = new Element("frame");
            frame.addContent(name);
            df.addContent(frame);
        }
        prefs.addContent(df);
        setIsDirty(false);  //  Resets only when stored
        return prefs;
    }
    private String fileName;

    public final void openFile(String fileName) {
        this.fileName = fileName;
        WebServerPreferencesXml prefsXml = new WebServerPreferences.WebServerPreferencesXml();
        File file = new File(this.fileName);
        Element root;
        try {
            root = prefsXml.rootFromFile(file);
        } catch (java.io.FileNotFoundException ea) {
            log.info("Could not find Web Server preferences file.  Normal if preferences have not been saved before.");
            root = null;
        } catch (Exception eb) {
            log.error("Exception while loading throttles preferences: " + eb);
            root = null;
        }
        if (root != null) {
            load(root);
        }
    }

    public void save() {
        if (fileName == null) {
            return;
        }

        XmlFile xmlFile = new XmlFile() {
        };
        xmlFile.makeBackupFile(fileName);
        File file = new File(fileName);
        try {
            File parentDir = file.getParentFile();
            if (!parentDir.exists()) {
                if (!parentDir.mkdir()) {
                    log.warn("Could not create parent directory for prefs file :" + fileName);
                    return;
                }
            }
            if (file.createNewFile()) {
                log.debug("Creating new Web Server prefs file: " + fileName);
            }
        } catch (Exception ea) {
            log.error("Could not create Web Server preferences file.");
        }

        try {
            xmlFile.writeXML(file, XmlFile.newDocument(store()));
        } catch (Exception eb) {
            log.warn("Exception in storing Web Server xml: " + eb);
        }
    }

    public boolean getIsDirty() {
        return isDirty;
    }

    public void setIsDirty(boolean value) {
        isDirty = value;
    }

    public int getClickDelay() {
        return clickDelay;
    }

    public void setClickDelay(int value) {
        clickDelay = value;
    }

    public int getRefreshDelay() {
        return refreshDelay;
    }

    public void setRefreshDelay(int value) {
        refreshDelay = value;
    }

    public List<String> getDisallowedFrames() {
        return disallowedFrames;
    }

    public boolean useAjax() {
        return useAjax;
    }

    public void setUseAjax(boolean value) {
        useAjax = value;
    }

    public boolean isPlain() {
        return plain;
    }

    public void setPlain(boolean value) {
        plain = value;
    }

    public void setDisallowedFrames(ArrayList<String> value) {
        disallowedFrames = value;
    }

    public void addDisallowedFrame(String frame) {
        disallowedFrames.add(frame);
    }

    public boolean isRebuildIndex() {
        return rebuildIndex;
    }

    public void setRebuildIndex(boolean value) {
        rebuildIndex = value;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int value) {
        port = value;
    }

    /**
     * @return the railRoadName
     */
    public String getRailRoadName() {
        return railRoadName;
    }

    /**
     * @param railRoadName the railRoadName to set
     */
    public void setRailRoadName(String railRoadName) {
        if (railRoadName != null) {
            this.railRoadName = railRoadName;
        } else {
            this.railRoadName = "JMRI";
        }
    }

    public static class WebServerPreferencesXml extends XmlFile {
    }
}

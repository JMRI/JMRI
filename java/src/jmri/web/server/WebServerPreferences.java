package jmri.web.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jmri.beans.Bean;
import jmri.jmrit.XmlFile;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Randall Wood Copyright (C) 2012
 * @version $Revision$
 */
public class WebServerPreferences extends Bean {

    // XML elements
    public static final String DisallowedFrames = "disallowedFrames"; // NOI18N
    public static final String WebServerPreferences = "WebServerPreferences"; // NOI18N
    public static final String Frame = "frame"; // NOI18N
    public static final String Port = "port"; // NOI18N
    public static final String ClickDelay = "clickDelay"; // NOI18N
    public static final String RefreshDelay = "refreshDelay"; // NOI18N
    public static final String UseAjax = "useAjax"; // NOI18N
    public static final String Simple = "simple"; // NOI18N
    public static final String RailRoadName = "railRoadName"; // NOI18N

    //  Flag that prefs have not been saved:
    private boolean isDirty = false;
    // initial defaults if prefs not found
    private int clickDelay = 1;
    private int refreshDelay = 5;
    private boolean useAjax = true;
    private boolean plain = false;
    private ArrayList<String> disallowedFrames = new ArrayList<String>(Arrays.asList(WebServer.getString("DefaultDisallowedFrames").split(";")));
    private String railRoadName = WebServer.getString("DefaultRailroadName");
    private int port = 12080;
    private static Logger log = LoggerFactory.getLogger(WebServerPreferences.class.getName());

    public WebServerPreferences(String fileName) {
        openFile(fileName);
    }

    public WebServerPreferences() {
    }

    public void load(Element child) {
        Attribute a;
        if ((a = child.getAttribute(ClickDelay)) != null) {
            try {
                setClickDelay(Integer.valueOf(a.getValue()));
            } catch (NumberFormatException e) {
                log.debug(e.getLocalizedMessage(), e);
            }
        }
        if ((a = child.getAttribute(RefreshDelay)) != null) {
            try {
                setRefreshDelay(Integer.valueOf(a.getValue()));
            } catch (NumberFormatException e) {
                log.debug(e.getLocalizedMessage(), e);
            }
        }
        if ((a = child.getAttribute(UseAjax)) != null) {
            setUseAjax(Boolean.parseBoolean(a.getValue()));
        }
        if ((a = child.getAttribute(Simple)) != null) {
            setPlain(Boolean.parseBoolean(a.getValue()));
        }
        if ((a = child.getAttribute(Port)) != null) {
            try {
                setPort(a.getIntValue());
            } catch (DataConversionException ex) {
                setPort(12080);
                log.error("Unable to read port. Setting to default value.", ex);
            }
        }
        if ((a = child.getAttribute(RailRoadName)) != null) {
            setRailRoadName(a.getValue());
        }
        Element df = child.getChild(DisallowedFrames);
        if (df != null) {
            this.disallowedFrames.clear();
            for (Object f : df.getChildren(Frame)) {
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
        if (getPort() != prefs.getPort()) {
            return true;
        }
        return !getRailRoadName().equals(prefs.getRailRoadName());
    }

    public void apply(WebServerPreferences prefs) {
        setClickDelay(prefs.getClickDelay());
        setRefreshDelay(prefs.getRefreshDelay());
        setUseAjax(prefs.useAjax());
        setDisallowedFrames((ArrayList<String>) prefs.getDisallowedFrames());
        setPort(prefs.getPort());
        setRailRoadName(prefs.getRailRoadName());
    }

    public Element store() {
        Element prefs = new Element(WebServerPreferences);
        prefs.setAttribute(ClickDelay, "" + getClickDelay());
        prefs.setAttribute(RefreshDelay, "" + getRefreshDelay());
        prefs.setAttribute(UseAjax, "" + useAjax());
        prefs.setAttribute(Simple, "" + isPlain());
        prefs.setAttribute(DisallowedFrames, "" + getDisallowedFrames());
        prefs.setAttribute(Port, "" + getPort());
        prefs.setAttribute(RailRoadName, getRailRoadName());
        Element df = new Element(DisallowedFrames);
        for (String name : getDisallowedFrames()) {
            Element frame = new Element(Frame);
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
        } catch (IOException eb) {
            log.error("Exception while loading throttles preferences: " + eb);
            root = null;
        } catch (JDOMException ec) {
            log.error("Exception while loading throttles preferences: " + ec);
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
        } catch (IOException ea) {
            log.error("Could not create Web Server preferences file.");
        }

        try {
            xmlFile.writeXML(file, XmlFile.newDocument(store()));
        } catch (IOException eb) {
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
            this.railRoadName = WebServer.getString("DefaultRailroadName");
        }
    }

    public static class WebServerPreferencesXml extends XmlFile {
    }
}

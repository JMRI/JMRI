package jmri.web.server;

import java.io.File;
import java.io.IOException;
import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import jmri.util.FileUtil;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide interface for managing the JMRI web server, including migrating from
 * the older 2.n Mini Web Server.
 *
 * @author rhwood
 */
public class WebServerManager {

    private WebServerPreferences preferences;
    private WebServer server;
    private final static Logger log = LoggerFactory.getLogger(WebServer.class.getName());

    private WebServerManager() {
        if (InstanceManager.getDefault(WebServerPreferences.class) == null) {
            File webServerPrefsFile = new File(FileUtil.getUserFilesPath() + "networkServices" + File.separator + "WebServerPreferences.xml"); // NOI18N
            File miniServerPrefsFile = new File(FileUtil.getUserFilesPath() + "miniserver" + File.separator + "MiniServerPreferences.xml"); // NOI18N
            if (!webServerPrefsFile.exists() && miniServerPrefsFile.exists()) {
                // import Mini Server preferences into Web Server preferences
                preferencesFromMiniServerPreferences(miniServerPrefsFile, webServerPrefsFile);
            } else if (!webServerPrefsFile.exists()) {
                InstanceManager.store(new WebServerPreferences(), WebServerPreferences.class);
            } else {
                InstanceManager.store(new WebServerPreferences(FileUtil.getUserFilesPath() + "networkServices" + File.separator + "WebServerPreferences.xml"), WebServerPreferences.class); // NOI18N
            }
        }
        preferences = InstanceManager.getDefault(WebServerPreferences.class);
    }

    public static WebServerManager getInstance() {
        if (InstanceManager.getDefault(WebServerManager.class) == null) {
            InstanceManager.setDefault(WebServerManager.class, new WebServerManager());
        }
        return InstanceManager.getDefault(WebServerManager.class);
    }

    public WebServerPreferences getPreferences() {
        if (preferences == null) {
            preferences = new WebServerPreferences();
        }
        return preferences;
    }

    public static WebServerPreferences getWebServerPreferences() {
        return getInstance().getPreferences();
    }

    public WebServer getServer() {
        if (server == null) {
            server = new WebServer();
        }
        return server;
    }

    public static WebServer getWebServer() {
        return getInstance().getServer();
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "REC_CATCH_EXCEPTION",
            justification = "Catch is covering both JDOMException and IOException, FindBugs seems confused")
    private void preferencesFromMiniServerPreferences(File MSFile, File WSFile) {
        WebServerPreferences.WebServerPreferencesXml xmlFile = new WebServerPreferences.WebServerPreferencesXml();
        try {
            Element MSRoot = xmlFile.rootFromFile(MSFile);
            Element WSRoot = new Element(WebServerPreferences.WebServerPreferences);
            Element MSPrefs = MSRoot.getChild("MiniServerPreferences"); // NOI18N
            for (Object pref : MSPrefs.getChildren()) {
                WSRoot.addContent((Element) pref);
            }
            for (Attribute attr : MSPrefs.getAttributes()) {
                if (attr.getName().equals("getDisallowedFrames")) { // NOI18N
                    Element DF = new Element(WebServerPreferences.DisallowedFrames);
                    String[] frames = attr.getValue().split("\\n"); // NOI18N
                    for (String frame : frames) {
                        DF.addContent(new Element(WebServerPreferences.Frame).addContent(frame));
                    }
                    WSRoot.addContent(DF);
                } else if (attr.getName().equals("getPort")) { // NOI18N
                    WSRoot.setAttribute(WebServerPreferences.Port, attr.getValue());
                } else if (attr.getName().equals("getClickDelay")) { // NOI18N
                    WSRoot.setAttribute(WebServerPreferences.ClickDelay, attr.getValue());
                } else if (attr.getName().equals("getRefreshDelay")) { // NOI18N
                    WSRoot.setAttribute(WebServerPreferences.RefreshDelay, attr.getValue());
                } else {
                    // double cast because clone() is Protected on Object
                    WSRoot.setAttribute(attr.clone());
                }
            }
            Document WSDoc = XmlFile.newDocument(WSRoot);
            File parent = new File(WSFile.getParent());
            if (!parent.exists()) {
                boolean created = parent.mkdir(); // directory known to not exist from previous conditional
                if (!created) {
                    log.error("Failed to create directory {}", parent.toString());
                    throw new java.io.IOException("Failed to create directory " + parent.toString());
                }
            }

            boolean created = WSFile.createNewFile(); // known to not exist or this method would not have been called
            if (!created) {
                log.error("Failed to new create file {}", WSFile.toString());
                throw new java.io.IOException("Failed to create new file " + WSFile.toString());
            }

            xmlFile.writeXML(WSFile, WSDoc);

        } catch (IOException ex) {
            log.error("Error converting miniServer preferences to Web Server preferences.", ex);
        } catch (JDOMException ex) {
            log.error("Error converting miniServer preferences to Web Server preferences.", ex);
        }
    }
}

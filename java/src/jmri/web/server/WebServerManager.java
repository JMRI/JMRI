/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.web.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import jmri.util.FileUtil;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rhwood
 */
public class WebServerManager {

    static private WebServerManager instance = null;
    private WebServerPreferences preferences;
    private WebServer server;
    static Logger log = LoggerFactory.getLogger(WebServer.class.getName());

    private WebServerManager() {
        if (InstanceManager.getDefault(WebServerPreferences.class) == null) {
            File webServerPrefsFile = new File(FileUtil.getUserFilesPath() + "networkServices" + File.separator + "WebServerPreferences.xml"); // NOI18N
            File miniServerPrefsFile = new File(FileUtil.getUserFilesPath() + "miniserver" + File.separator + "MiniServerPreferences.xml"); // NOI18N
            if (!webServerPrefsFile.exists() && miniServerPrefsFile.exists()) {
                // import Mini Server preferences into Web Server preferences
                preferencesFromMiniServerPreferences(miniServerPrefsFile, webServerPrefsFile);
            } else {
                InstanceManager.store(new WebServerPreferences(FileUtil.getUserFilesPath() + "networkServices" + File.separator + "WebServerPreferences.xml"), WebServerPreferences.class); // NOI18N
            }
            // disable during testing
            // this.removeV2Index();
        }
        preferences = InstanceManager.getDefault(WebServerPreferences.class);
    }

    public static WebServerManager getInstance() {
        if (instance == null) {
            instance = new WebServerManager();
        }
        return instance;
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
            try {
                this.rebuildIndex();
            } catch (Exception ex) {
                log.warn("Error rebuilding index.", ex);
            }
            server = new WebServer();
        }
        return server;
    }

    public static WebServer getWebServer() {
        return getInstance().getServer();
    }

    protected void rebuildIndex() throws IOException {
        this.rebuildIndex(false);
    }

    protected void rebuildIndex(boolean force) throws IOException {
        ResourceBundle rb = ResourceBundle.getBundle("jmri.web.server.Html");
        File indexFile = new File(FileUtil.getAbsoluteFilename(FileUtil.PREFERENCES + "networkServices" + File.separator + "index.html")); // NOI18N
        if (force || this.getPreferences().isRebuildIndex() || !indexFile.exists()) {
            FileWriter writer = new FileWriter(indexFile);
            writer.write(rb.getString("HTML5DocType"));
            writer.write(String.format(rb.getString("HeadFormat"),
                    rb.getString("HTML5DocType"),
                    this.getPreferences().getRailRoadName(),
                    "index", // NOI18N
                    rb.getString("IndexHeadExtras")));
            writer.write(rb.getString("Index"));
            writer.write(String.format(rb.getString("TailFormat"),
                    "html/web/index.shtml", // NOI18N
                    rb.getString("GeneralMenuItems")));
            writer.close();
            if (this.getPreferences().isRebuildIndex()) {
                this.getPreferences().setRebuildIndex(false);
                this.getPreferences().setIsDirty(true);
            }
        }
    }

    /*
     private void removeV2Index() {
     File indexFile = new File(FileUtil.getAbsoluteFilename(FileUtil.PREFERENCES + "index.html"));
     File backup = new File(FileUtil.getAbsoluteFilename(FileUtil.PREFERENCES + "index.v2.html"));
     try {
     if (indexFile.exists()) {
     indexFile.renameTo(backup);
     log.info("Renamed existing index.html in Preferences to index.v2.html.");
     }
     } catch (Exception ex) {
     log.error("Failed to move index.html.", ex);
     }
     }
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "REC_CATCH_EXCEPTION",
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
            for (Object attr : MSPrefs.getAttributes()) {
                if (((Attribute) attr).getName().equals("getDisallowedFrames")) { // NOI18N
                    Element DF = new Element(WebServerPreferences.DisallowedFrames);
                    String[] frames = ((Attribute) attr).getValue().split("\\n"); // NOI18N
                    for (String frame : frames) {
                        DF.addContent(new Element(WebServerPreferences.Frame).addContent(frame));
                    }
                    WSRoot.addContent(DF);
                } else if (((Attribute) attr).getName().equals("getPort")) { // NOI18N
                    WSRoot.setAttribute(WebServerPreferences.Port, ((Attribute) attr).getValue());
                } else if (((Attribute) attr).getName().equals("getClickDelay")) { // NOI18N
                    WSRoot.setAttribute(WebServerPreferences.ClickDelay, ((Attribute) attr).getValue());
                } else if (((Attribute) attr).getName().equals("getRefreshDelay")) { // NOI18N
                    WSRoot.setAttribute(WebServerPreferences.RefreshDelay, ((Attribute) attr).getValue());
                } else if (((Attribute) attr).getName().equals("isRebuildIndex")) { // NOI18N
                    WSRoot.setAttribute(WebServerPreferences.RebuildIndex, ((Attribute) attr).getValue());
                } else {
                    // double cast because clone() is Protected on Object
                    WSRoot.setAttribute((Attribute) ((Attribute) attr).clone());
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

        } catch (Exception ex) {
            log.error("Error converting miniServer preferences to Web Server preferences.", ex);
        }
    }
}

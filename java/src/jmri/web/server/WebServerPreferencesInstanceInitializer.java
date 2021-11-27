package jmri.web.server;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import jmri.implementation.AbstractInstanceInitializer;
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
 * @author Randall Wood Copyright 2017, 2019
 * @deprecated since 4.19.2 without direct replacement; users migrating from
 *             JMRI versions older than 4.0 will need to manually migrate web
 *             server preferences
 */
@Deprecated
public class WebServerPreferencesInstanceInitializer extends AbstractInstanceInitializer {

    private static final Logger log = LoggerFactory.getLogger(WebServerPreferencesInstanceInitializer.class);

    @Override
    public <T> Object getDefault(Class<T> type) {
        if (type == WebServerPreferences.class) {
            File webServerPrefsFile = new File(
                    FileUtil.getUserFilesPath() + "networkServices" + File.separator + "WebServerPreferences.xml"); // NOI18N
            File miniServerPrefsFile =
                    new File(FileUtil.getUserFilesPath() + "miniserver" + File.separator + "MiniServerPreferences.xml"); // NOI18N
            if (!webServerPrefsFile.exists() && miniServerPrefsFile.exists()) {
                // import Mini Server preferences into Web Server preferences
                preferencesFromMiniServerPreferences(miniServerPrefsFile, webServerPrefsFile);
            }
            if (!webServerPrefsFile.exists()) {
                return new WebServerPreferences();
            }
        }
        return super.getDefault(type);
    }

    @Override
    public Set<Class<?>> getInitalizes() {
        Set<Class<?>> set = super.getInitalizes();
        set.add(WebServerPreferences.class);
        return set;
    }

    private void preferencesFromMiniServerPreferences(File msFile, File wsFile) {
        XmlFile xmlFile = new XmlFile() {
        };
        try {
            Element msRoot = xmlFile.rootFromFile(msFile);
            Element wsRoot = new Element(WebServerPreferences.WEB_SERVER_PREFERENCES);
            Element msPrefs = msRoot.getChild("MiniServerPreferences"); // NOI18N
            msPrefs.getChildren().forEach(wsRoot::addContent);
            for (Attribute attr : msPrefs.getAttributes()) {
                switch (attr.getName()) {
                    case "getDisallowedFrames": // NOI18N
                        Element df = new Element(WebServerPreferences.DISALLOWED_FRAMES);
                        String[] frames = attr.getValue().split("\\n"); // NOI18N
                        for (String frame : frames) {
                            df.addContent(new Element(WebServerPreferences.FRAME).addContent(frame));
                        }
                        wsRoot.addContent(df);
                        break;
                    case "getPort": // NOI18N
                        wsRoot.setAttribute(WebServerPreferences.PORT, attr.getValue());
                        break;
                    case "getClickDelay": // NOI18N
                        wsRoot.setAttribute(WebServerPreferences.CLICK_DELAY, attr.getValue());
                        break;
                    case "getRefreshDelay": // NOI18N
                        wsRoot.setAttribute(WebServerPreferences.REFRESH_DELAY, attr.getValue());
                        break;
                    default:
                        // double cast because clone() is Protected on Object
                        wsRoot.setAttribute(attr.clone());
                        break;
                }
            }
            Document wsDoc = XmlFile.newDocument(wsRoot);
            File parent = new File(wsFile.getParent());
            if (!parent.exists()) {
                boolean created = parent.mkdir(); // directory known to not
                                                  // exist from previous
                                                  // conditional
                if (!created) {
                    log.error("Failed to create directory {}", parent);
                    throw new java.io.IOException("Failed to create directory " + parent.toString());
                }
            }

            boolean created = wsFile.createNewFile(); // known to not exist or
                                                      // this method would not
                                                      // have been called
            if (!created) {
                log.error("Failed to new create file {}", wsFile);
                throw new java.io.IOException("Failed to create new file " + wsFile.toString());
            }

            xmlFile.writeXML(wsFile, wsDoc);

        } catch (
                IOException |
                JDOMException ex) {
            log.error("Error converting miniServer preferences to Web Server preferences.", ex);
        }
    }
}

package jmri.web.server;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import jmri.InstanceInitializer;
import jmri.implementation.AbstractInstanceInitializer;
import jmri.jmrit.XmlFile;
import jmri.util.FileUtil;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide interface for managing the JMRI web server, including migrating from
 * the older 2.n Mini Web Server.
 *
 * @author Randall Wood Copyright 2017
 */
@ServiceProvider(service = InstanceInitializer.class)
public class WebServerPreferencesInstanceInitializer extends AbstractInstanceInitializer {

    private final static Logger log = LoggerFactory.getLogger(WebServer.class);

    @Override
    public <T> Object getDefault(Class<T> type) throws IllegalArgumentException {
        if (type == WebServerPreferences.class) {
            File webServerPrefsFile = new File(FileUtil.getUserFilesPath() + "networkServices" + File.separator + "WebServerPreferences.xml"); // NOI18N
            File miniServerPrefsFile = new File(FileUtil.getUserFilesPath() + "miniserver" + File.separator + "MiniServerPreferences.xml"); // NOI18N
            if (!webServerPrefsFile.exists() && miniServerPrefsFile.exists()) {
                // import Mini Server preferences into Web Server preferences
                preferencesFromMiniServerPreferences(miniServerPrefsFile, webServerPrefsFile);
            }
            if (!webServerPrefsFile.exists()) {
                return new WebServerPreferences();
            } else {
                return new WebServerPreferences(webServerPrefsFile.getAbsolutePath());
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

    private void preferencesFromMiniServerPreferences(File MSFile, File WSFile) {
        XmlFile xmlFile = new XmlFile() {
        };
        try {
            Element MSRoot = xmlFile.rootFromFile(MSFile);
            Element WSRoot = new Element(WebServerPreferences.WEB_SERVER_PREFERENCES);
            Element MSPrefs = MSRoot.getChild("MiniServerPreferences"); // NOI18N
            MSPrefs.getChildren().forEach((pref) -> {
                WSRoot.addContent(pref);
            });
            for (Attribute attr : MSPrefs.getAttributes()) {
                switch (attr.getName()) {
                    case "getDisallowedFrames": // NOI18N
                        Element DF = new Element(WebServerPreferences.DISALLOWED_FRAMES);
                        String[] frames = attr.getValue().split("\\n"); // NOI18N
                        for (String frame : frames) {
                            DF.addContent(new Element(WebServerPreferences.FRAME).addContent(frame));
                        }
                        WSRoot.addContent(DF);
                        break;
                    case "getPort": // NOI18N
                        WSRoot.setAttribute(WebServerPreferences.PORT, attr.getValue());
                        break;
                    case "getClickDelay": // NOI18N
                        WSRoot.setAttribute(WebServerPreferences.CLICK_DELAY, attr.getValue());
                        break;
                    case "getRefreshDelay": // NOI18N
                        WSRoot.setAttribute(WebServerPreferences.REFRESH_DELAY, attr.getValue());
                        break;
                    default:
                        // double cast because clone() is Protected on Object
                        WSRoot.setAttribute(attr.clone());
                        break;
                }
            }
            Document WSDoc = XmlFile.newDocument(WSRoot);
            File parent = new File(WSFile.getParent());
            if (!parent.exists()) {
                boolean created = parent.mkdir(); // directory known to not exist from previous conditional
                if (!created) {
                    log.error("Failed to create directory {}", parent);
                    throw new java.io.IOException("Failed to create directory " + parent.toString());
                }
            }

            boolean created = WSFile.createNewFile(); // known to not exist or this method would not have been called
            if (!created) {
                log.error("Failed to new create file {}", WSFile);
                throw new java.io.IOException("Failed to create new file " + WSFile.toString());
            }

            xmlFile.writeXML(WSFile, WSDoc);

        } catch (IOException | JDOMException ex) {
            log.error("Error converting miniServer preferences to Web Server preferences.", ex);
        }
    }
}

package jmri.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.help.JHelpContentViewer;
import javax.help.plaf.basic.BasicContentViewerUI;
import javax.swing.JComponent;
import javax.swing.event.HyperlinkEvent;
import javax.swing.plaf.ComponentUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A UI subclass that will open external links (website or mail links) in an
 * external browser
 * <p>
 * To use:
 * SwingHelpUtilities.setContentViewerUI("jmri.util.ExternalLinkContentViewerUI");
 *
 * @since JMRI 2.5.3 (or perhaps later, please check CVS)
 */
public class ExternalLinkContentViewerUI extends BasicContentViewerUI {

    public ExternalLinkContentViewerUI(JHelpContentViewer x) {
        super(x);
    }

    public static ComponentUI createUI(JComponent x) {
        return new ExternalLinkContentViewerUI((JHelpContentViewer) x);
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent he) {
        log.debug("event has type {}", he.getEventType());
        if (he.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            try {
                log.debug("event has URL {}", he.getURL());
                URL u = he.getURL();
                activateURL(u);
            } catch (IOException | URISyntaxException t) {
                log.error("Error processing request", t);
            }
        }
        super.hyperlinkUpdate(he);
    }

    public static void activateURL(URL u) throws IOException, URISyntaxException {
        if (u.getProtocol().equalsIgnoreCase("mailto") || u.getProtocol().equalsIgnoreCase("http")
                || u.getProtocol().equalsIgnoreCase("ftp")) {
            URI uri = new URI(u.toString());
            log.debug("defer protocol {} to browser via {}", u.getProtocol(), uri);
            Desktop.getDesktop().browse(uri);
        } else if (u.getProtocol().equalsIgnoreCase("file") && (u.getFile().endsWith("jpg")
                || u.getFile().endsWith("png")
                || u.getFile().endsWith("xml")
                || u.getFile().endsWith("gif"))) {

            // following was 
            // ("file:"+System.getProperty("user.dir")+"/"+u.getFile()) 
            // but that duplicated the path information; JavaHelp seems to provide
            // full pathnames here.
            URI uri = new URI(u.toString());
            log.debug("defer content of {} to browser with {}", u.getFile(), uri);
            Desktop.getDesktop().browse(uri);
        } else if (u.getProtocol().equalsIgnoreCase("file")) {
            // if file not present, fall back to web browser
            // first, get file name
            String pathName = u.getFile();
            if (pathName.contains("%20") && SystemType.isWindows()) {
                log.debug("Windows machine with space in path name! {}", pathName);
                // need to have the actual space in the path name for get file to work properly
                pathName = pathName.replace("%20", " ");
            }
            File file = new File(pathName);
            if (!file.exists()) {
                URI uri = new URI("http://jmri.org/" + u.getFile());
                log.debug("fallback to browser with {}", uri);
                Desktop.getDesktop().browse(uri);
            }
        }
    }
    private final static Logger log = LoggerFactory.getLogger(ExternalLinkContentViewerUI.class);
}

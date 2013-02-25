// ExternalLinkContentViewerUI.java

package jmri.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.help.*;
import javax.help.plaf.basic.*;


/**
 * A UI subclass that will open external links (website 
 * or mail links) in an external browser
 * <P>
 * To use:
 * SwingHelpUtilities.setContentViewerUI("jmri.util.ExternalLinkContentViewerUI");
 * <P.
 *
 @since JMRI 2.5.3 (or perhaps later, please check CVS)
 */
 
public class ExternalLinkContentViewerUI extends BasicContentViewerUI {
    public ExternalLinkContentViewerUI(JHelpContentViewer x){
		super(x);
	}

    public static javax.swing.plaf.ComponentUI createUI(JComponent x){
        return new ExternalLinkContentViewerUI((JHelpContentViewer)x);
    }

    public void hyperlinkUpdate(HyperlinkEvent he){
        if (log.isDebugEnabled()) log.debug("event has type "+he.getEventType());
        if(he.getEventType()==HyperlinkEvent.EventType.ACTIVATED){
            try{
                if (log.isDebugEnabled()) log.debug("event has URL  "+he.getURL());
                URL u = he.getURL();
                activateURL(u);
            } catch(Throwable t){log.error("Error processing request", t);}
        }
        super.hyperlinkUpdate(he);
    }
    
    public static void activateURL(URL u) throws java.io.IOException, java.net.URISyntaxException {
        if(u.getProtocol().equalsIgnoreCase("mailto")||u.getProtocol().equalsIgnoreCase("http")
                ||u.getProtocol().equalsIgnoreCase("ftp")
             ){
            URI uri = new URI(u.toString());
            log.debug("defer protocol "+u.getProtocol()+" to browser via "+uri);
            java.awt.Desktop.getDesktop().browse(uri);
            return;
        } else if ( u.getProtocol().equalsIgnoreCase("file") && (
                u.getFile().endsWith("jpg")
                ||u.getFile().endsWith("png")
                ||u.getFile().endsWith("xml")
                ||u.getFile().endsWith("gif")) ) {
        
            // following was 
            // ("file:"+System.getProperty("user.dir")+"/"+u.getFile()) 
            // but that duplicated the path information; JavaHelp seems to provide
            // full pathnames here.
            URI uri = new URI(u.toString());
            log.debug("defer content of "+u.getFile()+" to browser with "+uri);
            java.awt.Desktop.getDesktop().browse(uri);
            return;
        } else if ( u.getProtocol().equalsIgnoreCase("file") ) {
            // if file not present, fall back to web browser
            // first, get file name
            java.io.File file = new java.io.File(u.getFile());
            if (!file.exists()) {
                URI uri = new URI("http://jmri.org/"+u.getFile());
                log.debug("fallback to browser with "+uri);
                java.awt.Desktop.getDesktop().browse(uri);                  
            }
        }
    }
    
    static private Logger log = LoggerFactory.getLogger(ExternalLinkContentViewerUI.class.getName());
}


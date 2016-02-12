// WebServerAction.java
package jmri.web.server;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to start a web server. Doesn't show a panel.
 *
 * @author	Randall Wood Copyright (C) 2012
 */
public class WebServerAction extends JmriAbstractAction {

    private static final long serialVersionUID = 6023025995086573898L;
    private static ServerThread serverThread = null;
    private final static Logger log = LoggerFactory.getLogger(WebServerAction.class);

    public WebServerAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public WebServerAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    public WebServerAction() {
        super(Bundle.getMessage("MenuWebServerAction"));
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if (serverThread == null) {
            serverThread = new ServerThread();
            serverThread.start();
        } else {
            log.info("Web Server already running");
        }
    }

    @Override
    public jmri.util.swing.JmriPanel makePanel() { return null; } // not used by this classes actionPerformed, as it doesn't show anything
    
    static class ServerThread extends Thread {

        @Override
        public void run() {
            WebServerManager.getWebServer().start();
        }
    }
}

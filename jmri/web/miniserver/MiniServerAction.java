// MiniServerAction.java

package jmri.web.miniserver;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import javax.swing.*;
import java.io.*;

import jmri.util.zeroconf.ZeroConfUtil;

/**
 * Action to start a miniserver
 *
 * @author	    Bob Jacobsen    Copyright (C) 2004
 * @version         $Revision: 1.6 $
 */
public class MiniServerAction extends AbstractAction {

    int port = 12080;
    
    public MiniServerAction() { super("Start Mini Web Server");}
    
    public void actionPerformed(ActionEvent ev) {
        // make sure index page exists
        ensureIndexPage();
        
        // start server
        startServer();
        
        // advertise via zeroconf
        try {
           ZeroConfUtil.advertiseService("JMRI on "+ZeroConfUtil.getServerName("(unknown)"), "_http._tcp.local.", port, ZeroConfUtil.jmdnsInstance());
        } catch (java.io.IOException e) {
                org.apache.log4j.Logger.getLogger(MiniServerAction.class.getName()).error("can't advertise via ZeroConf: "+e);
        }
    }
    
    public void ensureIndexPage() {
        String name = jmri.jmrit.XmlFile.prefsDir()+"index.html";
        File file = new File(name);
        if (!file.exists()) {
            PrintStream out = null;
            try {
                // create it
                 out = new PrintStream(new FileOutputStream(file));
    
                java.util.ResourceBundle htmlStrings = java.util.ResourceBundle.getBundle("jmri.web.miniserver.Html");          
                out.println(htmlStrings.getString("Index"));
            } catch (IOException e) {}
            finally {
                if (out!=null) {
                    out.flush();
                    out.close();
                }
            }
        }
    }
    
    ServerThread s;
    public void startServer() {
    	if (s == null){
    		s = new ServerThread();
    		s.start();
    	}else{
    		log.info("Mini Server already running");
    	}
    }
    
    
    class ServerThread extends Thread {
        public void run() {
            new ThreadedMiniServer(port, 0) {
                void notifyServerStarted() {
                    // switch to Swing and notify
                    javax.swing.SwingUtilities.invokeLater(new Runnable(){
                        public void run() {
                            // Want non-modal dialog, 
                            // so can't use JOptionPane completely
                            JOptionPane optionPane = new JOptionPane();
                            optionPane.setMessage(msg);
                            optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
                            optionPane.setOptions(new Object[] {"Close"});
                            JDialog dialog = optionPane.createDialog(null, "Web Server Started");
                            dialog.setModal(false);
                            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                            dialog.setVisible(true);
                        }
                        String msg = "Web server started at http://"
                                +getLocalAddress()
                                +":"+getPort()+"/index.html";
                    });
                }
            };
            // this line won't be reached, 
            // as the MiniServer ctor is the service loop
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(MiniServerAction.class.getName());
}

/* @(#)MiniServerAction.java */

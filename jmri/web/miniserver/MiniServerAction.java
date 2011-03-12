// MiniServerAction.java

package jmri.web.miniserver;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import java.io.*;

import java.util.ResourceBundle;

import jmri.util.BareBonesBrowserLaunch;
import jmri.util.zeroconf.ZeroConfUtil;

/**
 * Action to start a miniserver
 *
 * @author	    Bob Jacobsen    Copyright (C) 2004
 * @version         $Revision: 1.11 $
 */
public class MiniServerAction extends AbstractAction {

    int port = Integer.parseInt(MiniServerManager.MiniServerPreferencesInstance().getPort());
    ResourceBundle htmlStrings;
    ResourceBundle serviceStrings;
    
    public MiniServerAction() { super("Start Mini Web Server");}
    
    public void actionPerformed(ActionEvent ev) {
        // make sure index page exists
        ensureIndexPage();
        
        //get port from preferences
        port = Integer.parseInt(MiniServerManager.MiniServerPreferencesInstance().getPort());
        
        // start server
        startServer();
        
        // advertise via zeroconf
        try {
           ZeroConfUtil.advertiseService("JMRI on "+ZeroConfUtil.getServerName("(unknown)"), "_http._tcp.local.", port, ZeroConfUtil.jmdnsInstance());
        } catch (java.io.IOException e) {
                log.error("can't advertise via ZeroConf: "+e);
        }
    }
    
    public void ensureIndexPage() {
        String name = jmri.jmrit.XmlFile.prefsDir()+"index.html";
        File file = new File(name);
        //build file if not found OR if rebuild set in preferences
        if (!file.exists() || MiniServerManager.MiniServerPreferencesInstance().isRebuildIndex()) {
            PrintStream out = null;
            try {
                // create it
                 out = new PrintStream(new FileOutputStream(file));
    
                if (htmlStrings == null) htmlStrings = ResourceBundle.getBundle("jmri.web.miniserver.Html");   
                
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
                        	JFrame frame = new JFrame();
                            JPanel panel = new JPanel();
                            final String url = "http://" + getLocalAddress()
                                +":"+getPort();
                            JButton webButton = new JButton("Open in Browser");
                            webButton.addActionListener(new ActionListener() {
                               public void actionPerformed(ActionEvent e) {
                                  BareBonesBrowserLaunch.openURL(url); }
                               } );
                            panel.add(new JLabel("Web server started at " + url + "\n"));
                            panel.add(webButton);
                            frame.setTitle("JMRI Mini Web Server");
//                            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                            frame.getContentPane().add(panel);
                            java.net.URL imageURL = ClassLoader.getSystemResource("resources/jmri32x32.gif");
                            frame.setIconImage(new ImageIcon(imageURL).getImage());
                            frame.pack();
                            frame.setVisible(true);
                        }
                    });
                }
            };
            // this line won't be reached, 
            // as the MiniServer ctor is the service loop
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MiniServerAction.class.getName());
}

/* @(#)MiniServerAction.java */

// MiniServerAction.java

package jmri.web.miniserver;

import javax.swing.ImageIcon;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import javax.swing.Icon;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import java.io.*;
import java.util.HashMap;

import java.util.ResourceBundle;

import jmri.util.BareBonesBrowserLaunch;
import jmri.util.zeroconf.ZeroConfService;

/**
 * Action to start a miniserver
 *
 * @author	    Bob Jacobsen    Copyright (C) 2004
 * @version         $Revision$
 */
public class MiniServerAction extends JmriAbstractAction {

    public MiniServerAction(String s, WindowInterface wi) {
    	super(s, wi);
    }
     
 	public MiniServerAction(String s, Icon i, WindowInterface wi) {
    	super(s, i, wi);
    }

    int port = Integer.parseInt(MiniServerManager.miniServerPreferencesInstance().getPort());
    ResourceBundle htmlStrings;
    ResourceBundle serviceStrings;
    
    public MiniServerAction() { super("Start Mini Web Server");}
    
    public void actionPerformed(ActionEvent ev) {
        // make sure index page exists
        ensureIndexPage();
        
        //get port from preferences
        port = Integer.parseInt(MiniServerManager.miniServerPreferencesInstance().getPort());
        
        // start server
        startServer();
        
        // advertise via zeroconf
        ZeroConfService.create("_http._tcp.local.", port, new HashMap<String,String>(){{put("path","/index.html");}}).publish();
        }
    
    public void ensureIndexPage() {
        String name = jmri.jmrit.XmlFile.prefsDir()+"index.html";
        File file = new File(name);
        //build file if not found OR if rebuild set in preferences
        if (!file.exists() || MiniServerManager.miniServerPreferencesInstance().isRebuildIndex()) {
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
                            frame.setIconImage(frame.getToolkit().getImage("resources/jmri32x32.gif"));
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

    // never invoked, because we overrode actionPerformed above
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MiniServerAction.class.getName());
}

/* @(#)MiniServerAction.java */

// ReportContext.java

package jmri.jmrit.mailreport;

import java.awt.*;
import javax.swing.*;
import jmri.util.JmriInsets;
import apps.Apps;


/**
 * Provide the JMRI context info.
 *<p>
 * Currently uses direct references to the apps.Apps class. 
 * It would be better to have Apps (or whatever) store the
 * needed info in e.g. system parameters, so they can
 * be retrieved without access outside the jmri.jmrit
 * package (or move this to the apps package).  See the
 * example for the configFilename property.
 *
 * @author	Bob Jacobsen    Copyright (C) 2007, 2009
 * @author  Matt Harris Copyright (C) 2008, 2009
 *
 * @version         $Revision$
 */
public class ReportContext {

    String report = "";
    
    public String getReport() {
        
        addString("JMRI Version: "+jmri.Version.name()+"  ");	 
        addString("JMRI configuration file name: "
                    +System.getProperty("org.jmri.apps.Apps.configFilename")+"  ");	 
        if (jmri.util.JmriJFrame.getFrameList().get(0)!=null)
            addString("JMRI main window name: "
                    +jmri.util.JmriJFrame.getFrameList().get(0).getTitle()+"  ");	 

        addString("JMRI Application: "+jmri.Application.getApplicationName()+"  ");
        addString("Connection one: "+Apps.getConnection1()+"  ");
        addString("Connection two: "+Apps.getConnection2()+"  ");
        addString("Connection three: "+Apps.getConnection3()+"  ");
        addString("Connection four: "+Apps.getConnection4()+"  ");

        String prefs = jmri.jmrit.XmlFile.prefsDir();
        addString("Preferences directory: "+prefs+"  ");
        
        String prog = System.getProperty("user.dir");
        addString("Program directory: "+prog+"  ");

        String roster = jmri.jmrit.roster.Roster.defaultRosterFilename();
        addString("Roster index location: "+roster+"  ");

        //String operations = jmri.jmrit.operations.setup.OperationsSetupXml.getFileLocation();
        //addString("Operations files location: "+operations+"  ");

        jmri.jmrit.audio.AudioFactory af = jmri.InstanceManager.audioManagerInstance().getActiveAudioFactory();
        String audio = af!=null?af.toString():"[not initialised]";
        addString("Audio factory type: "+audio+"  ");

        addProperty("java.version");
        addProperty("java.vendor");
        addProperty("java.home");

        addProperty("java.vm.version");
        addProperty("java.vm.vendor");
        addProperty("java.vm.name");

        addProperty("java.specification.version");
        addProperty("java.specification.vendor");
        addProperty("java.specification.name");

        addProperty("java.class.version");
        addProperty("java.class.path");
        addProperty("java.library.path");

        addProperty("java.compiler");
        addProperty("java.ext.dirs");
        		
        addProperty("os.name");
        addProperty("os.arch");
        addProperty("os.version");

        addProperty("python.home");
        addProperty("python.path");
        addProperty("python.startup");
        
        addProperty("user.name");
        addProperty("user.home");
        addProperty("user.dir");
        addProperty("jmri.log.path");

        addScreenSize();
        
        return report;
	
	}
		
	void addString(String val) {
        report = report + val+"\n";	    
    }
	void addProperty(String prop) {
        addString(prop+": "+System.getProperty(prop)+"  ");	    
    }
    
    /** 
     * Provide screen - size information.  This is
     * based on the jmri.util.JmriJFrame calculation, 
     * but isn't refactored to there because we 
     * also want diagnostic info
     */
    public void addScreenSize() {
        try {
            // Find screen size. This throws null-pointer exceptions on
            // some Java installs, however, for unknown reasons, so be
            // prepared to fal back.
            JFrame dummy = new JFrame();
            try {
                Insets insets = dummy.getToolkit().getScreenInsets(dummy.getGraphicsConfiguration());
                Dimension screen = dummy.getToolkit().getScreenSize();
                addString("Screen size h:"+screen.height+", w:"+screen.width+" Inset t:"+insets.top+", b:"+insets.bottom
                        +"; l:"+insets.left+", r:"+insets.right);
            } catch (NoSuchMethodError e) {
                Dimension screen = dummy.getToolkit().getScreenSize();
                addString("Screen size h:"+screen.height+", w:"+screen.width
                            +" (No Inset method available)");
            }
        } catch (Throwable e2) {
            // failed, fall back to standard method
            addString("(Cannot sense screen size due to "+e2.toString()+")");
        }
        
        try {
            // Find screen resolution. Not expected to fail, but just in case....
            int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
            addString("Screen resolution: "+dpi);
        } catch (Throwable e2) {
            addString("Screen resolution not available");
        }
        
        // look at context
        //Rectangle virtualBounds = new Rectangle();
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            addString("Environment max bounds: "+ge.getMaximumWindowBounds());
            
            try {
                GraphicsDevice[] gs = ge.getScreenDevices();
                for (int j = 0; j < gs.length; j++) { 
                    GraphicsDevice gd = gs[j];
                    GraphicsConfiguration[] gc = gd.getConfigurations();
                    for (int i=0; i < gc.length; i++) {
                        addString("bounds["+i+"] = "+gc[i].getBounds());
                        // virtualBounds = virtualBounds.union(gc[i].getBounds());
                    }
                    addString("Device: " + gd.getIDstring() + " bounds = " + gd.getDefaultConfiguration().getBounds() +
                              " " + gd.getDefaultConfiguration().toString());
                } 
            } catch (Throwable e2) {
                addString("Exception getting device bounds "+e2.getMessage());
            }
        } catch (Throwable e1) {
            addString("Exception getting max window bounds "+e1.getMessage());
        }
        // Return the insets using a custom class
        // which should return the correct values under
        // various Linux window managers
        try {
            Insets jmriInsets = JmriInsets.getInsets();
            addString("JmriInsets t:"+jmriInsets.top+", b:"+jmriInsets.bottom
                     +"; l:"+jmriInsets.left+", r:"+jmriInsets.right);
        }
        catch (Throwable e) {
            addString("Exception getting JmriInsets" + e.getMessage());
        }
    }
}

/* @(#)ReportContext.java */

// ReportContext.java

package jmri.jmrit.mailreport;

import jmri.util.JmriInsets;
import gnu.io.CommPortIdentifier;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Toolkit;
import java.net.SocketException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collection;
import java.util.Enumeration;
import java.util.ArrayList;
import javax.swing.JFrame;
import jmri.util.FileUtil;
import jmri.util.PortNameMapper;
import jmri.util.PortNameMapper.SerialPortFriendlyName;
import jmri.util.zeroconf.ZeroConfService;


/**
 * Provide the JMRI context info.
 *<p>
 *
 * @author	Bob Jacobsen    Copyright (C) 2007, 2009
 * @author  Matt Harris Copyright (C) 2008, 2009
 *
 * @version         $Revision$
 */
public class ReportContext {

    String report = "";
    
    /**
     * Provide a report of the current JMRI context
     * @param reportNetworkInfo true if network connection and zeroconf service
     *                          information to be included
     * @return current JMRI context
     */
    public String getReport(boolean reportNetworkInfo) {
        
        addString("JMRI Version: "+jmri.Version.name()+"  ");	 
        addString("JMRI configuration file name: "
                    +System.getProperty("org.jmri.apps.Apps.configFilename")+"  ");	 
        if (jmri.util.JmriJFrame.getFrameList().get(0)!=null)
            addString("JMRI main window name: "
                    +jmri.util.JmriJFrame.getFrameList().get(0).getTitle()+"  ");	 

        addString("JMRI Application: "+jmri.Application.getApplicationName()+"  ");
        ArrayList<Object> connList = jmri.InstanceManager.configureManagerInstance().getInstanceList(jmri.jmrix.ConnectionConfig.class);
        if (connList!=null){
            for (int x = 0; x<connList.size(); x++){
                jmri.jmrix.ConnectionConfig conn = (jmri.jmrix.ConnectionConfig)connList.get(x);
                addString("Connection " + x + ": " + conn.getManufacturer() + " connected via " + conn.name() + " on " + conn.getInfo()+ " Disabled " + conn.getDisabled());
            }
        }
        
        addString("Available Communication Ports:");
        addCommunicationPortInfo();

        String prefs = FileUtil.getUserFilesPath();
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

        addProperty("file.encoding");
        		
        addProperty("os.name");
        addProperty("os.arch");
        addProperty("os.version");

        addProperty("python.home");
        addProperty("python.path");
        addProperty("python.startup");
        
        addProperty("user.name");
        addProperty("user.home");
        addProperty("user.dir");
        addProperty("user.country");
        addProperty("user.language");
        addProperty("user.timezone");
        addProperty("jmri.log.path");

        addScreenSize();
        
        if (reportNetworkInfo) addNetworkInfo();
        
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
    
    /**
     * Add network connection and running service information
     */
    private void addNetworkInfo() {
        try {
            // This code is based on that in jmri.jmrit.withrottle.UserInterface which,
            // itself, was adapted from http://www.rgagnon.com/javadetails/java-0390.html
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    String hostAddress = inetAddress.getHostAddress();
                    if (!hostAddress.equals("0.0.0.0") && !hostAddress.regionMatches(0, "127", 0, 3) && !hostAddress.contains(":")) {
                        addString("Network Interface: " + networkInterface.getName());
                        addString(" Long Name: " + networkInterface.getDisplayName());
                        addString(" Host Name: " + inetAddress.getHostName());
                        addString(" IP address: " + hostAddress);
                    }
                }
            }
        } catch (SocketException ex) {
            addString("Unable to enumerate Network Interfaces");
        }
                    
        Collection<ZeroConfService> services = ZeroConfService.allServices();
        try {
            addString("ZeroConfService host: " + ZeroConfService.hostName() + " running " + services.size() + " service(s)");
        } catch (NullPointerException ex) {
            addString("ZeroConfService host not running");
        }
        if (services.size()>0) {
            for (ZeroConfService service: services) {
                addString("ZeroConfService: " + service.serviceInfo().getQualifiedName() + "  ");
                addString(" Name: " + service.name() + "  ");
                try {
                    for (String address: service.serviceInfo().getHostAddresses()) {
                        addString(" Address:" + address + "  ");
                    }
                } catch (NullPointerException ex) {
                        addString(" Address: [unknown due to NPE]");
                }
                addString(" Port: " + service.serviceInfo().getPort() + "  ");
                addString(" Server: " + service.serviceInfo().getServer() + "  ");
                addString(" Type: " + service.type() + "  ");
                try {
                    for (String url: service.serviceInfo().getURLs()) {
                        addString(" URL: " + url + "  ");                    
                    }
                } catch (NullPointerException ex) {
                        addString(" URL: [unknown due to NPE]");
                }
                addString(" Published: " + (service.isPublished()?"yes":"no"));
            }
        }
    }
    
    /**
     * Add communication port information
     */
    private void addCommunicationPortInfo() {
        @SuppressWarnings("unchecked")
        Enumeration<CommPortIdentifier> portIDs = CommPortIdentifier.getPortIdentifiers();
        
        ArrayList<CommPortIdentifier> ports = new ArrayList<CommPortIdentifier>();
        ArrayList<String> portsList = new ArrayList<String>();
        
        // find the names of suitable ports
        while (portIDs.hasMoreElements()) {
            CommPortIdentifier id = portIDs.nextElement();
            // filter out line printers 
            if (id.getPortType() != CommPortIdentifier.PORT_PARALLEL) {
                ports.add(id);
                portsList.add(id.getName());
            }
        }
        
        addString(String.format(" Found %s serial ports", ports.size()));
        
        // now output the details
        for (CommPortIdentifier id: ports) {
            // output details
            SerialPortFriendlyName port = PortNameMapper.getPortNameMap().get(id.getName());
            if(port==null){
                port = new SerialPortFriendlyName(id.getName(), null);
                PortNameMapper.getPortNameMap().put(id.getName(), port);
            }
            	addString(" Port: " + port.getDisplayName()
                        + (id.isCurrentlyOwned()
                        ? " - in use by: " + id.getCurrentOwner()
                        : " - not in use"));
        }
    }
    
}

/* @(#)ReportContext.java */

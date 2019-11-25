package jmri.jmrit.mailreport;

import java.awt.*;
import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import javax.swing.JFrame;

import apps.gui.GuiLafPreferencesManager;
import jmri.InstanceManager;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.util.FileUtil;
import jmri.util.JmriInsets;
import jmri.util.JmriJFrame;
import jmri.util.PortNameMapper;
import jmri.util.PortNameMapper.SerialPortFriendlyName;
import jmri.util.zeroconf.ZeroConfService;
import jmri.util.zeroconf.ZeroConfServiceManager;
import purejavacomm.CommPortIdentifier;

/**
 * Provide the JMRI context info.
 * <p>
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2009
 * @author Matt Harris Copyright (C) 2008, 2009
 */
public class ReportContext {

    String report = "";

    /**
     * Provide a report of the current JMRI context
     *
     * @param reportNetworkInfo true if network connection and zeroconf service
     *                          information to be included
     * @return current JMRI context
     */
    public String getReport(boolean reportNetworkInfo) {

        addString("JMRI Version: " + jmri.Version.name() + "   ");
        addString("JMRI configuration file name: "
                + System.getProperty("org.jmri.apps.Apps.configFilename") + "   (from org.jmri.apps.Apps.configFilename system property)");
        if (!jmri.util.JmriJFrame.getFrameList().isEmpty() && jmri.util.JmriJFrame.getFrameList().get(0) != null) {
            addString("JMRI main window name: "
                    + jmri.util.JmriJFrame.getFrameList().get(0).getTitle() + "   ");
        } else {
            addString("No main window present");
        }

        addString("JMRI Application: " + jmri.Application.getApplicationName() + "   ");
        ConnectionConfigManager cm = InstanceManager.getNullableDefault(ConnectionConfigManager.class);
        if (cm != null) {
            ConnectionConfig[] connList = cm.getConnections();
            if (connList != null) {
                for (int x = 0; x < connList.length; x++) {
                    ConnectionConfig conn = connList[x];
                    addString("Connection " + x + ": " + conn.getManufacturer() + " connected via " + conn.name() + " on " + conn.getInfo() + " Disabled " + conn.getDisabled() + "   ");
                }
            }
        } else {
            addString("No connections present");
        }
        
        addString("Available Communication Ports:");
        addCommunicationPortInfo();

        Profile profile = ProfileManager.getDefault().getActiveProfile();
        if (profile != null) {
            addString("Active profile: " + profile.getName() + "   ");
            addString("Profile location: " + profile.getPath().getPath() + "   ");
            addString("Profile ID: " + profile.getId() + "   ");
        } else {
            addString("No active profile");
        }
        
        addString("JMRI Network ID: " + jmri.util.node.NodeIdentity.networkIdentity());
        addString("JMRI Storage ID: " + jmri.util.node.NodeIdentity.storageIdentity(profile));

        String prefs = FileUtil.getUserFilesPath();
        addString("Preferences directory: " + prefs + "   ");

        String prog = System.getProperty("user.dir");
        addString("Program directory: " + prog + "   ");

        String roster = jmri.jmrit.roster.Roster.getDefault().getRosterIndexPath();
        addString("Roster index location: " + roster + "   ");

        File panel = jmri.configurexml.LoadXmlUserAction.getCurrentFile();
        addString("Current panel file: " + (panel == null ? "[none]" : panel.getPath()) + "   ");
        
        addString("Locale: " + InstanceManager.getDefault(GuiLafPreferencesManager.class).getLocale());

        //String operations = jmri.jmrit.operations.setup.OperationsSetupXml.getFileLocation();
        //addString("Operations files location: "+operations+"  ");
        jmri.jmrit.audio.AudioFactory af = jmri.InstanceManager.getNullableDefault(jmri.AudioManager.class).getActiveAudioFactory();
        String audio = af != null ? af.toString() : "[not initialised]";
        addString("Audio factory type: " + audio + "   ");

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
        addProperty("python.cachedir");
        addProperty("python.cachedir.skip");
        addProperty("python.startup");

        addProperty("user.name");
        addProperty("user.home");
        addProperty("user.dir");
        addProperty("user.country");
        addProperty("user.language");
        addProperty("user.timezone");
        addProperty("jmri.log.path");
        
        addString("FileSystemView#getDefaultDirectory(): "+javax.swing.filechooser.FileSystemView.getFileSystemView().getDefaultDirectory().getPath() );
        addString("FileSystemView#getHomeDirectory(): "+javax.swing.filechooser.FileSystemView.getFileSystemView().getHomeDirectory().getPath() );
        addString("Default JFileChooser(): "+(new javax.swing.JFileChooser()).getCurrentDirectory().getPath() );

        addScreenSize();

        if (reportNetworkInfo) {
            addNetworkInfo();
        }

        return report;

    }

    void addString(String val) {
        report = report + val + "\n";
    }

    void addProperty(String prop) {
        addString(prop + ": " + System.getProperty(prop) + "   ");
    }

    /**
     * Provide screen - size information. This is based on the
     * jmri.util.JmriJFrame calculation, but isn't refactored to there because
     * we also want diagnostic info
     */
    public void addScreenSize() {
        try {
            // Find screen size. This throws null-pointer exceptions on
            // some Java installs, however, for unknown reasons, so be
            // prepared to fall back.
            JFrame dummy = new JFrame();
            try {
                Insets insets = dummy.getToolkit().getScreenInsets(dummy.getGraphicsConfiguration());
                Dimension screen = dummy.getToolkit().getScreenSize();
                addString("Screen size h:" + screen.height + ", w:" + screen.width + " Inset t:" + insets.top + ", b:" + insets.bottom
                        + "; l:" + insets.left + ", r:" + insets.right);
            } catch (NoSuchMethodError ex) {
                Dimension screen = dummy.getToolkit().getScreenSize();
                addString("Screen size h:" + screen.height + ", w:" + screen.width
                        + " (No Inset method available)");
            }
        } catch (HeadlessException ex) {
            // failed, fall back to standard method
            addString("(Cannot sense screen size due to " + ex.toString() + ")");
        }

        try {
            // Find screen resolution. Not expected to fail, but just in case....
            int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
            addString("Screen resolution: " + dpi);
        } catch (HeadlessException ex) {
            addString("Screen resolution not available");
        }

        // look at context
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            addString("Environment max bounds: " + ge.getMaximumWindowBounds());

            try {
                for (JmriJFrame.ScreenDimensions sd: JmriJFrame.getScreenDimensions()) {
                    addString("Device: " + sd.getGraphicsDevice().getIDstring() + " bounds = " + sd.getBounds());
                    addString("Device: " + sd.getGraphicsDevice().getIDstring() + " insets = " + sd.getInsets());
                }
            } catch (HeadlessException ex) {
                addString("Exception getting device bounds " + ex.getMessage());
            }
        } catch (HeadlessException ex) {
            addString("Exception getting max window bounds " + ex.getMessage());
        }
        // Return the insets using a custom class
        // which should return the correct values under
        // various Linux window managers
        try {
            Insets jmriInsets = JmriInsets.getInsets();
            addString("JmriInsets t:" + jmriInsets.top + ", b:" + jmriInsets.bottom
                    + "; l:" + jmriInsets.left + ", r:" + jmriInsets.right);
        } catch (Exception ex) {
            addString("Exception getting JmriInsets" + ex.getMessage());
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

        Collection<ZeroConfService> services = InstanceManager.getDefault(ZeroConfServiceManager.class).allServices();
        for (InetAddress address : InstanceManager.getDefault(ZeroConfServiceManager.class).getAddresses()) {
            addString("ZeroConfService host: " + InstanceManager.getDefault(ZeroConfServiceManager.class).hostName(address) + " running " + services.size() + " service(s)");
        }
        if (services.size() > 0) {
            for (ZeroConfService service : services) {
                addString("ZeroConfService: " + service.getServiceInfo().getQualifiedName() + "  ");
                addString(" Name: " + service.getName() + "   ");
                try {
                    for (String address : service.getServiceInfo().getHostAddresses()) {
                        addString(" Address:" + address + "   ");
                    }
                } catch (NullPointerException ex) {
                    addString(" Address: [unknown due to NPE]");
                }
                addString(" Port: " + service.getServiceInfo().getPort() + "   ");
                addString(" Server: " + service.getServiceInfo().getServer() + "   ");
                addString(" Type: " + service.getType() + "   ");
                try {
                    for (String url : service.getServiceInfo().getURLs()) {
                        addString(" URL: " + url + "   ");
                    }
                } catch (NullPointerException ex) {
                    addString(" URL: [unknown due to NPE]");
                }
                addString(" Published: " + (service.isPublished() ? "yes" : "no"));
            }
        }
    }

    /**
     * Add communication port information
     */
    private void addCommunicationPortInfo() {
        Enumeration<CommPortIdentifier> portIDs = CommPortIdentifier.getPortIdentifiers();

        ArrayList<CommPortIdentifier> ports = new ArrayList<>();

        // find the names of suitable ports
        while (portIDs.hasMoreElements()) {
            CommPortIdentifier id = portIDs.nextElement();
            // filter out line printers 
            if (id.getPortType() != CommPortIdentifier.PORT_PARALLEL) {
                ports.add(id);
            }
        }

        addString(String.format(" Found %s serial ports", ports.size()));

        // now output the details
        for (CommPortIdentifier id : ports) {
            // output details
            SerialPortFriendlyName port = PortNameMapper.getPortNameMap().get(id.getName());
            if (port == null) {
                port = new SerialPortFriendlyName(id.getName(), null);
                PortNameMapper.getPortNameMap().put(id.getName(), port);
            }
            addString(" Port: " + port.getDisplayName()
                    + (id.isCurrentlyOwned()
                            ? " - in use by: " + id.getCurrentOwner()
                            : " - not in use") + "   ");
        }
    }

}



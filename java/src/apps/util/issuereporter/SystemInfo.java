package apps.util.issuereporter;

import java.awt.*;
import java.io.File;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import jmri.*;
import jmri.configurexml.LoadXmlUserAction;
import jmri.jmrit.audio.AudioFactory;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.roster.Roster;
import jmri.util.gui.GuiLafPreferencesManager;
import jmri.jmrix.ConnectionConfigManager;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.util.FileUtil;
import jmri.util.PortNameMapper;
import jmri.util.PortNameMapper.SerialPortFriendlyName;
import jmri.util.node.NodeIdentity;
import jmri.util.zeroconf.ZeroConfServiceManager;

import purejavacomm.CommPortIdentifier;

/**
 * Provide the JMRI context info.
 * <p>
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2009
 * @author Matt Harris Copyright (C) 2008, 2009
 */
public class SystemInfo {

    private final boolean inBody;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SystemInfo.class);

    SystemInfo(boolean inBody) {
        this.inBody = inBody;
    }

    @Override
    @Nonnull
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (inBody) {
            builder.append("<details>\n<summary>System Info</summary>\n\n");
        }
        asList().forEach(l -> builder.append(l).append("\n"));
        if (inBody) {
            builder.append("</details>\n");
        }
        return builder.toString();
    }

    public List<String> asList() {
        List<String> list = new ArrayList<>();
        list.add("|Item|Value|");
        list.add("|---|---|");

        addLine(list, "JMRI Version", Version.name());
        addLine(list, "Application", Application.getApplicationName());

        Profile profile = ProfileManager.getDefault().getActiveProfile();
        if (profile != null) {
            addLine(list, "Active profile", profile.getName());
            addLine(list, "Profile location", profile.getPath().getPath());
            addLine(list, "Profile ID", profile.getId());
        } else {
            addLine(list, "Active profile", "");
        }

        addLine(list, "JMRI Network ID", NodeIdentity.networkIdentity());
        addLine(list, "JMRI Storage ID", NodeIdentity.storageIdentity(profile));

        addLine(list, "Preferences directory", FileUtil.getUserFilesPath());
        addLine(list, "Program directory", FileUtil.getProgramPath());
        addLine(list, "Roster index", Roster.getDefault().getRosterIndexPath());

        File panel = LoadXmlUserAction.getCurrentFile();
        addLine(list, "Panel file", panel != null ? panel.getPath() : "");

        addLine(list, "Locale", InstanceManager.getDefault(GuiLafPreferencesManager.class).getLocale().toString());

        addLine(list, "Operations location", OperationsXml.getFileLocation());

        InstanceManager.getOptionalDefault(AudioManager.class).ifPresent(am -> {
            AudioFactory af = am.getActiveAudioFactory();
            addLine(list, "Audio factory", af != null ? af.toString() : "");
        });

        InstanceManager.getOptionalDefault(ConnectionConfigManager.class)
                .ifPresent(ccm -> Arrays.stream(ccm.getConnections())
                .forEach(c -> addLine(list,
                "Connection " + c.getConnectionName(),
                c.getManufacturer() + " connected via " + c.name() + " on " + c.getInfo() + (c.getDisabled() ? " (disabled)" : " (enabled)"))));

        addComPortInfo(list);

        System.getProperties().stringPropertyNames().stream().sorted()
                .filter(n -> !n.equals("line.separator"))
                .forEach(n -> addLine(list, n, System.getProperty(n)));

        if (!GraphicsEnvironment.isHeadless()) {
            addLine(list, "FileSystemView#getDefaultDirectory()", FileSystemView.getFileSystemView().getDefaultDirectory().getPath());
            addLine(list, "FileSystemView#getHomeDirectory()", FileSystemView.getFileSystemView().getHomeDirectory().getPath());
            addLine(list, "Default JFileChooser()", new JFileChooser().getCurrentDirectory().getPath());
        }
        addDisplayDimensions(list);

        addNetworkInfo(list);

        return list;
    }

    private void addDisplayDimensions(List<String> list) {
        if (!GraphicsEnvironment.isHeadless()) {
            Arrays.stream(GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices())
                    .forEachOrdered(device -> {
                        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(device.getDefaultConfiguration());
                        addLine(list,
                                "Display " + device.getIDstring(),
                                " size: width " + device.getDisplayMode().getWidth() + " height " + device.getDisplayMode().getHeight()
                                + " insets: top " + insets.top + " right " + insets.right + " bottom " + insets.bottom + " left " + insets.left);
                    });
        } else {
            addLine(list, "Display", "headless");
        }
    }

    private void addNetworkInfo(List<String> list) {
        try {
            Collections.list(NetworkInterface.getNetworkInterfaces()).stream().forEach(ni
                    -> ni.getInterfaceAddresses().forEach(ia
                            -> addLine(list,
                            "Network Interface " + ni.getDisplayName(),
                            ia.getAddress().getHostAddress())));
        } catch (SocketException ex) {
            log.error("Unable to enumerate network interfaces", ex);
        }
        InstanceManager.getDefault(ZeroConfServiceManager.class).allServices().forEach(zcs
                -> addLine(list,
                        "ZeroConf service " + zcs.getKey(),
                        zcs.getServiceInfo().getNiceTextString()));
    }

    private void addComPortInfo(List<String> list) {
        Collections.list(CommPortIdentifier.getPortIdentifiers()).stream()
                .filter(id -> id.getPortType() == CommPortIdentifier.PORT_SERIAL)
                .forEach(id -> {
                    SerialPortFriendlyName name = PortNameMapper.getPortNameMap()
                            .getOrDefault(id.getName(), new SerialPortFriendlyName(id.getName(), null));
                    addLine(list,
                            "Port " + name.getDisplayName(),
                            id.isCurrentlyOwned() ? " in use by " + id.getCurrentOwner() : " not in use");
                });
    }

    private void addLine(List<String> list, String item, String value) {
        list.add(String.format("|%s|%s|", escapePipes(item), escapePipes(value)));
    }

    private String escapePipes(String input) {
        return input.replace("|", "\\|");
    }
}

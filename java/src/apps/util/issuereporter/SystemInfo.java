package apps.util.issuereporter;

import java.awt.*;
import java.io.File;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

import javax.annotation.Nonnull;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import jmri.*;
import jmri.configurexml.LoadXmlUserAction;
import jmri.jmrit.audio.AudioFactory;
import jmri.jmrit.operations.setup.OperationsSetupXml;
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

    private static final String TD = "|";
    private static final String TDNL = "|\n";
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
        builder.append("|Item|Value|\n|---|---|\n");

        builder.append("|JMRI Version|").append(Version.name()).append(TDNL);
        builder.append("|Application|").append(Application.getApplicationName()).append(TDNL);

        Profile profile = ProfileManager.getDefault().getActiveProfile();
        if (profile != null) {
            appendLine(builder, "Active profile", profile.getName());
            appendLine(builder, "Profile location", profile.getPath().getPath());
            appendLine(builder, "Profile ID", profile.getId());
        } else {
            appendLine(builder, "Active profile", "");
        }

        appendLine(builder, "JMRI Network ID", NodeIdentity.networkIdentity());
        appendLine(builder, "JMRI Storage ID", NodeIdentity.storageIdentity(profile));

        appendLine(builder, "Preferences directory", FileUtil.getUserFilesPath());
        appendLine(builder, "Program directory", FileUtil.getProgramPath());
        appendLine(builder, "Roster index", Roster.getDefault().getRosterIndexPath());

        File panel = LoadXmlUserAction.getCurrentFile();
        appendLine(builder, "Panel file", panel != null ? panel.getPath() : "");

        appendLine(builder, "Locale", InstanceManager.getDefault(GuiLafPreferencesManager.class).getLocale().toString());

        appendLine(builder, "Operations location", OperationsSetupXml.getFileLocation());

        InstanceManager.getOptionalDefault(AudioManager.class).ifPresent(am -> {
            AudioFactory af = am.getActiveAudioFactory();
            appendLine(builder, "Audio factory", af != null ? af.toString() : "");
        });

        InstanceManager.getOptionalDefault(ConnectionConfigManager.class).ifPresent(ccm
                -> Arrays.stream(ccm.getConnections()).forEach(c
                        -> builder.append(TD).append("Connection ").append(c.getConnectionName())
                        .append(TD).append(c.getManufacturer()).append(" connected via ").append(c.name()).append(" on ").append(c.getInfo()).append(c.getDisabled() ? " (disabled)" : " (enabled)")
                        .append(TDNL)));

        addComPortInfo(builder);

        System.getProperties().stringPropertyNames().stream().sorted()
                .filter(n -> !n.equals("line.separator"))
                .forEach(n -> appendLine(builder, n, System.getProperty(n)));

        if (!GraphicsEnvironment.isHeadless()) {
            appendLine(builder, "FileSystemView#getDefaultDirectory()", FileSystemView.getFileSystemView().getDefaultDirectory().getPath());
            appendLine(builder, "FileSystemView#getHomeDirectory()", FileSystemView.getFileSystemView().getHomeDirectory().getPath());
            appendLine(builder, "Default JFileChooser()", new JFileChooser().getCurrentDirectory().getPath());
        }
        addDisplayDimensions(builder);

        addNetworkInfo(builder);
        if (inBody) {
            builder.append("</details>\n");
        }
        return builder.toString();
    }

    private void addDisplayDimensions(StringBuilder builder) {
        if (!GraphicsEnvironment.isHeadless()) {
            Arrays.stream(GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices())
                    .forEachOrdered(device -> {
                        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(device.getDefaultConfiguration());
                        builder.append(TD)
                                .append("Display ").append(device.getIDstring()).append(TD)
                                .append(" size: width ").append(device.getDisplayMode().getWidth()).append(" height ").append(device.getDisplayMode().getHeight())
                                .append(" insets: top ").append(insets.top).append(" right ").append(insets.right).append(" bottom ").append(insets.bottom).append(" left ").append(insets.left)
                                .append(TDNL);
                    });
        } else {
            appendLine(builder, "Display", "headless");
        }
    }

    private void addNetworkInfo(StringBuilder builder) {
        try {
            Collections.list(NetworkInterface.getNetworkInterfaces()).stream().forEach(ni -> {
                ni.getInterfaceAddresses().forEach(ia -> {
                    builder.append(TD).append("Network Interface ").append(ni.getDisplayName())
                            .append(TD).append(ia.getAddress().getHostAddress())
                            .append(TDNL);
                });
            });
        } catch (SocketException ex) {
            log.error("Unable to enumerate network interfaces", ex);
        }
        InstanceManager.getDefault(ZeroConfServiceManager.class).allServices().forEach(zcs -> {
            builder.append(TD).append("ZeroConf service ").append(zcs.getKey())
                    .append(TD).append(zcs.getServiceInfo().getNiceTextString())
                    .append(TDNL);
        });
    }

    private void addComPortInfo(StringBuilder builder) {
        Collections.list(CommPortIdentifier.getPortIdentifiers()).stream()
                .filter(id -> id.getPortType() == CommPortIdentifier.PORT_SERIAL)
                .forEach(id -> {
                    SerialPortFriendlyName name = PortNameMapper.getPortNameMap()
                            .getOrDefault(id.getName(), new SerialPortFriendlyName(id.getName(), null));
                    builder.append(TD).append("Port ").append(name.getDisplayName())
                            .append(TD).append(id.isCurrentlyOwned() ? " in use by " : " not in use").append(id.isCurrentlyOwned() ? id.getCurrentOwner() : "")
                            .append(TDNL);
                });
    }

    private void appendLine(StringBuilder builder, String item, String value) {
        builder.append(TD).append(escapePipes(item)).append(TD).append(escapePipes(value)).append(TDNL);
    }

    private String escapePipes(String input) {
        return input.replace("|", "\\|");
    }
}

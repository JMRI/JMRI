/*
 * Configure Zeroconf networking advertisements to use only the IPv4 or IPv6
 * Internet Protocol versions as well as to use link-local or loopback
 * interfaces. This can also effectively disable Zeroconf networking
 * advertisements by deselecting both protocols.
 * 
 * Works with JMRI 4.15.1 or newer.
 */

// Define Java classes
var InstanceManager = Java.type("jmri.InstanceManager");
var Preferences = Java.type("jmri.util.zeroconf.ZeroConfPreferences");
var ZeroConfServiceManager = Java.type("jmri.util.zeroconf.ZeroConfServiceManager");
var JCheckBox = Java.type("javax.swing.JCheckBox");
var JLabel = Java.type("javax.swing.JLabel");
var JPanel = Java.type("javax.swing.JPanel");
var JOptionPane = Java.type("javax.swing.JOptionPane");
var BoxLayout = Java.type("javax.swing.BoxLayout");

// Get preferences
var preferences = InstanceManager.getDefault(ZeroConfServiceManager.class).getPreferences();

// Build dialog
var useIPv4 = new JCheckBox("IPv4", preferences.isUseIPv4());
var useIPv6 = new JCheckBox("IPv6", preferences.isUseIPv6());
var useLinkLocal = new JCheckBox("Link Local", preferences.isUseLinkLocal());
var useLoopback = new JCheckBox("Loopback", preferences.isUseLoopback());
var panel = new JPanel();
panel.layout = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
panel.add(new JLabel("<html>Zeroconf service advertisements will<br>be for the following Internet Protocol versions and interface types:</html>"));
panel.add(useIPv4);
panel.add(useIPv6);
panel.add(useLinkLocal);
panel.add(useLoopback);
panel.add(new JLabel("<html>Note that this is a per-computer setting,<br>and applies to all profiles used on this computer.</html>"));
// Present preferences
if (JOptionPane.OK_OPTION === JOptionPane.showConfirmDialog(
        null,
        panel,
        "Zeroconf Service Advertisements",
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.QUESTION_MESSAGE
        )) {
    // Save preferences
    print("Setting Zeroconf configuration:");
    print("Advertise on IPv4: " + useIPv4.selected);
    print("Advertise on IPv6: " + useIPv6.selected);
    print("Advertise on LinkLocal: " + useLinkLocal.selected);
    print("Advertise on Loopback: " + useLoopback.selected);
    preferences.setUseIPv4(useIPv4.selected);
    preferences.setUseIPv6(useIPv6.selected);
    preferences.setUseLinkLocal(useLinkLocal.selected);
    preferences.setUseLoopback(useLoopback.selected);
} else {
    print("Not changing Zeroconf configuration");
}

/*
 * Configure Zeroconf networking advertisements to use only the IPv4 or IPv6
 * Internet Protocol versions. This can also effectively disable Zeroconf
 * networking advertisements by deselecting both protocols.
 * 
 * Works with JMRI 4.1.2 or newer.
 */

// Define Java classes
var Preferences = Java.type("java.util.prefs.Preferences");
var ProfileManager = Java.type("jmri.profile.ProfileManager");
var ProfileUtils = Java.type("jmri.profile.ProfileUtils");
var ZeroConfService = Java.type("jmri.util.zeroconf.ZeroConfService");
var JCheckBox = Java.type("javax.swing.JCheckBox");
var JLabel = Java.type("javax.swing.JLabel");
var JPanel = Java.type("javax.swing.JPanel");
var JOptionPane = Java.type("javax.swing.JOptionPane");
var BoxLayout = Java.type("javax.swing.BoxLayout");

// Get preferences
var preferences = ProfileUtils.getPreferences(ProfileManager.getDefault().getActiveProfile(),
        ZeroConfService.class,
        false);

// Build dialog
var useIPv4 = new JCheckBox("IPv4", preferences.getBoolean(ZeroConfService.IPv4, true));
var useIPv6 = new JCheckBox("IPv6", preferences.getBoolean(ZeroConfService.IPv6, true));
var panel = new JPanel();
panel.layout = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
panel.add(new JLabel("<html>Zeroconf service advertisements will<br>be for the following Internet Protocol versions:</html>"));
panel.add(useIPv4);
panel.add(useIPv6);
panel.add(new JLabel("<html>Note that this is a per computer,<br>per profile setting.</html>"));
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
    preferences.putBoolean(ZeroConfService.IPv4, useIPv4.selected);
    preferences.putBoolean(ZeroConfService.IPv6, useIPv6.selected);
    preferences.sync();
} else {
    print("Not changing Zeroconf configuration");
}

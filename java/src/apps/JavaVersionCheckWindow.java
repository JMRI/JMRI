package apps;

import javax.swing.JOptionPane;

/**
 * Check Java version during startup and complain if not current enough
 *
 * @author Bob Jacobsen Copyright 2021
 */
public class JavaVersionCheckWindow {

    enum Compatibility {
        SUPPORTED,
        INCOMPATIBLE,
        UNSUPPORTED; // Maybe yes, maybe no
    }
    /**
     * @return true if OK
     */
    static Compatibility checkJavaVersion() {

        String version = System.getProperty("java.version");
        switch (version) {
            case "11":
            case "12":
            case "13":
                return Compatibility.SUPPORTED;
            default:
                if (version.startsWith("1.8")) {
                    return Compatibility.INCOMPATIBLE;
                } else {
                    switch (version) {
                        case "17":
                            return Compatibility.UNSUPPORTED;
                        default:
                            return Compatibility.UNSUPPORTED;
                    }
                }
        }
    }

    public static void main(String[] opts) {
        Compatibility result = checkJavaVersion();
        if (result == Compatibility.INCOMPATIBLE) {
            System.err.println("JMRI cannot run on Java version "+System.getProperty("java.version"));
            JOptionPane.showMessageDialog(null, "JMRI cannot run on Java version "+System.getProperty("java.version"), "Alert", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }
}

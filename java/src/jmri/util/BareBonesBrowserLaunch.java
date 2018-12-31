package jmri.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JOptionPane;

/**
 *
 * @deprecated since 4.5.2. Use {@link java.awt.Desktop#browse(java.net.URI)}
 * instead.
 */
@Deprecated  // 4.5.2
public class BareBonesBrowserLaunch {

    private static final String errMsg = "Error attempting to launch web browser";

    /**
     * Wrapper around {@link java.awt.Desktop#browse(java.net.URI)}. Exceptions
     * display a generic error message. Use Desktop#browse() directly to provide
     * task-specific handling of the inability to open the URL.
     *
     * @param url web page to open
     */
    public static void openURL(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(new URI(url));
        } catch (URISyntaxException | IOException ex) {
            JOptionPane.showMessageDialog(null, errMsg + ":\n" + ex.getLocalizedMessage());
        }
    }

}

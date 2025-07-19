package apps.TrainCrew;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.JPanel;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Install TrainCrew app from a URL
 *
 * @author Bob Jacobsen Copyright (C) 2018
 */
public class InstallFromURL extends JmriAbstractAction {

    public InstallFromURL(String s, WindowInterface wi) {
        super(s, wi);
    }

    public InstallFromURL(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    public InstallFromURL(String s) {
        super(s);
    }

    public InstallFromURL(String s, JPanel who) {
        super(s);
    }

    public InstallFromURL() {
        super(Bundle.getMessage("TrainCrewInstallMenu"));
    }

    JPanel _who;

    @Override
    public void actionPerformed(ActionEvent e) {

        // get the input URL
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("apps.TrainCrew.InstallFromURL");
        String urlString = bundle.getString("TrainCrewZipURL");
        String targetDirectory = bundle.getString("TrainCrewInstallDirectory");
        
        log.info("Will install from {} to {}", urlString, targetDirectory);
        
        try {
            // create URL
            URL url = new URI(urlString).toURL();

            try {
                // open connection
                InputStream inStream = url.openConnection().getInputStream();
                
                // transfer and unpack
                jmri.util.UnzipFileClass.unzipFunction(new File(targetDirectory), inStream);
                
                log.info("Complete!");
            } catch (java.io.IOException ex) {
                log.error("Error in transfer", ex);
            }
        } catch (java.net.MalformedURLException | java.net.URISyntaxException ex) {
            log.error("Invalid URL", ex);
        }
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(InstallFromURL.class);

}

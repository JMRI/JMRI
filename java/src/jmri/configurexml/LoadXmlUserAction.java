// LoadXmlConfigAction.java
package jmri.configurexml;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load configuration information from an XML file.
 * <P>
 * The file context for this is the "user" file chooser.
 * <P>
 * This will load whatever information types are present in the file. See
 * {@link jmri.ConfigureManager} for information on the various types of
 * information stored in configuration files.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @version $Revision$
 * @see jmri.jmrit.XmlFile
 */
public class LoadXmlUserAction extends LoadXmlConfigAction {

    /**
     *
     */
    private static final long serialVersionUID = 5470543428367047464L;
    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");

    public LoadXmlUserAction() {
        this(rb.getString("MenuItemLoad"));
    }

    public LoadXmlUserAction(String s) {
        super(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String oldButtonText = userFileChooser.getApproveButtonText();
        String oldDialogTitle = userFileChooser.getDialogTitle();
        int oldDialogType = userFileChooser.getDialogType();
        userFileChooser.setDialogType(javax.swing.JFileChooser.OPEN_DIALOG);
        userFileChooser.setApproveButtonText(rb.getString("MenuItemLoad"));
        userFileChooser.setDialogTitle(rb.getString("MenuItemLoad"));

        boolean results = loadFile(userFileChooser);
        log.debug(results ? "load was successful" : "load failed");
        if (!results) {
            JOptionPane.showMessageDialog(null,
                    rb.getString("PanelHasErrors") + "\n"
                    + rb.getString("CheckPreferences") + "\n"
                    + rb.getString("ConsoleWindowHasInfo"),
                    rb.getString("PanelLoadError"), JOptionPane.ERROR_MESSAGE);
        }

        // The last thing we do is restore the Approve button text.
        userFileChooser.setDialogType(oldDialogType);
        userFileChooser.setApproveButtonText(oldButtonText);
        userFileChooser.setDialogTitle(oldDialogTitle);
    }

    public static File getCurrentFile() {
        return userFileChooser.getSelectedFile();
    }

    // initialize logging
    static Logger log = LoggerFactory.getLogger(LoadXmlUserAction.class.getName());

}

// FileLocationPane.java

package apps;

import java.awt.FlowLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;

/**
 * Provide GUI to configure Swing GUI LAF defaults
 * <P>
 * Provides GUI configuration for SWING LAF by
 * displaying radiobuttons for each LAF implementation available.
 * This information is then persisted separately
 * (e.g. by {@link jmri.configurexml.FileLocationPaneXml})
 * <P>
 * Locale default language and country is also considered a
 * GUI (and perhaps LAF) configuration item.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003, 2010
 * @version	$Revision: 1.1 $
 * @since 2.9.5  (Previously in jmri package)
 */
public class FileLocationPane extends JPanel {

    public FileLocationPane() {
    
        JButton bScript = new JButton("Set");
        final JFileChooser fcScript;
        fcScript = new JFileChooser(jmri.jmrit.XmlFile.scriptsDir());

        fcScript.setDialogTitle("Select Directory");
        fcScript.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fcScript.setAcceptAllFileFilterUsed(false);
        bScript.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // get the file
                fcScript.showOpenDialog(null);
                if (fcScript.getSelectedFile()==null) return; // cancelled
                scriptLocation.setText(fcScript.getSelectedFile()+File.separator);
                validate();
                if (getTopLevelAncestor()!=null) ((JFrame)getTopLevelAncestor()).pack();
            }
        });
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        JLabel scripts = new JLabel("Jython Script");
        p.add(scripts);
        p.add(scriptLocation);
        p.add(bScript);
        scriptLocation.setColumns(20);
        scriptLocation.setText(jmri.jmrit.XmlFile.scriptsDir());
        add(p);
        
        p = new JPanel();
        JLabel users = new JLabel("User File Locations");
        p.add(users);
        p.add(userLocation);
        userLocation.setColumns(20);
        userLocation.setText(jmri.jmrit.XmlFile.userFileLocationDefault());
        
        JButton bUser = new JButton("Set");
        final JFileChooser fcUser;
        fcUser = new JFileChooser(jmri.jmrit.XmlFile.userFileLocationDefault());

        fcUser.setDialogTitle("Select Directory");
        fcUser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fcUser.setAcceptAllFileFilterUsed(false);
        bUser.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // get the file
                fcUser.showOpenDialog(null);
                if (fcUser.getSelectedFile()==null) return; // cancelled
                userLocation.setText(fcUser.getSelectedFile()+File.separator);
                validate();
                if (getTopLevelAncestor()!=null) ((JFrame)getTopLevelAncestor()).pack();
            }
        });
        p.add(bUser);
        add(p);
        
        /*p = new JPanel();
        JLabel throttle = new JLabel("Default Throttle Location");
        p.add(throttle);
        p.add(throttleLocation);
        throttleLocation.setColumns(20);
        throttleLocation.setText(jmri.jmrit.throttle.ThrottleFrame.getDefaultThrottleFolder());
        add(p);*/
        
    }
    
    public static void save(){
        jmri.jmrit.XmlFile.setScriptsFileLocationDefault(scriptLocation.getText());
        jmri.jmrit.XmlFile.setUserFileLocationDefault(userLocation.getText());
        //jmri.jmrit.throttle.ThrottleFrame.setDefaultThrottleLocation(throttleLocation.getText());
    }
    
    protected static JTextField scriptLocation = new JTextField();
    protected static JTextField userLocation = new JTextField();
    protected static JTextField throttleLocation = new JTextField();

}


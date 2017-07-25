package jmri.jmrit;

import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import jmri.jmrit.roster.Roster;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to display the JMRI directory locations.
 * <P>
 * Although this has "XML" in it's name, it's actually much more general. It
 * displays: <ul>
 * <li>The preferences directory <li>The program directory <li>and any log files
 * seen in the program directory </ul>
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2007
 */
public class XmlFileLocationAction extends AbstractAction {

    public XmlFileLocationAction() {
        super();
    }

    final static String user = FileUtil.getUserFilesPath();
    final static String roster = Roster.getDefault().getRosterLocation();
    final static String profile = FileUtil.getProfilePath();
    final static String settings = FileUtil.getPreferencesPath();
    final static String scripts = FileUtil.getScriptsPath();
    final static String prog = System.getProperty("user.dir");
    final static String logDir = System.getProperty("jmri.log.path");
    final static String tmpDir = System.getProperty("java.io.tmpdir");


    @Override
    public void actionPerformed(ActionEvent ev) {

        JFrame frame = new jmri.util.JmriJFrame();  // to ensure fits

        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout());
        pane.add(buttons);

        JButton b = new JButton("Open User Files Location");
        buttons.add(b);
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    Desktop.getDesktop().open(new File(user));
                } catch (IOException e) {
                    XmlFileLocationAction.log.error("Error when opening user files location: " + e);
                } catch (UnsupportedOperationException e) {
                    XmlFileLocationAction.log.error("Error when opening user files location: " + e);
                }
            }
        });
        b = new JButton("Open Roster Location");
        buttons.add(b);
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    Desktop.getDesktop().open(new java.io.File(roster));
                } catch (java.io.IOException e) {
                    XmlFileLocationAction.log.error("Error when opening roster location: " + e);
                } catch (UnsupportedOperationException e) {
                    XmlFileLocationAction.log.error("Error when opening roster location: " + e);
                }
            }
        });
        b = new JButton("Open Profile Location");
        buttons.add(b);
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    Desktop.getDesktop().open(new java.io.File(profile));
                } catch (java.io.IOException e) {
                    XmlFileLocationAction.log.error("Error when opening profile location: " + e);
                } catch (UnsupportedOperationException e) {
                    XmlFileLocationAction.log.error("Error when opening profile location: " + e);
                }
            }
        });
        b = new JButton("Open Settings Location");
        buttons.add(b);
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    Desktop.getDesktop().open(new java.io.File(settings));
                } catch (java.io.IOException e) {
                    XmlFileLocationAction.log.error("Error when opening settings location: " + e);
                } catch (UnsupportedOperationException e) {
                    XmlFileLocationAction.log.error("Error when opening settings location: " + e);
                }
            }
        });
        b = new JButton("Open Scripts Location");
        buttons.add(b);
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    Desktop.getDesktop().open(new java.io.File(scripts));
                } catch (java.io.IOException e) {
                    XmlFileLocationAction.log.error("Error when opening scripts location: " + e);
                } catch (UnsupportedOperationException e) {
                    XmlFileLocationAction.log.error("Error when opening scripts location: " + e);
                }
            }
        });
        b = new JButton("Open Program Location");
        buttons.add(b);
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    Desktop.getDesktop().open(new java.io.File(prog));
                } catch (java.io.IOException e) {
                    XmlFileLocationAction.log.error("Error when opening program location: " + e);
                } catch (UnsupportedOperationException e) {
                    XmlFileLocationAction.log.error("Error when opening program location: " + e);
                }
            }
        });

        b = new JButton("Open Log Files Location");
        buttons.add(b);
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    Desktop.getDesktop().open(new java.io.File(logDir));
                } catch (java.io.IOException e) {
                    XmlFileLocationAction.log.error("Error when opening log files location: " + e);
                } catch (UnsupportedOperationException e) {
                    XmlFileLocationAction.log.error("Error when opening log files location: " + e);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(pane);
        frame.getContentPane().add(scroll);

        JTextArea textPane = new javax.swing.JTextArea();
        textPane.setEditable(false);
        pane.add(textPane);

        textPane.append(getLocationsReport());

        frame.pack();
        frame.setVisible(true);
    }

    //return a text string listing the various locations and filenames of interest
    public static String getLocationsReport() {

        String configName = System.getProperty("org.jmri.Apps.configFilename");
        if (!new File(configName).isAbsolute()) {
            // must be relative, but we want it to 
            // be relative to the preferences directory
            configName = profile + configName;
        }
        
        StringBuffer s = new StringBuffer();       
        s.append("User Files Location: ").append(user).append("\n");
        s.append("Roster Location: ").append(roster).append("\n");
        s.append("Profile Location: ").append(profile).append("\n");
        s.append("Settings Location: ").append(settings).append("\n");
        s.append("Current Config file: ").append(configName).append("\n");
        s.append("Scripts Location: ").append(scripts).append("\n");
        s.append("Program Location: ").append(prog).append("\n");
        s.append("Temp Files Location: ").append(tmpDir).append("\n");
        s.append("Log Files Location: ").append(logDir).append("\n");
        
        //include names of any *.log files in log folder
        File dir = new File(logDir);
        String[] files = dir.list();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].indexOf(".log") != -1) {
                    s.append("  ").append(logDir).append(files[i]).append("\n");
                }
            }
        }
        return s.toString();
    }

    private static final Logger log = LoggerFactory.getLogger(XmlFileLocationAction.class.getName());
}

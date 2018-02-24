package jmri.jmrit;

import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
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
 * <p>
 * Although this has "XML" in its name, it's actually much more general. It
 * displays:
 * <ul>
 * <li>The user files and profiles directories
 * <li>The roster directory
 * <li>The preferences directory
 * <li>The program directory
 * <li>and any log files seen in the program directory
 * </ul>
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2007
 */
public class XmlFileLocationAction extends AbstractAction {

    public XmlFileLocationAction() {
        super();
    }

    @Override
    public void actionPerformed(ActionEvent ev) {

        JFrame frame = new jmri.util.JmriJFrame(); // to ensure fits

        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout());
        pane.add(buttons);

        JButton b = new JButton(Bundle.getMessage("ButtonOpenLocX",
                Bundle.getMessage("ButtonUserFilesLoc")));
        buttons.add(b);
        b.addActionListener((ActionEvent event) -> {
            try {
                Desktop.getDesktop().open(new File(FileUtil.getUserFilesPath()));
            } catch (IOException | UnsupportedOperationException e) {
                log.error("Error when opening user files location: ", e);
            }
        });
        b = new JButton(Bundle.getMessage("ButtonOpenLocX",
                Bundle.getMessage("ButtonRosterLoc")));
        buttons.add(b);
        b.addActionListener((ActionEvent event) -> {
            try {
                Desktop.getDesktop().open(new java.io.File(Roster.getDefault().getRosterLocation()));
            } catch (java.io.IOException | UnsupportedOperationException e) {
                log.error("Error when opening roster location: ", e);
            }
        });
        b = new JButton(Bundle.getMessage("ButtonOpenLocX",
                Bundle.getMessage("ButtonProfileLoc")));
        buttons.add(b);
        b.addActionListener((ActionEvent event) -> {
            try {
                Desktop.getDesktop().open(new java.io.File(FileUtil.getProfilePath()));
            } catch (java.io.IOException | UnsupportedOperationException e) {
                XmlFileLocationAction.log.error("Error when opening profile location: ", e);
            }
        });
        b = new JButton(Bundle.getMessage("ButtonOpenLocX",
                Bundle.getMessage("ButtonSettingsLoc")));
        buttons.add(b);
        b.addActionListener((ActionEvent event) -> {
            try {
                Desktop.getDesktop().open(new java.io.File(FileUtil.getPreferencesPath()));
            } catch (java.io.IOException | UnsupportedOperationException e) {
                log.error("Error when opening settings location: ", e);
            }
        });
        b = new JButton(Bundle.getMessage("ButtonOpenLocX",
                Bundle.getMessage("ButtonScriptsLoc")));
        buttons.add(b);
        b.addActionListener((ActionEvent event) -> {
            try {
                Desktop.getDesktop().open(new java.io.File(FileUtil.getScriptsPath()));
            } catch (java.io.IOException | UnsupportedOperationException e) {
                log.error("Error when opening scripts location: ", e);
            }
        });
        b = new JButton(Bundle.getMessage("ButtonOpenLocX",
                Bundle.getMessage("ButtonProgramLoc")));
        buttons.add(b);
        b.addActionListener((ActionEvent event) -> {
            try {
                Desktop.getDesktop().open(new java.io.File(System.getProperty("user.dir")));
            } catch (java.io.IOException | UnsupportedOperationException e) {
                log.error("Error when opening program location: ", e);
            }
        });

        b = new JButton(Bundle.getMessage("ButtonOpenLocX",
                Bundle.getMessage("ButtonLogFilesLoc")));
        buttons.add(b);
        b.addActionListener((ActionEvent event) -> {
            try {
                Desktop.getDesktop().open(new java.io.File(System.getProperty("jmri.log.path")));
            } catch (java.io.IOException | UnsupportedOperationException e) {
                log.error("Error when opening log files location: ", e);
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
        String logDir = System.getProperty("jmri.log.path");

        String configName = System.getProperty("org.jmri.Apps.configFilename");
        if (!new File(configName).isAbsolute()) {
            // must be relative, but we want it to
            // be relative to the preferences directory
            configName = FileUtil.getProfilePath() + configName;
        }

        StringBuilder s = new StringBuilder();
        s.append(Bundle.getMessage("ButtonUserFilesLoc") + ": ").append(FileUtil.getUserFilesPath()).append("\n");
        s.append(Bundle.getMessage("ButtonRosterLoc") + ": ").append(Roster.getDefault().getRosterLocation()).append("\n");
        s.append(Bundle.getMessage("ButtonProfileLoc") + ": ").append(FileUtil.getProfilePath()).append("\n");
        s.append(Bundle.getMessage("ButtonSettingsLoc") + ": ").append(FileUtil.getPreferencesPath()).append("\n");
        s.append(Bundle.getMessage("CurrentConfig")).append(configName).append("\n");
        s.append(Bundle.getMessage("ButtonScriptsLoc") + ": ").append(FileUtil.getScriptsPath()).append("\n");
        s.append(Bundle.getMessage("ButtonProgramLoc") + ": ").append(System.getProperty("user.dir")).append("\n");
        s.append(Bundle.getMessage("TempFilesLoc")).append(System.getProperty("java.io.tmpdir")).append("\n");
        s.append(Bundle.getMessage("ButtonLogFilesLoc") + ": ").append(logDir).append("\n");

        //include names of any *.log files in log folder
        File dir = new File(logDir);
        String[] files = dir.list();
        if (files != null) {
            for (String file : files) {
                if (file.contains(".log")) {
                    s.append("  ").append(logDir).append(file).append("\n");
                }
            }
        }
        return s.toString();
    }

    private static final Logger log = LoggerFactory.getLogger(XmlFileLocationAction.class);

}

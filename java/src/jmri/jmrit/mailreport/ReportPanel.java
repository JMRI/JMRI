package jmri.jmrit.mailreport;

import apps.PerformFileModel;
import apps.StartupActionsManager;
import java.awt.FlowLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.util.MultipartMessage;
import jmri.util.javaworld.GridLayout2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User interface for sending a problem report via email.
 * <p>
 * The report is sent to a dedicated SourceForge mailing list, from which people
 * can retrieve it.
 *
 * @author Bob Jacobsen Copyright (C) 2009
 * @author Matthew Harris Copyright (c) 2014
 */
public class ReportPanel extends JPanel {

    JButton sendButton;
    JTextField emailField = new JTextField(40);
    JTextField summaryField = new JTextField(40);
    JTextArea descField = new JTextArea(8, 40);
    JCheckBox checkContext;
    JCheckBox checkNetwork;
    JCheckBox checkLog;
    JCheckBox checkPanel;
    JCheckBox checkProfile;
    JCheckBox checkCopy;

    // Define which profile sub-directories to include
    // In lowercase as I was too lazy to do a proper case-insensitive check...
    String[] profDirs = {"networkservices", "profile", "programmers", "throttle"};

    public ReportPanel() {
        ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.jmrit.mailreport.ReportBundle");

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel p1;

        p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        p1.add(new JLabel(rb.getString("LabelTop")));
        add(p1);

        // grid of options
        p1 = new JPanel();
        p1.setLayout(new GridLayout2(3, 2));
        add(p1);

        JLabel l = new JLabel(rb.getString("LabelEmail"));
        l.setToolTipText(rb.getString("TooltipEmail"));
        p1.add(l);
        emailField.setToolTipText(rb.getString("TooltipEmail"));
        p1.add(emailField);

        l = new JLabel(rb.getString("LabelSummary"));
        l.setToolTipText(rb.getString("TooltipSummary"));
        p1.add(l);
        summaryField.setToolTipText(rb.getString("TooltipSummary"));
        p1.add(summaryField);

        l = new JLabel(rb.getString("LabelDescription"));
        p1.add(l);
        // This ensures that the long-description JTextArea font
        // is the same as the JTextField fields.
        // With some L&F, default font for JTextArea differs.
        descField.setFont(summaryField.getFont());
        descField.setBorder(summaryField.getBorder());
        descField.setLineWrap(true);
        descField.setWrapStyleWord(true);
        p1.add(descField);

        // buttons on bottom
        p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        checkContext = new JCheckBox(rb.getString("CheckContext"));
        checkContext.setSelected(true);
        checkContext.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                checkNetwork.setEnabled(checkContext.isSelected());
            }
        });
        p1.add(checkContext);

        checkNetwork = new JCheckBox(rb.getString("CheckNetwork"));
        checkNetwork.setSelected(true);
        p1.add(checkNetwork);

        checkLog = new JCheckBox(rb.getString("CheckLog"));
        checkLog.setSelected(true);
        p1.add(checkLog);
        add(p1);

        p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        checkPanel = new JCheckBox(rb.getString("CheckPanel"));
        checkPanel.setSelected(true);
        p1.add(checkPanel);

        checkProfile = new JCheckBox(rb.getString("CheckProfile"));
        checkProfile.setSelected(true);
        p1.add(checkProfile);

        checkCopy = new JCheckBox(rb.getString("CheckCopy"));
        checkCopy.setSelected(true);
        p1.add(checkCopy);
        add(p1);

        sendButton = new javax.swing.JButton(rb.getString("ButtonSend"));
        sendButton.setToolTipText(rb.getString("TooltipSend"));
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                sendButtonActionPerformed(e);
            }
        });
        add(sendButton);
    }
    
    // made static, public, not final so can be changed via script
    static public String requestURL = "http://jmri.org/problem-report.php";  //NOI18N

    @SuppressWarnings("unchecked")
    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.mailreport.ReportBundle");
        try {
            sendButton.setEnabled(false);
            log.debug("initial checks");
            InternetAddress email = new InternetAddress(emailField.getText());
            email.validate();

            log.debug("start send");
            String charSet = "UTF-8";  //NO18N

            MultipartMessage msg = new MultipartMessage(requestURL, charSet);

            // add reporter email address
            log.debug("start creating message");
            msg.addFormField("reporter", emailField.getText());

            // add if to Cc sender
            msg.addFormField("sendcopy", checkCopy.isSelected() ? "yes" : "no");

            // add problem summary
            msg.addFormField("summary", summaryField.getText());

            // build detailed error report (include context if selected)
            String report = descField.getText() + "\r\n";
            if (checkContext.isSelected()) {
                report += "=========================================================\r\n"; //NOI18N
                report += (new ReportContext()).getReport(checkNetwork.isSelected() && checkNetwork.isEnabled());
            }
            msg.addFormField("problem", report);

            log.debug("start adding attachments");
            // add panel file if OK
            if (checkPanel.isSelected()) {
                log.debug("prepare panel attachment");
                // Check that some startup panel files have been loaded
                for (PerformFileModel m : InstanceManager.getDefault(StartupActionsManager.class).getActions(PerformFileModel.class)) {
                    String fn = m.getFileName();
                    File f = new File(fn);
                    log.info("Add panel file loaded at startup: {}", f);
                    msg.addFilePart("logfileupload[]", f);
                }
                // Check that a manual panel file has been loaded
                File file = jmri.configurexml.LoadXmlUserAction.getCurrentFile();
                if (file != null) {
                    log.info("Adding manually-loaded panel file: {}", file.getPath());
                    msg.addFilePart("logfileupload[]", jmri.configurexml.LoadXmlUserAction.getCurrentFile());
                } else {
                    // No panel file loaded by manual action
                    log.debug("No panel file manually loaded");
                }
            }

            // add profile files if OK
            if (checkProfile.isSelected()) {
                log.debug("prepare profile attachment");
                // Check that a profile has been loaded
                Profile profile = ProfileManager.getDefault().getActiveProfile();
                if (profile != null) {
                    File file = profile.getPath();
                    if (file != null) {
                        log.debug("add profile: {}", file.getPath());
                        // Now zip-up contents of profile
                        // Create temp file that will be deleted when Java quits
                        File temp = File.createTempFile("profile", ".zip");
                        temp.deleteOnExit();

                        FileOutputStream out = new FileOutputStream(temp);
                        ZipOutputStream zip = new ZipOutputStream(out);

                        addDirectory(zip, file);

                        zip.close();
                        out.close();

                        msg.addFilePart("logfileupload[]", temp);
                    }
                } else {
                    // No profile loaded
                    log.warn("No profile loaded - not sending");
                }
            }

            // add the log if OK
            if (checkLog.isSelected()) {
                log.debug("prepare log attachments");
                // search for an appender that stores a file
                for (java.util.Enumeration<org.apache.log4j.Appender> en = org.apache.log4j.Logger.getRootLogger().getAllAppenders(); en.hasMoreElements();) {
                    // does this have a file?
                    org.apache.log4j.Appender a = en.nextElement();
                    // see if it's one of the ones we know
                    if (log.isDebugEnabled()) {
                        log.debug("check appender {}", a);
                    }
                    try {
                        org.apache.log4j.FileAppender f = (org.apache.log4j.FileAppender) a;
                        log.debug("find file: {}", f.getFile());
                        msg.addFilePart("logfileupload[]", new File(f.getFile()), "application/octet-stream");
                    } catch (ClassCastException ex) {
                    }
                }
            }
            log.debug("done adding attachments");

            // finalise and get server response (if any)
            log.debug("posting report...");
            List<String> response = msg.finish();
            log.debug("send complete");
            log.debug("server response:");
            boolean checkResponse = false;
            for (String line : response) {
                log.debug("               :{}", line);
                if (line.contains("<p>Message successfully sent!</p>")) {
                    checkResponse = true;
                }
            }

            if (checkResponse) {
                JOptionPane.showMessageDialog(null, rb.getString("InfoMessage"), rb.getString("InfoTitle"), JOptionPane.INFORMATION_MESSAGE);
                // close containing Frame
                getTopLevelAncestor().setVisible(false);
            } else {
                JOptionPane.showMessageDialog(null, rb.getString("ErrMessage"), rb.getString("ErrTitle"), JOptionPane.ERROR_MESSAGE); // TODO add Bundle to folder and use ErrorTitle key in NamedBeanBundle props
                sendButton.setEnabled(true);
            }

        } catch (IOException ex) {
            log.error("Error when attempting to send report: " + ex);
            sendButton.setEnabled(true);
        } catch (AddressException ex) {
            log.error("Invalid email address: " + ex);
            JOptionPane.showMessageDialog(null, rb.getString("ErrAddress"), rb.getString("ErrTitle"), JOptionPane.ERROR_MESSAGE); // TODO add Bundle to folder and use ErrorTitle key in NamedBeanBundle props
            sendButton.setEnabled(true);
        }
    }

    private void addDirectory(ZipOutputStream out, File source) {
        log.debug("Add profile: {}", source.getName());
        addDirectory(out, source, "");
    }

    private void addDirectory(ZipOutputStream out, File source, String directory) {
        // get directory contents
        File[] files = source.listFiles();

        log.debug("Add directory: {}", directory);

        for (File file : files) {
            // if current file is a directory, call recursively
            if (file.isDirectory()) {
                // Only include certain sub-directories
                if (!directory.equals("") || Arrays.asList(profDirs).contains(file.getName().toLowerCase())) {
                    try {
                        out.putNextEntry(new ZipEntry(directory + file.getName() + "/"));
                    } catch (IOException ex) {
                        log.error("Exception when adding directory: " + ex);
                    }
                    addDirectory(out, file, directory + file.getName() + "/");
                } else {
                    log.debug("Skipping: {}{}", directory, file.getName());
                }
                continue;
            }
            // Got here - add file
            try {
                // Only include certain files
                if (!directory.equals("") || file.getName().toLowerCase().matches(".*(config\\.xml|\\.properties)")) {
                    log.debug("Add file: {}{}", directory, file.getName());
                    byte[] buffer = new byte[1024];
                    try (FileInputStream in = new FileInputStream(file)) {
                        out.putNextEntry(new ZipEntry(directory + file.getName()));

                        int length;
                        while ((length = in.read(buffer)) > 0) {
                            out.write(buffer, 0, length);
                        }
                        out.closeEntry();
                        in.close();
                    }
                } else {
                    log.debug("Skip file: {}{}", directory, file.getName());
                }
            } catch (FileNotFoundException ex) {
                log.error("Exception when adding file: " + ex);
            } catch (IOException ex) {
                log.error("Exception when adding file: " + ex);
            }
        }
    }

    private static final Logger log = LoggerFactory.getLogger(ReportPanel.class);
}

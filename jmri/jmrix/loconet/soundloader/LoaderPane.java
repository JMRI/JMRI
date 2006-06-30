// LoaderPane.java

package jmri.jmrix.loconet.soundloader;

import java.awt.FlowLayout;

import javax.swing.*;
import java.util.ResourceBundle;
import jmri.jmrix.loconet.*;
import jmri.jmrix.loconet.spjfile.SpjFile;

import java.io.*;

/**
 * Pane for downloading .hex files
 * @author	    Bob Jacobsen   Copyright (C) 2005
 * @version	    $Revision: 1.1 $
 */
public class LoaderPane extends javax.swing.JPanel {

    // GUI member declarations
    static ResourceBundle res = ResourceBundle.getBundle("jmri.jmrix.loconet.downloader.Loader");

    JLabel inputFileName = new JLabel("");

    JButton readButton;
    JButton loadButton;

    JLabel  comment = new JLabel("");
    
    JProgressBar    bar;
    JLabel          status = new JLabel("");

    public LoaderPane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            JButton b = new JButton(res.getString("ButtonSelect"));
            b.addActionListener(new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    selectInputFile();
                }
            });
            p.add(b);
            p.add(new JLabel(res.getString("LabelInpFile")));
            p.add(inputFileName);

            add(p);
        }

        add(new JSeparator());


        {
            JPanel p = new JPanel();
            p.setLayout(new FlowLayout());

            readButton = new JButton(res.getString("ButtonRead"));
            readButton.setEnabled(false);
            readButton.setToolTipText(res.getString("TipReadDisabled"));
            p.add(readButton);
            readButton.addActionListener(new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    doRead();
                }
            });

            add(p);
        }

        {
            JPanel p = new JPanel();
            p.setLayout(new FlowLayout());

            p.add(new JLabel("file comment: "));
            p.add(comment);
            add(p);
        }

        add(new JSeparator());

        {
            JPanel p = new JPanel();
            p.setLayout(new FlowLayout());

            loadButton = new JButton(res.getString("ButtonLoad"));
            loadButton.setEnabled(false);
            loadButton.setToolTipText(res.getString("TipLoadDisabled"));
            p.add(loadButton);
            loadButton.addActionListener(new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    doLoad();
                }
            });

            add(p);

            add(new JSeparator());

            bar = new JProgressBar();
            add(bar);

            add(new JSeparator());

            {
                p = new JPanel();
                p.setLayout(new FlowLayout());
                status.setText(res.getString("StatusSelectFile"));
                status.setAlignmentX(JLabel.LEFT_ALIGNMENT);
                p.add(status);
                add(p);
            }

        }
    }

    void selectInputFile() {
        String name = inputFileName.getText();
        if (name.equals("")) {
            name = System.getProperty("user.dir");
        }
        JFileChooser chooser = new JFileChooser(name);
        int retVal = chooser.showOpenDialog(this);
        if (retVal != JFileChooser.APPROVE_OPTION) return;  // give up if no file selected
        inputFileName.setText(chooser.getSelectedFile().getPath());
        readButton.setEnabled(true);
        readButton.setToolTipText(res.getString("TipReadEnabled"));
        loadButton.setEnabled(false);
        loadButton.setToolTipText(res.getString("TipLoadDisabled"));
        status.setText(res.getString("StatusReadFile"));
    }

    void doRead() {
        if (inputFileName.getText() == "") {
            JOptionPane.showMessageDialog(this, res.getString("ErrorNoInputFile"),
                                      res.getString("ErrorTitle"),
                                      JOptionPane.ERROR_MESSAGE);
            return;
        }

        // force load, verify disabled in case read fails
        loadButton.setEnabled(false);
        loadButton.setToolTipText(res.getString("TipLoadDisabled"));


        SpjFile file;
        
        try {
            file = new SpjFile(inputFileName.getText());
            file.read();
        } catch (FileNotFoundException f) {
            JOptionPane.showMessageDialog(this, res.getString("ErrorFileNotFound"),
                                      res.getString("ErrorTitle"),
                                      JOptionPane.ERROR_MESSAGE);
            return;
        } catch (IOException f) {
            JOptionPane.showMessageDialog(this, res.getString("ErrorIOError"),
                                      res.getString("ErrorTitle"),
                                      JOptionPane.ERROR_MESSAGE);
            return;
        }

        // display contents
        comment.setText(file.getComment());
        
        // set up for next step
        loadButton.setEnabled(true);
        loadButton.setToolTipText(res.getString("TipLoadEnabled"));
        status.setText(res.getString("StatusDoDownload"));

    }

    void doLoad() {
        status.setText(res.getString("StatusDownloading"));
        readButton.setEnabled(false);
        readButton.setToolTipText(res.getString("TipDisabledDownload"));
        loadButton.setEnabled(false);
        loadButton.setToolTipText(res.getString("TipDisabledDownload"));

        // start the download itself
    }


    /**
     * get rid of any held resources
     */
    void dispose() {
    }


    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LoaderPane.class.getName());

}

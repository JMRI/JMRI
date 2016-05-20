// LoaderPane.java

package jmri.jmrix.loconet.soundloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.FlowLayout;

import javax.swing.*;
import java.util.ResourceBundle;
import jmri.jmrix.loconet.spjfile.SpjFile;

import java.io.*;
import jmri.util.FileUtil;

/**
 * Pane for downloading .hex files
 * @author	    Bob Jacobsen   Copyright (C) 2005
 * @version	    $Revision$
 */
public class LoaderPane extends jmri.jmrix.loconet.swing.LnPanel {

    // GUI member declarations
    static ResourceBundle res = ResourceBundle.getBundle("jmri.jmrix.loconet.soundloader.Loader");

    JLabel inputFileName = new JLabel("");

    JButton readButton;
    JButton loadButton;

    JTextField  comment = new JTextField(32);
    
    JProgressBar    bar;
    JLabel          status = new JLabel("");
    String          statusText = "";
    
    SpjFile file;
    LoaderEngine engine;
    
    public String getHelpTarget() { return "package.jmri.jmrix.loconet.soundloader.LoaderFrame"; }
    public String getTitle() { 
        return getTitle(jmri.jmrix.loconet.LocoNetBundle.bundle().getString("MenuItemSoundload")); 
    }
    
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

            p.add(new JLabel(res.getString("LabelFileComment")));
            comment.setEditable(false);
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

    JFileChooser chooser;

    void selectInputFile() {
        String name = inputFileName.getText();
        if (name.equals("")) {
            name = FileUtil.getUserFilesPath();
        }
        if (chooser == null) chooser = new JFileChooser(name);
        inputFileName.setText("");  // clear out in case of failure
        int retVal = chooser.showOpenDialog(this);
        if (retVal != JFileChooser.APPROVE_OPTION) return;  // give up if no file selected
        inputFileName.setText(chooser.getSelectedFile().getName());

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
        
        try {
            file = new SpjFile(chooser.getSelectedFile());
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

        // Create a loader to run in a separate thread
        // Override notify() method to do a swing-thread update of status field
        if (engine == null) engine = new LoaderEngine(memo){
            public void notify(String s) {
                javax.swing.SwingUtilities.invokeLater(new Notifier(s));
            }
        };
        
        // start the download itself
        new Thread() {
            public void run() {
                engine.runDownload(file);
            }
        }.start();
        
    }
    
    
    /**
     * Define objects to update status JLabel in pane
     */
    private class Notifier implements Runnable {
        public Notifier(String msg) {this.msg = msg;}
        String msg;
        public void run() {
            status.setText(msg);
        }
    }

    /**
     * Get rid of any held resources
     */
    public void dispose() {
        if (file!=null) file.dispose();
        file = null;  // not for GC, this flags need to reinit
        
        if (engine!=null) engine.dispose();
        engine = null;  // not for GC, this flags need to reinit
    }


    static Logger log = LoggerFactory.getLogger(LoaderPane.class.getName());

}

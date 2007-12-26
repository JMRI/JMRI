// EditorFrame.java

package jmri.jmrix.loconet.soundloader;

import java.awt.Dimension;
import java.util.ResourceBundle;
import java.io.IOException;

import javax.swing.*;
import java.awt.*;

import jmri.util.JmriJFrame;

/**
 * Frame for editing Digitrax SPJ files.
 *
 * This is just an enclosure for the EditorPane, which does the real work.
 * 
 * This handles file read/write.
 *
 * @author		Bob Jacobsen   Copyright (C) 2006
 * @version             $Revision: 1.6 $
 */
public class EditorFrame extends JmriJFrame {

    // GUI member declarations
    EditorPane pane;

    ResourceBundle res;
    JButton open;
    JButton save;
    
    public EditorFrame() {
        super(ResourceBundle.getBundle("jmri.jmrix.loconet.soundloader.Editor").getString("TitleEditor"));
        
        // Its unfortunate that we have to read that bundle twice, but it's due to Java init order
        res = ResourceBundle.getBundle("jmri.jmrix.loconet.soundloader.Editor");

        // general GUI config
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        
        // add file button
        open = new JButton(res.getString("ButtonOpen"));
        open.addActionListener(new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                selectInputFile();
            }
        });

        save = new JButton(res.getString("ButtonSave"));
        save.addActionListener(new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                selectSaveFile();
            }
        });
        getContentPane().add(save);
        save.setEnabled(false);

        JPanel p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(open);
        p.add(save);
        getContentPane().add(p);
        
        // for now, for debugging, load the file
        // from a fixed name
        // pane = new EditorPane("ac4400.spj");
        // pane = new EditorPane("ac4400-silence.spj");
        // pane = new EditorPane("java/test/jmri/jmrix/loconet/spjfile/sd38_2.spj");
        //getContentPane().add(pane);

        // add help menu to window
    	addHelpMenu("package.jmri.jmrix.loconet.soundloader.EditorFrame", true);

        pack();
 
    }

    static JFileChooser chooser;  // shared across all of these
    
    void selectInputFile() {
        if (chooser == null) chooser = jmri.jmrit.XmlFile.userFileChooser();
        chooser.rescanCurrentDirectory();
        int retVal = chooser.showOpenDialog(this);
        if (retVal != JFileChooser.APPROVE_OPTION) return;  // give up if no file selected
        
        // success, open the file
        addFile(chooser.getSelectedFile().getName());
    }

    void selectSaveFile() {
        if (chooser == null) chooser = new JFileChooser(System.getProperty("user.dir"));
        int retVal = chooser.showSaveDialog(this);
        if (retVal != JFileChooser.APPROVE_OPTION) return;  // give up if no file selected
        
        // success, open the file
        try {
            saveFile(chooser.getSelectedFile().getName());
        } catch (IOException e) {
            // failed, warn user
            JOptionPane.showMessageDialog(this, "Error during save: "+e, 
                                    "Save failed!", JOptionPane.WARNING_MESSAGE);
        }
    }

    void addFile(String name) {
        if (pane != null) {
            // already defined
            return;
        }
        pane = new EditorPane(name);
        getContentPane().add(pane);
        open.setEnabled(false);
        save.setEnabled(true);
        pack();
    }
    
    void saveFile(String name) throws IOException {
        pane.saveFile(name);
    }
    
    public void dispose() {
        if (pane!=null) pane.dispose();
        super.dispose();
    }
}

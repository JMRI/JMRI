package jmri.jmrix.loconet.soundloader;

import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Frame for editing Digitrax SPJ files.
 * <p>
 * This is just an enclosure for the EditorPane, which does the real work.
 * <p>
 * This handles file read/write.
 *
 * @author Bob Jacobsen Copyright (C) 2006, 2007, 2008, 2010
 */
public class EditorPane extends jmri.jmrix.loconet.swing.LnPanel {

    // GUI member declarations
    EditorFilePane pane;

    JButton open;
    JButton save;

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.soundloader.EditorFrame"; // NOI18N
    }

    @Override
    public String getTitle() {
        return getTitle(Bundle.getMessage("MenuItemSoundEditor"));
    }

    public EditorPane() {
        super();

        // general GUI config
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // add file button
        open = new JButton(Bundle.getMessage("ButtonOpen"));
        open.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                selectInputFile();
            }
        });

        save = new JButton(Bundle.getMessage("ButtonSave"));
        save.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                selectSaveFile();
            }
        });
        super.add(save);
        save.setEnabled(false);

        JPanel p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(open);
        p.add(save);
        super.add(p);

        // for now, for debugging, load the file
        // from a fixed name
        // pane = new EditorPane("ac4400.spj");
        // pane = new EditorPane("ac4400-silence.spj");
        // pane = new EditorPane("java/test/jmri/jmrix/loconet/spjfile/sd38_2.spj");
        //add(pane);
    }

    static JFileChooser chooser;  // shared across all of these

    void selectInputFile() {
        if (chooser == null) {
            chooser = jmri.jmrit.XmlFile.userFileChooser();
        }
        chooser.rescanCurrentDirectory();
        int retVal = chooser.showOpenDialog(this);
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return;  // give up if no file selected
        }
        // success, open the file
        addFile(chooser.getSelectedFile());
    }

    void selectSaveFile() {
        if (chooser == null) {
            chooser = new JFileChooser(System.getProperty("user.dir")); // NOI18N
        }
        int retVal = chooser.showSaveDialog(this);
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return;  // give up if no file selected
        }
        // success, open the file
        try {
            saveFile(chooser.getSelectedFile().getPath());
        } catch (IOException e) {
            // failed, warn user
            JOptionPane.showMessageDialog(this, "Error during save: " + e,
                    "Save failed!", JOptionPane.WARNING_MESSAGE);
        }
    }

    void addFile(File name) {
        if (pane != null) {
            // already defined
            return;
        }
        pane = new EditorFilePane(name);
        add(pane);
        open.setEnabled(false);
        save.setEnabled(true);
        revalidate();
        // major resize, repack
        ((JFrame) getTopLevelAncestor()).pack();
    }

    void saveFile(String name) throws IOException {
        pane.saveFile(name);
    }

    @Override
    public void dispose() {
        if (pane != null) {
            pane.dispose();
        }
        super.dispose();
    }
}

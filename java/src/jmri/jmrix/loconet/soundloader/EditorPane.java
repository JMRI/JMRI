// EditorPane.java
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
import jmri.jmrix.loconet.LocoNetBundle;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

/**
 * Frame for editing Digitrax SPJ files.
 *
 * This is just an enclosure for the EditorPane, which does the real work.
 *
 * This handles file read/write.
 *
 * @author	Bob Jacobsen Copyright (C) 2006, 2007, 2008, 2010
 * @version $Revision$
 */
public class EditorPane extends jmri.jmrix.loconet.swing.LnPanel {

    /**
     *
     */
    private static final long serialVersionUID = 4382326277234572738L;

    // GUI member declarations
    EditorFilePane pane;

    ResourceBundle res;
    JButton open;
    JButton save;
    LocoNetSystemConnectionMemo memo;

    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.soundloader.EditorFrame";
    }

    public String getTitle() {
        return getTitle(LocoNetBundle.bundle().getString("MenuItemSoundEditor"));
    }

    public EditorPane() {
        super();

        // Its unfortunate that we have to read that bundle twice, but it's due to Java init order
        res = ResourceBundle.getBundle("jmri.jmrix.loconet.soundloader.Editor");

        // general GUI config
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // add file button
        open = new JButton(res.getString("ButtonOpen"));
        open.addActionListener(new AbstractAction() {
            /**
             *
             */
            private static final long serialVersionUID = -6691600637263742650L;

            public void actionPerformed(java.awt.event.ActionEvent e) {
                selectInputFile();
            }
        });

        save = new JButton(res.getString("ButtonSave"));
        save.addActionListener(new AbstractAction() {
            /**
             *
             */
            private static final long serialVersionUID = -8592850250263713770L;

            public void actionPerformed(java.awt.event.ActionEvent e) {
                selectSaveFile();
            }
        });
        add(save);
        save.setEnabled(false);

        JPanel p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(open);
        p.add(save);
        add(p);

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
            chooser = new JFileChooser(System.getProperty("user.dir"));
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

    public void dispose() {
        if (pane != null) {
            pane.dispose();
        }
        super.dispose();
    }
}

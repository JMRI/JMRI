// LokProgImportAction.java
package jmri.jmrit.symbolicprog;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import jmri.util.FileChooserFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to import the CV values from a LokProgrammer CV list file.
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @author	Dave Heap Copyright (C) 2014
 * @version $Revision: 22821 $
 */
public class LokProgImportAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -2113094604113840817L;
    CvTableModel mModel;
    JFrame mParent;
    FileChooserFilter fileFilter;
    JFileChooser fileChooser;

    public LokProgImportAction(String actionName, CvTableModel pModel, JFrame pParent) {
        super(actionName);
        mModel = pModel;
        mParent = pParent;

    }

    public void actionPerformed(ActionEvent e) {

        log.debug("start to import LokProgrammer file");

        if (fileChooser == null) {
            fileChooser = jmri.jmrit.XmlFile.userFileChooser("LokProgrammer CV list files");

        }

        int retVal = fileChooser.showOpenDialog(mParent);

        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            log.debug("Import from LokProgrammer file: " + file);

            try {
                // ctor launches operation
                new LokProgImporter(file, mModel);
            } catch (IOException ex) {
            }
        }
    }

    static Logger log = LoggerFactory.getLogger(LokProgImportAction.class.getName());
}

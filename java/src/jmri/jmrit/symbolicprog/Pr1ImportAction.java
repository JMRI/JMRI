// Pr1ImportAction.java
package jmri.jmrit.symbolicprog;

import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to import the CV values from a PR1WIN/PR1DOS data file.
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @author	Dave Heap Copyright (C) 2015
 * @version $Revision$
 */
public class Pr1ImportAction extends GenericImportAction {

    public Pr1ImportAction(String actionName, CvTableModel pModel, JFrame pParent, JLabel pStatus) {
        super(actionName, pModel, pParent, pStatus, "PR1 files", "dec", null);
    }

    @Override
    boolean launchImporter(File file, CvTableModel tableModel) {
            try {
                // ctor launches operation
                Pr1Importer importer = new Pr1Importer(file);
                importer.setCvTable(mModel);
                return true;
            } catch (IOException ex) {
                return false;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(Pr1ImportAction.class.getName());
}

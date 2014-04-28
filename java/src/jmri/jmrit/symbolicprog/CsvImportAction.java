// CsvImportAction.java

package jmri.jmrit.symbolicprog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.util.FileChooserFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

/**
 * Action to import the CV values from a CSV format file.
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @author	Dave Heap   Copyright (C) 2014
 * @version     $Revision: 22821 $
 */

public class CsvImportAction  extends AbstractAction {

    CvTableModel mModel;
    JFrame mParent ;
    FileChooserFilter fileFilter ;
    JFileChooser fileChooser ;

    public CsvImportAction(String actionName, CvTableModel pModel, JFrame pParent) {
        super(actionName);
        mModel = pModel;
        mParent = pParent ;

    }

    public void actionPerformed(ActionEvent e) {

        log.debug("start to import CSV file");

        if( fileChooser == null ){
            fileChooser = jmri.jmrit.XmlFile.userFileChooser("CSV list files", "csv");

        }

        int retVal = fileChooser.showOpenDialog( mParent ) ;

        if(retVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (log.isDebugEnabled()) log.debug("Import from CSV file: " + file );

            try {
                // ctor launches operation
                new CsvImporter( file, mModel ) ;
            }
            catch (IOException ex) {
            }
        }
    }

    static Logger log = LoggerFactory.getLogger(CsvImportAction.class.getName());
}

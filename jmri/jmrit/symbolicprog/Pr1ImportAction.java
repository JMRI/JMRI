// Pr1ImportAction.java

package jmri.jmrit.symbolicprog;

import jmri.jmrit.FileChooserFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

/**
 * Action to import the CV values from a PR1WIN/PR1DOS data file.
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @version     $Revision: 1.5 $
 */

public class Pr1ImportAction  extends AbstractAction {

    CvTableModel mModel;
    JFrame mParent ;
    FileChooserFilter fileFilter ;
    JFileChooser fileChooser ;

    public Pr1ImportAction(String actionName, CvTableModel pModel, JFrame pParent) {
        super(actionName);
        mModel = pModel;
        mParent = pParent ;

    }

    public void actionPerformed(ActionEvent e) {

        log.debug("start to import PR1 file");

        if( fileChooser == null ){
            fileChooser = new JFileChooser() ;
            try {  // This may fail on old JVMs, in which case we provide no filter
                fileFilter = new FileChooserFilter( "PR1 Filter" ) ;
                fileFilter.addExtension( "dec" );

                fileChooser.setFileFilter( fileFilter );
            } catch (NoClassDefFoundError ex) {}

        }

        int retVal = fileChooser.showOpenDialog( mParent ) ;

        if(retVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (log.isDebugEnabled()) log.debug("Import from PR1 file: " + file );

            try {
                Pr1Importer importer = new Pr1Importer( file ) ;
                importer.setCvTable( mModel ) ;
            }
            catch (IOException ex) {
            }
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Pr1ImportAction.class.getName());
}

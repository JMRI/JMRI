// Pr1ImportAction.java

package jmri.jmrit.symbolicprog;

import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import jmri.jmrit.FileChooserFilter;

/**
 * Action to import the CV values from a PR1WIN/PR1DOS data file.
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @version     $Revision: 1.3 $
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
            fileFilter = new FileChooserFilter( "PR1 Filter" ) ;
            fileFilter.addExtension( "dec" );
            
            fileChooser.setFileFilter( fileFilter );
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

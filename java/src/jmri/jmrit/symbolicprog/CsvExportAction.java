// CsvExportAction.java

package jmri.jmrit.symbolicprog;

import org.apache.log4j.Logger;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

/**
 * Action to export the CV values to a Comma Separated Valiable (CSV) data file.
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @version     $Revision$
 */
public class CsvExportAction  extends AbstractAction {

    public CsvExportAction(String actionName, CvTableModel pModel, JFrame pParent) {
        super(actionName);
        mModel = pModel;
        mParent = pParent ;
    }

    JFileChooser fileChooser ;
    JFrame mParent ;

    /**
     * CvTableModel to load
     */
    CvTableModel mModel;


    public void actionPerformed(ActionEvent e) {

        if ( fileChooser == null ){
            fileChooser = new JFileChooser() ;
        }

        int retVal = fileChooser.showSaveDialog( mParent ) ;

        if(retVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (log.isDebugEnabled())  log.debug("start to export to PR1 file "+file);

            try {

                PrintStream str = new PrintStream(new FileOutputStream(file));

                str.println("CV, value");
                for (int i=0; i<mModel.getRowCount(); i++) {
                    CvValue cv = mModel.getCvByRow(i);
                    int num = cv.number();
                    int value = cv.getValue();
                    str.println(""+num+","+value);
                }

                str.flush();
                str.close();

            }
            catch (IOException ex) {
                log.error("Error writing file: "+ex);
            }
        }
    }


    static Logger log = Logger.getLogger(CsvExportAction.class.getName());
}

// Pr1ExportAction.java

package jmri.jmrit.symbolicprog;

import java.awt.event.*;
import java.util.*;
import java.io.*;

import javax.swing.*;

/**
 * Action to export the CV values to a PR1WIN data file.
 * <P>
 * Note that this format is somewhat different from the PR1DOS format, and
 * it's not clear they will interoperate.
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @version     $Revision: 1.2 $
 */
public class Pr1ExportAction  extends AbstractAction {

    /**
     * Create the action
     * @param actionName String name to be displayed in menus, etc
     * @param pModel  CvTableModel that contains the data to (eventually) be exported
     * @param pParent JFrame that will eventually invoke the action, used to anchor a file dialog
     */
    public Pr1ExportAction(String actionName, CvTableModel pModel, JFrame pParent) {
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

                str.println("[DecoderData]");
                for (int i=0; i<mModel.getRowCount(); i++) {
                    CvValue cv = mModel.getCvByRow(i);
                    int num = cv.number();
                    int value = cv.getValue();
                    str.println("CV"+num+"="+value);
                }
                str.println("Version=0");
                str.flush();
                str.close();
            }
            catch (IOException ex) {
                log.error("Error writing file: "+ex);
            }
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Pr1ExportAction.class.getName());
}

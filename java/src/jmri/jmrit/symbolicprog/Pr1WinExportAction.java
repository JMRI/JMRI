// Pr1WinExportAction.java

package jmri.jmrit.symbolicprog;

import org.apache.log4j.Logger;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

/**
 * Action to export the CV values to a PR1WIN data file.
 * <P>
 * Note that this format is somewhat different from the PR1DOS format, and
 * it's not clear they will interoperate.
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @version     $Revision$
 */
public class Pr1WinExportAction  extends AbstractAction {

    /**
     * Create the action
     * @param actionName String name to be displayed in menus, etc
     * @param pModel  CvTableModel that contains the data to (eventually) be exported
     * @param pParent JFrame that will eventually invoke the action, used to anchor a file dialog
     */
    public Pr1WinExportAction(String actionName, CvTableModel pModel, JFrame pParent) {
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
                for (int i=1; i<=256; i++) {
                    int lowCvIndex = (i-1)*4;
                    CvValue cv1 = mModel.allCvVector().elementAt(lowCvIndex+1);
                    int value1 = (cv1!=null) ? cv1.getValue() : 0;
                    CvValue cv2 = mModel.allCvVector().elementAt(lowCvIndex+2);
                    int value2 = (cv2!=null) ? cv2.getValue() : 0;
                    CvValue cv3 = mModel.allCvVector().elementAt(lowCvIndex+3);
                    int value3 = (cv3!=null) ? cv3.getValue() : 0;
                    CvValue cv4 = mModel.allCvVector().elementAt(lowCvIndex);
                    int value4 = (cv4!=null) ? cv4.getValue() : 0;


                    long lValue = value1 + (value2 << 8) + (value3 << 16) ;
                    if( value4 > 127 )
                      lValue = -2147483647 + lValue + (( value4 - 127 ) << 24) ;
                    else
                      lValue += value4 << 24 ;

                    str.println("CV" + i+ "=" + lValue );
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

    static Logger log = Logger.getLogger(Pr1ExportAction.class.getName());
}

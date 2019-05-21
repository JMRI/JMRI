package jmri.jmrit.symbolicprog;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to export the CV values to a PR1WIN data file.
 * <p>
 * Note that this format is somewhat different from the PR1DOS format, and it's
 * not clear they will interoperate.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 */
public class Pr1WinExportAction extends AbstractAction {

    /**
     * Create the action
     *
     * @param actionName String name to be displayed in menus, etc
     * @param pModel     CvTableModel that contains the data to (eventually) be
     *                   exported
     * @param pParent    JFrame that will eventually invoke the action, used to
     *                   anchor a file dialog
     */
    public Pr1WinExportAction(String actionName, CvTableModel pModel, JFrame pParent) {
        super(actionName);
        mModel = pModel;
        mParent = pParent;
    }

    JFileChooser fileChooser;
    JFrame mParent;

    /**
     * CvTableModel to load
     */
    CvTableModel mModel;

    @Override
    public void actionPerformed(ActionEvent e) {

        if (fileChooser == null) {
            fileChooser = new JFileChooser();
        }

        int retVal = fileChooser.showSaveDialog(mParent);

        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (log.isDebugEnabled()) {
                log.debug("start to export to PR1 file " + file);
            }

            try {

                PrintStream str = new PrintStream(new FileOutputStream(file));

                str.println("[DecoderData]");
                for (int i = 1; i <= 256; i++) {
                    int lowCvIndex = (i - 1) * 4;
                    CvValue cv1 = mModel.allCvMap().get("" + (lowCvIndex + 1));
                    int value1 = (cv1 != null) ? cv1.getValue() : 0;
                    CvValue cv2 = mModel.allCvMap().get("" + (lowCvIndex + 2));
                    int value2 = (cv2 != null) ? cv2.getValue() : 0;
                    CvValue cv3 = mModel.allCvMap().get("" + (lowCvIndex + 3));
                    int value3 = (cv3 != null) ? cv3.getValue() : 0;
                    CvValue cv4 = mModel.allCvMap().get("" + (lowCvIndex));
                    int value4 = (cv4 != null) ? cv4.getValue() : 0;

                    long lValue = value1 + (value2 << 8) + (value3 << 16);
                    if (value4 > 127) {
                        lValue = -2147483647 + lValue + ((value4 - 127) << 24);
                    } else {
                        lValue += value4 << 24;
                    }

                    str.println("CV" + i + "=" + lValue);
                }
                str.println("Version=0");
                str.flush();
                str.close();
            } catch (IOException ex) {
                log.error("Error writing file: " + ex);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(Pr1ExportAction.class);
}

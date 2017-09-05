package jmri.jmrit.symbolicprog;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to import the CV values from a LokProgrammer CV list file.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Dave Heap Copyright (C) 2015
 */
public class GenericImportAction extends AbstractAction {

    CvTableModel mModel;
    JFrame mParent;
    JLabel mStatus;
    String mActionName;
    String mFileFilterName;
    String mFileExt1;
    String mFileExt2;
    JFileChooser fileChooser;

    public GenericImportAction(String actionName, CvTableModel pModel, JFrame pParent, JLabel pStatus, String fileFilterName, String fileExt1, String fileExt2) {
        super(actionName);
        mModel = pModel;
        mParent = pParent;
        mStatus = pStatus;
        mActionName = actionName;
        mFileFilterName = fileFilterName;
        mFileExt1 = fileExt1;
        mFileExt2 = fileExt2;

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        log.debug("start to import " + mActionName);
        mStatus.setText(java.text.MessageFormat.format(
                SymbolicProgBundle.getMessage("MenuImportAction"),
                new Object[]{mActionName}));

        if (fileChooser == null) {
            fileChooser = jmri.jmrit.XmlFile.userFileChooser(mFileFilterName, mFileExt1, mFileExt2);

        }

        int retVal = fileChooser.showOpenDialog(mParent);

        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            log.debug("Import from " + mActionName + " \"" + file + "\"");

            if (launchImporter(file, mModel)) {
                mStatus.setText(Bundle.getMessage("StateOK"));
            } else {
                mStatus.setText(java.text.MessageFormat.format(
                SymbolicProgBundle.getMessage("MenuImportError"),
                new Object[]{file}));
            }
        } else {
            mStatus.setText(Bundle.getMessage("StateCancelled"));
        }
    }

    boolean launchImporter(File file, CvTableModel tableModel) {
        return false;
    }
    
    private final static Logger log = LoggerFactory.getLogger(GenericImportAction.class);
}

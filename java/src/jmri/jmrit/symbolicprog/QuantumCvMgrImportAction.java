package jmri.jmrit.symbolicprog;

import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Action to import the CV values from a Quantum CV Manager .qcv file.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Dave Heap Copyright (C) 2015
 */
@API(status = MAINTAINED)
public class QuantumCvMgrImportAction extends GenericImportAction {

    public QuantumCvMgrImportAction(String actionName, CvTableModel pModel, JFrame pParent, JLabel pStatus) {
        super(actionName, pModel, pParent, pStatus, "Quantum CV Manager files", "qcv", null);
    }

    @Override
    boolean launchImporter(File file, CvTableModel tableModel) {
            try {
                // ctor launches operation
                new QuantumCvMgrImporter(file, mModel);
                return true;
            } catch (IOException ex) {
                return false;
        }
    }
}

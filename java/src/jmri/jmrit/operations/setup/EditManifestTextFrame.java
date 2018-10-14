package jmri.jmrit.operations.setup;

import jmri.jmrit.operations.OperationsFrame;

/**
 * Frame for user edit of manifest text strings
 *
 * @author Dan Boudreau Copyright (C) 2013
 * 
 */
public class EditManifestTextFrame extends OperationsFrame {

    public EditManifestTextFrame() {
        super(Bundle.getMessage("TitleManifestText"), new EditManifestTextPanel());
    }

    @Override
    public void initComponents() {
        super.initComponents();

        // build menu
        addHelpMenu("package.jmri.jmrit.operations.Operations_ManifestPrintOptionsTools", true); // NOI18N

        initMinimumSize();
    }

//    private static final Logger log = LoggerFactory.getLogger(OperationsSetupFrame.class);
}

package jmri.jmrit.operations.setup;

import jmri.jmrit.operations.OperationsFrame;

/**
 * Frame for user edit of switch list text strings
 *
 * @author Dan Boudreau Copyright (C) 2013
 * 
 */
public class EditSwitchListTextFrame extends OperationsFrame {

    public EditSwitchListTextFrame() {
        super(Bundle.getMessage("TitleSwitchListText"), new EditSwitchListTextPanel());
    }

    @Override
    public void initComponents() {
        super.initComponents();

        // build menu
        addHelpMenu("package.jmri.jmrit.operations.Operations_ManifestPrintOptionsTools", true); // NOI18N

        initMinimumSize();
    }

//    private static final Logger log = LoggerFactory.getLogger(EditSwitchListTextFrame.class);
}

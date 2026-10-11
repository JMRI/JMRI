package jmri.jmrit.operations.setup.gui;

import jmri.jmrit.operations.OperationsFrame;

/**
 * Frame for user edit of day to name mapping
 *
 * @author Dan Boudreau Copyright (C) 2026
 * 
 */
public class EditDayToNameMapFrame extends OperationsFrame {

    public EditDayToNameMapFrame() {
        super(Bundle.getMessage("TitleDayToNameMap"), new EditDayToNameMapPanel());
    }

    @Override
    public void initComponents() {
        super.initComponents();

        // build menu
        addHelpMenu("package.jmri.jmrit.operations.Operations_ManifestDayNameMapping", true); // NOI18N

        initMinimumSize();
    }
}

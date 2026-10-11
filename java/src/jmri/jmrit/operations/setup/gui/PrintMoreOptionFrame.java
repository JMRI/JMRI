package jmri.jmrit.operations.setup.gui;

import java.awt.Dimension;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.setup.Control;

/**
 * Frame for user edit of additional manifest print options
 *
 * @author Dan Boudreau Copyright (C) 2012
 * 
 */
public class PrintMoreOptionFrame extends OperationsFrame {

    public PrintMoreOptionFrame() {
        super(Bundle.getMessage("TitlePrintMoreOptions"), new PrintMoreOptionPanel());
    }

    @Override
    public void initComponents() {
        super.initComponents();

        // build menu
        addHelpMenu("package.jmri.jmrit.operations.Operations_ManifestPrintOptionsTools", true); // NOI18N

        initMinimumSize(new Dimension(Control.panelWidth300, Control.panelHeight400));
    }

    //private static final Logger log = LoggerFactory.getLogger(PrintMoreOptionFrame.class);
}

// PrintMoreOptionFrame.java
package jmri.jmrit.operations.setup;

import jmri.jmrit.operations.OperationsFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user edit of additional manifest print options
 *
 * @author Dan Boudreau Copyright (C) 2012
 * @version $Revision: 21846 $
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
    }

    private static final Logger log = LoggerFactory.getLogger(PrintMoreOptionFrame.class);
}

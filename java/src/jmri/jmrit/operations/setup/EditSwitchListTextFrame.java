// ManifestTextFrame.java
package jmri.jmrit.operations.setup;

import jmri.jmrit.operations.OperationsFrame;

/**
 * Frame for user edit of switch list text strings
 *
 * @author Dan Boudreau Copyright (C) 2013
 * @version $Revision: 21846 $
 */
public class EditSwitchListTextFrame extends OperationsFrame {

    /**
     *
     */
    private static final long serialVersionUID = -8868355208674070617L;

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

package jmri.jmrit.whereused;

import javax.swing.JTextArea;
import jmri.NamedBean;
import jmri.NamedBean.DisplayOptions;

/**
 * Find Block references.
 *
 * @author Dave Sand Copyright (C) 2020
 */

public class BlockWhereUsed {

    /**
     * Populate a textarea with the where used content for the supplied sensor.
     * @param block The block bean.
     * @return a populated textarea.
     */
    static public JTextArea getWhereUsed(NamedBean block) {
        JTextArea textArea = new JTextArea();
        String label = Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameBlock"));  // NOI18N
        textArea.append(Bundle.getMessage("ReferenceTitle", label, block.getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME)));  // NOI18N
        textArea.append(Bundle.getMessage("ListenerCount", block.getNumPropertyChangeListeners()));  // NOI18N

        textArea.append(WhereUsedCollectors.checkLayoutBlocks(block));
        textArea.append(WhereUsedCollectors.checkSignalMastLogic(block));
        textArea.append(WhereUsedCollectors.checkSections(block));
        textArea.append(WhereUsedCollectors.checkPanels(block));
        textArea.append(WhereUsedCollectors.checkCTC(block));
        textArea.append(WhereUsedCollectors.checkLogixNGConditionals(block));
        return textArea;
    }
}

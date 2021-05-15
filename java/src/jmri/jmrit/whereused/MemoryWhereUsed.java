package jmri.jmrit.whereused;

import javax.swing.JTextArea;
import jmri.NamedBean;
import jmri.NamedBean.DisplayOptions;

/**
 * Find memory references.
 *
 * @author Dave Sand Copyright (C) 2020
 */

public class MemoryWhereUsed {

    /**
     * Populate a textarea with the where used content for the supplied memory.
     * @param memory The memory bean.
     * @return a populated textarea.
     */
    static public JTextArea getWhereUsed(NamedBean memory) {
        JTextArea textArea = new JTextArea();
        String label = Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameMemory"));  // NOI18N
        textArea.append(Bundle.getMessage("ReferenceTitle", label, memory.getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME)));  // NOI18N
        textArea.append(Bundle.getMessage("ListenerCount", memory.getNumPropertyChangeListeners()));  // NOI18N

        textArea.append(WhereUsedCollectors.checkLayoutBlocks(memory));
        textArea.append(WhereUsedCollectors.checkLogixConditionals(memory));
        textArea.append(WhereUsedCollectors.checkLogixNGConditionals(memory));
        textArea.append(WhereUsedCollectors.checkPanels(memory));
        return textArea;
    }
}

package jmri.jmrit.whereused;

import javax.swing.JTextArea;
import jmri.NamedBean;

/**
 * Find Warrant references.
 *
 * @author Dave Sand Copyright (C) 2020
 */

public class WarrantWhereUsed {

    /**
     * Populate a textarea with the where used content for the supplied bean.
     * @param warrant The warrant bean.
     * @return a populated textarea.
     */
    static public JTextArea getWhereUsed(NamedBean warrant) {
        JTextArea textArea = new JTextArea();
        String label = Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameWarrant"));  // NOI18N
        textArea.append(Bundle.getMessage("ReferenceTitle", label, warrant.getDisplayName()));  // NOI18N
        textArea.append(Bundle.getMessage("ListenerCount", warrant.getNumPropertyChangeListeners()));  // NOI18N

        textArea.append(WhereUsedCollectors.checkLogixConditionals(warrant));
        textArea.append(WhereUsedCollectors.checkOBlocks(warrant));
        textArea.append(WhereUsedCollectors.checkWarrants(warrant));
        return textArea;
    }
}

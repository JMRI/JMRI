package jmri.jmrit.whereused;

import javax.swing.JTextArea;
import jmri.NamedBean;
import jmri.NamedBean.DisplayOptions;

/**
 * Find entry/exit references.
 *
 * @author Dave Sand Copyright (C) 2020
 */

public class EntryExitWhereUsed {

    /**
     * Populate a textarea with the where used content for the supplied bean.
     * @param entryexit The entry/exit bean.
     * @return a populated textarea.
     */
    static public JTextArea getWhereUsed(NamedBean entryexit) {
        JTextArea textArea = new JTextArea();
        String label = Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameEntryExit"));  // NOI18N
        textArea.append(Bundle.getMessage("ReferenceTitle", label, entryexit.getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME)));  // NOI18N
        textArea.append(Bundle.getMessage("ListenerCount", entryexit.getNumPropertyChangeListeners()));  // NOI18N

        textArea.append(WhereUsedCollectors.checkLogixConditionals(entryexit));
        textArea.append(WhereUsedCollectors.checkLogixNGConditionals(entryexit));
        textArea.append(WhereUsedCollectors.checkEntryExit(entryexit));
        return textArea;
    }
}

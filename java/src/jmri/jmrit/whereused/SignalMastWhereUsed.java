package jmri.jmrit.whereused;

import javax.swing.JTextArea;
import jmri.NamedBean;
import jmri.NamedBean.DisplayOptions;

/**
 * Find signal mast references.
 *
 * @author Dave Sand Copyright (C) 2020
 */

public class SignalMastWhereUsed {

    /**
     * Populate a textarea with the where used content for the supplied signal mast.
     * @param signalMast The signal mast bean.
     * @return a populated textarea.
     */
    static public JTextArea getWhereUsed(NamedBean signalMast) {
        JTextArea textArea = new JTextArea();
        String label = Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameSignalMast"));  // NOI18N
        textArea.append(Bundle.getMessage("ReferenceTitle", label, signalMast.getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME)));  // NOI18N
        textArea.append(Bundle.getMessage("ListenerCount", signalMast.getNumPropertyChangeListeners()));  // NOI18N

        textArea.append(WhereUsedCollectors.checkSignalMastLogic(signalMast));
        textArea.append(WhereUsedCollectors.checkSignalGroups(signalMast));
        textArea.append(WhereUsedCollectors.checkOBlocks(signalMast));
        textArea.append(WhereUsedCollectors.checkWarrants(signalMast));
        textArea.append(WhereUsedCollectors.checkEntryExit(signalMast));
        textArea.append(WhereUsedCollectors.checkLogixConditionals(signalMast));
        textArea.append(WhereUsedCollectors.checkLogixNGConditionals(signalMast));
        textArea.append(WhereUsedCollectors.checkPanels(signalMast));
        textArea.append(WhereUsedCollectors.checkCTC(signalMast));
        return textArea;
    }
}

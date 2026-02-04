package jmri.jmrit.whereused;

import javax.swing.JTextArea;
import jmri.NamedBean;
import jmri.NamedBean.DisplayOptions;

/**
 * Find signal head references.
 *
 * @author Dave Sand Copyright (C) 2020
 */

public class SignalHeadWhereUsed {

    /**
     * Populate a textarea with the where used content for the supplied signal head.
     * @param signalHead The signal head bean.
     * @return a populated textarea.
     */
    static public JTextArea getWhereUsed(NamedBean signalHead) {
        JTextArea textArea = new JTextArea();
        String label = Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameSignalHead"));  // NOI18N
        textArea.append(Bundle.getMessage("ReferenceTitle", label, signalHead.getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME)));  // NOI18N
        textArea.append(Bundle.getMessage("ListenerCount", signalHead.getNumPropertyChangeListeners()));  // NOI18N

        textArea.append(WhereUsedCollectors.checkSignalHeadLogic(signalHead));
        textArea.append(WhereUsedCollectors.checkSignalGroups(signalHead));
        textArea.append(WhereUsedCollectors.checkSignalMasts(signalHead));
        textArea.append(WhereUsedCollectors.checkOBlocks(signalHead));
        textArea.append(WhereUsedCollectors.checkWarrants(signalHead));
        textArea.append(WhereUsedCollectors.checkEntryExit(signalHead));
        textArea.append(WhereUsedCollectors.checkLogixConditionals(signalHead));
        textArea.append(WhereUsedCollectors.checkLogixNGConditionals(signalHead));
        textArea.append(WhereUsedCollectors.checkPanels(signalHead));
        textArea.append(WhereUsedCollectors.checkCTC(signalHead));
        return textArea;
    }
}

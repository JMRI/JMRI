package jmri.jmrit.whereused;

import javax.swing.JTextArea;
import jmri.NamedBean;
import jmri.NamedBean.DisplayOptions;

/**
 * Find turnout references.
 *
 * @author Dave Sand Copyright (C) 2020
 */

public class TurnoutWhereUsed {

    /**
     * Populate a textarea with the where used content for the supplied turnout.
     * @param turnout The turnout bean.
     * @return a populated textarea.
     */
    static public JTextArea getWhereUsed(NamedBean turnout) {
        JTextArea textArea = new JTextArea();
        String label = Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameTurnout"));  // NOI18N
        textArea.append(Bundle.getMessage("ReferenceTitle", label, turnout.getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME)));  // NOI18N
        textArea.append(Bundle.getMessage("ListenerCount", turnout.getNumPropertyChangeListeners()));  // NOI18N

        textArea.append(WhereUsedCollectors.checkLights(turnout));
        textArea.append(WhereUsedCollectors.checkRoutes(turnout));
        textArea.append(WhereUsedCollectors.checkBlocks(turnout));
        textArea.append(WhereUsedCollectors.checkSignalHeadLogic(turnout));
        textArea.append(WhereUsedCollectors.checkSignalMastLogic(turnout));
        textArea.append(WhereUsedCollectors.checkSignalGroups(turnout));
        textArea.append(WhereUsedCollectors.checkOBlocks(turnout));
        textArea.append(WhereUsedCollectors.checkLogixConditionals(turnout));
        textArea.append(WhereUsedCollectors.checkLogixNGConditionals(turnout));
        textArea.append(WhereUsedCollectors.checkPanels(turnout));
        textArea.append(WhereUsedCollectors.checkCTC(turnout));
        return textArea;
    }
}

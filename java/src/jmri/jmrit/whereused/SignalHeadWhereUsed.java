package jmri.jmrit.whereused;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.SortedSet;
import javax.swing.JTextArea;

import jmri.*;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.blockboss.BlockBossLogic;
import jmri.jmrit.logix.OBlockManager;

/**
 * Find signal head references.
 * <ul>
 * <li>Signal Heads - SSL definitions</li>
 * <li>Signal Group - Heads</li>
 * <li>OBlocks - OPaths</li>
 * <li>Logix Conditionals</li>
 * <li>Panels - Signal head icons</li>
 * <li>CTC - OS signal heads TODO</li>
 * </ul>
 *
 * @author Dave Sand Copyright (C) 2020
 */

public class SignalHeadWhereUsed {

    /**
     * Populate a textarea with the where used content for the supplied sensor.
     * @param sensor The sensor bean.
     * @return a populated textarea.
     */
    static public JTextArea getSignalHeadWhereUsed(SignalHead signalHead) {
        JTextArea textArea = new JTextArea();
        String label = Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameSignalHead"));  // NOI18N
        textArea.append(Bundle.getMessage("ReferenceTitle", label, signalHead.getDisplayName()));  // NOI18N
        textArea.append(Bundle.getMessage("ListenerCount", signalHead.getNumPropertyChangeListeners()));
        textArea.append(WhereUsedCollectors.checkSignalHeadLogic(signalHead));
        textArea.append(WhereUsedCollectors.checkSignalGroup(signalHead));
        textArea.append(WhereUsedCollectors.checkOBlocks(signalHead));
        textArea.append(WhereUsedCollectors.checkLogixConditionals(signalHead));
        textArea.append(WhereUsedCollectors.checkPanels(signalHead));
        textArea.append(WhereUsedCollectors.checkCTC(signalHead));
        return textArea;
    }
}

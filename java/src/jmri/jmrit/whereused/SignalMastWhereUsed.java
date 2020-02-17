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
 * Find signal mast references.
 * <ul>
 * <li>Signal Masts - SML definitions</li>
 * <li>Signal Group - Masts</li>
 * <li>OBlocks - OPaths</li>
 * <li>Logix Conditionals</li>
 * <li>Panels - Signal mast icons</li>
 * <li>CTC - OS signal masts TODO</li>
 * </ul>
 *
 * @author Dave Sand Copyright (C) 2020
 */

public class SignalMastWhereUsed {

    /**
     * Populate a textarea with the where used content for the supplied sensor.
     * @param sensor The sensor bean.
     * @return a populated textarea.
     */
    static public JTextArea getSignalMastWhereUsed(SignalMast signalMast) {
        JTextArea textArea = new JTextArea();
        String label = Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameSignalMast"));  // NOI18N
        textArea.append(Bundle.getMessage("ReferenceTitle", label, signalMast.getDisplayName()));  // NOI18N
        textArea.append(Bundle.getMessage("ListenerCount", signalMast.getNumPropertyChangeListeners()));
        textArea.append(WhereUsedCollectors.checkSignalMastLogic(signalMast));
        textArea.append(WhereUsedCollectors.checkSignalGroup(signalMast));
        textArea.append(WhereUsedCollectors.checkOBlocks(signalMast));
        textArea.append(WhereUsedCollectors.checkLogixConditionals(signalMast));
        textArea.append(WhereUsedCollectors.checkPanels(signalMast));
        textArea.append(WhereUsedCollectors.checkCTC(signalMast));
        return textArea;
    }
}

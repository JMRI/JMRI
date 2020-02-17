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
 * Find light references.
 * <ul>
 * <li>Logix Conditionals</li>
 * <li>Panels - Light icons</li>
 * </ul>
 *
 * @author Dave Sand Copyright (C) 2020
 */

public class LightWhereUsed {

    /**
     * Populate a textarea with the where used content for the supplied sensor.
     * @param light The light bean.
     * @return a populated textarea.
     */
    static public JTextArea getLightWhereUsed(Light light) {
        JTextArea textArea = new JTextArea();
        String label = Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameLight"));  // NOI18N
        textArea.append(Bundle.getMessage("ReferenceTitle", label, light.getDisplayName()));  // NOI18N
        textArea.append(Bundle.getMessage("ListenerCount", light.getNumPropertyChangeListeners()));
        textArea.append(WhereUsedCollectors.checkLogixConditionals(light));
        textArea.append(WhereUsedCollectors.checkPanels(light));
        return textArea;
    }
}

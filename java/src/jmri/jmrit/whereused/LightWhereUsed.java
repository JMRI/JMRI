package jmri.jmrit.whereused;

import javax.swing.JTextArea;
import jmri.NamedBean;
import jmri.NamedBean.DisplayOptions;

/**
 * Find light references.
 *
 * @author Dave Sand Copyright (C) 2020
 */

public class LightWhereUsed {

    /**
     * Populate a textarea with the where used content for the supplied sensor.
     * @param light The light bean.
     * @return a populated textarea.
     */
    static public JTextArea getWhereUsed(NamedBean light) {
        JTextArea textArea = new JTextArea();
        String label = Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameLight"));  // NOI18N
        textArea.append(Bundle.getMessage("ReferenceTitle", label, light.getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME)));  // NOI18N
        textArea.append(Bundle.getMessage("ListenerCount", light.getNumPropertyChangeListeners()));  // NOI18N

        textArea.append(WhereUsedCollectors.checkLogixConditionals(light));
        textArea.append(WhereUsedCollectors.checkLogixNGConditionals(light));
        textArea.append(WhereUsedCollectors.checkPanels(light));
        return textArea;
    }
}

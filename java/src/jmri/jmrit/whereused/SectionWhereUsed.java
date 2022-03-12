package jmri.jmrit.whereused;

import javax.swing.JTextArea;
import jmri.NamedBean;
import jmri.NamedBean.DisplayOptions;

/**
 * Find Section references.
 *
 * @author Dave Sand Copyright (C) 2020
 */

public class SectionWhereUsed {

    /**
     * Populate a textarea with the where used content for the supplied sensor.
     * @param section The section bean.
     * @return a populated textarea.
     */
    static public JTextArea getWhereUsed(NamedBean section) {
        JTextArea textArea = new JTextArea();
        String label = Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameSection"));  // NOI18N
        textArea.append(Bundle.getMessage("ReferenceTitle", label, section.getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME)));  // NOI18N
        textArea.append(Bundle.getMessage("ListenerCount", section.getNumPropertyChangeListeners()));  // NOI18N

        textArea.append(WhereUsedCollectors.checkTransits(section));
        return textArea;
    }
}

package jmri.jmrit.whereused;

import javax.swing.JTextArea;
import jmri.NamedBean;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Find Section references.
 *
 * @author Dave Sand Copyright (C) 2020
 */

@API(status = MAINTAINED)
public class SectionWhereUsed {

    /**
     * Populate a textarea with the where used content for the supplied sensor.
     * @param section The section bean.
     * @return a populated textarea.
     */
    static public JTextArea getWhereUsed(NamedBean section) {
        JTextArea textArea = new JTextArea();
        String label = Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameSection"));  // NOI18N
        textArea.append(Bundle.getMessage("ReferenceTitle", label, section.getDisplayName()));  // NOI18N
        textArea.append(Bundle.getMessage("ListenerCount", section.getNumPropertyChangeListeners()));  // NOI18N

        textArea.append(WhereUsedCollectors.checkTransits(section));
        return textArea;
    }
}

package jmri.jmrit.whereused;

import javax.swing.JTextArea;
import jmri.NamedBean;
import jmri.NamedBean.DisplayOptions;

/**
 * Find Audio references.
 *
 * @author Dave Sand Copyright (C) 2023
 */

public class AudioWhereUsed {

    /**
     * Populate a textarea with the where used content for the supplied Audio bean.
     * @param audio The audio bean.
     * @return a populated textarea.
     */
    static public JTextArea getWhereUsed(NamedBean audio) {
        JTextArea textArea = new JTextArea();
        String label = Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameAudio"));  // NOI18N
        textArea.append(Bundle.getMessage("ReferenceTitle", label, audio.getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME)));  // NOI18N
        textArea.append(Bundle.getMessage("ListenerCount", audio.getNumPropertyChangeListeners()));  // NOI18N

        textArea.append(WhereUsedCollectors.checkAudio(audio));
        textArea.append(WhereUsedCollectors.checkLogixConditionals(audio));
        textArea.append(WhereUsedCollectors.checkLogixNGConditionals(audio));
        textArea.append(WhereUsedCollectors.checkPanels(audio));
        return textArea;
    }
}

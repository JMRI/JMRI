package jmri.jmrit.whereused;

import javax.swing.JTextArea;
import jmri.NamedBean;
import jmri.NamedBean.DisplayOptions;

/**
 * Find Reporter references.
 *
 * @author Dave Sand Copyright (C) 2020
 */

public class ReporterWhereUsed {

    /**
     * Populate a textarea with the where used content for the supplied sensor.
     * @param reporter The reporter bean.
     * @return a populated textarea.
     */
    static public JTextArea getWhereUsed(NamedBean reporter) {
        JTextArea textArea = new JTextArea();
        String label = Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameReporter"));  // NOI18N
        textArea.append(Bundle.getMessage("ReferenceTitle", label, reporter.getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME)));  // NOI18N
        textArea.append(Bundle.getMessage("ListenerCount", reporter.getNumPropertyChangeListeners()));  // NOI18N

        textArea.append(WhereUsedCollectors.checkBlocks(reporter));
        return textArea;
    }
}

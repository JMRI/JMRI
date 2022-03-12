package jmri.jmrit.whereused;

import javax.swing.JTextArea;
import jmri.NamedBean;
import jmri.NamedBean.DisplayOptions;

/**
 * Find sensor references.
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class SensorWhereUsed {

    /**
     * Populate a textarea with the whereused content for the supplied sensor.
     * @param sensor The sensor bean.
     * @return a populated textarea.
     */
    static public JTextArea getWhereUsed(NamedBean sensor) {
        JTextArea textArea = new JTextArea();
        String label = Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameSensor"));  // NOI18N
        textArea.append(Bundle.getMessage("ReferenceTitle", label, sensor.getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME)));  // NOI18N
        textArea.append(Bundle.getMessage("ListenerCount", sensor.getNumPropertyChangeListeners()));  // NOI18N

        textArea.append(WhereUsedCollectors.checkTurnouts(sensor));
        textArea.append(WhereUsedCollectors.checkLights(sensor));
        textArea.append(WhereUsedCollectors.checkRoutes(sensor));
        textArea.append(WhereUsedCollectors.checkBlocks(sensor));
        textArea.append(WhereUsedCollectors.checkLayoutBlocks(sensor));
        textArea.append(WhereUsedCollectors.checkSignalHeadLogic(sensor));
        textArea.append(WhereUsedCollectors.checkSignalMastLogic(sensor));
        textArea.append(WhereUsedCollectors.checkSignalGroups(sensor));
        textArea.append(WhereUsedCollectors.checkOBlocks(sensor));
        textArea.append(WhereUsedCollectors.checkEntryExit(sensor));
        textArea.append(WhereUsedCollectors.checkLogixConditionals(sensor));
        textArea.append(WhereUsedCollectors.checkLogixNGConditionals(sensor));
        textArea.append(WhereUsedCollectors.checkSections(sensor));
        textArea.append(WhereUsedCollectors.checkTransits(sensor));
        textArea.append(WhereUsedCollectors.checkPanels(sensor));
        textArea.append(WhereUsedCollectors.checkCTC(sensor));
        return textArea;
    }
}

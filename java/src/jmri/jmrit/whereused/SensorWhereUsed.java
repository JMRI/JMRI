package jmri.jmrit.whereused;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.SortedSet;
import javax.swing.JTextArea;

import jmri.*;
import jmri.jmrit.blockboss.BlockBossLogic;

/**
 * Find sensor references.
 * <ul>
 * <li>Turnouts - Feedback sensors</li>
 * <li>Blocks - Occupancy sensors</li>
 * <li>LayoutBlocks - Occupancy sensors TODO</li>
 * <li>Signal Heads - SSL definitions</li>
 * <li>Signal Masts - SML definitions</li>
 * <li>OBlocks TODO</li>
 * <li>Dispatcher TODO</li>
 * <li>Panels - Sensor icons</li>
 * <li>CTC - OS sensors TODO</li>
 * </ul>
 *
 * @author Dave Sand Copyright (C) 2020
 */

public class SensorWhereUsed {

    static JTextArea getSensorWhereUsed(Sensor sensor, JTextArea textArea) {
        String label = Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameSensor"));  // NOI18N
        textArea.append(Bundle.getMessage("ReferenceTitle", label, sensor.getDisplayName()));  // NOI18N
//         textArea.append(String.format("\n\t\t\t%s %d\n", Bundle.getMessage("ListenerCount"), sensor.getNumPropertyChangeListeners()));
        textArea.append(checkTurnouts(sensor));
        textArea.append(checkBlocks(sensor));
        textArea.append(checkSignalHeadLogic(sensor));
        textArea.append(checkSignalMastLogic(sensor));
        textArea.append(checkPanels(sensor));
        return textArea;
    }
    static String checkTurnouts(Sensor sensor) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet().forEach((turnout) -> {
            int feedback = turnout.getFeedbackMode();
            if (feedback == Turnout.ONESENSOR || feedback == Turnout.TWOSENSOR) {
                turnout.getUsageReport(sensor).forEach((report) -> {
                    if (report.usageKey.startsWith("TurnoutFeedback")) {
                        sb.append(Bundle.getMessage("ReferenceLine", turnout.getUserName(), turnout.getSystemName()));  // NOI18N
                    }
                });
            }
        });
        return addHeader(sb, "ReferenceFeedback");
    }

    static String checkBlocks(Sensor sensor) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(BlockManager.class).getNamedBeanSet().forEach((block) -> {
            block.getUsageReport(sensor).forEach((report) -> {
                if (report.usageKey.equals("BlockSensor")) {
                    sb.append(Bundle.getMessage("ReferenceLine", block.getUserName(), block.getSystemName()));  // NOI18N
                }
            });
        });
        return addHeader(sb, "ReferenceOccupancy");
    }

    static String checkSignalHeadLogic(Sensor sensor) {
        StringBuilder sb = new StringBuilder();

        Enumeration<BlockBossLogic> e = BlockBossLogic.entries();
        while (e.hasMoreElements()) {
            BlockBossLogic ssl = e.nextElement();
            ssl.getUsageReport(sensor).forEach((report) -> {
                if (report.usageKey.startsWith("SSLSensor")) {  // NOI18N
                    sb.append(Bundle.getMessage("ReferenceLine", report.usingBean.getUserName(), report.usingBean.getSystemName()));  // NOI18N
                }
            });
        }
        return addHeader(sb, "ReferenceHeadSSL");  // NOI18N
    }

    static String checkSignalMastLogic(Sensor sensor) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(SignalMastLogicManager.class).getNamedBeanSet().forEach((sml) -> {
            log.info("sml = {}", sml.getUsageReport(sensor));
            SignalMast src = sml.getSourceMast();
            sml.getDestinationList().forEach((dest) -> {
                if (sml.getSensors(dest).contains(sensor)) {
                        sb.append(Bundle.getMessage("ReferenceLinePair", src.getDisplayName(), dest.getDisplayName()));  // NOI18N
                }
            });
        });
        return addHeader(sb, "ReferenceMastSML");  // NOI18N
    }

    static String checkPanels(Sensor sensor) {
        StringBuilder sb = new StringBuilder();
        jmri.jmrit.display.PanelMenu panelMenu = InstanceManager.getDefault(jmri.jmrit.display.PanelMenu.class);
        panelMenu.getEditorPanelList().forEach((panel) -> {
            panel.getContents().forEach((pos) -> {
                if (sensor == pos.getNamedBean()) {
                    sb.append(Bundle.getMessage("ReferenceLineShort", panel.getTitle()));  // NOI18N
                }
            });
        });
        return addHeader(sb, "ReferencePanels");  // NOI18N
    }

    static String addHeader(StringBuilder sb, String bundleKey) {
        if (sb.length() > 0) {
            sb.insert(0, Bundle.getMessage("ReferenceHeader", Bundle.getMessage(bundleKey)));  // NOI18N
            sb.append("\n");
        }
        return sb.toString();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SensorWhereUsed.class);
}

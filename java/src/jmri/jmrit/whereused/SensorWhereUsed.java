package jmri.jmrit.whereused;

import java.lang.StringBuilder;
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
                if (sensor == turnout.getFirstSensor() || sensor == turnout.getSecondSensor()) {
                    sb.append(Bundle.getMessage("ReferenceLine", turnout.getUserName(), turnout.getSystemName()));  // NOI18N
                }
            }
        });
        return addHeader(sb, "ReferenceFeedback");
    }

    static String checkBlocks(Sensor sensor) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(BlockManager.class).getNamedBeanSet().forEach((block) -> {
            if (sensor == block.getSensor()) {
                sb.append(Bundle.getMessage("ReferenceLine", block.getUserName(), block.getSystemName()));  // NOI18N
            }
        });
        return addHeader(sb, "ReferenceOccupancy");
    }

    static String checkSignalHeadLogic(Sensor sensor) {
        StringBuilder sb = new StringBuilder();
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);

        Enumeration<BlockBossLogic> e = BlockBossLogic.entries();
        while (e.hasMoreElements()) {
            BlockBossLogic ssl = e.nextElement();
            List<Sensor> sensors = new ArrayList<>();
            if (ssl.getApproachSensor1() != null) sensors.add(sm.getSensor(ssl.getApproachSensor1()));
            if (ssl.getSensor1() != null) sensors.add(sm.getSensor(ssl.getSensor1()));
            if (ssl.getSensor2() != null) sensors.add(sm.getSensor(ssl.getSensor2()));
            if (ssl.getSensor3() != null) sensors.add(sm.getSensor(ssl.getSensor3()));
            if (ssl.getSensor4() != null) sensors.add(sm.getSensor(ssl.getSensor4()));
            if (ssl.getSensor5() != null) sensors.add(sm.getSensor(ssl.getSensor5()));
            if (ssl.getWatchedSensor1() != null) sensors.add(sm.getSensor(ssl.getWatchedSensor1()));
            if (ssl.getWatchedSensor1Alt() != null) sensors.add(sm.getSensor(ssl.getWatchedSensor1Alt()));
            if (ssl.getWatchedSensor2() != null) sensors.add(sm.getSensor(ssl.getWatchedSensor2()));
            if (ssl.getWatchedSensor2Alt() != null) sensors.add(sm.getSensor(ssl.getWatchedSensor2Alt()));
            if (sensors.contains(sensor)) {
                SignalHead head = ssl.getDrivenSignalNamedBean().getBean();
                sb.append(Bundle.getMessage("ReferenceLine", head.getUserName(), head.getSystemName()));  // NOI18N
            }
        }
        return addHeader(sb, "ReferenceHeadSSL");
    }

    static String checkSignalMastLogic(Sensor sensor) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(SignalMastLogicManager.class).getNamedBeanSet().forEach((sml) -> {
            SignalMast src = sml.getSourceMast();
            sml.getDestinationList().forEach((dest) -> {
                if (sml.getSensors(dest).contains(sensor)) {
                        sb.append(Bundle.getMessage("ReferenceLinePair", src.getDisplayName(), dest.getDisplayName()));  // NOI18N
                }
            });
        });
        return addHeader(sb, "ReferenceMastSML");
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
        return addHeader(sb, "ReferencePanels");
    }

    static String addHeader(StringBuilder sb, String bundleKey) {
        if (sb.length() > 0) {
            sb.insert(0, Bundle.getMessage("ReferenceHeader", Bundle.getMessage(bundleKey)));  // NOI18N
            sb.append("\n");
        }
        return sb.toString();
    }

//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SensorWhereUsed.class);
}

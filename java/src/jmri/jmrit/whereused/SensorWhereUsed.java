package jmri.jmrit.whereused;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.SortedSet;
import javax.swing.JTextArea;

import jmri.*;
import jmri.jmrit.blockboss.BlockBossLogic;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
/**
 * Find sensor references.
 * <ul>
 * <li>Turnouts - Feedback sensors</li>
 * <li>Lights - Light control sensor</li>
 * <li>Routes - Route definitions</li>
 * <li>Blocks - Occupancy sensors</li>
 * <li>LayoutBlocks - Occupancy sensors</li>
 * <li>Signal Heads - SSL definitions</li>
 * <li>Signal Masts - SML definitions</li>
 * <li>OBlocks</li>
 * <li>Logix Conditionals</li>
 * <li>Section - Direction and Stopping sensors</li>
 * <li>Transit - Stop Allocation and Action sensors</li>
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
        textArea.append(Bundle.getMessage("ListenerCount", sensor.getNumPropertyChangeListeners()));
        textArea.append(checkTurnouts(sensor));
        textArea.append(checkLights(sensor));
        textArea.append(checkRoutes(sensor));
        textArea.append(checkBlocks(sensor));
        textArea.append(checkLayoutBlocks(sensor));
        textArea.append(checkSignalHeadLogic(sensor));
        textArea.append(checkSignalMastLogic(sensor));
        textArea.append(checkOBlocks(sensor));
        textArea.append(checkLogixConditionals(sensor));
        textArea.append(checkSections(sensor));
        textArea.append(checkTransits(sensor));
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

    static String checkLights(Sensor sensor) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(LightManager.class).getNamedBeanSet().forEach((light) -> {
            light.getUsageReport(sensor).forEach((report) -> {
                if (report.usageKey.startsWith("Sensor")) {
                    sb.append(Bundle.getMessage("ReferenceLineData", light.getUserName(), light.getSystemName(), report.usageData));  // NOI18N
                }
            });
        });
        return addHeader(sb, "ReferenceLightControl");  // NOI18N
    }

    static String checkRoutes(Sensor sensor) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(RouteManager.class).getNamedBeanSet().forEach((route) -> {
            route.getUsageReport(sensor).forEach((report) -> {
                if (report.usageKey.startsWith("RouteSensor")) {
                    sb.append(Bundle.getMessage("ReferenceLine", route.getUserName(), route.getSystemName()));  // NOI18N
                }
            });
        });
        return addHeader(sb, "ReferenceRoutes");  // NOI18N
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
        return addHeader(sb, "ReferenceBlockOccupancy");  // NOI18N
    }

    static String checkLayoutBlocks(Sensor sensor) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(LayoutBlockManager.class).getNamedBeanSet().forEach((layoutBlock) -> {
            layoutBlock.getUsageReport(sensor).forEach((report) -> {
                if (report.usageKey.equals("LayoutBlockSensor")) {
                    sb.append(Bundle.getMessage("ReferenceLine", layoutBlock.getUserName(), layoutBlock.getSystemName()));  // NOI18N
                }
            });
        });
        return addHeader(sb, "ReferenceLayoutBlockOccupancy");  // NOI18N
    }

    static String checkSignalHeadLogic(Sensor sensor) {
        StringBuilder sb = new StringBuilder();
        Enumeration<BlockBossLogic> e = BlockBossLogic.entries();
        while (e.hasMoreElements()) {
            BlockBossLogic ssl = e.nextElement();
            ssl.getUsageReport(sensor).forEach((report) -> {
                if (report.usageKey.startsWith("SSLSensor")) {  // NOI18N
                    sb.append(Bundle.getMessage("ReferenceLine", report.usageBean.getUserName(), report.usageBean.getSystemName()));  // NOI18N
                }
            });
        }
        return addHeader(sb, "ReferenceHeadSSL");  // NOI18N
    }

    static String checkSignalMastLogic(Sensor sensor) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(SignalMastLogicManager.class).getNamedBeanSet().forEach((sml) -> {
            sml.getUsageReport(sensor).forEach((report) -> {
                if (report.usageKey.startsWith("SMLSensor")) {
                    sb.append(Bundle.getMessage("ReferenceLinePair", sml.getSourceMast().getDisplayName(), report.usageBean.getDisplayName()));  // NOI18N
                }
            });
        });
        return addHeader(sb, "ReferenceMastSML");  // NOI18N
    }

    static String checkOBlocks(Sensor sensor) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(OBlockManager.class).getNamedBeanSet().forEach((oblock) -> {
            oblock.getUsageReport(sensor).forEach((report) -> {
                if (report.usageKey.startsWith("OBlockSensor")) {
                    sb.append(Bundle.getMessage("ReferenceLine", oblock.getUserName(), oblock.getSystemName()));  // NOI18N
                }
            });
        });
        return addHeader(sb, "ReferenceOBlockOccupancy");  // NOI18N
    }

    static String checkLogixConditionals(Sensor sensor) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(LogixManager.class).getNamedBeanSet().forEach((logix) -> {
            logix.getUsageReport(sensor).forEach((report) -> {
                if (report.usageKey.startsWith("ConditionalVariable") || report.usageKey.startsWith("ConditionalAction")) {  // NOI18N
                    sb.append(Bundle.getMessage("ReferenceLineConditional", logix.getUserName(), logix.getSystemName(),  // NOI18N
                            report.usageData));
                }
            });
        });
        return addHeader(sb, "ReferenceConditionals");  // NOI18N
    }

    static String checkSections(Sensor sensor) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(SectionManager.class).getNamedBeanSet().forEach((section) -> {
            section.getUsageReport(sensor).forEach((report) -> {
                if (report.usageKey.startsWith("SectionSensor")) {
                    sb.append(Bundle.getMessage("ReferenceLine", section.getUserName(), section.getSystemName()));  // NOI18N
                }
            });
        });
        return addHeader(sb, "ReferenceSections");  // NOI18N
    }

    static String checkTransits(Sensor sensor) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(TransitManager.class).getNamedBeanSet().forEach((transit) -> {
            transit.getUsageReport(sensor).forEach((report) -> {
                if (report.usageKey.startsWith("TransitSensorStop")) {  // NOI18N
                    sb.append(Bundle.getMessage("ReferenceLine", transit.getUserName(), transit.getSystemName()));  // NOI18N
                }
                if (report.usageKey.startsWith("TransitSensorAction")) {  // NOI18N
                    sb.append(Bundle.getMessage("ReferenceLineAction", transit.getUserName(), transit.getSystemName(),  // NOI18N
                            report.usageBean.getDisplayName()));
                }
            });
        });
        return addHeader(sb, "ReferenceTransits");  // NOI18N
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

//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SensorWhereUsed.class);
}

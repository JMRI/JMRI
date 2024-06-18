package jmri.jmrit.whereused;

import jmri.jmrit.blockboss.BlockBossLogicProvider;
import jmri.jmrit.display.EditorManager;

import java.util.Collections;
import java.util.Enumeration;

import jmri.*;
import jmri.jmrit.blockboss.BlockBossLogic;
import jmri.jmrit.ctc.CtcManager;
import jmri.jmrit.display.switchboardEditor.SwitchboardEditor;
import jmri.jmrit.entryexit.EntryExitPairs;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.WarrantManager;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.ModuleManager;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
/**
 * Find references.  Each collector method calls a corresponding getUsageReport(NamedBean)
 * in the main implementation class for the object type.  The matches are returned in an
 * array list of NamedBeanUsageReport objects.
 *
 * Collectors:
 * <ul>
 * <li>checkAudio</li>
 * <li>checkTurnouts</li>
 * <li>checkLights</li>
 * <li>checkRoutes</li>
 * <li>checkBlocks</li>
 * <li>checkLayoutBlocks</li>
 * <li>checkSignalHeadLogic</li>
 * <li>checkSignalMastLogic</li>
 * <li>checkSignalGroups</li>
 * <li>checkSignalHeads</li>
 * <li>checkSignalMasts</li>
 * <li>checkOBlocks</li>
 * <li>checkWarrants</li>
 * <li>checkEntryExit</li>
 * <li>checkLogixConditionals</li>
 * <li>checkLogixNGConditionals</li>
 * <li>checkSections</li>
 * <li>checkTransits</li>
 * <li>checkPanels</li>
 * <li>checkCTC</li>
 * </ul>
 *
 * @author Dave Sand Copyright (C) 2020
 */

public class WhereUsedCollectors {

    /**
     * Create the Audio usage string.
     * Usage keys:
     * <ul>
     * <li>AudioBuffer</li>
     * </ul>
     * @param bean The requesting bean:  Audio.
     * @return usage string
     */
    static String checkAudio(NamedBean bean) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(AudioManager.class).getNamedBeanSet().forEach((audio) -> audio.getUsageReport(bean).forEach((report) -> {
            if (report.usageKey.startsWith("Audio")) {  // NOI18N
                String name = audio.getDisplayName(NamedBean.DisplayOptions.USERNAME_SYSTEMNAME);
                sb.append(Bundle.getMessage("ReferenceLineName", name));  // NOI18N
            }
        }));
        return addHeader(sb, "ReferenceAudio");  // NOI18N
    }

    /**
     * Create the Turnout usage string.
     * Usage keys:
     * <ul>
     * <li>TurnoutFeedback1</li>
     * <li>TurnoutFeedback2</li>
     * </ul>
     * @param bean The requesting bean:  Sensor.
     * @return usage string
     */
    static String checkTurnouts(NamedBean bean) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet().forEach((turnout) -> {
            int feedback = turnout.getFeedbackMode();
            if (feedback == Turnout.ONESENSOR || feedback == Turnout.TWOSENSOR) {
                turnout.getUsageReport(bean).forEach((report) -> {
                    if (report.usageKey.startsWith("TurnoutFeedback")) {  // NOI18N
                        String name = turnout.getDisplayName(NamedBean.DisplayOptions.USERNAME_SYSTEMNAME);
                        sb.append(Bundle.getMessage("ReferenceLineName", name));  // NOI18N
                    }
                });
            }
        });
        return addHeader(sb, "ReferenceFeedback");  // NOI18N
    }

    /**
     * Create the Light usage string.
     * Usage keys:
     * <ul>
     * <li>LightControlSensor1</li>
     * <li>LightControlSensor2</li>
     * <li>LightControlSensorTimed</li>
     * <li>LightControlTurnout</li>
     * </ul>
     * @param bean The requesting bean:  Sensor, Turnout.
     * @return usage string
     */
    static String checkLights(NamedBean bean) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(LightManager.class).getNamedBeanSet().forEach((light) -> light.getUsageReport(bean).forEach((report) -> {
            if (report.usageKey.startsWith("LightControl")) {  // NOI18N
                String name = light.getDisplayName(NamedBean.DisplayOptions.USERNAME_SYSTEMNAME);
                sb.append(Bundle.getMessage("ReferenceLineData", name, report.usageData));  // NOI18N
            }
        }));
        return addHeader(sb, "ReferenceLightControl");  // NOI18N
    }

    /**
     * Create the Route usage string.
     * Usage keys:
     * <ul>
     * <li>RouteTurnoutOutput</li>
     * <li>RouteSensorOutput</li>
     * <li>RouteSensorControl</li>
     * <li>RouteSensorAligned</li>
     * <li>RouteTurnoutControl</li>
     * <li>RouteTurnoutLock</li>
     * </ul>
     * @param bean The requesting bean:  Sensor, Turnout.
     * @return usage string
     */
    static String checkRoutes(NamedBean bean) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(RouteManager.class).getNamedBeanSet().forEach((route) -> route.getUsageReport(bean).forEach((report) -> {
            if (report.usageKey.startsWith("Route")) {  // NOI18N
                String name = route.getDisplayName(NamedBean.DisplayOptions.USERNAME_SYSTEMNAME);
                sb.append(Bundle.getMessage("ReferenceLineName", name));  // NOI18N
            }
        }));
        return addHeader(sb, "ReferenceRoutes");  // NOI18N
    }

    /**
     * Create the Block usage string.
     * Usage keys:
     * <ul>
     * <li>BlockSensor</li>
     * <li>BlockReporter</li>
     * <li>BlockPathNeighbor</li>
     * <li>BlockPathTurnout</li>
     * </ul>
     * @param bean The requesting bean:  Block (Path neighbor), Sensor, Reporter, Turnout (Path).
     * @return usage string
     */
    static String checkBlocks(NamedBean bean) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(BlockManager.class).getNamedBeanSet().forEach((block) -> block.getUsageReport(bean).forEach((report) -> {
            if (report.usageKey.startsWith("Block")) {  // NOI18N
                String name = block.getDisplayName(NamedBean.DisplayOptions.USERNAME_SYSTEMNAME);
                sb.append(Bundle.getMessage("ReferenceLineName", name));  // NOI18N
            }
        }));
        return addHeader(sb, "ReferenceBlock");  // NOI18N
    }

    /**
     * Create the LayoutBlock usage string.
     * Usage keys:
     * <ul>
     * <li>LayoutBlockBlock</li>
     * <li>LayoutBlockMemory</li>
     * <li>LayoutBlockSensor</li>
     * <li>LayoutBlockNeighbor</li>
     * </ul>
     * @param bean The requesting bean:  Block, Memory, Sensor.
     * @return usage string
     */
    static String checkLayoutBlocks(NamedBean bean) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(LayoutBlockManager.class).getNamedBeanSet().forEach((layoutBlock) -> layoutBlock.getUsageReport(bean).forEach((report) -> {
            if (report.usageKey.startsWith("LayoutBlock")) {  // NOI18N
                String name = layoutBlock.getDisplayName(NamedBean.DisplayOptions.USERNAME_SYSTEMNAME);
                sb.append(Bundle.getMessage("ReferenceLineData", name, report.usageData));  // NOI18N
            }
        }));
        return addHeader(sb, "ReferenceLayoutBlock");  // NOI18N
    }

    /**
     * Create the Signal Head Logic usage string.
     * Usage keys:
     * <ul>
     * <li>SSLSignal</li>
     * <li>SSLSensor1-5</li>
     * <li>SSLTurnout</li>
     * <li>SSLSignal1</li>
     * <li>SSLSignal1Alt</li>
     * <li>SSLSignal2</li>
     * <li>SSLSignal2Alt</li>
     * <li>SSLSensorWatched1</li>
     * <li>SSLSensorWatched1Alt</li>
     * <li>SSLSensorWatched2</li>
     * <li>SSLSensorWatched2Alt</li>
     * <li>SSLSensorApproach</li>
     * </ul>
     * @param bean The requesting bean:  Sensor, Signal Head, Turnout.
     * @return usage string
     */
    static String checkSignalHeadLogic(NamedBean bean) {
        StringBuilder sb = new StringBuilder();
        Enumeration<BlockBossLogic> e = Collections.enumeration(InstanceManager.getDefault(BlockBossLogicProvider.class).provideAll());
        while (e.hasMoreElements()) {
            BlockBossLogic ssl = e.nextElement();
            ssl.getUsageReport(bean).forEach((report) -> {
                if (report.usageKey.startsWith("SSL")) {  // NOI18N
                    String name = report.usageBean.getDisplayName(NamedBean.DisplayOptions.USERNAME_SYSTEMNAME);
                    sb.append(Bundle.getMessage("ReferenceLineName", name));  // NOI18N
                }
            });
        }
        return addHeader(sb, "ReferenceHeadSSL");  // NOI18N
    }

    /**
     * Create the Signal Mast Logic usage string.
     * Usage keys:
     * <ul>
     * <li>SMLSourceMast</li>
     * <li>SMLDestinationMast</li>
     * <li>SMLBlockAuto</li>
     * <li>SMLBlockUser</li>
     * <li>SMLTurnoutAuto</li>
     * <li>SMLTurnoutUser</li>
     * <li>SMLSensor</li>
     * <li>SMLMastAuto</li>
     * <li>SMLMastUser</li>
     * </ul>
     * @param bean The requesting bean:  Block, Turnout, Sensor, Signal Mast.
     * @return usage string
     */
    static String checkSignalMastLogic(NamedBean bean) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(SignalMastLogicManager.class).getNamedBeanSet().forEach((sml) -> sml.getUsageReport(bean).forEach((report) -> {
            String name = bean.getDisplayName(NamedBean.DisplayOptions.USERNAME_SYSTEMNAME);
            if (report.usageKey.startsWith("SMLSource")) {  // NOI18N
                sb.append(Bundle.getMessage("ReferenceLineData", name, Bundle.getMessage("SourceMast")));  // NOI18N
                return;
            }
            if (report.usageKey.startsWith("SMLDest")) {  // NOI18N
                sb.append(Bundle.getMessage("ReferenceLineData", name, Bundle.getMessage("DestMast")));  // NOI18N
                return;
            }
            if (report.usageKey.startsWith("SML")) {  // NOI18N
                sb.append(Bundle.getMessage("ReferenceLinePair", sml.getSourceMast().getDisplayName(), report.usageBean.getDisplayName()));  // NOI18N
            }
        }));
        return addHeader(sb, "ReferenceMastSML");  // NOI18N
    }

    /**
     * Create the Signal Group usage string.
     * Usage keys:
     * <ul>
     * <li>SignalGroupMast</li>
     * <li>SignalGroupHead</li>
     * <li>SignalGroupHeadSensor</li>
     * <li>SignalGroupHeadTurnout</li>
     * </ul>
     * @param bean The requesting bean:  Sensor, Signal Head, Signal Mast, Turnout.
     * @return usage string
     */
    static String checkSignalGroups(NamedBean bean) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(SignalGroupManager.class).getNamedBeanSet().forEach((group) -> group.getUsageReport(bean).forEach((report) -> {
            if (report.usageKey.startsWith("SignalGroup")) {  // NOI18N
                String name = group.getDisplayName(NamedBean.DisplayOptions.USERNAME_SYSTEMNAME);
                sb.append(Bundle.getMessage("ReferenceLineName", name));  // NOI18N
            }
        }));
        return addHeader(sb, "ReferenceSignalGroup");  // NOI18N
    }

    /**
     * Create the Signal Head usage string.
     * Usage keys:
     * <ul>
     * <li>SignalHeadTurnout</li>
     * </ul>
     * @param bean The requesting bean:  Turnout.
     * @return usage string
     */
    static String checkSignalHeads(NamedBean bean) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet().forEach((head) -> head.getUsageReport(bean).forEach((report) -> {
            if (report.usageKey.startsWith("SignalHead")) {  // NOI18N
                String name = head.getDisplayName(NamedBean.DisplayOptions.USERNAME_SYSTEMNAME);
                sb.append(Bundle.getMessage("ReferenceLineName", name));  // NOI18N
            }
        }));
        return addHeader(sb, "ReferenceSignalHead");  // NOI18N
    }

    /**
     * Create the Signal Mast usage string.
     * Usage keys:
     * <ul>
     * <li>SignalMastTurnout</li>
     * <li>SignalMastSignalHead</li>
     * </ul>
     * @param bean The requesting bean:  Signal Head, Turnout.
     * @return usage string
     */
    static String checkSignalMasts(NamedBean bean) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(SignalMastManager.class).getNamedBeanSet().forEach((mast) -> mast.getUsageReport(bean).forEach((report) -> {
            if (report.usageKey.startsWith("SignalMast")) {  // NOI18N
                String name = mast.getDisplayName(NamedBean.DisplayOptions.USERNAME_SYSTEMNAME);
                sb.append(Bundle.getMessage("ReferenceLineName", name));  // NOI18N
            }
        }));
        return addHeader(sb, "ReferenceSignalMast");  // NOI18N
    }

    /**
     * Create the OBlock usage string.
     * Usage keys:
     * <ul>
     * <li>OBlockSensor</li>
     * <li>OBlockSensorError</li>
     * <li>OBlockPortalNeighborOBlock</li>
     * <li>OBlockPortalSignal</li>
     * <li>OBlockPortalPathTurnout</li>
     * <li>OBlockWarrant</li>
     * </ul>
     * @param bean The requesting bean:  OBlock (Neightbor), Sensor, SignalHead, SignalMast, Turnout, Warrant.
     * @return usage string
     */
    static String checkOBlocks(NamedBean bean) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(OBlockManager.class).getNamedBeanSet().forEach((oblock) -> oblock.getUsageReport(bean).forEach((report) -> {
            if (report.usageKey.startsWith("OBlock")) {  // NOI18N
                String name = oblock.getDisplayName(NamedBean.DisplayOptions.USERNAME_SYSTEMNAME);
                sb.append(Bundle.getMessage("ReferenceLineData", name, report.usageData));  // NOI18N
            }
        }));
        return addHeader(sb, "ReferenceOBlock");  // NOI18N
    }

    /**
     * Create the Warrant usage string.
     * Usage keys:
     * <ul>
     * <li>WarrantBlocking</li>
     * <li>WarrantBlock</li>
     * <li>WarrantSignal</li>
     * </ul>
     * @param bean The requesting bean:  OBlock SignalHead, SignalMast, Warrant.
     * @return usage string
     */
    static String checkWarrants(NamedBean bean) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(WarrantManager.class).getNamedBeanSet().forEach((warrant) -> warrant.getUsageReport(bean).forEach((report) -> {
            if (report.usageKey.startsWith("Warrant")) {  // NOI18N
                String name = warrant.getDisplayName(NamedBean.DisplayOptions.USERNAME_SYSTEMNAME);
                sb.append(Bundle.getMessage("ReferenceLineName", name));  // NOI18N
            }
        }));
        return addHeader(sb, "ReferenceWarrant");  // NOI18N
    }

    /**
     * Create the Entry/Exit usage string.
     * Usage keys:
     * <ul>
     * <li>EntryExitSourceSensor</li>
     * <li>EntryExitSourceSignal</li>
     * <li>EntryExitDestinationSensor</li>
     * <li>EntryExitDestinationSignal</li>
     * </ul>
     * @param bean The requesting bean:  Sensor SignalHead, SignalMast.
     * @return usage string
     */
    static String checkEntryExit(NamedBean bean) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(EntryExitPairs.class).getNamedBeanSet().forEach((destPoint) -> destPoint.getUsageReport(bean).forEach((report) -> {
            if (report.usageKey.startsWith("EntryExit")) {  // NOI18N
                String name = destPoint.getDisplayName();
                sb.append(Bundle.getMessage("ReferenceLineName", name));  // NOI18N
            }
        }));
        return addHeader(sb, "ReferenceEntryExit");  // NOI18N
    }

    /**
     * Create the Logix/Conditional usage string.
     * Usage keys:
     * <ul>
     * <li>ConditionalAction</li>
     * <li>ConditionalVariable</li>
     * <li>ConditionalVariableData</li>
     * </ul>
     * @param bean The requesting bean:  Many.
     * @return usage string
     */
    static String checkLogixConditionals(NamedBean bean) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(LogixManager.class).getNamedBeanSet().forEach((logix) -> logix.getUsageReport(bean).forEach((report) -> {
            if (report.usageKey.startsWith("ConditionalVariable") || report.usageKey.startsWith("ConditionalAction")) {  // NOI18N
                String name = logix.getDisplayName(NamedBean.DisplayOptions.USERNAME_SYSTEMNAME);
                String cdlName = report.usageBean.getDisplayName();
                sb.append(Bundle.getMessage("ReferenceLineConditional", name, cdlName, Bundle.getMessage(report.usageKey), report.usageData));  // NOI18N
            }
        }));
        return addHeader(sb, "ReferenceConditionals");  // NOI18N
    }

    /**
     * Create the LogixNG/ConditionalNG usage string.
     * Usage keys:
     * <ul>
     * <li>LogixNGAction</li>
     * <li>LogixNGExpression</li>
     * </ul>
     * @param bean The requesting bean:  Many.
     * @return usage string
     */
    static String checkLogixNGConditionals(NamedBean bean) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(LogixNG_Manager.class).getNamedBeanSet().forEach((logixng) -> logixng.getUsageReport(bean).forEach((report) -> {
            if (report.usageKey.startsWith("LogixNG")) {  // NOI18N
                String name = logixng.getDisplayName(NamedBean.DisplayOptions.USERNAME_SYSTEMNAME);
                String cdlName = report.usageBean != null ? report.usageBean.getDisplayName() : "";
                sb.append(Bundle.getMessage("ReferenceLineLogixNG", name, cdlName, Bundle.getMessage(report.usageKey), report.usageData));  // NOI18N
            }
        }));
        InstanceManager.getDefault(ModuleManager.class).getNamedBeanSet().forEach((module) -> module.getUsageReport(bean).forEach((report) -> {
            if (report.usageKey.startsWith("LogixNG")) {  // NOI18N
                String name = module.getDisplayName(NamedBean.DisplayOptions.USERNAME_SYSTEMNAME);
                sb.append(Bundle.getMessage("ReferenceLineModule", name, Bundle.getMessage(report.usageKey), report.usageData));  // NOI18N
            }
        }));
        return addHeader(sb, "ReferenceLogixNG");  // NOI18N
    }

    /**
     * Create the Section usage string.
     * Usage keys:
     * <ul>
     * <li>SectionBlock</li>
     * <li>SectionSensorForwardBlocking</li>
     * <li>SectionSensorForwardStopping</li>
     * <li>SectionSensorReverseBlocking</li>
     * <li>SectionSensorReverseStopping</li>
     * </ul>
     * @param bean The requesting bean:  Block, Sensor.
     * @return usage string
     */
    static String checkSections(NamedBean bean) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(SectionManager.class).getNamedBeanSet().forEach((section) -> section.getUsageReport(bean).forEach((report) -> {
            if (report.usageKey.startsWith("SectionSensor")) {  // NOI18N
                String name = section.getDisplayName(NamedBean.DisplayOptions.USERNAME_SYSTEMNAME);
                sb.append(Bundle.getMessage("ReferenceLineName", name));  // NOI18N
            }
        }));
        return addHeader(sb, "ReferenceSections");  // NOI18N
    }

    /**
     * Create the Transit usage string.
     * Usage keys:
     * <ul>
     * <li>TransitSection</li>
     * <li>TransitSensorStopAllocation</li>
     * <li>TransitActionSensorWhen</li>
     * <li>TransitActionSensorWhat</li>
     * <li>TransitActionSignalHeadWhat</li>
     * <li>TransitActionSignalMastWhat</li>
     * </ul>
     * @param bean The requesting bean:  Section, Sensor, Signal Head, Signal Mast.
     * @return usage string
     */
    static String checkTransits(NamedBean bean) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(TransitManager.class).getNamedBeanSet().forEach((transit) -> transit.getUsageReport(bean).forEach((report) -> {
            String name = transit.getDisplayName(NamedBean.DisplayOptions.USERNAME_SYSTEMNAME);
            if (report.usageKey.startsWith("TransitSensor") || report.usageKey.startsWith("TransitSection")) {  // NOI18N
                sb.append(Bundle.getMessage("ReferenceLineName", name));  // NOI18N
            }
            if (report.usageKey.startsWith("TransitAction")) {  // NOI18N
                sb.append(Bundle.getMessage("ReferenceLineAction", name, report.usageBean.getDisplayName()));  // NOI18N
            }
        }));
        return addHeader(sb, "ReferenceTransits");  // NOI18N
    }

    /**
     * Create the Panel usage string.  The string includes the icon class name.
     * Usage keys:
     * <ul>
     * <li>PositionalIcon</li>
     * <li>LayoutEditorTurnout</li>
     * <li>LayoutEditorTurnout2</li>
     * <li>LayoutEditorTurnoutBlock</li>
     * <li>LayoutEditorTurnoutSensor</li>
     * <li>LayoutEditorTurnoutSignalHead</li>
     * <li>LayoutEditorTurnoutSignalMast</li>
     * <li>LayoutEditorPointSensor</li>
     * <li>LayoutEditorPointSignalHead</li>
     * <li>LayoutEditorPointSignalMast</li>
     * <li>LayoutEditorSegmentBlock</li>
     * <li>LayoutEditorXingBlock</li>
     * <li>LayoutEditorXingOther (sensor, head, mast)</li>
     * <li>Switchboard (sensor, turnout, light)</li>
     * </ul>
     * Note:  The getUsageReport is invoked at either Editor or LayoutEditor depending on the
     * panel type.  The LayoutEditor version does a super call and then does special turnout
     * checking since LE turnouts are not icons.
     * @param bean The requesting bean:  Many.
     * @return usage string
     */
    static String checkPanels(NamedBean bean) {
        StringBuilder sb = new StringBuilder();
        InstanceManager.getDefault(EditorManager.class).getAll().forEach(panel ->
            panel.getUsageReport(bean).forEach(report -> {
                if (panel instanceof SwitchboardEditor) {
                    sb.append(Bundle.getMessage("ReferenceLineName", report.usageData));  // NOI18N
                } else {
                    sb.append(Bundle.getMessage("ReferenceLinePanel", panel.getTitle(), report.usageData));  // NOI18N
                }
            }));
        return addHeader(sb, "ReferencePanels");  // NOI18N
    }

    /**
     * Create the CTC usage string.
     * The CTC manager is found using the ConfigureManager instead of the InstanceManager.
     * The CTC manager uses InstanceManagerAutoDefault which can result in unnecessary
     * XML content when CTC is not being used.
     * Usage keys:
     * <ul>
     * <li>CtcWhereUsedOther</li>
     * <li>CtcWhereUsedCBHD</li>
     * </ul>
     * @param bean The requesting bean:  Block, Sensor, Signal Head, Signal Mast, Turnout.
     * @return usage string
     */
    static String checkCTC(NamedBean bean) {
        StringBuilder sb = new StringBuilder();

        // Get the CTC manager via the ConfigureManager to avoid auto default.
        InstanceManager.getOptionalDefault(ConfigureManager.class).ifPresent(cm -> {
            cm.getInstanceList(CtcManager.class).forEach(m -> {
                mgr = (CtcManager) m;
            });
        });

        if (mgr != null) {
            mgr.getUsageReport(bean).forEach((report) -> {
                sb.append(Bundle.getMessage("ReferenceLineName", report.usageData));  // NOI18N
            });
        }
        return addHeader(sb, "ReferenceCTC");  // NOI18N
    }
    static CtcManager mgr = null;

    /**
     * Add the specified section to the beginning of the string builder if there is data.
     * @param sb The current string builder.
     * @param bundleKey The key for the section header.
     * @return the resulting string.
     */
    static String addHeader(StringBuilder sb, String bundleKey) {
        if (sb.length() > 0) {
            sb.insert(0, Bundle.getMessage("ReferenceHeader", Bundle.getMessage(bundleKey)));  // NOI18N
            sb.append("\n");
        }
        return sb.toString();
    }

//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WhereUsedCollectors.class);
}

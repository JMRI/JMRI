package jmri.jmrit.beantable;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;

import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.SensorManager;
import jmri.SignalHeadManager;
import jmri.SignalMastManager;
import jmri.TransitSection;
import jmri.TransitSectionAction;
import jmri.jmrit.dispatcher.TrainInfoFile;
import jmri.jmrit.roster.Roster;

/**
 * Words a Transit Section Action, as shown in the Transit editor's Special
 * Actions table and in the Transit Actions window; finds the names it uses that
 * are not defined, and actions that cannot run where they are; copies actions,
 * and replaces a name in them.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public final class TransitActionText {

    static final ResourceBundle rbx = ResourceBundle.getBundle("jmri.jmrit.beantable.SectionTransitTableBundle");

    private TransitActionText() {
    }

    /**
     * Word the When part of an action.
     *
     * @param tsa the action
     * @return the When part, with the values entered
     */
    @Nonnull
    public static String whenText(@Nonnull TransitSectionAction tsa) {
        switch (tsa.getWhenCode()) {
            case TransitSectionAction.ENTRY:
                if (tsa.getDataWhen() > 0) {
                    return MessageFormat.format(rbx.getString("OnEntryDelayedFull"),
                            new Object[]{"" + tsa.getDataWhen()});
                }
                return rbx.getString("OnEntryFull");
            case TransitSectionAction.EXIT:
                if (tsa.getDataWhen() > 0) {
                    return MessageFormat.format(rbx.getString("OnExitDelayedFull"),
                            new Object[]{"" + tsa.getDataWhen()});
                }
                return rbx.getString("OnExitFull");
            case TransitSectionAction.BLOCKENTRY:
                if (tsa.getDataWhen() > 0) {
                    return MessageFormat.format(rbx.getString("OnBlockEntryDelayedFull"),
                            new Object[]{"" + tsa.getDataWhen(), block(tsa.getStringWhen())});
                }
                return MessageFormat.format(rbx.getString("OnBlockEntryFull"),
                        new Object[]{block(tsa.getStringWhen())});
            case TransitSectionAction.BLOCKEXIT:
                if (tsa.getDataWhen() > 0) {
                    return MessageFormat.format(rbx.getString("OnBlockExitDelayedFull"),
                            new Object[]{"" + tsa.getDataWhen(), block(tsa.getStringWhen())});
                }
                return MessageFormat.format(rbx.getString("OnBlockExitFull"),
                        new Object[]{block(tsa.getStringWhen())});
            case TransitSectionAction.TRAINSTOP:
                if (tsa.getDataWhen() > 0) {
                    return MessageFormat.format(rbx.getString("TrainStopDelayedFull"),
                            new Object[]{"" + tsa.getDataWhen()});
                }
                return rbx.getString("TrainStopFull");
            case TransitSectionAction.TRAINSTART:
                if (tsa.getDataWhen() > 0) {
                    return MessageFormat.format(rbx.getString("TrainStartDelayedFull"),
                            new Object[]{"" + tsa.getDataWhen()});
                }
                return rbx.getString("TrainStartFull");
            case TransitSectionAction.SENSORACTIVE:
                if (tsa.getDataWhen() > 0) {
                    return MessageFormat.format(rbx.getString("OnSensorActiveDelayedFull"),
                            new Object[]{"" + tsa.getDataWhen(), sensor(tsa.getStringWhen())});
                }
                return MessageFormat.format(rbx.getString("OnSensorActiveFull"),
                        new Object[]{sensor(tsa.getStringWhen())});
            case TransitSectionAction.SENSORINACTIVE:
                if (tsa.getDataWhen() > 0) {
                    return MessageFormat.format(rbx.getString("OnSensorInactiveDelayedFull"),
                            new Object[]{"" + tsa.getDataWhen(), sensor(tsa.getStringWhen())});
                }
                return MessageFormat.format(rbx.getString("OnSensorInactiveFull"),
                        new Object[]{sensor(tsa.getStringWhen())});
            case TransitSectionAction.PRESTARTDELAY:
                return MessageFormat.format(rbx.getString("PreStartDelayFull"),
                        new Object[]{"" + tsa.getDataWhen(), tsa.getStringWhen()});
            case TransitSectionAction.PRESTARTACTION:
                return MessageFormat.format(rbx.getString("PreStartActionFull"),
                        new Object[]{"" + tsa.getDataWhen(), tsa.getStringWhen()});
            default:
                log.warn("Unhandled transit section action When code: {}",tsa.getWhenCode());
                return("");
        }
    }

    /**
     * Word the What part of an action.
     *
     * @param tsa the action
     * @return the What part, with the values entered
     */
    @Nonnull
    public static String whatText(@Nonnull TransitSectionAction tsa) {
        switch (tsa.getWhatCode()) {
            case TransitSectionAction.PAUSE:
                return MessageFormat.format(rbx.getString("PauseFull"),
                        new Object[]{tsa.getDataWhat1()});
            case TransitSectionAction.SETMAXSPEED:
                return MessageFormat.format(rbx.getString("SetMaxSpeedFull"),
                        new Object[]{tsa.getDataWhat1()});
            case TransitSectionAction.SETCURRENTSPEED:
                return MessageFormat.format(rbx.getString("SetTrainSpeedFull"),
                        new Object[]{tsa.getDataWhat1()});
            case TransitSectionAction.RAMPTRAINSPEED:
                return MessageFormat.format(rbx.getString("RampTrainSpeedFull"),
                        new Object[]{"" + tsa.getDataWhat1()});
            case TransitSectionAction.TOMANUALMODE:
                if (tsa.getStringWhat().length() > 0) {
                    return MessageFormat.format(rbx.getString("ToManualModeAltFull"),
                            new Object[]{sensor(tsa.getStringWhat())});
                }
                return rbx.getString("ToManualModeFull");
            case TransitSectionAction.SETLIGHT:
                if (tsa.getStringWhat().equals("Off")) {
                    return MessageFormat.format(rbx.getString("SetLightFull"),
                        new Object[]{Bundle.getMessage("StateOff")});
                }
                return MessageFormat.format(rbx.getString("SetLightFull"),
                        new Object[]{Bundle.getMessage("StateOn")});
            case TransitSectionAction.STARTBELL:
                return rbx.getString("StartBellFull");
            case TransitSectionAction.STOPBELL:
                return rbx.getString("StopBellFull");
            case TransitSectionAction.SOUNDHORN:
                return MessageFormat.format(rbx.getString("SoundHornFull"),
                        new Object[]{tsa.getDataWhat1()});
            case TransitSectionAction.SOUNDHORNPATTERN:
                return MessageFormat.format(rbx.getString("SoundHornPatternFull"),
                        new Object[]{tsa.getStringWhat(), "" + tsa.getDataWhat1(), "" + tsa.getDataWhat2()});
            case TransitSectionAction.LOCOFUNCTION:
                if (tsa.getStringWhat().equals("Off")) {
                    return MessageFormat.format(rbx.getString("LocoFunctionFull"),
                            new Object[]{"" + tsa.getDataWhat1(), Bundle.getMessage("StateOff")});
                }
                return MessageFormat.format(rbx.getString("LocoFunctionFull"),
                        new Object[]{"" + tsa.getDataWhat1(), Bundle.getMessage("StateOn")});
            case TransitSectionAction.SETSENSORACTIVE:
                return MessageFormat.format(rbx.getString("SetSensorActiveFull"),
                        new Object[]{sensor(tsa.getStringWhat())});
            case TransitSectionAction.SETSENSORINACTIVE:
                return MessageFormat.format(rbx.getString("SetSensorInactiveFull"),
                        new Object[]{sensor(tsa.getStringWhat())});
            case TransitSectionAction.HOLDSIGNAL:
                return MessageFormat.format(rbx.getString("HoldSignalFull"),
                        new Object[]{signal(tsa.getStringWhat())});
            case TransitSectionAction.RELEASESIGNAL:
                return MessageFormat.format(rbx.getString("ReleaseSignalFull"),
                        new Object[]{signal(tsa.getStringWhat())});
            case TransitSectionAction.PRESTARTRESUME:
                return MessageFormat.format(rbx.getString("PreStartResumeFull"),
                        new Object[]{tsa.getDataWhen()});
            case TransitSectionAction.ESTOP:
                return rbx.getString("EStopFull");
            case TransitSectionAction.TERMINATETRAIN:
                return rbx.getString("TerminateTrain");
            case TransitSectionAction.FORCEALLOCATEPASSSAFESECTION:
                return rbx.getString("ForcePassNextSafe");
            case TransitSectionAction.LOADTRAININFO:
                switch (tsa.getDataWhat2()) {
                    case TransitSectionAction.LOCOADDRESSTYPEROSTER:
                        return MessageFormat.format(rbx.getString("LoadTrainInfoRosterFull"),
                                new Object[]{tsa.getStringWhat(),tsa.getStringWhat2()});
                    case TransitSectionAction.LOCOADDRESSTYPENUMBER:
                        return MessageFormat.format(rbx.getString("LoadTrainInfoNumberFull"),
                                new Object[]{tsa.getStringWhat(),tsa.getStringWhat2()});
                    case TransitSectionAction.LOCOADDRESSTYPECURRENT:
                        return MessageFormat.format(rbx.getString("LoadTrainInfoCurrentFull"),
                                new Object[]{tsa.getStringWhat()});
                    case TransitSectionAction.LOCOADDRESSTYPEDEFAULT:
                    default:
                        return MessageFormat.format(rbx.getString("LoadTrainInfoDefaultFull"),
                                new Object[]{tsa.getStringWhat()});
                 }
            case TransitSectionAction.SELECTWHAT:
                return rbx.getString("SelectWhat");
            default:
                log.warn("Unhandled transit section action What code: {}", tsa.getWhatCode());
                break;
        }
        return "WHAT";
    }

    /**
     * Find the names an action uses that are not defined: Blocks, Sensors,
     * Signal Heads or Masts, TrainInfo files and Roster entries.
     *
     * @param tsa the action
     * @return one line for each name not found; empty if there are none
     */
    @Nonnull
    public static List<String> problems(@Nonnull TransitSectionAction tsa) {
        List<String> found = new ArrayList<>();
        switch (tsa.getWhenCode()) {
            case TransitSectionAction.BLOCKENTRY:
            case TransitSectionAction.BLOCKEXIT:
                if (InstanceManager.getDefault(BlockManager.class).getBlock(nonNull(tsa.getStringWhen())) == null) {
                    found.add(MessageFormat.format(rbx.getString("ActionsReportNoBlock"), tsa.getStringWhen()));
                }
                break;
            case TransitSectionAction.SENSORACTIVE:
            case TransitSectionAction.SENSORINACTIVE:
                checkSensor(tsa.getStringWhen(), found);
                break;
            default:
                break;
        }
        switch (tsa.getWhatCode()) {
            case TransitSectionAction.SETSENSORACTIVE:
            case TransitSectionAction.SETSENSORINACTIVE:
                checkSensor(tsa.getStringWhat(), found);
                break;
            case TransitSectionAction.TOMANUALMODE:
                if (!nonNull(tsa.getStringWhat()).isEmpty()) {
                    checkSensor(tsa.getStringWhat(), found);
                }
                break;
            case TransitSectionAction.HOLDSIGNAL:
            case TransitSectionAction.RELEASESIGNAL:
                if (InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(nonNull(tsa.getStringWhat())) == null
                        && InstanceManager.getDefault(SignalMastManager.class).getSignalMast(nonNull(tsa.getStringWhat())) == null) {
                    found.add(MessageFormat.format(rbx.getString("ActionsReportNoSignal"), tsa.getStringWhat()));
                }
                break;
            case TransitSectionAction.LOADTRAININFO:
                if (!Arrays.asList(new TrainInfoFile().getTrainInfoFileNames()).contains(tsa.getStringWhat())) {
                    found.add(MessageFormat.format(rbx.getString("ActionsReportNoTrainInfo"), tsa.getStringWhat()));
                }
                if (tsa.getDataWhat2() == TransitSectionAction.LOCOADDRESSTYPEROSTER
                        && Roster.getDefault().getEntryForId(nonNull(tsa.getStringWhat2())) == null) {
                    found.add(MessageFormat.format(rbx.getString("ActionsReportNoRoster"), tsa.getStringWhat2()));
                }
                break;
            default:
                break;
        }
        return found;
    }

    /**
     * Find the problems of an action in its Section: the names it uses that are
     * not defined, as {@link #problems(TransitSectionAction)}, a Block that is
     * not in the Section, and a Delay Start Action with no Delay Start in the
     * Section to start it.
     *
     * @param ts  the Transit Section the action belongs to
     * @param tsa the action
     * @return one line for each problem; empty if there are none
     */
    @Nonnull
    public static List<String> problems(@Nonnull TransitSection ts, @Nonnull TransitSectionAction tsa) {
        List<String> found = problems(tsa);
        if ((tsa.getWhenCode() == TransitSectionAction.BLOCKENTRY || tsa.getWhenCode() == TransitSectionAction.BLOCKEXIT)
                && ts.getSection() != null) {
            Block b = InstanceManager.getDefault(BlockManager.class).getBlock(nonNull(tsa.getStringWhen()));
            if (b != null && !ts.getSection().getBlockList().contains(b)) {
                found.add(MessageFormat.format(rbx.getString("ActionsReportBlockNotInSection"),
                        b.getDisplayName(), ts.getSection().getDisplayName()));
            }
        }
        if (tsa.getWhenCode() == TransitSectionAction.PRESTARTACTION) {
            boolean started = false;
            for (TransitSectionAction other : ts.getTransitSectionActionList()) {
                if (other.getWhenCode() == TransitSectionAction.PRESTARTDELAY) {
                    started = true;
                }
            }
            if (!started) {
                found.add(rbx.getString("ActionsReportNoPreStart"));
            }
        }
        return found;
    }

    /**
     * Make a new action with the same When and What as another.
     *
     * @param tsa the action to copy
     * @return the copy
     */
    @Nonnull
    public static TransitSectionAction copy(@Nonnull TransitSectionAction tsa) {
        return new TransitSectionAction(tsa.getWhenCode(), tsa.getWhatCode(), tsa.getDataWhen(),
                tsa.getDataWhat1(), tsa.getDataWhat2(), tsa.getStringWhen(), tsa.getStringWhat(),
                tsa.getStringWhat2());
    }

    /**
     * Replace a name an action uses: a Block, Sensor, Signal Head or Mast,
     * TrainInfo file or Roster entry. Only a whole name is replaced: the name
     * stored in the action, or, for a Block, Sensor or Signal, another name of
     * the same one, such as its user name for a stored system name.
     *
     * @param tsa     the action
     * @param oldName the name to replace
     * @param newName the name to use instead
     * @return true if the action used the name, and now uses the new one
     */
    public static boolean replaceName(@Nonnull TransitSectionAction tsa, @Nonnull String oldName, @Nonnull String newName) {
        boolean changed = false;
        switch (tsa.getWhenCode()) {
            case TransitSectionAction.BLOCKENTRY:
            case TransitSectionAction.BLOCKEXIT:
                if (same(tsa.getStringWhen(), oldName, TransitActionText::findBlock)) {
                    tsa.setStringWhen(newName);
                    changed = true;
                }
                break;
            case TransitSectionAction.SENSORACTIVE:
            case TransitSectionAction.SENSORINACTIVE:
                if (same(tsa.getStringWhen(), oldName, TransitActionText::findSensor)) {
                    tsa.setStringWhen(newName);
                    changed = true;
                }
                break;
            default:
                break;
        }
        switch (tsa.getWhatCode()) {
            case TransitSectionAction.SETSENSORACTIVE:
            case TransitSectionAction.SETSENSORINACTIVE:
            case TransitSectionAction.TOMANUALMODE:
                if (same(tsa.getStringWhat(), oldName, TransitActionText::findSensor)) {
                    tsa.setStringWhat(newName);
                    changed = true;
                }
                break;
            case TransitSectionAction.HOLDSIGNAL:
            case TransitSectionAction.RELEASESIGNAL:
                if (same(tsa.getStringWhat(), oldName, TransitActionText::signalBean)) {
                    tsa.setStringWhat(newName);
                    changed = true;
                }
                break;
            case TransitSectionAction.LOADTRAININFO:
                if (oldName.equals(tsa.getStringWhat())) {
                    tsa.setStringWhat(newName);
                    changed = true;
                }
                if (tsa.getWhatCode() == TransitSectionAction.LOADTRAININFO
                        && tsa.getDataWhat2() == TransitSectionAction.LOCOADDRESSTYPEROSTER
                        && oldName.equals(tsa.getStringWhat2())) {
                    tsa.setStringWhat2(newName);
                    changed = true;
                }
                break;
            default:
                break;
        }
        return changed;
    }

    private static void checkSensor(String name, List<String> found) {
        if (name == null || InstanceManager.getDefault(SensorManager.class).getSensor(name) == null) {
            found.add(MessageFormat.format(rbx.getString("ActionsReportNoSensor"), name));
        }
    }

    /**
     * The name to show for a Block an action names: its display name, or the
     * name stored in the action if there is no such Block.
     */
    private static String block(String name) {
        jmri.NamedBean b = name == null ? null : InstanceManager.getDefault(BlockManager.class).getBlock(name);
        return b == null ? nonNull(name) : b.getDisplayName();
    }

    /**
     * The name to show for a Sensor an action names: its display name, or the
     * name stored in the action if there is no such Sensor.
     */
    private static String sensor(String name) {
        jmri.NamedBean b = name == null || name.isEmpty() ? null
                : InstanceManager.getDefault(SensorManager.class).getSensor(name);
        return b == null ? nonNull(name) : b.getDisplayName();
    }

    /**
     * The name to show for a Signal Head or Mast an action names: its display
     * name, or the name stored in the action if there is no such signal.
     */
    private static String signal(String name) {
        jmri.NamedBean b = signalBean(name);
        return b == null ? nonNull(name) : b.getDisplayName();
    }

    private static jmri.NamedBean signalBean(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        jmri.NamedBean b = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(name);
        return b != null ? b : InstanceManager.getDefault(SignalMastManager.class).getSignalMast(name);
    }

    /**
     * Whether a name stored in an action and a name given for it are the same
     * Block, Sensor or Signal: the same name, or names of the same bean, such
     * as its user name and system name.
     */
    private static boolean same(String stored, String given, java.util.function.Function<String, jmri.NamedBean> find) {
        if (given.equals(stored)) {
            return true;
        }
        if (stored == null || stored.isEmpty()) {
            return false;
        }
        jmri.NamedBean a = find.apply(stored);
        return a != null && a == find.apply(given);
    }

    private static jmri.NamedBean findBlock(String name) {
        return InstanceManager.getDefault(BlockManager.class).getBlock(name);
    }

    private static jmri.NamedBean findSensor(String name) {
        return InstanceManager.getDefault(SensorManager.class).getSensor(name);
    }

    private static String nonNull(String s) {
        return s == null ? "" : s;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TransitActionText.class);

}

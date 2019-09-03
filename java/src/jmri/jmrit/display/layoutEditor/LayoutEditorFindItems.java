package jmri.jmrit.display.layoutEditor;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckReturnValue;
import javax.annotation.CheckForNull;
import jmri.NamedBean;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection of tools to find various object on the layout editor panel.
 *
 * @author Dave Duchamp Copyright (c) 2004-2007
 * @author George Warner Copyright (c) 2017-2018
 */
public class LayoutEditorFindItems {

    private LayoutEditor layoutEditor;

    public LayoutEditorFindItems(LayoutEditor editor) {
        layoutEditor = editor;
    }

    public TrackSegment findTrackSegmentByName(String name) {
        if (!name.isEmpty()) {
            for (TrackSegment t : layoutEditor.getTrackSegments()) {
                if (t.getId().equals(name)) {
                    return t;
                }
            }
        }
        return null;
    }

    public PositionablePoint findPositionablePointByName(String name) {
        if (!name.isEmpty()) {
            for (PositionablePoint p : layoutEditor.getPositionablePoints()) {
                if (p.getId().equals(name)) {
                    return p;
                }
            }
        }
        return null;
    }

    public PositionablePoint findPositionablePointAtTrackSegments(TrackSegment tr1, TrackSegment tr2) {
        for (PositionablePoint p : layoutEditor.getPositionablePoints()) {
            if (((p.getConnect1() == tr1) && (p.getConnect2() == tr2))
                    || ((p.getConnect1() == tr2) && (p.getConnect2() == tr1))) {
                return p;
            }
        }
        return null;
    }

    public PositionablePoint findPositionableLinkPoint(LayoutBlock blk1) {
        for (PositionablePoint p : layoutEditor.getPositionablePoints()) {
            if (p.getType() == PositionablePoint.EDGE_CONNECTOR) {
                if ((p.getConnect1() != null && p.getConnect1().getLayoutBlock() == blk1)
                        || (p.getConnect2() != null && p.getConnect2().getLayoutBlock() == blk1)) {
                    return p;
                }
            }
        }
        return null;
    }

    /**
     * Returns an array list of track segments matching the block name.
     */
    public ArrayList<TrackSegment> findTrackSegmentByBlock(String name) {
        if (name.isEmpty()) {
            return null;
        }
        ArrayList<TrackSegment> ts = new ArrayList<>();
        for (TrackSegment t : layoutEditor.getTrackSegments()) {
            if (t.getBlockName().equals(name)) {
                ts.add(t);
            }
        }
        return ts;
    }

    public PositionablePoint findPositionablePointByEastBoundSignal(String signalName) {
        for (PositionablePoint p : layoutEditor.getPositionablePoints()) {
            if (p.getEastBoundSignal().equals(signalName)) {
                return p;
            }
        }
        return null;
    }

    public PositionablePoint findPositionablePointByWestBoundSignal(String signalName) {
        for (PositionablePoint p : layoutEditor.getPositionablePoints()) {
            if (p.getWestBoundSignal().equals(signalName)) {
                return p;
            }
        }
        return null;
    }

    public PositionablePoint findPositionablePointByWestBoundBean(NamedBean bean) {
        if (bean instanceof SignalMast) {
            for (PositionablePoint p : layoutEditor.getPositionablePoints()) {
                if (p.getWestBoundSignalMast() == bean) {
                    return p;
                }
            }
        } else if (bean instanceof Sensor) {
            for (PositionablePoint p : layoutEditor.getPositionablePoints()) {
                if (p.getWestBoundSensor() == bean) {
                    return p;
                }
            }
        } else if (bean instanceof SignalHead) {
            for (PositionablePoint p : layoutEditor.getPositionablePoints()) {
                if (p.getWestBoundSignal().equals(bean.getSystemName())) {
                    return p;
                }
            }
        }
        return null;
    }

    public PositionablePoint findPositionablePointByEastBoundBean(NamedBean bean) {
        if (bean instanceof SignalMast) {
            for (PositionablePoint p : layoutEditor.getPositionablePoints()) {
                if (p.getEastBoundSignalMast() == bean) {
                    return p;
                }
            }
        } else if (bean instanceof Sensor) {
            for (PositionablePoint p : layoutEditor.getPositionablePoints()) {
                if (p.getEastBoundSensor() == bean) {
                    return p;
                }
            }
        } else if (bean instanceof SignalHead) {
            for (PositionablePoint p : layoutEditor.getPositionablePoints()) {
                if (p.getEastBoundSignal().equals(bean.getSystemName())) {
                    return p;
                }
            }
        }
        return null;
    }

    public PositionablePoint findPositionablePointByWestBoundSignalMast(String signalMastName) {
        for (PositionablePoint p : layoutEditor.getPositionablePoints()) {
            if (p.getWestBoundSignalMastName().equals(signalMastName)) {
                return p;
            }
        }
        return null;
    }

    public PositionablePoint findPositionablePointByBean(NamedBean bean) {
        if (bean instanceof SignalMast) {
            for (PositionablePoint p : layoutEditor.getPositionablePoints()) {
                if (p.getWestBoundSignalMast() == bean
                        || p.getEastBoundSignalMast() == bean) {
                    return p;
                }
            }
        } else if (bean instanceof Sensor) {
            for (PositionablePoint p : layoutEditor.getPositionablePoints()) {
                if (p.getWestBoundSensor() == bean
                        || p.getEastBoundSensor() == bean) {
                    return p;
                }
            }
        } else if (bean instanceof SignalHead) {
            for (PositionablePoint p : layoutEditor.getPositionablePoints()) {
                if (p.getEastBoundSignal().equals(bean.getSystemName())
                        || p.getWestBoundSignal().equals(bean.getSystemName())) {
                    return p;
                }
                if (bean.getUserName() != null && (p.getEastBoundSignal().equals(bean.getSystemName())
                        || p.getWestBoundSignal().equals(bean.getSystemName()))) {
                    return p;
                }
            }
        }
        return null;

    }

    @CheckReturnValue
    public LayoutTurnout findLayoutTurnoutBySignalMast(String signalMastName) throws IllegalArgumentException {
        return findLayoutTurnoutByBean(jmri.InstanceManager.getDefault(jmri.SignalMastManager.class).provideSignalMast(signalMastName));
    }

    @CheckReturnValue
    public LayoutTurnout findLayoutTurnoutByBean(@CheckForNull NamedBean bean) {
        List<LayoutTurnout> layoutTurnouts = layoutEditor.getLayoutTurnouts();
        if (bean instanceof SignalMast) {
            for (LayoutTurnout t : layoutTurnouts) {
                if (t.getSignalAMast() == bean
                        || t.getSignalBMast() == bean
                        || t.getSignalCMast() == bean
                        || t.getSignalDMast() == bean) {
                    return t;
                }
            }
        } else if (bean instanceof Sensor) {
            for (LayoutTurnout t : layoutTurnouts) {
                if (t.getSensorA() == bean
                        || t.getSensorB() == bean
                        || t.getSensorC() == bean
                        || t.getSensorD() == bean) {
                    return t;
                }
            }
        } else if (bean instanceof SignalHead) {
            for (LayoutTurnout t : layoutTurnouts) {
                if (t.getSignalA1Name().equals(bean.getSystemName())
                        || t.getSignalA2Name().equals(bean.getSystemName())
                        || t.getSignalA3Name().equals(bean.getSystemName())) {
                    return t;
                }

                if (t.getSignalB1Name().equals(bean.getSystemName())
                        || t.getSignalB2Name().equals(bean.getSystemName())) {
                    return t;
                }
                if (t.getSignalC1Name().equals(bean.getSystemName())
                        || t.getSignalC2Name().equals(bean.getSystemName())) {
                    return t;
                }
                if (t.getSignalD1Name().equals(bean.getSystemName())
                        || t.getSignalD2Name().equals(bean.getSystemName())) {
                    return t;
                }
                if (bean.getUserName() != null) {
                    if (t.getSignalA1Name().equals(bean.getUserName())
                            || t.getSignalA2Name().equals(bean.getUserName())
                            || t.getSignalA3Name().equals(bean.getUserName())) {
                        return t;
                    }

                    if (t.getSignalB1Name().equals(bean.getUserName())
                            || t.getSignalB2Name().equals(bean.getUserName())) {
                        return t;
                    }
                    if (t.getSignalC1Name().equals(bean.getUserName())
                            || t.getSignalC2Name().equals(bean.getUserName())) {
                        return t;
                    }
                    if (t.getSignalD1Name().equals(bean.getUserName())
                            || t.getSignalD2Name().equals(bean.getUserName())) {
                        return t;
                    }
                }
            }
        } else if (bean instanceof Turnout) {
            for (LayoutTurnout t : layoutTurnouts) {
                if (bean.equals(t.getTurnout())) {
                    return t;
                }
            }
        }
        return null;
    }   // findLayoutTurnoutByBean

    @CheckReturnValue
    public LayoutTurnout findLayoutTurnoutBySensor(String sensorName) throws IllegalArgumentException {
        return findLayoutTurnoutByBean(jmri.InstanceManager.sensorManagerInstance().provideSensor(sensorName));
    }

    public LevelXing findLevelXingBySignalMast(String signalMastName) throws IllegalArgumentException {
        return findLevelXingByBean(jmri.InstanceManager.getDefault(jmri.SignalMastManager.class).provideSignalMast(signalMastName));
    }

    public LevelXing findLevelXingBySensor(String sensorName) throws IllegalArgumentException {
        return findLevelXingByBean(jmri.InstanceManager.sensorManagerInstance().provideSensor(sensorName));
    }

    public LevelXing findLevelXingByBean(NamedBean bean) {
        List<LevelXing> levelXings = layoutEditor.getLevelXings();
        if (bean instanceof SignalMast) {
            for (LevelXing l : levelXings) {
                if (l.getSignalAMast() == bean
                        || l.getSignalBMast() == bean
                        || l.getSignalCMast() == bean
                        || l.getSignalDMast() == bean) {
                    return l;
                }
            }
        } else if (bean instanceof Sensor) {
            for (LevelXing l : levelXings) {
                if (l.getSensorA() == bean
                        || l.getSensorB() == bean
                        || l.getSensorC() == bean
                        || l.getSensorD() == bean) {
                    return l;
                }
            }

        } else if (bean instanceof SignalHead) {
            for (LevelXing l : levelXings) {
                if (l.getSignalAName().equals(bean.getSystemName())
                        || l.getSignalBName().equals(bean.getSystemName())
                        || l.getSignalCName().equals(bean.getSystemName())
                        || l.getSignalDName().equals(bean.getSystemName())) {
                    return l;
                }
                if (bean.getUserName() != null && (l.getSignalAName().equals(bean.getUserName())
                        || l.getSignalBName().equals(bean.getUserName())
                        || l.getSignalCName().equals(bean.getUserName())
                        || l.getSignalDName().equals(bean.getUserName()))) {
                    return l;
                }
            }
        }
        return null;
    }

    public LayoutSlip findLayoutSlipByBean(NamedBean bean) {
        List<LayoutSlip> layoutSlips = layoutEditor.getLayoutSlips();
        if (bean instanceof SignalMast) {
            for (LayoutSlip l : layoutSlips) {
                if (l.getSignalAMast() == bean
                        || l.getSignalBMast() == bean
                        || l.getSignalCMast() == bean
                        || l.getSignalDMast() == bean) {
                    return l;
                }
            }
        } else if (bean instanceof Sensor) {
            for (LayoutSlip l : layoutSlips) {
                if (l.getSensorA() == bean
                        || l.getSensorB() == bean
                        || l.getSensorC() == bean
                        || l.getSensorD() == bean) {
                    return l;
                }
            }
        } else if (bean instanceof SignalHead) {
            for (LayoutSlip l : layoutSlips) {
                if (l.getSignalA1Name().equals(bean.getSystemName())
                        || l.getSignalA2Name().equals(bean.getSystemName())
                        || l.getSignalA3Name().equals(bean.getSystemName())) {
                    return l;
                }

                if (l.getSignalB1Name().equals(bean.getSystemName())
                        || l.getSignalB2Name().equals(bean.getSystemName())) {
                    return l;
                }
                if (l.getSignalC1Name().equals(bean.getSystemName())
                        || l.getSignalC2Name().equals(bean.getSystemName())) {
                    return l;
                }
                if (l.getSignalD1Name().equals(bean.getSystemName())
                        || l.getSignalD2Name().equals(bean.getSystemName())) {
                    return l;
                }
                if (l.getSignalA1Name().equals(bean.getUserName())
                        || l.getSignalA2Name().equals(bean.getUserName())
                        || l.getSignalA3Name().equals(bean.getUserName())) {
                    return l;
                }
                if (bean.getUserName() != null) {
                    if (l.getSignalB1Name().equals(bean.getUserName())
                            || l.getSignalB2Name().equals(bean.getUserName())) {
                        return l;
                    }
                    if (l.getSignalC1Name().equals(bean.getUserName())
                            || l.getSignalC2Name().equals(bean.getUserName())) {
                        return l;
                    }
                    if (l.getSignalD1Name().equals(bean.getUserName())
                            || l.getSignalD2Name().equals(bean.getUserName())) {
                        return l;
                    }
                }
            }
        } else if (bean instanceof Turnout) {
            for (LayoutSlip l : layoutSlips) {
                if (bean.equals(l.getTurnout())) {
                    return l;
                }
                if (bean.equals(l.getTurnoutB())) {
                    return l;
                }
            }
        }
        return null;
    }

    public LayoutSlip findLayoutSlipBySignalMast(String signalMastName) throws IllegalArgumentException {
        return findLayoutSlipByBean(jmri.InstanceManager.getDefault(jmri.SignalMastManager.class).provideSignalMast(signalMastName));
    }

    public LayoutSlip findLayoutSlipBySensor(String sensorName) throws IllegalArgumentException {
        return findLayoutSlipByBean(jmri.InstanceManager.sensorManagerInstance().provideSensor(sensorName));
    }

    public PositionablePoint findPositionablePointByEastBoundSensor(String sensorName) {
        PositionablePoint result = null;
        for (PositionablePoint p : layoutEditor.getPositionablePoints()) {
            if (p.getEastBoundSensorName().equals(sensorName)) {
                result = p;
                break;
            }
        }
        return result;
    }

    public PositionablePoint findPositionablePointByWestBoundSensor(String sensorName) {
        PositionablePoint result = null;
        for (PositionablePoint p : layoutEditor.getPositionablePoints()) {
            if (p.getWestBoundSensorName().equals(sensorName)) {
                result = p;
                break;
            }
        }
        return result;
    }

    @CheckReturnValue
    public LayoutTurnout findLayoutTurnoutByName(String name) {
        LayoutTurnout result = null;
        if ((name != null) && !name.isEmpty()) {
            for (LayoutTurnout t : layoutEditor.getLayoutTurnouts()) {
                if (t.getName().equals(name)) {
                    result = t;
                    break;
                }
            }
        }
        return result;
    }

    @CheckReturnValue
    public LayoutTurnout findLayoutTurnoutByTurnoutName(String turnoutName) {
        LayoutTurnout result = null;
        if ((turnoutName != null) && !turnoutName.isEmpty()) {
            for (LayoutTurnout t : layoutEditor.getLayoutTurnouts()) {
                if (t.getTurnoutName().equals(turnoutName)) {
                    result = t;
                }
            }
        }
        return result;
    }

    public LevelXing findLevelXingByName(String name) {
        LevelXing result = null;
        if ((name != null) && !name.isEmpty()) {
            for (LevelXing x : layoutEditor.getLevelXings()) {
                if (x.getId().equals(name)) {
                    result = x;
                    break;
                }
            }
        }
        return result;
    }

    public LayoutSlip findLayoutSlipByName(String name) {
        LayoutSlip result = null;
        if ((name != null) && !name.isEmpty()) {
            for (LayoutSlip x : layoutEditor.getLayoutSlips()) {
                if (x.getName().equals(name)) {
                    result = x;
                    break;
                }
            }
        }
        return result;
    }

    public LayoutTurntable findLayoutTurntableByName(String name) {
        LayoutTurntable result = null;
        if ((name != null) && !name.isEmpty()) {
            for (LayoutTurntable x : layoutEditor.getLayoutTurntables()) {
                if (x.getId().equals(name)) {
                    result = x;
                    break;
                }
            }
        }
        return result;
    }

    public LayoutShape findLayoutShapeByName(String name) {
        LayoutShape result = null;
        if ((name != null) && !name.isEmpty()) {
            for (LayoutShape x : layoutEditor.getLayoutShapes()) {
                if (x.getName().equals(name)) {
                    result = x;
                    break;
                }
            }
        }
        return result;
    }

    // data encapsulation means that no one external to an object should
    // care about its type... we treat all objects as equal and it's up
    // to each object to implement methods specific to that type.
    //
    // JMRI is full of pages of "if (type == XXX) {...} else if (type == XXX)", etc.
    // all that should be refactored to "object.doVerbWith(params);"...
    // and again how each object (class) implements "doVerbWith" is up
    // that class.
    //
    // This would get rid of all the object specific code that's not
    // implemented in those specific classes and vastly simplify
    // the rest of JMRI.
    // [/rant] (brought to you by geowar)
    //
    // Long story short (too late); we can start this transition to
    // a "type-less" system by replacing this routine with a type-less one:
    // (BTW: AFAICT this routine is only called by the setObjects routine in TrackSegment.java)
    //

    /*
     * @deprecated since 4.7.1 use @link{findObjectByName()} instead.
     */
    @Deprecated
    public LayoutTrack findObjectByTypeAndName(int type, String name) {
        if (name.isEmpty()) {
            return null;
        }
        switch (type) {
            case LayoutTrack.NONE:
                return null;
            case LayoutTrack.POS_POINT:
                return findPositionablePointByName(name);
            case LayoutTrack.TURNOUT_A:
            case LayoutTrack.TURNOUT_B:
            case LayoutTrack.TURNOUT_C:
            case LayoutTrack.TURNOUT_D:
                return findLayoutTurnoutByName(name);
            case LayoutTrack.LEVEL_XING_A:
            case LayoutTrack.LEVEL_XING_B:
            case LayoutTrack.LEVEL_XING_C:
            case LayoutTrack.LEVEL_XING_D:
                return findLevelXingByName(name);
            case LayoutTrack.SLIP_A:
            case LayoutTrack.SLIP_B:
            case LayoutTrack.SLIP_C:
            case LayoutTrack.SLIP_D:
                return findLayoutSlipByName(name);
            case LayoutTrack.TRACK:
                return findTrackSegmentByName(name);
            default:
                if (type >= LayoutTrack.TURNTABLE_RAY_OFFSET) {
                    return findLayoutTurntableByName(name);
                }
        }
        log.error("did not find Object '" + name + "' of type " + type);
        return null;
    }

    /**
     * find object by name
     *
     * @param name the name of the object that you are looking for
     * @return object the named object
     */
    // NOTE: This replacement routine for findObjectByTypeAndName (above)
    // uses the unique name prefixes to determine what type of item to find.
    // Currently this routine (like the one above that it replaces) is only
    // called by the setObjects routine in TrackSegment.java however in the
    // move toward encapsulation this routine should see a lot more usage;
    // specifically, instead of a TON of "if (type == XXX) { findXXXByName(...)...}"
    // code you would just call this method instead.
    public LayoutTrack findObjectByName(String name) {
        LayoutTrack result = null;   // assume failure (pessimist!)
        if ((name != null) && !name.isEmpty()) {
            if (name.startsWith("TO")) {
                result = findLayoutTurnoutByName(name);
            } else if (name.startsWith("A") || name.startsWith("EB") || name.startsWith("EC") || name.matches("F\\d+-A-\\d+")) {
                result = findPositionablePointByName(name);
            } else if (name.startsWith("X")) {
                result = findLevelXingByName(name);
            } else if (name.startsWith("SL")) {
                result = findLayoutSlipByName(name);
            } else if (name.startsWith("TUR")) {
                result = findLayoutTurntableByName(name);
            } else if (name.startsWith("T") || name.matches("F\\d+-S-\\d+")) {  // (this prefix has to go after "TO" & "TUR" prefixes above)
                result = findTrackSegmentByName(name);
            } else if (name.endsWith("-EB")) {  //BUGFIX: a 3rd party JMRI exporter gets this one wrong.
                result = findPositionablePointByName(name);
            } else {
                log.warn("findObjectByName({}): unknown type name prefix", name);
            }
            if (result == null) {
                log.debug("findObjectByName({}) returned null", name);
            }
        }
        return result;
    }

    /**
     * Determine the first unused object name...
     *
     * @param inPrefix     ...with this prefix...
     * @param inStartIndex ...and this starting index...
     * @return the first unused object name
     */
    public String uniqueName(String inPrefix, int inStartIndex) {
        String result;
        for (int idx = inStartIndex; true; idx++) {
            result = String.format("%s%d", inPrefix, idx);
            if ((inPrefix.equals("S") // LayoutShape?
                    && (findLayoutShapeByName(result) == null))
                    || (findObjectByName(result) == null)) {
                break;
            }
        }
        return result;
    }

    /**
     * Determine the first unused object name...
     *
     * @param inPrefix     ...with this prefix...
     * @return the first unused object name
     */
    public String uniqueName(String inPrefix) {
        return uniqueName(inPrefix, 1);
    }

    private final static Logger log = LoggerFactory.getLogger(LayoutEditorFindItems.class);
}

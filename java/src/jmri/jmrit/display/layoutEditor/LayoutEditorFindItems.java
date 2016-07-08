package jmri.jmrit.display.layoutEditor;

import java.util.ArrayList;
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
*/
public class LayoutEditorFindItems {

    LayoutEditor layoutEditor;

    public LayoutEditorFindItems(LayoutEditor editor) {
        layoutEditor = editor;
    }

    public TrackSegment findTrackSegmentByName(String name) {
        if (name.length() <= 0) {
            return null;
        }
        for (int i = 0; i < layoutEditor.trackList.size(); i++) {
            TrackSegment t = layoutEditor.trackList.get(i);
            if (t.getID().equals(name)) {
                return t;
            }
        }
        return null;
    }

    public PositionablePoint findPositionablePointByName(String name) {
        if (name.length() <= 0) {
            return null;
        }
        for (int i = 0; i < layoutEditor.pointList.size(); i++) {
            PositionablePoint p = layoutEditor.pointList.get(i);
            if (p.getID().equals(name)) {
                return p;
            }
        }
        return null;
    }

    public PositionablePoint findPositionablePointAtTrackSegments(TrackSegment tr1, TrackSegment tr2) {
        for (int i = 0; i < layoutEditor.pointList.size(); i++) {
            PositionablePoint p = layoutEditor.pointList.get(i);
            if (((p.getConnect1() == tr1) && (p.getConnect2() == tr2))
                    || ((p.getConnect1() == tr2) && (p.getConnect2() == tr1))) {
                return p;
            }
        }
        return null;
    }

    public PositionablePoint findPositionableLinkPoint(LayoutBlock blk1) {
        for (PositionablePoint p : layoutEditor.pointList) {
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
        if (name.length() <= 0) {
            return null;
        }
        ArrayList<TrackSegment> ts = new ArrayList<TrackSegment>();
        for (int i = 0; i < layoutEditor.trackList.size(); i++) {
            TrackSegment t = layoutEditor.trackList.get(i);
            if (t.getBlockName().equals(name)) {
                ts.add(t);
            }
        }
        return ts;
    }

    public PositionablePoint findPositionablePointByEastBoundSignal(String signalName) {
        for (int i = 0; i < layoutEditor.pointList.size(); i++) {
            PositionablePoint p = layoutEditor.pointList.get(i);
            if (p.getEastBoundSignal().equals(signalName)) {
                return p;
            }
        }
        return null;
    }

    public PositionablePoint findPositionablePointByWestBoundSignal(String signalName) {
        for (int i = 0; i < layoutEditor.pointList.size(); i++) {
            PositionablePoint p = layoutEditor.pointList.get(i);
            if (p.getWestBoundSignal().equals(signalName)) {
                return p;
            }
        }
        return null;
    }

    public PositionablePoint findPositionablePointByWestBoundBean(NamedBean bean) {
        if (bean instanceof SignalMast) {
            for (PositionablePoint p : layoutEditor.pointList) {
                if (p.getWestBoundSignalMast() == bean) {
                    return p;
                }
            }
        } else if (bean instanceof Sensor) {
            for (PositionablePoint p : layoutEditor.pointList) {
                if (p.getWestBoundSensor() == bean) {
                    return p;
                }
            }
        } else if (bean instanceof SignalHead) {
            for (PositionablePoint p : layoutEditor.pointList) {
                if (p.getWestBoundSignal().equals(bean.getSystemName())
                        || p.getWestBoundSignal().equals(bean.getSystemName())) {
                    return p;
                }
            }
        }
        return null;
    }

    public PositionablePoint findPositionablePointByEastBoundBean(NamedBean bean) {
        if (bean instanceof SignalMast) {
            for (PositionablePoint p : layoutEditor.pointList) {
                if (p.getEastBoundSignalMast() == bean) {
                    return p;
                }
            }
        } else if (bean instanceof Sensor) {
            for (PositionablePoint p : layoutEditor.pointList) {
                if (p.getEastBoundSensor() == bean) {
                    return p;
                }
            }
        } else if (bean instanceof SignalHead) {
            for (PositionablePoint p : layoutEditor.pointList) {
                if (p.getEastBoundSignal().equals(bean.getSystemName())
                        || p.getEastBoundSignal().equals(bean.getSystemName())) {
                    return p;
                }
            }
        }
        return null;
    }

    public PositionablePoint findPositionablePointByWestBoundSignalMast(String signalMastName) {
        for (int i = 0; i < layoutEditor.pointList.size(); i++) {
            PositionablePoint p = layoutEditor.pointList.get(i);
            if (p.getWestBoundSignalMastName().equals(signalMastName)) {
                return p;
            }
        }
        return null;
    }

    public PositionablePoint findPositionablePointByBean(NamedBean bean) {
        if (bean instanceof SignalMast) {
            for (PositionablePoint p : layoutEditor.pointList) {
                if (p.getWestBoundSignalMast() == bean
                        || p.getEastBoundSignalMast() == bean) {
                    return p;
                }
            }
        } else if (bean instanceof Sensor) {
            for (PositionablePoint p : layoutEditor.pointList) {
                if (p.getWestBoundSensor() == bean
                        || p.getEastBoundSensor() == bean) {
                    return p;
                }
            }
        } else if (bean instanceof SignalHead) {
            for (PositionablePoint p : layoutEditor.pointList) {
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

    public LayoutTurnout findLayoutTurnoutBySignalMast(String signalMastName) throws IllegalArgumentException {
        return findLayoutTurnoutByBean(jmri.InstanceManager.getDefault(jmri.SignalMastManager.class).provideSignalMast(signalMastName));
    }

    public LayoutTurnout findLayoutTurnoutByBean(NamedBean bean) {
        if (bean instanceof SignalMast) {
            for (LayoutTurnout t : layoutEditor.turnoutList) {
                if (t.getSignalAMast() == bean
                        || t.getSignalBMast() == bean
                        || t.getSignalCMast() == bean
                        || t.getSignalDMast() == bean) {
                    return t;
                }
            }
        } else if (bean instanceof Sensor) {
            for (LayoutTurnout t : layoutEditor.turnoutList) {
                if (t.getSensorA() == bean
                        || t.getSensorB() == bean
                        || t.getSensorC() == bean
                        || t.getSensorD() == bean) {
                    return t;
                }
            }
        } else if (bean instanceof SignalHead) {
            for (LayoutTurnout t : layoutEditor.turnoutList) {
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
            for (LayoutTurnout t : layoutEditor.turnoutList) {
                if (bean.equals(t.getTurnout())) {
                    return t;
                }
            }
        }
        return null;
    }

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
        if (bean instanceof SignalMast) {
            for (LevelXing l : layoutEditor.xingList) {
                if (l.getSignalAMast() == bean
                        || l.getSignalBMast() == bean
                        || l.getSignalCMast() == bean
                        || l.getSignalDMast() == bean) {
                    return l;
                }
            }
        } else if (bean instanceof Sensor) {
            for (LevelXing l : layoutEditor.xingList) {
                if (l.getSensorA() == bean
                        || l.getSensorB() == bean
                        || l.getSensorC() == bean
                        || l.getSensorD() == bean) {
                    return l;
                }
            }

        } else if (bean instanceof SignalHead) {
            for (LevelXing l : layoutEditor.xingList) {
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
        if (bean instanceof SignalMast) {
            for (LayoutSlip l : layoutEditor.slipList) {
                if (l.getSignalAMast() == bean
                        || l.getSignalBMast() == bean
                        || l.getSignalCMast() == bean
                        || l.getSignalDMast() == bean) {
                    return l;
                }
            }
        } else if (bean instanceof Sensor) {
            for (LayoutSlip l : layoutEditor.slipList) {
                if (l.getSensorA() == bean
                        || l.getSensorB() == bean
                        || l.getSensorC() == bean
                        || l.getSensorD() == bean) {
                    return l;
                }
            }
        } else if (bean instanceof SignalHead) {
            for (LayoutSlip l : layoutEditor.slipList) {
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
            for (LayoutSlip l : layoutEditor.slipList) {
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
        for (int i = 0; i < layoutEditor.pointList.size(); i++) {
            PositionablePoint p = layoutEditor.pointList.get(i);
            if (p.getEastBoundSensorName().equals(sensorName)) {
                return p;
            }
        }
        return null;
    }

    public PositionablePoint findPositionablePointByWestBoundSensor(String sensorName) {
        for (int i = 0; i < layoutEditor.pointList.size(); i++) {
            PositionablePoint p = layoutEditor.pointList.get(i);
            if (p.getWestBoundSensorName().equals(sensorName)) {
                return p;
            }

        }
        return null;
    }

    public LayoutTurnout findLayoutTurnoutByName(String name) {
        if (name.length() <= 0) {
            return null;
        }
        for (int i = 0; i < layoutEditor.turnoutList.size(); i++) {
            LayoutTurnout t = layoutEditor.turnoutList.get(i);
            if (t.getName().equals(name)) {
                return t;
            }
        }
        return null;
    }

    public LayoutTurnout findLayoutTurnoutByTurnoutName(String name) {
        if (name.length() <= 0) {
            return null;
        }
        for (int i = 0; i < layoutEditor.turnoutList.size(); i++) {
            LayoutTurnout t = layoutEditor.turnoutList.get(i);
            if (t.getTurnoutName().equals(name)) {
                return t;
            }
        }
        return null;
    }

    public LevelXing findLevelXingByName(String name) {
        if (name.length() <= 0) {
            return null;
        }
        for (int i = 0; i < layoutEditor.xingList.size(); i++) {
            LevelXing x = layoutEditor.xingList.get(i);
            if (x.getID().equals(name)) {
                return x;
            }
        }
        return null;
    }

    public LayoutSlip findLayoutSlipByName(String name) {
        if (name.length() <= 0) {
            return null;
        }
        for (int i = 0; i < layoutEditor.slipList.size(); i++) {
            LayoutSlip x = layoutEditor.slipList.get(i);
            if (x.getName().equals(name)) {
                return x;
            }
        }
        return null;
    }

    public LayoutTurntable findLayoutTurntableByName(String name) {
        if (name.length() <= 0) {
            return null;
        }
        for (int i = 0; i < layoutEditor.turntableList.size(); i++) {
            LayoutTurntable x = layoutEditor.turntableList.get(i);
            if (x.getID().equals(name)) {
                return x;
            }
        }
        return null;
    }

    public Object findObjectByTypeAndName(int type, String name) {
        if (name.length() <= 0) {
            return null;
        }
        switch (type) {
            case LayoutEditor.NONE:
                return null;
            case LayoutEditor.POS_POINT:
                return findPositionablePointByName(name);
            case LayoutEditor.TURNOUT_A:
            case LayoutEditor.TURNOUT_B:
            case LayoutEditor.TURNOUT_C:
            case LayoutEditor.TURNOUT_D:
                return findLayoutTurnoutByName(name);
            case LayoutEditor.LEVEL_XING_A:
            case LayoutEditor.LEVEL_XING_B:
            case LayoutEditor.LEVEL_XING_C:
            case LayoutEditor.LEVEL_XING_D:
                return findLevelXingByName(name);
            case LayoutEditor.SLIP_A:
            case LayoutEditor.SLIP_B:
            case LayoutEditor.SLIP_C:
            case LayoutEditor.SLIP_D:
                return findLayoutSlipByName(name);
            case LayoutEditor.TRACK:
                return findTrackSegmentByName(name);
            default:
                if (type >= LayoutEditor.TURNTABLE_RAY_OFFSET) {
                    return findLayoutTurntableByName(name);
                }
        }
        log.error("did not find Object '" + name + "' of type " + type);
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(LayoutEditorFindItems.class.getName());
}

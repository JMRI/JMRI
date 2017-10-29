package jmri.jmrit.operations.trains;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Loads and stores the switch list text strings.
 *
 * @author Daniel Boudreau Copyright (C) 2013
 * 
 *
 */
public class TrainSwitchListText {

    private static String switchListFor = Bundle.getMessage("SwitchListFor");
    private static String scheduledWork = Bundle.getMessage("ScheduledWork");

    private static String departsAt = Bundle.getMessage("DepartsAt");
    private static String departsAtExpectedArrival = Bundle.getMessage("DepartsAtExpectedArrival");
    private static String departedExpected = Bundle.getMessage("DepartedExpected");

    private static String visitNumber = Bundle.getMessage("VisitNumber");
    private static String visitNumberDeparted = Bundle.getMessage("VisitNumberDeparted"); // this get's appended to "no scheduled work at"
    private static String visitNumberTerminates = Bundle.getMessage("VisitNumberTerminates");
    private static String visitNumberTerminatesDeparted = Bundle.getMessage("VisitNumberTerminatesDeparted");
    private static String visitNumberDone = Bundle.getMessage("VisitNumberDone");

    private static String trainDirectionChange = Bundle.getMessage("TrainDirectionChange");
    private static String noCarPickUps = Bundle.getMessage("NoCarPickUps");
    private static String noCarDrops = Bundle.getMessage("NoCarDrops");
    private static String trainDone = Bundle.getMessage("TrainDone");

    private static String trainDepartsCars = Bundle.getMessage("TrainDepartsCars");
    private static String trainDepartsLoads = Bundle.getMessage("TrainDepartsLoads");
    
    private static String switchListByTrack = Bundle.getMessage("SwitchListByTrack");
    private static String holdCar = Bundle.getMessage("HoldCar"); 

    public static String getStringSwitchListFor() {
        return switchListFor;
    }

    public static void setStringSwitchListFor(String s) {
        switchListFor = s;
    }

    public static String getStringScheduledWork() {
        return scheduledWork;
    }

    public static void setStringScheduledWork(String s) {
        scheduledWork = s;
    }

    public static String getStringDepartsAt() {
        return departsAt;
    }

    public static void setStringDepartsAt(String s) {
        departsAt = s;
    }

    public static String getStringDepartsAtExpectedArrival() {
        return departsAtExpectedArrival;
    }

    public static void setStringDepartsAtExpectedArrival(String s) {
        departsAtExpectedArrival = s;
    }

    public static String getStringDepartedExpected() {
        return departedExpected;
    }

    public static void setStringDepartedExpected(String s) {
        departedExpected = s;
    }

    public static String getStringVisitNumber() {
        return visitNumber;
    }

    public static void setStringVisitNumber(String s) {
        visitNumber = s;
    }

    public static String getStringVisitNumberDeparted() {
        return visitNumberDeparted;
    }

    public static void setStringVisitNumberDeparted(String s) {
        visitNumberDeparted = s;
    }

    public static String getStringVisitNumberTerminates() {
        return visitNumberTerminates;
    }

    public static void setStringVisitNumberTerminates(String s) {
        visitNumberTerminates = s;
    }

    public static String getStringVisitNumberTerminatesDeparted() {
        return visitNumberTerminatesDeparted;
    }

    public static void setStringVisitNumberTerminatesDeparted(String s) {
        visitNumberTerminatesDeparted = s;
    }

    public static String getStringVisitNumberDone() {
        return visitNumberDone;
    }

    public static void setStringVisitNumberDone(String s) {
        visitNumberDone = s;
    }

    public static String getStringTrainDirectionChange() {
        return trainDirectionChange;
    }

    public static void setStringTrainDirectionChange(String s) {
        trainDirectionChange = s;
    }

    public static String getStringNoCarPickUps() {
        return noCarPickUps;
    }

    public static void setStringNoCarPickUps(String s) {
        noCarPickUps = s;
    }

    public static String getStringNoCarDrops() {
        return noCarDrops;
    }

    public static void setStringNoCarDrops(String s) {
        noCarDrops = s;
    }

    public static String getStringTrainDone() {
        return trainDone;
    }

    public static void setStringTrainDone(String s) {
        trainDone = s;
    }

    public static String getStringTrainDepartsCars() {
        return trainDepartsCars;
    }

    public static void setStringTrainDepartsCars(String s) {
        trainDepartsCars = s;
    }

    public static String getStringTrainDepartsLoads() {
        return trainDepartsLoads;
    }

    public static void setStringTrainDepartsLoads(String s) {
        trainDepartsLoads = s;
    }
    
    public static String getStringSwitchListByTrack() {
        return switchListByTrack;
    }

    public static void setStringSwitchListByTrack(String s) {
        switchListByTrack = s;
    }
    
    public static String getStringHoldCar() {
        return holdCar;
    }

    public static void setStringHoldCar(String s) {
        holdCar = s;
    }

    // must synchronize changes with operation-config.dtd
    public static Element store() {
        Element values;
        Element e = new Element(Xml.SWITCH_LIST_TEXT_STRINGS);
        // only save strings that have been modified
        if (!getStringSwitchListFor().equals(Bundle.getMessage("SwitchListFor"))) {
            e.addContent(values = new Element(Xml.SWICH_LIST_FOR));
            values.setAttribute(Xml.TEXT, getStringSwitchListFor());
        }
        if (!getStringScheduledWork().equals(Bundle.getMessage("ScheduledWork"))) {
            e.addContent(values = new Element(Xml.SCHEDULED_WORK_TRAIN));
            values.setAttribute(Xml.TEXT, getStringScheduledWork());
        }

        if (!getStringDepartsAt().equals(Bundle.getMessage("DepartsAt"))) {
            e.addContent(values = new Element(Xml.DEPARTS_AT));
            values.setAttribute(Xml.TEXT, getStringDepartsAt());
        }
        if (!getStringDepartsAtExpectedArrival().equals(Bundle.getMessage("DepartsAtExpectedArrival"))) {
            e.addContent(values = new Element(Xml.DEPARTS_EXPECTED_ARRIVAL));
            values.setAttribute(Xml.TEXT, getStringDepartsAtExpectedArrival());
        }
        if (!getStringDepartedExpected().equals(Bundle.getMessage("DepartedExpected"))) {
            e.addContent(values = new Element(Xml.DEPARTED_EXPECTED));
            values.setAttribute(Xml.TEXT, getStringDepartedExpected());
        }

        if (!getStringVisitNumber().equals(Bundle.getMessage("VisitNumber"))) {
            e.addContent(values = new Element(Xml.VISIT_NUMBER));
            values.setAttribute(Xml.TEXT, getStringVisitNumber());
        }
        if (!getStringVisitNumberDeparted().equals(Bundle.getMessage("VisitNumberDeparted"))) {
            e.addContent(values = new Element(Xml.VISIT_NUMBER_DEPARTED));
            values.setAttribute(Xml.TEXT, getStringVisitNumberDeparted());
        }
        if (!getStringVisitNumberTerminates().equals(Bundle.getMessage("VisitNumberTerminates"))) {
            e.addContent(values = new Element(Xml.VISIT_NUMBER_TERMINATES));
            values.setAttribute(Xml.TEXT, getStringVisitNumberTerminates());
        }
        if (!getStringVisitNumberTerminatesDeparted().equals(Bundle.getMessage("VisitNumberTerminatesDeparted"))) {
            e.addContent(values = new Element(Xml.VISIT_NUMBER_TERMINATES_DEPARTED));
            values.setAttribute(Xml.TEXT, getStringVisitNumberTerminatesDeparted());
        }
        if (!getStringVisitNumberDone().equals(Bundle.getMessage("VisitNumberDone"))) {
            e.addContent(values = new Element(Xml.VISIT_NUMBER_DONE));
            values.setAttribute(Xml.TEXT, getStringVisitNumberDone());
        }

        if (!getStringTrainDirectionChange().equals(Bundle.getMessage("TrainDirectionChange"))) {
            e.addContent(values = new Element(Xml.TRAIN_DIRECTION_CHANGE));
            values.setAttribute(Xml.TEXT, getStringTrainDirectionChange());
        }
        if (!getStringNoCarPickUps().equals(Bundle.getMessage("NoCarPickUps"))) {
            e.addContent(values = new Element(Xml.NO_CAR_PICK_UPS));
            values.setAttribute(Xml.TEXT, getStringNoCarPickUps());
        }
        if (!getStringNoCarDrops().equals(Bundle.getMessage("NoCarDrops"))) {
            e.addContent(values = new Element(Xml.NO_CAR_SET_OUTS));
            values.setAttribute(Xml.TEXT, getStringNoCarDrops());
        }
        if (!getStringTrainDone().equals(Bundle.getMessage("TrainDone"))) {
            e.addContent(values = new Element(Xml.TRAIN_DONE));
            values.setAttribute(Xml.TEXT, getStringTrainDone());
        }
        if (!getStringTrainDepartsCars().equals(Bundle.getMessage("TrainDepartsCars"))) {
            e.addContent(values = new Element(Xml.TRAIN_DEPARTS_CARS));
            values.setAttribute(Xml.TEXT, getStringTrainDepartsCars());
        }
        if (!getStringTrainDepartsLoads().equals(Bundle.getMessage("TrainDepartsLoads"))) {
            e.addContent(values = new Element(Xml.TRAIN_DEPARTS_LOADS));
            values.setAttribute(Xml.TEXT, getStringTrainDepartsLoads());
        }
        if (!getStringSwitchListByTrack().equals(Bundle.getMessage("SwitchListByTrack"))) {
            e.addContent(values = new Element(Xml.SWITCH_LIST_TRACK));
            values.setAttribute(Xml.TEXT, getStringSwitchListByTrack());
        }
        if (!getStringHoldCar().equals(Bundle.getMessage("HoldCar"))) {
            e.addContent(values = new Element(Xml.HOLD_CAR));
            values.setAttribute(Xml.TEXT, getStringHoldCar());
        }

        return e;
    }

    public static void load(Element e) {
        Element emts = e.getChild(Xml.SWITCH_LIST_TEXT_STRINGS);
        if (emts == null) {
            return;
        }
        Attribute a;
        if (emts.getChild(Xml.SWICH_LIST_FOR) != null) {
            if ((a = emts.getChild(Xml.SWICH_LIST_FOR).getAttribute(Xml.TEXT)) != null) {
                setStringSwitchListFor(a.getValue());
            }
        }
        if (emts.getChild(Xml.SCHEDULED_WORK_TRAIN) != null) {
            if ((a = emts.getChild(Xml.SCHEDULED_WORK_TRAIN).getAttribute(Xml.TEXT)) != null) {
                setStringScheduledWork(a.getValue());
            }
        }

        if (emts.getChild(Xml.DEPARTS_AT) != null) {
            if ((a = emts.getChild(Xml.DEPARTS_AT).getAttribute(Xml.TEXT)) != null) {
                setStringDepartsAt(a.getValue());
            }
        }
        if (emts.getChild(Xml.DEPARTS_EXPECTED_ARRIVAL) != null) {
            if ((a = emts.getChild(Xml.DEPARTS_EXPECTED_ARRIVAL).getAttribute(Xml.TEXT)) != null) {
                setStringDepartsAtExpectedArrival(a.getValue());
            }
        }
        if (emts.getChild(Xml.DEPARTED_EXPECTED) != null) {
            if ((a = emts.getChild(Xml.DEPARTED_EXPECTED).getAttribute(Xml.TEXT)) != null) {
                setStringDepartedExpected(a.getValue());
            }
        }

        if (emts.getChild(Xml.VISIT_NUMBER) != null) {
            if ((a = emts.getChild(Xml.VISIT_NUMBER).getAttribute(Xml.TEXT)) != null) {
                setStringVisitNumber(a.getValue());
            }
        }
        if (emts.getChild(Xml.VISIT_NUMBER_DEPARTED) != null) {
            if ((a = emts.getChild(Xml.VISIT_NUMBER_DEPARTED).getAttribute(Xml.TEXT)) != null) {
                setStringVisitNumberDeparted(a.getValue());
            }
        }
        if (emts.getChild(Xml.VISIT_NUMBER_TERMINATES) != null) {
            if ((a = emts.getChild(Xml.VISIT_NUMBER_TERMINATES).getAttribute(Xml.TEXT)) != null) {
                setStringVisitNumberTerminates(a.getValue());
            }
        }
        if (emts.getChild(Xml.VISIT_NUMBER_TERMINATES_DEPARTED) != null) {
            if ((a = emts.getChild(Xml.VISIT_NUMBER_TERMINATES_DEPARTED).getAttribute(Xml.TEXT)) != null) {
                setStringVisitNumberTerminatesDeparted(a.getValue());
            }
        }
        if (emts.getChild(Xml.VISIT_NUMBER_DONE) != null) {
            if ((a = emts.getChild(Xml.VISIT_NUMBER_DONE).getAttribute(Xml.TEXT)) != null) {
                setStringVisitNumberDone(a.getValue());
            }
        }

        if (emts.getChild(Xml.TRAIN_DIRECTION_CHANGE) != null) {
            if ((a = emts.getChild(Xml.TRAIN_DIRECTION_CHANGE).getAttribute(Xml.TEXT)) != null) {
                setStringTrainDirectionChange(a.getValue());
            }
        }
        if (emts.getChild(Xml.NO_CAR_PICK_UPS) != null) {
            if ((a = emts.getChild(Xml.NO_CAR_PICK_UPS).getAttribute(Xml.TEXT)) != null) {
                setStringNoCarPickUps(a.getValue());
            }
        }
        if (emts.getChild(Xml.NO_CAR_SET_OUTS) != null) {
            if ((a = emts.getChild(Xml.NO_CAR_SET_OUTS).getAttribute(Xml.TEXT)) != null) {
                setStringNoCarDrops(a.getValue());
            }
        }
        if (emts.getChild(Xml.TRAIN_DONE) != null) {
            if ((a = emts.getChild(Xml.TRAIN_DONE).getAttribute(Xml.TEXT)) != null) {
                setStringTrainDone(a.getValue());
            }
        }
        if (emts.getChild(Xml.TRAIN_DEPARTS_CARS) != null) {
            if ((a = emts.getChild(Xml.TRAIN_DEPARTS_CARS).getAttribute(Xml.TEXT)) != null) {
                setStringTrainDepartsCars(a.getValue());
            }
        }
        if (emts.getChild(Xml.TRAIN_DEPARTS_LOADS) != null) {
            if ((a = emts.getChild(Xml.TRAIN_DEPARTS_LOADS).getAttribute(Xml.TEXT)) != null) {
                setStringTrainDepartsLoads(a.getValue());
            }
        }
        if (emts.getChild(Xml.SWITCH_LIST_TRACK) != null) {
            if ((a = emts.getChild(Xml.SWITCH_LIST_TRACK).getAttribute(Xml.TEXT)) != null) {
                setStringSwitchListByTrack(a.getValue());
            }
        }
        if (emts.getChild(Xml.HOLD_CAR) != null) {
            if ((a = emts.getChild(Xml.HOLD_CAR).getAttribute(Xml.TEXT)) != null) {
                setStringHoldCar(a.getValue());
            }
        }
    }
}

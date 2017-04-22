package jmri.jmrit.operations.trains;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Loads and stores the manifest text strings.
 *
 * @author Daniel Boudreau Copyright (C) 2013
 *
 */
public class TrainManifestText {

    private static String manifestForTrain = Bundle.getMessage("ManifestForTrain");
    private static String valid = Bundle.getMessage("Valid");
    private static String scheduledWorkAt = Bundle.getMessage("ScheduledWorkAt");
    private static String departureTime = Bundle.getMessage("WorkDepartureTime");
    private static String arrivalTime = Bundle.getMessage("WorkArrivalTime");
    private static String noScheduledWorkAt = Bundle.getMessage("NoScheduledWorkAt");
    private static String noScheduledWorkAtRouteComment = Bundle.getMessage("NoScheduledWorkAtWithRouteComment");
    private static String departTime = Bundle.getMessage("departureTime"); // this get's appended to "no scheduled work at"
    private static String trainDepartsCars = Bundle.getMessage("TrainDepartsCars");
    private static String trainDepartsLoads = Bundle.getMessage("TrainDepartsLoads");
    private static String trainTerminatesIn = Bundle.getMessage("TrainTerminatesIn");

    private static String destination = Bundle.getMessage("destination");
    private static String from = Bundle.getMessage("from");
    private static String to = Bundle.getMessage("to");

    private static String fd = Bundle.getMessage("FD");
    private static String dest = Bundle.getMessage("dest");

    private static String addHelpersAt = Bundle.getMessage("AddHelpersAt");
    private static String removeHelpersAt = Bundle.getMessage("RemoveHelpersAt");
    private static String locoChangeAt = Bundle.getMessage("LocoChangeAt");
    private static String cabooseChangeAt = Bundle.getMessage("CabooseChangeAt");
    private static String locoAndCabooseChangeAt = Bundle.getMessage("LocoAndCabooseChangeAt");

    public static String getStringManifestForTrain() {
        return manifestForTrain;
    }

    public static void setStringManifestForTrain(String s) {
        manifestForTrain = s;
    }

    public static String getStringValid() {
        return valid;
    }

    public static void setStringValid(String s) {
        valid = s;
    }

    public static String getStringScheduledWork() {
        return scheduledWorkAt;
    }

    public static void setStringScheduledWork(String s) {
        scheduledWorkAt = s;
    }

    public static String getStringWorkDepartureTime() {
        return departureTime;
    }

    public static void setStringWorkDepartureTime(String s) {
        departureTime = s;
    }

    public static String getStringWorkArrivalTime() {
        return arrivalTime;
    }

    public static void setStringWorkArrivalTime(String s) {
        arrivalTime = s;
    }

    public static String getStringNoScheduledWork() {
        return noScheduledWorkAt;
    }

    public static void setStringNoScheduledWork(String s) {
        noScheduledWorkAt = s;
    }

    public static String getStringNoScheduledWorkWithRouteComment() {
        return noScheduledWorkAtRouteComment;
    }

    public static void setStringNoScheduledWorkWithRouteComment(String s) {
        noScheduledWorkAtRouteComment = s;
    }

    public static String getStringDepartTime() {
        return departTime;
    }

    public static void setStringDepartTime(String s) {
        departTime = s;
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

    public static String getStringTrainTerminates() {
        return trainTerminatesIn;
    }

    public static void setStringTrainTerminates(String s) {
        trainTerminatesIn = s;
    }

    public static String getStringDestination() {
        return destination;
    }

    public static void setStringDestination(String s) {
        destination = s;
    }

    public static String getStringFrom() {
        return from;
    }

    public static void setStringFrom(String s) {
        from = s;
    }

    public static String getStringTo() {
        return to;
    }

    public static void setStringTo(String s) {
        to = s;
    }

    public static String getStringDest() {
        return dest;
    }

    public static void setStringDest(String s) {
        dest = s;
    }

    public static String getStringFinalDestination() {
        return fd;
    }

    public static void setStringFinalDestination(String s) {
        fd = s;
    }

    public static String getStringAddHelpers() {
        return addHelpersAt;
    }

    public static void setStringAddHelpers(String s) {
        addHelpersAt = s;
    }

    public static String getStringRemoveHelpers() {
        return removeHelpersAt;
    }

    public static void setStringRemoveHelpers(String s) {
        removeHelpersAt = s;
    }

    public static String getStringLocoChange() {
        return locoChangeAt;
    }

    public static void setStringLocoChange(String s) {
        locoChangeAt = s;
    }

    public static String getStringCabooseChange() {
        return cabooseChangeAt;
    }

    public static void setStringCabooseChange(String s) {
        cabooseChangeAt = s;
    }

    public static String getStringLocoAndCabooseChange() {
        return locoAndCabooseChangeAt;
    }

    public static void setStringLocoAndCabooseChange(String s) {
        locoAndCabooseChangeAt = s;
    }

    // must synchronize changes with operation-config.dtd
    public static Element store() {
        Element values;
        Element e = new Element(Xml.MANIFEST_TEXT_STRINGS);
        // only save strings that have been modified
        if (!getStringManifestForTrain().equals(Bundle.getMessage("ManifestForTrain"))) {
            e.addContent(values = new Element(Xml.MANIFEST_FOR_TRAIN));
            values.setAttribute(Xml.TEXT, getStringManifestForTrain());
        }
        if (!getStringValid().equals(Bundle.getMessage("Valid"))) {
            e.addContent(values = new Element(Xml.VALID));
            values.setAttribute(Xml.TEXT, getStringValid());
        }
        if (!getStringScheduledWork().equals(Bundle.getMessage("ScheduledWorkAt"))) {
            e.addContent(values = new Element(Xml.SCHEDULED_WORK));
            values.setAttribute(Xml.TEXT, getStringScheduledWork());
        }
        if (!getStringWorkDepartureTime().equals(Bundle.getMessage("WorkDepartureTime"))) {
            e.addContent(values = new Element(Xml.WORK_DEPARTURE_TIME));
            values.setAttribute(Xml.TEXT, getStringWorkDepartureTime());
        }
        if (!getStringWorkArrivalTime().equals(Bundle.getMessage("WorkArrivalTime"))) {
            e.addContent(values = new Element(Xml.WORK_ARRIVAL_TIME));
            values.setAttribute(Xml.TEXT, getStringWorkArrivalTime());
        }
        if (!getStringNoScheduledWork().equals(Bundle.getMessage("NoScheduledWorkAt"))) {
            e.addContent(values = new Element(Xml.NO_SCHEDULED_WORK));
            values.setAttribute(Xml.TEXT, getStringNoScheduledWork());
        }
        if (!getStringNoScheduledWorkWithRouteComment().equals(Bundle.getMessage("NoScheduledWorkAtWithRouteComment"))) {
            e.addContent(values = new Element(Xml.NO_SCHEDULED_WORK_ROUTE_COMMENT));
            values.setAttribute(Xml.TEXT, getStringNoScheduledWorkWithRouteComment());
        }
        if (!getStringDepartTime().equals(Bundle.getMessage("departureTime"))) {
            e.addContent(values = new Element(Xml.DEPART_TIME));
            values.setAttribute(Xml.TEXT, getStringDepartTime());
        }
        if (!getStringTrainDepartsCars().equals(Bundle.getMessage("TrainDepartsCars"))) {
            e.addContent(values = new Element(Xml.TRAIN_DEPARTS_CARS));
            values.setAttribute(Xml.TEXT, getStringTrainDepartsCars());
        }
        if (!getStringTrainDepartsLoads().equals(Bundle.getMessage("TrainDepartsLoads"))) {
            e.addContent(values = new Element(Xml.TRAIN_DEPARTS_LOADS));
            values.setAttribute(Xml.TEXT, getStringTrainDepartsLoads());
        }
        if (!getStringTrainTerminates().equals(Bundle.getMessage("TrainTerminatesIn"))) {
            e.addContent(values = new Element(Xml.TRAIN_TERMINATES));
            values.setAttribute(Xml.TEXT, getStringTrainTerminates());
        }
        if (!getStringDestination().equals(Bundle.getMessage("destination"))) {
            e.addContent(values = new Element(Xml.DESTINATION));
            values.setAttribute(Xml.TEXT, getStringDestination());
        }
        if (!getStringFrom().equals(Bundle.getMessage("from"))) {
            e.addContent(values = new Element(Xml.FROM));
            values.setAttribute(Xml.TEXT, getStringFrom());
        }
        if (!getStringTo().equals(Bundle.getMessage("to"))) {
            e.addContent(values = new Element(Xml.TO));
            values.setAttribute(Xml.TEXT, getStringTo());
        }
        if (!getStringDest().equals(Bundle.getMessage("dest"))) {
            e.addContent(values = new Element(Xml.DEST));
            values.setAttribute(Xml.TEXT, getStringDest());
        }
        if (!getStringFinalDestination().equals(Bundle.getMessage("FD"))) {
            e.addContent(values = new Element(Xml.FINAL_DEST));
            values.setAttribute(Xml.TEXT, getStringFinalDestination());
        }
        if (!getStringAddHelpers().equals(Bundle.getMessage("AddHelpersAt"))) {
            e.addContent(values = new Element(Xml.ADD_HELPERS));
            values.setAttribute(Xml.TEXT, getStringAddHelpers());
        }
        if (!getStringRemoveHelpers().equals(Bundle.getMessage("RemoveHelpersAt"))) {
            e.addContent(values = new Element(Xml.REMOVE_HELPERS));
            values.setAttribute(Xml.TEXT, getStringRemoveHelpers());
        }
        if (!getStringLocoChange().equals(Bundle.getMessage("LocoChangeAt"))) {
            e.addContent(values = new Element(Xml.LOCO_CHANGE));
            values.setAttribute(Xml.TEXT, getStringLocoChange());
        }
        if (!getStringCabooseChange().equals(Bundle.getMessage("CabooseChangeAt"))) {
            e.addContent(values = new Element(Xml.CABOOSE_CHANGE));
            values.setAttribute(Xml.TEXT, getStringCabooseChange());
        }
        if (!getStringLocoAndCabooseChange().equals(Bundle.getMessage("LocoAndCabooseChangeAt"))) {
            e.addContent(values = new Element(Xml.LOCO_CABOOSE_CHANGE));
            values.setAttribute(Xml.TEXT, getStringLocoAndCabooseChange());
        }

        return e;
    }

    public static void load(Element e) {
        Element emts = e.getChild(Xml.MANIFEST_TEXT_STRINGS);
        if (emts == null) {
            return;
        }
        Attribute a;
        if (emts.getChild(Xml.MANIFEST_FOR_TRAIN) != null) {
            if ((a = emts.getChild(Xml.MANIFEST_FOR_TRAIN).getAttribute(Xml.TEXT)) != null) {
                setStringManifestForTrain(a.getValue());
            }
        }
        if (emts.getChild(Xml.VALID) != null) {
            if ((a = emts.getChild(Xml.VALID).getAttribute(Xml.TEXT)) != null) {
                setStringValid(a.getValue());
            }
        }
        if (emts.getChild(Xml.SCHEDULED_WORK) != null) {
            if ((a = emts.getChild(Xml.SCHEDULED_WORK).getAttribute(Xml.TEXT)) != null) {
                setStringScheduledWork(a.getValue());
            }
        }
        if (emts.getChild(Xml.WORK_DEPARTURE_TIME) != null) {
            if ((a = emts.getChild(Xml.WORK_DEPARTURE_TIME).getAttribute(Xml.TEXT)) != null) {
                setStringWorkDepartureTime(a.getValue());
            }
        }
        if (emts.getChild(Xml.WORK_ARRIVAL_TIME) != null) {
            if ((a = emts.getChild(Xml.WORK_ARRIVAL_TIME).getAttribute(Xml.TEXT)) != null) {
                setStringWorkArrivalTime(a.getValue());
            }
        }
        if (emts.getChild(Xml.NO_SCHEDULED_WORK) != null) {
            if ((a = emts.getChild(Xml.NO_SCHEDULED_WORK).getAttribute(Xml.TEXT)) != null) {
                setStringNoScheduledWork(a.getValue());
            }
        }
        if (emts.getChild(Xml.NO_SCHEDULED_WORK_ROUTE_COMMENT) != null) {
            if ((a = emts.getChild(Xml.NO_SCHEDULED_WORK_ROUTE_COMMENT).getAttribute(Xml.TEXT)) != null) {
                setStringNoScheduledWorkWithRouteComment(a.getValue());
            }
        }
        if (emts.getChild(Xml.DEPART_TIME) != null) {
            if ((a = emts.getChild(Xml.DEPART_TIME).getAttribute(Xml.TEXT)) != null) {
                setStringDepartTime(a.getValue());
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
        if (emts.getChild(Xml.TRAIN_TERMINATES) != null) {
            if ((a = emts.getChild(Xml.TRAIN_TERMINATES).getAttribute(Xml.TEXT)) != null) {
                setStringTrainTerminates(a.getValue());
            }
        }

        if (emts.getChild(Xml.DESTINATION) != null) {
            if ((a = emts.getChild(Xml.DESTINATION).getAttribute(Xml.TEXT)) != null) {
                setStringDestination(a.getValue());
            }
        }
        if (emts.getChild(Xml.TO) != null) {
            if ((a = emts.getChild(Xml.TO).getAttribute(Xml.TEXT)) != null) {
                setStringTo(a.getValue());
            }
        }
        if (emts.getChild(Xml.FROM) != null) {
            if ((a = emts.getChild(Xml.FROM).getAttribute(Xml.TEXT)) != null) {
                setStringFrom(a.getValue());
            }
        }
        if (emts.getChild(Xml.DEST) != null) {
            if ((a = emts.getChild(Xml.DEST).getAttribute(Xml.TEXT)) != null) {
                setStringDest(a.getValue());
            }
        }
        if (emts.getChild(Xml.FINAL_DEST) != null) {
            if ((a = emts.getChild(Xml.FINAL_DEST).getAttribute(Xml.TEXT)) != null) {
                setStringFinalDestination(a.getValue());
            }
        }
        if (emts.getChild(Xml.ADD_HELPERS) != null) {
            if ((a = emts.getChild(Xml.ADD_HELPERS).getAttribute(Xml.TEXT)) != null) {
                setStringAddHelpers(a.getValue());
            }
        }
        if (emts.getChild(Xml.REMOVE_HELPERS) != null) {
            if ((a = emts.getChild(Xml.REMOVE_HELPERS).getAttribute(Xml.TEXT)) != null) {
                setStringRemoveHelpers(a.getValue());
            }
        }
        if (emts.getChild(Xml.LOCO_CHANGE) != null) {
            if ((a = emts.getChild(Xml.LOCO_CHANGE).getAttribute(Xml.TEXT)) != null) {
                setStringLocoChange(a.getValue());
            }
        }
        if (emts.getChild(Xml.CABOOSE_CHANGE) != null) {
            if ((a = emts.getChild(Xml.CABOOSE_CHANGE).getAttribute(Xml.TEXT)) != null) {
                setStringCabooseChange(a.getValue());
            }
        }
        if (emts.getChild(Xml.LOCO_CABOOSE_CHANGE) != null) {
            if ((a = emts.getChild(Xml.LOCO_CABOOSE_CHANGE).getAttribute(Xml.TEXT)) != null) {
                setStringLocoAndCabooseChange(a.getValue());
            }
        }
    }
}

package jmri.jmrit.operations.trains;

import java.io.IOException;
import java.util.List;
import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;
import org.apache.commons.csv.CSVPrinter;

/**
 * Contains the csv operators for manifests and switch lists
 *
 * @author Daniel Boudreau Copyright (C) 2011, 2013, 2015
 * 
 *
 */
public class TrainCsvCommon extends TrainCommon {

    protected final void printDepartureTime(CSVPrinter printer, String time) throws IOException {
        printer.printRecord("DT", Bundle.getMessage("csvDepartureTime"), time); // NOI18N
    }
    
    protected final void printHeader(CSVPrinter printer) throws IOException {
        printer.printRecord(Bundle.getMessage("csvOperator"), Bundle.getMessage("csvDescription"), Bundle.getMessage("csvParameters")); // NOI18N
    }
    
    protected final void printLocationComment(CSVPrinter printer, String comment) throws IOException {
        printer.printRecord("LC", Bundle.getMessage("csvLocationComment"), comment); // NOI18N
    }

    protected final void printLocationName(CSVPrinter printer, String name) throws IOException {
        printer.printRecord("LN", Bundle.getMessage("csvLocationName"), name); // NOI18N
    }

    protected final void printPrinterName(CSVPrinter printer, String name) throws IOException {
        printer.printRecord("PRNTR", Bundle.getMessage("csvPrinterName"), name); // NOI18N
    }

    protected final void printRailroadName(CSVPrinter printer, String name) throws IOException {
        printer.printRecord("RN", Bundle.getMessage("csvRailroadName"), name); // NOI18N
    }

    protected final void printRemoveHelpers(CSVPrinter printer) throws IOException {
        printer.printRecord("RH", Bundle.getMessage("csvRemoveHelpers")); // NOI18N
    }

    protected final void printRouteLocationComment(CSVPrinter printer, String comment) throws IOException {
        printer.printRecord("RLC", Bundle.getMessage("csvRouteLocationComment"), comment); // NOI18N
    }

    protected final void printTrainDeparts(CSVPrinter printer, String name, String direction) throws IOException {
        printer.printRecord("TD", Bundle.getMessage("csvTrainDeparts"), name, direction); // NOI18N
    }
    
    protected final void printTrainDescription(CSVPrinter printer, String description) throws IOException {
        printer.printRecord("TM", Bundle.getMessage("csvTrainManifestDescription"), description); // NOI18N
    }

    protected final void printTrainLength(CSVPrinter printer, int length, int empty, int total) throws IOException {
        printer.printRecord("TL", Bundle.getMessage("csvTrainLengthEmptiesCars"), length, empty, total); // NOI18N
    }

    protected final void printTrainName(CSVPrinter printer, String name) throws IOException {
        printer.printRecord("TN", Bundle.getMessage("csvTrainName"), name); // NOI18N
    }    

    protected final void printTrainTerminates(CSVPrinter printer, String name) throws IOException {
        printer.printRecord("TT", Bundle.getMessage("csvTrainTerminates"), name);
    }

    protected final void printTrainWeight(CSVPrinter printer, int weight) throws IOException {
        printer.printRecord("TW", Bundle.getMessage("csvTrainWeight"), weight); // NOI18N
    }

    protected final void printValidity(CSVPrinter printer, String date) throws IOException {
        printer.printRecord("VT", Bundle.getMessage("csvValid"), date); // NOI18N
    }

    protected void printCar(CSVPrinter fileOut, Car car, String code, String message, int count) throws IOException {
        fileOut.printRecord(code,
                message,
                car.getRoadName(),
                car.getNumber(),
                car.getTypeName(),
                car.getLength(),
                car.getLoadName(),
                car.getColor(),
                car.getLocationName(),
                car.getTrackName(),
                car.getDestinationName(),
                car.getDestinationTrackName(),
                car.getOwner(),
                car.getKernelName(),
                car.getComment(),
                car.getPickupComment(),
                car.getDropComment(),
                car.isCaboose() ? "C" : "",
                car.hasFred() ? "F" : "",
                car.isHazardous() ? "H" : "",
                car.getRfid(),
                car.getReturnWhenEmptyDestinationName(),
                car.getReturnWhenEmptyDestTrackName(),
                car.isUtility() ? "U" : "",
                count,
                car.getFinalDestinationName(),
                car.getFinalDestinationTrackName(),
                car.getLoadType());
    }

    protected void printEngine(CSVPrinter fileOut, Engine engine, String code, String message) throws IOException {
        fileOut.printRecord(code,
                message,
                engine.getRoadName(),
                engine.getNumber(),
                engine.getModel(),
                engine.getLength(),
                engine.getTypeName(),
                engine.getHp(),
                engine.getLocationName(),
                engine.getTrackName(),
                engine.getDestinationName(),
                engine.getDestinationTrackName(),
                engine.getOwner(),
                engine.getConsistName(),
                engine.isLead() ? "Lead loco" : "", // NOI18N
                engine.getComment(),
                engine.getRfid());
    }

    protected final void checkForEngineOrCabooseChange(CSVPrinter fileOut, Train train, RouteLocation rl) throws IOException {
        if (train.getSecondLegOptions() != Train.NO_CABOOSE_OR_FRED) {
            if (rl == train.getSecondLegStartRouteLocation()) {
                engineCsvChange(fileOut, rl, train.getSecondLegOptions());
            }
            if (rl == train.getSecondLegEndRouteLocation()) {
                printRemoveHelpers(fileOut);
            }
        }
        if (train.getThirdLegOptions() != Train.NO_CABOOSE_OR_FRED) {
            if (rl == train.getThirdLegStartRouteLocation()) {
                engineCsvChange(fileOut, rl, train.getThirdLegOptions());
            }
            if (rl == train.getThirdLegEndRouteLocation()) {
                printRemoveHelpers(fileOut);
            }
        }
    }

    protected void engineCsvChange(CSVPrinter fileOut, RouteLocation rl, int legOptions) throws IOException {
        if ((legOptions & Train.HELPER_ENGINES) == Train.HELPER_ENGINES) {
            fileOut.printRecord("AH", Bundle.getMessage("csvAddHelpers"));
        }
        if ((legOptions & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE ||
                (legOptions & Train.ADD_CABOOSE) == Train.ADD_CABOOSE) {
            fileOut.printRecord("CC", Bundle.getMessage("csvChangeCaboose"));
        }
        if ((legOptions & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES) {
            fileOut.printRecord("CL", Bundle.getMessage("csvChangeLocos"));
        }
    }

    protected void printTrackComments(CSVPrinter fileOut, RouteLocation rl, List<Car> carList) throws IOException {
        Location location = rl.getLocation();
        if (location != null) {
            List<Track> tracks = location.getTracksByNameList(null);
            for (Track track : tracks) {
                // any pick ups or set outs to this track?
                boolean pickup = false;
                boolean setout = false;
                for (Car car : carList) {
                    if (car.getRouteLocation() == rl && car.getTrack() != null && car.getTrack() == track) {
                        pickup = true;
                    }
                    if (car.getRouteDestination() == rl &&
                            car.getDestinationTrack() != null &&
                            car.getDestinationTrack() == track) {
                        setout = true;
                    }
                }
                // print the appropriate comment if there's one
                // each comment can have multiple lines
                if (pickup && setout && !track.getCommentBoth().equals(Track.NONE)) {
                    String[] comments = track.getCommentBoth().split(NEW_LINE);
                    for (String comment : comments) {
                        fileOut.printRecord("TKCB", Bundle.getMessage("csvTrackCommentBoth"), comment); // NOI18N
                    }
                } else if (pickup && !setout && !track.getCommentPickup().equals(Track.NONE)) {
                    String[] comments = track.getCommentPickup().split(NEW_LINE);
                    for (String comment : comments) {
                        fileOut.printRecord("TKCP", Bundle.getMessage("csvTrackCommentPickUp"), comment); // NOI18N
                    }
                } else if (!pickup && setout && !track.getCommentSetout().equals(Track.NONE)) {
                    String[] comments = track.getCommentSetout().split(NEW_LINE);
                    for (String comment : comments) {
                        fileOut.printRecord("TKCS", Bundle.getMessage("csvTrackCommentSetOut"), comment); // NOI18N
                    }
                }
            }
        }
    }

    protected void listCarsLocationUnknown(CSVPrinter fileOut) throws IOException {
        List<Car> cars = InstanceManager.getDefault(CarManager.class).getCarsLocationUnknown();
        if (cars.isEmpty()) {
            return; // no cars to search for!
        }
        fileOut.printRecord("SMCM", Bundle.getMessage("csvSearchMiaMessage"), Setup.getMiaComment());
        for (Car car : cars) {
            printCar(fileOut, car, "SMC", Bundle.getMessage("csvSearchMissingCar"), 0);
        }
    }
}

package jmri.jmrit.operations.trains.tools;

import java.awt.Frame;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.rollingstock.cars.*;
import jmri.jmrit.operations.rollingstock.cars.tools.PrintCarLoadsAction;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainCommon;
import jmri.util.davidflanagan.HardcopyWriter;

/**
 * Print the cars in the train.
 * <p>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author Daniel Boudreau Copyright (C) 2025
 */
public class PrintShowCarsInTrain {

    static final String NEW_LINE = "\n"; // NOI18N

    CarManager carManager = InstanceManager.getDefault(CarManager.class);
    static int fieldSize =
            InstanceManager.getDefault(CarRoads.class).getMaxNameLength() + Control.max_len_string_road_number;
    static final String TAB = padString("", fieldSize);

    public PrintShowCarsInTrain(Train train, boolean isPreview) {
        super();
        printCarsInTrain(train, isPreview);
    }

    private void printCarsInTrain(Train train, boolean isPreview) {
        if (train.isBuilt()) {
            // obtain a HardcopyWriter to do this
            try (HardcopyWriter writer =
                    new HardcopyWriter(new Frame(), Bundle.getMessage("TitleShowCarsInTrain", train.getName()),
                            Control.reportFontSize, .5,
                            .5, .5, .5, isPreview);) {

                printCarsAtLocation(writer, train, train.getCurrentRouteLocation());

            } catch (HardcopyWriter.PrintCanceledException ex) {
                log.debug("Print cancelled");
            } catch (IOException ex) {
                log.error("Error printing car roster: {}", ex.getLocalizedMessage());
            }
        }
    }

    private void printCarsAtLocation(HardcopyWriter writer, Train train, RouteLocation rl) throws IOException {
        if (rl != null) {
            // print location name followed by header
            writer.write(rl.getName() + NEW_LINE);
            writer.write(getHeader());
            for (RouteLocation rld : train.getRoute().getLocationsBySequenceList()) {
                for (Car car : carManager.getByTrainDestinationList(train)) {
                    if (TrainCommon.isNextCar(car, rl, rld, true)) {
                        log.debug("car ({}) routelocation ({}) track ({}) route destination ({})",
                                car.toString(), car
                                        .getRouteLocation().getName(),
                                car.getTrackName(), car.getRouteDestination().getName());
                        String s = car.getRoadName().split(TrainCommon.HYPHEN)[0] +
                                " " +
                                TrainCommon.splitString(car.getNumber()) +
                                NEW_LINE;
                        if (car.getRouteDestination() == rl) {
                            writer.write(TAB + TAB + s); // set out
                        } else if (car.getRouteLocation() == rl && car.getTrack() != null) {
                            writer.write(s); // pick up
                        } else {
                            writer.write(TAB + s); // in train
                        }
                    }
                }
            }
        }
    }

    private String getHeader() {
        int fieldSize =
                InstanceManager.getDefault(CarRoads.class).getMaxNameLength() + Control.max_len_string_road_number;
        String header = padString(Bundle.getMessage("Pickup"), fieldSize) +
                padString(Bundle.getMessage("InTrain"), fieldSize) +
                padString(Bundle.getMessage("SetOut"), fieldSize) +
                NEW_LINE;
        return header;
    }

    private static String padString(String s, int fieldSize) {
        return TrainCommon.padString(s, fieldSize);
    }

    private final static Logger log = LoggerFactory.getLogger(PrintCarLoadsAction.class);
}

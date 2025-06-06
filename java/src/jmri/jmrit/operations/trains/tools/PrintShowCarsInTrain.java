package jmri.jmrit.operations.trains.tools;

import java.awt.Frame;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.trainbuilder.TrainCommon;
import jmri.util.davidflanagan.HardcopyWriter;

/**
 * Print the cars in the train.
 * <p>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author Daniel Boudreau Copyright (C) 2025
 */
public class PrintShowCarsInTrain extends TrainCommon {

    static final String NEW_LINE = "\n"; // NOI18N

    static int fieldSize =
            InstanceManager.getDefault(CarRoads.class).getMaxNameLength() + Control.max_len_string_road_number;
    static final String TAB = padString("", fieldSize);

    /**
     * Prints the block order for a train at the train's current location.
     * 
     * @param train     the train.
     * @param isPreview if true preview, otherwise print
     */
    public void printCarsInTrain(Train train, boolean isPreview) {
        if (train.isBuilt()) {
            // obtain a HardcopyWriter to do this
            try (HardcopyWriter writer =
                    new HardcopyWriter(new Frame(), Bundle.getMessage("TitleShowCarsInTrain", train.getName()),
                            Control.reportFontSize, .5,
                            .5, .5, .5, isPreview);) {

                printCarsAtLocation(writer, train, train.getCurrentRouteLocation());

            } catch (HardcopyWriter.PrintCanceledException ex) {
                log.debug("Print canceled");
            } catch (IOException ex) {
                log.error("Error printing car roster: {}", ex.getLocalizedMessage());
            }
        }
    }

    public void printCarsAtLocation(HardcopyWriter writer, Train train, RouteLocation rl) throws IOException {
        if (rl != null) {
            log.debug("RouteLocation rl: {}", rl.getName());
            // print location name followed by header
            writer.write(rl.getSplitName() + NEW_LINE);
            writer.write(getHeader());
            for (RouteLocation rld : train.getRoute().getBlockingOrder()) {
                log.debug("RouteLocation rld: {}", rld.getName());
                for (Car car : carManager.getByTrainDestinationList(train)) {
                    if (isNextCar(car, rl, rld, true)) {
                        log.debug("car ({}) routelocation ({}) track ({}) route destination ({})",
                                car.toString(), car.getRouteLocation().getName(),
                                car.getTrackName(), car.getRouteDestination().getName());
                        String s = getCarId(car) + NEW_LINE;
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

    /**
     * Prints the block order for a train for each station starting at the
     * train's current location.
     * 
     * @param train     the train.
     * @param isPreview if true preview, otherwise print
     */
    public void printCarsInTrainRoute(Train train, boolean isPreview) {
        if (train.isBuilt()) {
            // obtain a HardcopyWriter to do this
            try (HardcopyWriter writer =
                    new HardcopyWriter(new Frame(), Bundle.getMessage("TitleShowCarsInTrain", train.getName()),
                            Control.reportFontSize, .5,
                            .5, .5, .5, isPreview);) {
                printCarsRoute(writer, train);
            } catch (HardcopyWriter.PrintCanceledException ex) {
                log.debug("Print canceled");
            } catch (IOException ex) {
                log.error("Error printing car roster: {}", ex.getLocalizedMessage());
            }
        }
    }

    public void printCarsRoute(HardcopyWriter writer, Train train) throws IOException {
        loadCarsInTrain(train);
        // start printing at the train's current location
        boolean foundTrainLoc = false;
        for (RouteLocation rl : train.getRoute().getLocationsBySequenceList()) {
            if (rl != null) {
                if (!foundTrainLoc && rl != train.getCurrentRouteLocation()) {
                    continue;
                }
                foundTrainLoc = true;
                log.debug("RouteLocation rl: {}", rl.getName());
                // print location name followed by header
                writer.write(rl.getSplitName() + NEW_LINE);
                if (isThereWorkAtLocation(carManager.getByTrainDestinationList(train), null, rl)) {
                    writer.write(getHeader());
                    printCars(writer, train, rl);
                    writer.write(NEW_LINE);
                }
            }
        }
    }

    List<Car> carsInTrain = new ArrayList<>();

    private void printCars(HardcopyWriter writer, Train train, RouteLocation rl) throws IOException {
        for (Car car : carManager.getByTrainDestinationList(train)) {
            if (car.getRouteLocation() == rl && !carsInTrain.contains(car)) {
                carsInTrain.add(car);
                writer.write(getCarId(car) + NEW_LINE); // pick up
            } else if (car.getRouteDestination() == rl) {
                carsInTrain.remove(car);
                writer.write(TAB + TAB + getCarId(car) + NEW_LINE); // set out
            } else if (carsInTrain.contains(car)) {
                writer.write(TAB + getCarId(car) + NEW_LINE); // in train
            }
        }
    }

    private void loadCarsInTrain(Train train) {
        carsInTrain.clear();
        for (Car car : carManager.getByTrainDestinationList(train)) {
            if (car.getTrack() == null) {
                carsInTrain.add(car);
            }
        }
    }

    private String getCarId(Car car) {
        return car.getRoadName().split(HYPHEN)[0] +
                " " +
                splitString(car.getNumber());
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

    private final static Logger log = LoggerFactory.getLogger(PrintShowCarsInTrain.class);
}

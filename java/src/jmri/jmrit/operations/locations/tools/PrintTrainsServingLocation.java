package jmri.jmrit.operations.locations.tools;

import java.awt.Frame;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.*;
import jmri.util.davidflanagan.HardcopyWriter;

/**
 * @author Daniel Boudreau Copyright (C) 2024
 */
public class PrintTrainsServingLocation {

    public static final String NEW_LINE = "\n"; // NOI18N
    static final String TAB = "\t"; // NOI18N

    boolean _isPreview;
    Location _location;
    Track _track;
    String _carType;

    public PrintTrainsServingLocation(boolean isPreview, Location location, Track track, String carType) {
        super();
        _isPreview = isPreview;
        _location = location;
        _track = track;
        _carType = carType;
        printLocations();
    }

    private void printLocations() {

        // obtain a HardcopyWriter
        String title = Bundle.getMessage("TitleLocationsTable");
        if (_location != null) {
            title = _location.getName();
        }
        try (HardcopyWriter writer =
                new HardcopyWriter(new Frame(), title, Control.reportFontSize, .5, .5, .5, .5, _isPreview)) {

            printTrains(writer);

        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
        } catch (IOException we) {
            log.error("Error printing PrintLocationAction: {}", we.getLocalizedMessage());
        }
    }

    private void printTrains(HardcopyWriter writer) throws IOException {
        // show track name if selected
        if (_track != null) {
            writer.write(Bundle.getMessage("Track") + TAB + _track.getName() + NEW_LINE);
        }
        // show car type if selected
        if (!_carType.isEmpty()) {
            writer.write(Bundle.getMessage("Type") + TAB + _carType + NEW_LINE);
        }
        writer.write(getHeader());
        for (Train train : InstanceManager.getDefault(TrainManager.class).getTrainsByNameList()) {
            Route route = train.getRoute();
            if (route == null) {
                continue;
            }
            // determine if the car type is accepted by train
            if (!_carType.isEmpty() && !train.isTypeNameAccepted(_carType)) {
                continue;
            }
            for (RouteLocation rl : route.getLocationsBySequenceList()) {
                if (_location != null && rl.getName().equals(_location.getName())) {
                    boolean pickup = false;
                    boolean setout = false;
                    if (rl.isPickUpAllowed() &&
                            rl.getMaxCarMoves() > 0 &&
                            !train.isLocationSkipped(rl.getId()) &&
                            (train.isLocalSwitcher() ||
                                    (rl.getTrainDirection() & _location.getTrainDirections()) != 0) &&
                            (train.isLocalSwitcher() ||
                                    _track == null ||
                                    ((rl.getTrainDirection() & _track.getTrainDirections()) != 0)) &&
                            (_track == null || _track.isPickupTrainAccepted(train))) {
                        pickup = true;
                    }
                    if (rl.isDropAllowed() &&
                            rl.getMaxCarMoves() > 0 &&
                            !train.isLocationSkipped(rl.getId()) &&
                            (train.isLocalSwitcher() ||
                                    (rl.getTrainDirection() & _location.getTrainDirections()) != 0) &&
                            (train.isLocalSwitcher() ||
                                    _track == null ||
                                    ((rl.getTrainDirection() & _track.getTrainDirections()) != 0)) &&
                            (_track == null || _track.isDropTrainAccepted(train)) &&
                            (_track == null ||
                                    _carType.isEmpty() ||
                                    _track.checkScheduleAttribute(Track.TYPE, _carType, null))) {
                        setout = true;
                    }
                    // now display results
                    if (pickup || setout) {
                        // Train name, direction, pick ups? set outs?
                        StringBuffer sb =
                                new StringBuffer(padOutString(train.getName(), Control.max_len_string_train_name));

                        // train direction when servicing this location
                        sb.append(rl.getTrainDirectionString() + TAB);
                        if (pickup) {
                            sb.append(Bundle.getMessage("OkayPickUp") + TAB);
                        } else {
                            sb.append(Bundle.getMessage("NoPickUp") + TAB);
                        }
                        if (setout) {
                            sb.append(Bundle.getMessage("OkaySetOut"));
                        } else {
                            sb.append(Bundle.getMessage("NoSetOut"));
                        }
                        writer.write(sb.toString() + NEW_LINE);
                    }
                }
            }
        }
    }

    private String getHeader() {
        String s = padOutString(Bundle.getMessage("Trains"), Control.max_len_string_train_name) +
                Bundle.getMessage("AbbrevationDirection") +
                TAB +
                Bundle.getMessage("Pickups") +
                TAB +
                Bundle.getMessage("Drop") +
                NEW_LINE;
        return s;
    }

    private String padOutString(String s, int length) {
        return TrainCommon.padAndTruncate(s, length);
    }

    private final static Logger log = LoggerFactory.getLogger(PrintTrainsServingLocation.class);
}

package jmri.jmrit.operations.locations.schedules.tools;

import java.awt.*;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.locations.schedules.Schedule;
import jmri.jmrit.operations.locations.schedules.ScheduleItem;
import jmri.jmrit.operations.rollingstock.cars.*;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.trainbuilder.TrainCommon;
import jmri.util.davidflanagan.HardcopyWriter;

/**
 * @author Daniel Boudreau Copyright (C) 2026
 */
public class PrintSchedulesByTypeAndLoad {

    LocationManager locationManager = InstanceManager.getDefault(LocationManager.class);
    CarLoads carLoads = InstanceManager.getDefault(CarLoads.class);

    // Maximum string lengths
    int trackMax = locationManager.getMaxTrackNameLength();
    int scheduleMax = 12; // limit so everything fits on one line
    int locationMax = locationManager.getMaxLocationNameLength();
    int carTypeMax = InstanceManager.getDefault(CarTypes.class).getMaxNameSubStringLength();
    int randomMax = 3;
    int trainScheduleMax = 9; // currently no maximum length
    int carRoadMax = InstanceManager.getDefault(CarRoads.class).getMaxNameSubStringLength();
    int carLoadMax = carLoads.getMaxNameLength();

    protected static final String NEW_LINE = "\n"; // NOI18N
    protected static final String SPACE = " "; // NOI18N
    protected static final String TAB = "\t"; // NOI18N

    boolean _isPreview;
    String _carType;
    String _carLoad;

    public PrintSchedulesByTypeAndLoad(boolean isPreview, String carType, String carLoad) {
        super();
        _isPreview = isPreview;
        _carType = carType;
        _carLoad = carLoad;
        printSchedules();
    }

    private void printSchedules() {
        // obtain a HardcopyWriter
        String title = Bundle.getMessage("SchedulesByTypeAndLoad");
        try (HardcopyWriter writer =
                new HardcopyWriter(new Frame(), title, null, null, Control.reportFontSize - 3, .5 * 72, .5 * 72,
                        .5 * 72, .5 * 72, _isPreview, "", true, true, null, null)) {

            printSchedules(writer);

        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print canceled");
        } catch (IOException we) {
            log.error("Error printing: {}", we.getLocalizedMessage());
        }
    }

    private void printSchedules(HardcopyWriter writer) throws IOException {
        printBold(writer,
                Bundle.getMessage("Location") +
                        TAB.repeat(8) +
                        (_carType == null ? Bundle.getMessage("allTypes") : _carType) +
                        TAB +
                        (_carLoad == null ? Bundle.getMessage("allLoads") : _carLoad));

        writer.write(getHeader());

        // determine if load is default empty or load
        boolean defaultLoad =
                carLoads.getDefaultLoadName().equals(_carLoad) || carLoads.getDefaultEmptyName().equals(_carLoad);
        for (Location location : locationManager.getLocationsByNameList()) {
            // only spurs have schedules
            if (!location.hasSchedules())
                continue;
            printBold(writer, location.getName());
            // now look for a spur with a schedule
            for (Track spur : location.getTracksByNameList(Track.SPUR)) {
                Schedule sch = spur.getSchedule();
                if (sch == null) {
                    continue;
                }

                // determine if schedule is requesting car type and load
                for (ScheduleItem si : sch.getItemsBySequenceList()) {
                    // skip if car type doesn't carry load name
                    if (_carType == null &&
                            _carLoad != null &&
                            !carLoads.containsName(si.getTypeName(), _carLoad)) {
                        continue;
                    }
                    if ((_carType == null || si.getTypeName().equals(_carType)) &&
                            (_carLoad == null ||
                                    si.getReceiveLoadName().equals(_carLoad) ||
                                    si.getReceiveLoadName().equals(ScheduleItem.NONE) ||
                                    si.getShipLoadName().equals(_carLoad) ||
                                    (si.getShipLoadName().equals(ScheduleItem.NONE) && defaultLoad))) {
                        // is the schedule item valid?
                        String status = spur.checkScheduleValid();
                        if (!status.equals(Schedule.SCHEDULE_OKAY)) {
                            writer.write(Color.red, status + NEW_LINE);
                        }
                        writer.write(getLine(spur, si));
                        // report if spur can't service the selected load
                        if (_carType != null &&
                                si.getReceiveLoadName().equals(ScheduleItem.NONE) &&
                                !spur.isLoadNameAndCarTypeAccepted(_carLoad, _carType)) {
                            String warnLoad = Bundle.getMessage("spurNotTypeLoad", spur.getName(), _carType, _carLoad);
                            writer.write(Color.BLUE, warnLoad + NEW_LINE);
                        }
                    }
                }
            }
        }
    }

    private String getHeader() {
        String s = SPACE +
                padAndSpaceString(Bundle.getMessage("Spur"), trackMax) +
                padAndSpaceString(Bundle.getMessage("Schedule"), scheduleMax) +
                padAndSpaceString(Bundle.getMessage("ScheduleMode"), 1) +
                padAndSpaceString(Bundle.getMessage("Type"), carTypeMax) +
                padAndSpaceString(Bundle.getMessage("Random"), randomMax) +
                padAndSpaceString(Bundle.getMessage("Delivery"), trainScheduleMax) +
                padAndSpaceString(Bundle.getMessage("Road"), carRoadMax) +
                padAndSpaceString(Bundle.getMessage("Receive"), carLoadMax) +
                padAndSpaceString(Bundle.getMessage("Ship"), carLoadMax) +
                padAndSpaceString(Bundle.getMessage("Destination"), locationMax) +
                padAndSpaceString(Bundle.getMessage("Track"), trackMax) +
                Bundle.getMessage("Pickup") +
                NEW_LINE;
        return s;
    }

    private String getLine(Track spur, ScheduleItem si) {
        String s = SPACE +
                padAndSpaceString(spur.getName(), trackMax) +
                padAndSpaceString(spur.getScheduleName(), scheduleMax) +
                padAndSpaceString(spur.getScheduleModeName(), 1) +
                padAndSpaceString(si.getTypeName(), carTypeMax) +
                padAndSpaceString(si.getRandom(), randomMax) +
                padAndSpaceString(si.getSetoutTrainScheduleName(), trainScheduleMax) +
                padAndSpaceString(si.getRoadName(), carRoadMax) +
                padAndSpaceString(si.getReceiveLoadName(), carLoadMax) +
                padAndSpaceString(si.getShipLoadName(), carLoadMax) +
                padAndSpaceString(si.getDestinationName(), locationMax) +
                padAndSpaceString(si.getDestinationTrackName(), trackMax) +
                TrainCommon.padAndTruncate(si.getPickupTrainScheduleName(), trainScheduleMax) +
                NEW_LINE;
        return s;
    }

    private void printBold(HardcopyWriter writer, String s) throws IOException {
        writer.setFont(null, Font.BOLD, null);
        writer.write(s + NEW_LINE);
        writer.setFont(null, Font.PLAIN, null);
    }

    private String padAndSpaceString(String s, int length) {
        return TrainCommon.padAndTruncate(s, length) + SPACE;
    }

    private static final Logger log = LoggerFactory.getLogger(PrintSchedulesByTypeAndLoad.class);
}

package jmri.jmrit.operations.locations.schedules.tools;

import java.awt.Font;
import java.awt.Frame;
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
public class PrintStagingAndSchedulesByTypeAndLoad {

    LocationManager locationManager = InstanceManager.getDefault(LocationManager.class);
    CarLoads carLoads = InstanceManager.getDefault(CarLoads.class);

    // Maximum string lengths
    int trackMax = locationManager.getMaxTrackNameLength();
    int scheduleMax = Control.max_len_string_location_name - 3;
    int locationMax = locationManager.getMaxLocationNameLength();
    int carTypeMax = InstanceManager.getDefault(CarTypes.class).getMaxNameSubStringLength();
    int carRoadMax = InstanceManager.getDefault(CarRoads.class).getMaxNameSubStringLength();
    int carLoadMax = carLoads.getMaxNameLength();
    int optionsMax = 23;

    protected static final String NEW_LINE = "\n"; // NOI18N
    protected static final String SPACE = " "; // NOI18N
    protected static final String TAB = "\t"; // NOI18N

    boolean _isPreview;
    String _carType;
    String _carLoad;
    SchedulesAndStagingFrame _ssf;

    public PrintStagingAndSchedulesByTypeAndLoad(boolean isPreview, SchedulesAndStagingFrame ssf) {
        super();
        _isPreview = isPreview;
        _ssf = ssf;
        _carType = ssf.getCarType();
        _carLoad = ssf.getCarLoad();
        printStagingAndSchedules();
    }

    private void printStagingAndSchedules() {
        // obtain a HardcopyWriter
        String title = Bundle.getMessage("StagingAndSchedulesByTypeAndLoad");
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
                Bundle.getMessage("Staging") +
                        TAB.repeat(10) +
                        (_carType == null ? Bundle.getMessage("allTypes") : _carType) +
                        TAB +
                        (_carLoad == null ? Bundle.getMessage("allLoads") : _carLoad));

        writer.write(getHeader());

        for (Location location : locationManager.getLocationsByNameList()) {
            if (!location.isStaging())
                continue;
            printBold(writer, location.getName());

            for (Track staging : location.getTracksByNameList(Track.STAGING)) {
                if (!staging.isAddCustomLoadsAnySpurEnabled() && !staging.isAddCustomLoadsEnabled()) {
                    continue;
                }
                if (!staging.isTypeNameAccepted(_carType)) {
                    continue;
                }
                writer.write(getLine(staging));
                if (_carLoad == null) {
                    for (String load : carLoads.getNames(_carType)) {
                        if (staging.isLoadNameAndCarTypeShipped(load, _carType)) {
                            printSpurs(writer, _carType, load);
                        }
                    }
                } else if (staging.isLoadNameAndCarTypeShipped(_carLoad, _carType)) {
                    printSpurs(writer, _carType, _carLoad);
                }
                writer.write(NEW_LINE);
            }
        }
    }

    private String getHeader() {
        String s = SPACE +
                padAndSpaceString(Bundle.getMessage("Track"), trackMax) +
                padAndSpaceString(Bundle.getMessage("CarLoadOptions"), optionsMax) +
                padAndSpaceString(Bundle.getMessage("ShipLoadOption"), optionsMax) +
                padAndSpaceString(Bundle.getMessage("Load"), carLoadMax) +
                padAndSpaceString(Bundle.getMessage("Destination"), locationMax) +
                padAndSpaceString(Bundle.getMessage("Track"), trackMax) +
                //                padAndSpaceString(Bundle.getMessage("LoadOption"), optionsMax) +
                padAndSpaceString(Bundle.getMessage("Schedule"), scheduleMax) +
                Bundle.getMessage("Id") +
                NEW_LINE;
        return s;
    }

    private String getLine(Track staging) {
        String s = SPACE +
                padAndSpaceString(staging.getName(), trackMax) +
                padAndSpaceString(_ssf.getTrackCarLoadOptions(staging), optionsMax) +
                padAndSpaceString(staging.getShipLoadOptionString(), optionsMax);
        return s;
    }

    private void printBold(HardcopyWriter writer, String s) throws IOException {
        writer.setFont(null, Font.BOLD, null);
        writer.write(s + NEW_LINE);
        writer.setFont(null, Font.PLAIN, null);
    }

    private void printSpurs(HardcopyWriter writer, String type, String load) throws IOException {
        // ignore default empty and load names
        if (_ssf.generatedLoadsCheckBox.isSelected() &&
                (load.equals(carLoads.getDefaultEmptyName()) || load.equals(carLoads.getDefaultLoadName()))) {
            return;
        }
        // now list all of the spurs with schedules for this type and load
        for (Location location : locationManager.getLocationsByNameList()) {
            // only spurs have schedules
            if (!location.hasSchedules())
                continue;
            // find spurs with a schedule
            for (Track spur : location.getTracksByNameList(Track.SPUR)) {
                Schedule sch = spur.getSchedule();
                if (sch == null) {
                    continue;
                }
                // determine if schedule is requesting car type and load
                if (spur.isLoadNameAndCarTypeAccepted(load, type)) {
                    for (ScheduleItem si : sch.getItemsBySequenceList()) {
                        if (si.getTypeName().equals(type) &&
                                (si.getReceiveLoadName().equals(load) ||
                                        (si.getReceiveLoadName().equals(ScheduleItem.NONE) &&
                                                !_ssf.generatedLoadsCheckBox.isSelected()))) {
                            String s = SPACE +
                                    padAndSpaceString("", trackMax) +
                                    padAndSpaceString("", optionsMax) +
                                    padAndSpaceString("", optionsMax) +
                                    padAndSpaceString(load, carLoadMax) +
                                    padAndSpaceString(location.getName(), locationMax) +
                                    padAndSpaceString(spur.getName(), trackMax) +
                                    //                                    padAndSpaceString(spur.getLoadOptionString(), optionsMax) +
                                    padAndSpaceString(sch.getName(), scheduleMax) +
                                    si.getId();
                            writer.write(s + NEW_LINE);
                        }
                    }
                }
            }
        }
    }

    private String padAndSpaceString(String s, int length) {
        return TrainCommon.padAndTruncate(s, length) + SPACE;
    }

    private static final Logger log = LoggerFactory.getLogger(PrintStagingAndSchedulesByTypeAndLoad.class);
}

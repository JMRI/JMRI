# TimeTable.py -- Sample program to display the timetable data

import java
import jmri

# Get the Timetable data manager
tdm = jmri.jmrit.timetable.TimeTableDataManager.getDataManager();

# Get the layouts sorted by name
for layout in tdm.getLayouts(True):
    print layout

    # Get the train types sorted by name
    for trainType in tdm.getTrainTypes(layout.getLayoutId(), True):
        print '  {:<15} color: {}'.format(trainType.getTypeName(), trainType.getTypeColor())

    # Get the segments sorted by name
    for segment in tdm.getSegments(layout.getLayoutId(), True):
        print '  {}'.format(segment.getSegmentName())

        # Get the stations sorted by distance
        for station in tdm.getStations(segment.getSegmentId(), True):
            print '    {:<10} dist: {}'.format(station.getStationName(), station.getDistance())

    # Get the schedules sorted by name
    for schedule in tdm.getSchedules(layout.getLayoutId(), True):
        print '  {:<5} start: {}, duration: {}'.format(schedule.getScheduleName(), schedule.getStartHour(), schedule.getDuration())

        # Get the trains sorted by name
        for train in tdm.getTrains(schedule.getScheduleId(), 0, True):
            hh = train.getStartTime() / 60
            mm = train.getStartTime() % 60
            print '    {:<5} start: {:02d}:{:02d} ({})'.format(train.getTrainName(), hh, mm, train.getStartTime())

            # Get the stops sorted by stop sequence
            for stop in tdm.getStops(train.getTrainId(), 0, True):
                arriveH = stop.getArriveTime() / 60
                arriveM = stop.getArriveTime() % 60
                departH = stop.getDepartTime() / 60
                departM = stop.getDepartTime() % 60
                print '      {:<12}  {:02d}:{:02d}   {:02d}:{:02d}'.format(stop, arriveH, arriveM, departH, departM)


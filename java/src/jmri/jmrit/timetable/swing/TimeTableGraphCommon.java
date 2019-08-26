package jmri.jmrit.timetable.swing;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.util.*;
import javax.swing.*;
import jmri.jmrit.timetable.*;

/**
 * The left column has the layout information along with the station names next to the diagram box.
 * The column width is dynamic based on the width of the items.
 * Across the top, lined up with the diagram box, are the throttle lines.
 * The main section is the diagram box.
 * Across the bottom, lined up with the diagram box, is the hours section.
 * <pre>
 *       +--------- canvas -------------+
 *       | info    | throttle lines     |
 *       |         |+------------------+|
 *       | station ||                  ||
 *       | station || diagram box      ||
 *       | station ||                  ||
 *       |         |+------------------+|
 *       |         | hours              |
 *       +------------------------------+
 * </pre>
 * A normal train line will be "a-b-c-d-e" for a through train, or "a-b-c-b-a" for a turn.
 * <p>
 * A multi-segment train will be "a1-b1-c1-x2-y2-z2" where c is the junction. The
 * reverse will be "z2-y2-z2-c2-b1-a1".  Notice:  While c is in both segments, for
 * train stop purposes, the arrival "c" is used and the departure "c" is skipped.
 */
public class TimeTableGraphCommon {

    /**
     * Initialize the data used by paint() and supporting methods when the
     * panel is displayed.
     * @param segmentId The segment to be displayed.  For multiple segment
     * layouts separate graphs are required.
     * @param scheduleId The schedule to be used for this graph.
     * @param showTrainTimes When true, include the minutes portion of the
     * train times at each station.
     */
    void init(int segmentId, int scheduleId, boolean showTrainTimes, double height, double width, boolean displayType) {
        _segmentId = segmentId;
        _scheduleId = scheduleId;
        _showTrainTimes = showTrainTimes;

        _dataMgr = TimeTableDataManager.getDataManager();
        _segment = _dataMgr.getSegment(_segmentId);
        _layout = _dataMgr.getLayout(_segment.getLayoutId());
        _throttles = _layout.getThrottles();
        _schedule = _dataMgr.getSchedule(_scheduleId);
        _startHour = _schedule.getStartHour();
        _duration = _schedule.getDuration();
        _stations = _dataMgr.getStations(_segmentId, true);
        _trains = _dataMgr.getTrains(_scheduleId, 0, true);
        _dimHeight = height;
        _dimWidth = width;
    }

    final Font _stdFont = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
    final Font _smallFont = new Font(Font.SANS_SERIF, Font.PLAIN, 8);
    final static BasicStroke gridstroke = new BasicStroke(0.5f);
    final static BasicStroke stroke = new BasicStroke(2.0f);

    TimeTableDataManager _dataMgr;
    int _segmentId;
    int _scheduleId;

    Layout _layout;
    int _throttles;

    Segment _segment;

    Schedule _schedule;
    int _startHour;
    int _duration;

    ArrayList<Station> _stations;
    ArrayList<Train> _trains;
    ArrayList<Stop> _stops;

    // ------------ global variables ------------
    HashMap<Integer, Double> _stationGrid = new HashMap<>();
    HashMap<Integer, Double> _hourMap = new HashMap<>();
    ArrayList<Double> _hourGrid = new ArrayList<>();
    int _infoColWidth = 0;
    double _hourOffset = 0;
    double _graphHeight = 0;
    double _graphWidth = 0;
    double _graphTop = 0;
    double _graphBottom = 0;
    double _graphLeft = 0;
    double _graphRight = 0;
    Graphics2D _g2;
    boolean _showTrainTimes;
    PageFormat _pf;
    double _dimHeight = 0;
    double _dimWidth = 0;

    // ------------ train variables ------------
    ArrayList<Rectangle2D> _textLocation = new ArrayList<>();

    // Train
    String _trainName;
    int _trainThrottle;
    Color _trainColor;
    Path2D _trainLine;

    // Stop
    int _stopCnt;
    int _stopIdx;
    int _arriveTime;
    int _departTime;

    // Stop processing
    double _maxDistance;
    String _direction;
//     int _baseTime;
    boolean _firstStop;
    boolean _lastStop;

    double _firstX;
    double _lastX;

    double _sizeMinute;
    double _throttleX;

    public void doPaint(Graphics g) {
        if (g instanceof Graphics2D) {
            _g2 = (Graphics2D) g;
        } else {
            throw new IllegalArgumentException();
        }
        _g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        _stationGrid.clear();
        _hourGrid.clear();
        _textLocation.clear();

//         Dimension dim = getSize();
//         double dimHeight = _pf.getImageableHeight();
//         double dimWidth = _pf.getImageableWidth() * 2;
//         double dimHeight = _dimHeight;
//         double dimWidth = _dimWidth;

        // Get the height of the throttle section and set the graph top
        _graphTop = 70.0;
        if (_layout.getThrottles() > 4) {
            _graphTop = _layout.getThrottles() * 15.0;
        }
        _graphHeight = _dimHeight - _graphTop - 30.0;
        _graphBottom = _graphTop + _graphHeight;

        // Draw the left column components
        drawInfoSection();
        drawStationSection();

        // Set the horizontal graph dimensions based on the width of the left column
        _graphLeft = _infoColWidth + 50.0;
        _graphWidth = _dimWidth - _infoColWidth - 65.0;
        _graphRight = _graphLeft + _graphWidth;

        drawHours();
        drawThrottleNumbers();
        drawGraphGrid();
        drawTrains();
    }

    void drawInfoSection() {
        // Info section
        _g2.setFont(_stdFont);
        _g2.setColor(Color.BLACK);
        String layoutName = String.format("%s %s", Bundle.getMessage("LabelLayoutName"), _layout.getLayoutName());  // NOI18N
        String segmentName = String.format("%s %s", Bundle.getMessage("LabelSegmentName"), _segment.getSegmentName());  // NOI18N
        String scheduleName = String.format("%s %s", Bundle.getMessage("LabelScheduleName"), _schedule.getScheduleName());  // NOI18N
        String effDate = String.format("%s %s", Bundle.getMessage("LabelEffDate"), _schedule.getEffDate());  // NOI18N

        _infoColWidth = Math.max(_infoColWidth, _g2.getFontMetrics().stringWidth(layoutName));
        _infoColWidth = Math.max(_infoColWidth, _g2.getFontMetrics().stringWidth(scheduleName));
        _infoColWidth = Math.max(_infoColWidth, _g2.getFontMetrics().stringWidth(effDate));

        _g2.drawString(layoutName, 10, 20);
        _g2.drawString(segmentName, 10, 40);
        _g2.drawString(scheduleName, 10, 60);
        _g2.drawString(effDate, 10, 80);
    }

    void drawStationSection() {
        _maxDistance = _stations.get(_stations.size() - 1).getDistance();
        _g2.setFont(_stdFont);
        _g2.setColor(Color.BLACK);
        _stationGrid.clear();
        for (Station station : _stations) {
            String stationName = station.getStationName();
            _infoColWidth = Math.max(_infoColWidth, _g2.getFontMetrics().stringWidth(stationName) + 5);
            double distance = station.getDistance();
            double stationY = ((_graphHeight - 50) / _maxDistance) * distance + _graphTop + 30;  // calculate the Y offset
            _g2.drawString(stationName, 15.0f, (float) stationY);
            _stationGrid.put(station.getStationId(), stationY);
        }
    }

    void drawHours() {
        int currentHour = _startHour;
        double hourWidth = _graphWidth / (_duration + 1);
        _hourOffset = hourWidth / 2;
        _g2.setFont(_stdFont);
        _g2.setColor(Color.BLACK);
        _hourGrid.clear();
        for (int i = 0; i <= _duration; i++) {
            String hourString = Integer.toString(currentHour);
            double hourX = (hourWidth * i) + _hourOffset + _graphLeft;
            int hOffset = _g2.getFontMetrics().stringWidth(hourString) / 2;
            _g2.drawString(hourString, (float) hourX - hOffset, (float) _graphBottom + 20);
            if (i < _duration) {
                _hourMap.put(currentHour, hourX);
            }
            _hourGrid.add(hourX);
            if (i == 0) {
                _firstX = hourX - hOffset;
            }
            if (i == _duration) {
                _lastX = hourX - hOffset;
            }
            currentHour++;
            if (currentHour > 23) {
                currentHour -= 24;
            }
        }
    }

    void drawThrottleNumbers() {
        _g2.setFont(_smallFont);
        _g2.setColor(Color.BLACK);
        for (int i = 1; i <= _throttles; i++) {
            _g2.drawString(Integer.toString(i), (float) _graphLeft, (float) i * 14);
        }
    }

    void drawGraphGrid() {
        // Print the graph box
        _g2.draw(new Rectangle2D.Double(_graphLeft, _graphTop, _graphWidth, _graphHeight));

        // Print the grid lines
        _g2.setStroke(gridstroke);
        _g2.setColor(Color.GRAY);
        _stationGrid.forEach((i, y) -> {
            _g2.draw(new Line2D.Double(_graphLeft, y, _graphRight, y));
        });
        _hourGrid.forEach((x) -> {
            _g2.draw(new Line2D.Double(x, _graphTop, x, _graphBottom));
        });
    }

    /**
     * Create the train line for each train with labels.  Include times if
     * selected.
     * <p>
     * All defined trains their stops are processed.  If a stop has a station
     * in the segment, it is included.  Most trains only use a single segment.
     */
    void drawTrains() {
//         _baseTime = _startHour * 60;
        _sizeMinute = _graphWidth / ((_duration + 1) * 60);
        _throttleX = 0;
        for (Train train : _trains) {
            _trainName = train.getTrainName();
            _trainThrottle = train.getThrottle();
            String typeColor = _dataMgr.getTrainType(train.getTypeId()).getTypeColor();
            _trainColor = Color.decode(typeColor);
            _trainLine = new Path2D.Double();

            boolean activeSeg = false;

            _stops = _dataMgr.getStops(train.getTrainId(), 0, true);
            _stopCnt = _stops.size();
            _firstStop = true;
            _lastStop = false;

            for (_stopIdx = 0; _stopIdx < _stopCnt; _stopIdx++) {
                Stop stop = _stops.get(_stopIdx);

                // Set basic values
                _arriveTime = stop.getArriveTime();
                _departTime = stop.getDepartTime();
                Station stopStation = _dataMgr.getStation(stop.getStationId());
                int stopSegmentId = stopStation.getSegmentId();
                if (_stopIdx > 0) _firstStop = false;
                if (_stopIdx == _stopCnt - 1) _lastStop = true;

                if (!activeSeg) {
                    if (stopSegmentId != _segmentId) {
                        continue;
                    }
                    activeSeg = true;
                    setBegin(stop);
                    if (_lastStop) {
                        // One stop route or only one stop in current segment
                        setEnd(stop, false);
                        break;
                    }
                    continue;
                }

                // activeSeg always true here
                if (stopSegmentId != _segmentId) {
                    // No longer in active segment, do the end process
                    setEnd(stop, true);
                    activeSeg = false;
                    continue;
                } else {
                    drawLine(stop);
                    if (_lastStop) {
                        // At the end, do the end process
                        setEnd(stop, false);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Draw a train name on the graph.
     * <p>
     * The base location is provided by x and y.  justify is used to offset
     * the x axis.  invert is used to flip the y offsets.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param justify "Center" moves the string left half of the distance.  "Right"
     * moves the string left the full width of the string.
     * @param invert If true, the y coordinate offset is flipped.
     * @param throttle If true, a throttle line item.
     */
    void drawTrainName(double x, double y, String justify, boolean invert, boolean throttle) {
        Rectangle2D textRect = _g2.getFontMetrics().getStringBounds(_trainName, _g2);

        // Position train name
        if (justify.equals("Center")) {  // NOI18N
            x = x - textRect.getWidth() / 2;
        } else if (justify.equals("Right")) {  // NOI18N
            x = x - textRect.getWidth();
        }

        if (invert) {
            y = y + ((_direction.equals("down") || throttle) ? -7 : 13);  // NOI18N
        } else {
            y = y + ((_direction.equals("down") || throttle) ? 13 : -7);  // NOI18N
        }

        textRect.setRect(
                x,
                y,
                textRect.getWidth(),
                textRect.getHeight()
                );
        textRect = adjustText(textRect);
        x = textRect.getX();

        _g2.setFont(_stdFont);
        _g2.setColor(Color.BLACK);
        _g2.drawString(_trainName, (float) x, (float) y);
        _textLocation.add(textRect);
    }

    /**
     * Draw the minutes value on the graph if enabled.
     * @param time The time in total minutes.  Converted to remainder minutes.
     * @param mode Used to set the x and y offsets based on type of time.
     * @param x The base x coordinate.
     * @param y The base y coordinate.
     */
    void drawTrainTime(int time, String mode, double x, double y) {
        if (!_showTrainTimes) {
            return;
        }
        String minutes = String.format("%02d", time % 60);  // NOI18N
        Rectangle2D textRect = _g2.getFontMetrics().getStringBounds(minutes, _g2);
        switch (mode) {
            case "begin":  // NOI18N
                x = x + ((_direction.equals("down")) ? 2 : 2);  // NOI18N
                y = y + ((_direction.equals("down")) ? 10 : -1);  // NOI18N
                break;
            case "arrive":  // NOI18N
                x = x + ((_direction.equals("down")) ? 2 : 3);  // NOI18N
                y = y + ((_direction.equals("down")) ? -2 : 10);  // NOI18N
                break;
            case "depart":  // NOI18N
                x = x + ((_direction.equals("down")) ? 2 : 2);  // NOI18N
                y = y + ((_direction.equals("down")) ? 10 : -2);  // NOI18N
                break;
            case "end":  // NOI18N
                x = x + ((_direction.equals("down")) ? 0 : 0);  // NOI18N
                y = y + ((_direction.equals("down")) ? 0 : 0);  // NOI18N
                break;
            default:
                log.error("drawTrainTime mode {} is unknown");  // NOI18N
                return;
        }

        textRect.setRect(
                x,
                y,
                textRect.getWidth(),
                textRect.getHeight()
                );
        textRect = adjustText(textRect);
        x = textRect.getX();

        _g2.setFont(_smallFont);
        _g2.setColor(Color.GRAY);
        _g2.drawString(minutes, (float) x, (float) y);
        _textLocation.add(textRect);
    }  // TODO End?

    /**
     * Move text that overlaps existing text.
     * @param textRect The proposed text rectangle.
     */
    Rectangle2D adjustText(Rectangle2D textRect) {
        double xLoc = textRect.getX();
        double yLoc = textRect.getY();
        double xLen = textRect.getWidth();

        double wrkX = xLoc;
        double xMin;
        double xMax;
        boolean chgX = false;

        for (Rectangle2D workRect : _textLocation) {
            if (workRect.getY() == yLoc) {
                xMin = workRect.getX();
                xMax = xMin + workRect.getWidth();

                if (xLoc > xMin && xLoc < xMax) {
                    wrkX = xMax + 2;
                    chgX = true;
                }

                if (xLoc + xLen > xMin && xLoc + xLen < xMax) {
                    wrkX = xMin - xLen -2;
                    chgX = true;
                }
            }
        }

        if (chgX) {
            textRect.setRect(
                    wrkX,
                    yLoc,
                    textRect.getWidth(),
                    textRect.getHeight()
                    );
        }

        return textRect;
    }

    /**
     * Determine direction of travel on the graph: up or down
     */
    void setDirection() {
        if (_stopCnt == 1) {
            // Single stop train, default to down
            _direction = "down";  // NOI18N
            return;
        }

        Stop stop = _stops.get(_stopIdx);
        Station currStation = _dataMgr.getStation(stop.getStationId());
        Station nextStation;
        Station prevStation;
        double currDistance = currStation.getDistance();

        if (_firstStop) {
            // For the first stop, use the next stop to set the direction
            nextStation = _dataMgr.getStation(_stops.get(_stopIdx + 1).getStationId());
            _direction = (nextStation.getDistance() > currDistance) ? "down" : "up";  // NOI18N
            return;
        }

        prevStation = _dataMgr.getStation(_stops.get(_stopIdx - 1).getStationId());
        if (_lastStop) {
            // For the last stop, use the previous stop to set the direction
            // Last stop may also be only stop after segment change; if so wait for next "if"
            if (prevStation.getSegmentId() == _segmentId) {
                _direction = (prevStation.getDistance() < currDistance) ? "down" : "up";  // NOI18N
                return;
            }
        }

        if (prevStation.getSegmentId() != _segmentId) {
            // For the first stop after segment change, use the transfer point to set the direction
            String prevName = prevStation.getStationName();

            // Find the corresponding station in the current Segment
            for (Station segStation : _stations) {
                if (segStation.getStationName().equals(prevName)) {
                    _direction = (segStation.getDistance() < currDistance) ? "down" : "up";  // NOI18N
                    return;
                }
            }
        }

        // For all other stops in the active segment, use the next stop.
        if (!_lastStop) {
            nextStation = _dataMgr.getStation(_stops.get(_stopIdx + 1).getStationId());
            if (nextStation.getSegmentId() == _segmentId) {
                _direction = (nextStation.getDistance() > currDistance) ? "down" : "up";  // NOI18N
                return;
            }
        }

        // At this point, don't change anything.
    }

    /**
     * Set the starting point for the _trainLine path.
     * The normal case will be the first stop (aka start) for the train.
     * <p>
     * The other case is a multi-segment train.  The first stop in the current
     * segment will be the station AFTER the junction.  That means the start
     * will actually be at the junction station.
     * @param stop The current stop.
     */
    void setBegin(Stop stop) {
        double x;
        double y;
        boolean segmentChange = false;

        if (_stopIdx > 0) {
            // Begin after segment change
            segmentChange = true;
            Stop prevStop = _stops.get(_stopIdx - 1);
            Station prevStation = _dataMgr.getStation(prevStop.getStationId());
            String prevName = prevStation.getStationName();

            // Find matching station in the current segment for the last station in the other segment
            for (Station segStation : _stations) {
                if (segStation.getStationName().equals(prevName)) {
                    // x is based on previous depart time, y is based on corresponding station position
                    x = calculateX(prevStop.getDepartTime());
                    y = _stationGrid.get(segStation.getStationId());
                    _trainLine.moveTo(x, y);
                    _throttleX = x;  // save for drawing the throttle line at setEnd

                    setDirection();
                    drawTrainName(x, y, "Center", true, false);  // NOI18N
                    drawTrainTime(prevStop.getDepartTime(), "begin", x, y);  // NOI18N
                    break;
                }
            }
        }
        x = calculateX(stop.getArriveTime());
        y = _stationGrid.get(stop.getStationId());

        if (segmentChange) {
            _trainLine.lineTo(x, y);
            setDirection();
            drawTrainTime(stop.getArriveTime(), "arrive", x, y);  // NOI18N
        } else {
            _trainLine.moveTo(x, y);
            _throttleX = x;  // save for drawing the throttle line at setEnd

            setDirection();
            drawTrainName(x, y, "Center", true, false);  // NOI18N
            drawTrainTime(stop.getArriveTime(), "begin", x, y);  // NOI18N
        }

        // Check for stop duration before depart
        if (stop.getDuration() > 0) {
            x = calculateX(stop.getDepartTime());
            _trainLine.lineTo(x, y);
            drawTrainTime(stop.getDepartTime(), "depart", x, y);  // NOI18N
        }
    }

    /**
     * Extend the train line with additional stops.
     * @param stop The current stop.
     */
    void drawLine(Stop stop) {
        double x = calculateX(_arriveTime);
        double y = _stationGrid.get(stop.getStationId());
        _trainLine.lineTo(x, y);
        drawTrainTime(_arriveTime, "arrive", x, y);  // NOI18N

        setDirection();
        // Check for duration after arrive
        if (stop.getDuration() > 0) {
            x = calculateX(_departTime);
            if (x < _trainLine.getCurrentPoint().getX()) {
                // The line wraps around to the beginning, do the line in two pieces
                _trainLine.lineTo(_graphRight - _hourOffset, y);
                drawTrainName(_graphRight - _hourOffset, y, "Right", false, false);  // NOI18N
                _trainLine.moveTo(_graphLeft + _hourOffset, y);
                _trainLine.lineTo(x, y);
                drawTrainName(_graphLeft + _hourOffset, y, "Left", true, false);  // NOI18N
                drawTrainTime(_departTime, "depart", x, y);  // NOI18N
            } else {
                _trainLine.lineTo(x, y);
                drawTrainTime(_departTime, "depart", x, y);  // NOI18N
            }
        }
    }

    /**
     * Finish the train line, draw it, the train name and the throttle line if used.
     * @param stop The current stop.
     */
    void setEnd(Stop stop, boolean endSegment) {
        double x;
        double y;
        boolean skipLine = false;

        if (_stops.size() == 1 || endSegment) {
            x = _trainLine.getCurrentPoint().getX();
            y = _trainLine.getCurrentPoint().getY();
            skipLine = true;
        } else {
            x = calculateX(_arriveTime);
            y = _stationGrid.get(stop.getStationId());
        }

        drawTrainName(x, y, "Center", false, false);  // NOI18N
        _g2.setColor(_trainColor);
        _g2.setStroke(stroke);
        if (!skipLine) {
            _trainLine.lineTo(x, y);
        }
        _g2.draw(_trainLine);

        // Process throttle line
        if (_trainThrottle > 0) {
            _g2.setFont(_smallFont);
            double throttleY = (_trainThrottle * 14);
            if (x < _throttleX) {
                 _g2.draw(new Line2D.Double(_throttleX, throttleY, _graphRight - _hourOffset, throttleY));
                 _g2.draw(new Line2D.Double(_graphLeft + _hourOffset, throttleY, x, throttleY));
                drawTrainName(_throttleX + 10, throttleY + 5, "Left", true, true);  // NOI18N
                drawTrainName(_graphLeft + _hourOffset + 10, throttleY + 5, "Left", true, true);  // NOI18N
           } else {
                _g2.draw(new Line2D.Double(_throttleX, throttleY, x, throttleY));
                drawTrainName(_throttleX + 10, throttleY + 5, "Left", true, true);  // NOI18N
            }
        }
    }

    /**
     * Convert the time value, 0 - 1439 to the x graph position.
     * @param time The time value.
     * @return the x value.
     */
    double calculateX(int time) {
        if (time < 0) time = 0;
        if (time > 1439) time = 1439;

        int hour = time / 60;
        int min = time % 60;

        return _hourMap.get(hour) + (min * _sizeMinute);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimeTableGraphCommon.class);

}

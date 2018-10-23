package jmri.jmrit.timetable.swing;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;
import jmri.jmrit.timetable.*;

/**
 * The left column has the layout information along with the station names next to the diagram box.
 * The column width is dynamic based on the width of the items.
 * Across the top, lined up with the diagram box, are the throttle lines.
 * The main section is the diagram box.
 * Across the bottom, lined up with the diagram box, is the hours section.
 *       +--------- canvas -------------+
 *       | info    | throttle lines     |
 *       |         |+------------------+|
 *       | station ||                  ||
 *       | station || diagram box      ||
 *       | station ||                  ||
 *       |         |+------------------+|
 *       |         | hours              |
 *       +------------------------------+
 */
public class TimeTableGraph extends JPanel {

    void init(int segmentId, int scheduleId, TimeTableDataManager dataMgr, boolean showTrainTimes) {
        log.info("init");

        _segmentId = segmentId;
        _scheduleId = scheduleId;
        _dataMgr = dataMgr;
        _showTrainTimes = showTrainTimes;

        _segment = _dataMgr.getSegment(_segmentId);
        _layout = _dataMgr.getLayout(_segment.getLayoutId());
        _throttles = _layout.getThrottles();
        _schedule = _dataMgr.getSchedule(_scheduleId);
        _startHour = _schedule.getStartHour();
        _duration = _schedule.getDuration();
        _stations = _dataMgr.getStations(_segmentId, true);
        _trains = _dataMgr.getTrains(_scheduleId, 0, true);
    }

    final Font _infoFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    final Font _stationFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    final Font _throttleFont = new Font(Font.SANS_SERIF, Font.PLAIN, 10);

    final static BasicStroke thinstroke = new BasicStroke(1.0f);
    final static BasicStroke gridstroke = new BasicStroke(0.5f);
    final static BasicStroke stroke = new BasicStroke(2.0f);
    final static BasicStroke wideStroke = new BasicStroke(8.0f);

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
    int _baseTime;

    double _firstX;
    double _lastX;

    double _sizeMinute;
    double _throttleX;


    @Override
    public void paint(Graphics g) {
        if (g instanceof Graphics2D) {
            _g2 = (Graphics2D) g;
        } else {
            throw new IllegalArgumentException();
        }
        _g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        _stationGrid.clear();
        _hourGrid.clear();
        _textLocation.clear();

        Dimension dim = getSize();
        double dimHeight = dim.getHeight();
        double dimWidth = dim.getWidth();

        // Get the height of the throttle section and set the graph top
        _graphTop = 70.0;
        if (_layout.getThrottles() > 4) {
            _graphTop = _layout.getThrottles() * 15.0;
        }
        _graphHeight = dimHeight - _graphTop - 30.0;
        _graphBottom = _graphTop + _graphHeight;

        // Draw the left column components
        drawInfoSection(_g2);
        drawStationSection(_g2);

        // Set the horizontal graph dimensions based on the width of the left column
        _graphLeft = _infoColWidth + 50.0;
        _graphWidth = dimWidth - _infoColWidth - 65.0;
        _graphRight = _graphLeft + _graphWidth;

        drawHours(_g2);
        drawThrottleNumbers(_g2);
        drawGraphGrid(_g2);
        drawTrains(_g2);
    }

    void drawInfoSection(Graphics2D _g2) {
        // Info section
        _g2.setFont(_infoFont);
        String layoutName = String.format("%s %s", Bundle.getMessage("LabelLayoutName"), _layout.getLayoutName());  // NOI18N
        String segmentName = String.format("%s %s", Bundle.getMessage("LabelSegmentName"), _segment.getSegmentName());  // NOI18N
        String scheduleName = String.format("%s %s", Bundle.getMessage("LabelScheduleName"), _schedule.getScheduleName());  // NOI18N
        String effDate = String.format("%s %s", Bundle.getMessage("LabelEffDate"), _schedule.getEffDate());  // NOI18N

        _infoColWidth = Math.max(_infoColWidth, _g2.getFontMetrics().stringWidth(layoutName));
        _infoColWidth = Math.max(_infoColWidth, _g2.getFontMetrics().stringWidth(scheduleName));
        _infoColWidth = Math.max(_infoColWidth, _g2.getFontMetrics().stringWidth(effDate));

        _g2.setFont(_infoFont);
        _g2.setColor(Color.BLACK);
        _g2.drawString(layoutName, 10, 20);
        _g2.drawString(segmentName, 10, 40);
        _g2.drawString(scheduleName, 10, 60);
        _g2.drawString(effDate, 10, 80);
    }

    void drawStationSection(Graphics2D _g2) {
        _maxDistance = _stations.get(_stations.size() - 1).getDistance();
        _g2.setFont(_stationFont);
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

    void drawHours(Graphics2D _g2) {
        int currentHour = _startHour;
        double hourWidth = _graphWidth / (_duration + 1);
        _hourOffset = hourWidth / 2;
        _g2.setFont(_stationFont);
        _g2.setColor(Color.BLACK);
        _hourGrid.clear();
        for (int i = 0; i <= _duration; i++) {
            String hourString = Integer.toString(currentHour);
            double hourX = (hourWidth * i) + _hourOffset + _graphLeft;
            int hOffset = _g2.getFontMetrics().stringWidth(hourString) / 2;
            _g2.drawString(hourString, (float) hourX - hOffset, (float) _graphBottom + 20);
            currentHour++;
            if (currentHour > 23) {
                currentHour -= 24;
            }
            _hourGrid.add(hourX);
            if (i == 0) {
                _firstX = hourX - hOffset;
            }
            if (i == _duration) {
                _lastX = hourX - hOffset;
            }
        }
    }

    void drawThrottleNumbers(Graphics2D _g2) {
        _g2.setFont(_throttleFont);
        _g2.setColor(Color.BLACK);
        for (int i = 1; i <= _throttles; i++) {
            _g2.drawString(Integer.toString(i), (float) _graphLeft, (float) i * 14);
        }
    }

    void drawGraphGrid(Graphics2D _g2) {
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

    void drawTrains(Graphics2D _g2) {
        _baseTime = _startHour * 60;
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

            for (_stopIdx = 0; _stopIdx < _stopCnt; _stopIdx++) {
                Stop stop = _stops.get(_stopIdx);
                int lclSegmentId;

                // Set basic values
                _arriveTime = stop.getArriveTime();
                _departTime = stop.getDepartTime();
                Station lclStation = _dataMgr.getStation(stop.getStationId());
                lclSegmentId = lclStation.getSegmentId();

                if (!activeSeg) {
                    if (lclSegmentId != _segmentId) {
                        continue;
                    }
                    activeSeg = true;
                    setBegin();
                    if (_stops.size() == 1 || _stopIdx == _stops.size() - 1) {
                        setEnd(stop, false);
                        break;
                    }
                    continue;
                }

                if (activeSeg) {
                    setDirection();
                    if (lclSegmentId != _segmentId) {
                        // No longer in active segment, do the end process
                        setEnd(stop, true);
                        break;
                    } else {
                        drawLine(stop);
                        if (_stopIdx == _stopCnt - 1) {
                            // At the end, do the end process
                            setEnd(stop, false);
                            break;
                        }
                    }
                }
            }
        }
    }

    void drawTrainName(double x, double y, String justify, boolean invert, boolean throttle) {
        Rectangle2D textRect = _g2.getFontMetrics().getStringBounds(_trainName, _g2);

        // Position train name
        if (justify.equals("Center")) {  // NOI18N
            x = x - textRect.getWidth() / 2;
        } else if (justify.equals("Right")) {  // NOI18N
            x = x - textRect.getWidth();
        }

        if (invert) {
            y = y + ((_direction.equals("down") || throttle) ? 13 : -7);  // NOI18N
        } else {
            y = y + ((_direction.equals("down") || throttle) ? -7 : 13);  // NOI18N
        }

        textRect.setRect(
                x,
                y,
                textRect.getWidth(),
                textRect.getHeight()
                );
        textRect = adjustText(textRect);
        x = textRect.getX();

        _g2.setFont(_stationFont);
        _g2.setColor(Color.BLACK);
        _g2.drawString(_trainName, (float) x, (float) y);
        _textLocation.add(textRect);
    }  // TODO

    void drawTrainTime(int time, String mode, double x, double y) {
        if (!_showTrainTimes) {
            return;
        }
        String minutes = String.format("%02d", time % 60);  // NOI18N
        Rectangle2D textRect = _g2.getFontMetrics().getStringBounds(minutes, _g2);
        switch (mode) {
            case "begin":
                x = x + ((_direction.equals("down")) ? 2 : 2);  // NOI18N
                y = y + ((_direction.equals("down")) ? 10 : -1);  // NOI18N
                break;
            case "arrive":
                x = x + ((_direction.equals("down")) ? 2 : 3);  // NOI18N
                y = y + ((_direction.equals("down")) ? -2 : 10);  // NOI18N
                break;
            case "depart":
                x = x + ((_direction.equals("down")) ? 2 : 1);  // NOI18N
                y = y + ((_direction.equals("down")) ? 10 : -2);  // NOI18N
                break;
            case "end":
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

        _g2.setFont(_throttleFont);
        _g2.setColor(Color.GRAY);
        _g2.drawString(minutes, (float) x, (float) y);
        _textLocation.add(textRect);
    }

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

    void setBegin() {
        boolean segmentChange = false;
        double x;
        double y;
        Stop stop = _stops.get(_stopIdx);

        if (_stopIdx > 0) {
            // Enter after segment change
//             log.info("segment change for train {}", _trainName);
            segmentChange = true;
            Stop prevStop = _stops.get(_stopIdx - 1);
            Station prevStation = _dataMgr.getStation(prevStop.getStationId());
            String prevName = prevStation.getStationName();
            for (Station segStation : _stations) {
                if (segStation.getStationName().equals(prevName)) {
                    x = _graphLeft + ((prevStop.getDepartTime() - _baseTime) * _sizeMinute) + _hourOffset;
                    y = _stationGrid.get(segStation.getStationId());

                    _trainLine.moveTo(x, y);
                    _throttleX = x;  // save for drawing the throttle line at setEnd

                    setDirection();
                    drawTrainName(x, y, "Center", false, false);  // NOI18N
                    drawTrainTime(stop.getDepartTime(), "begin", x, y);  // NOI18N
//                     log.info("found transition station: name = {}, x = {}, y = {}, time = {}", prevName, x, y, stop.getDepartTime());
//                     if (_stopIdx == _stops.size() - 1) {
//                         log.info("this is the last stop");
//                     }
                    break;
                }
            }
        }

        x = _graphLeft + ((stop.getArriveTime() - _baseTime) * _sizeMinute) + _hourOffset;
        y = _stationGrid.get(stop.getStationId());

        if (segmentChange) {
            _trainLine.lineTo(x, y);
        } else {
            _trainLine.moveTo(x, y);
            _throttleX = x;  // save for drawing the throttle line at setEnd

            setDirection();
            drawTrainName(x, y, "Center", false, false);  // NOI18N
            drawTrainTime(stop.getArriveTime(), "begin", x, y);  // NOI18N
        }


        // Check for stop duration before depart
        if (stop.getDuration() != 0) {
            x = _graphLeft + ((stop.getDepartTime() - _baseTime) * _sizeMinute) + _hourOffset;
            _trainLine.lineTo(x, y);
//             drawTrainTime(stop.getDepartTime(), "depart", x, y);
        }
    }

    void setDirection() {
        if (_stops.size() == 1 || _stopIdx < 0 || _stopIdx > _stops.size() - 1) {
            // Single stop train or bad index value, default to down
            _direction = "down";  // NOI18N
            return;
        }

        Stop stop = _stops.get(_stopIdx);
        Station station = _dataMgr.getStation(stop.getStationId());
        double currDistance = station.getDistance();

        // For the first stop, use the next stop to set the direction
        if (_stopIdx == 0) {
            Station nextStation = _dataMgr.getStation(_stops.get(_stopIdx + 1).getStationId());
            if (nextStation.getDistance() > currDistance) {
                _direction = "down";  // NOI18N
            } else {
                _direction = "up";  // NOI18N
            }
            return;
        }

        // For all other stops in the same segment, use the previous stop.
        Station prevStation = _dataMgr.getStation(_stops.get(_stopIdx - 1).getStationId());
        if (prevStation.getSegmentId() == _segmentId) {
            if (prevStation.getDistance() < currDistance) {
                _direction = "down";  // NOI18N
            } else {
                _direction = "up";  // NOI18N
            }
            return;
        }

        // Handle segment change...
        // Get the name of the previous station
        String prevName = prevStation.getStationName();

        // Find the corresponding station in the current Segment
        for (Station segStation : _stations) {
            if (segStation.getStationName().equals(prevName)) {
                if (segStation.getDistance() < currDistance) {
                    _direction = "down";  // NOI18N
                } else {
                    _direction = "up";  // NOI18N
                }
                return;
            }
        }
    }

    void drawLine(Stop stop) {
        int arriveTime = stop.getArriveTime();
        int departTime = stop.getDepartTime();
        double x = _graphLeft + ((arriveTime - _baseTime) * _sizeMinute) + _hourOffset;
        double y = _stationGrid.get(stop.getStationId());
        _trainLine.lineTo(x, y);
        drawTrainTime(arriveTime, "arrive", x, y);  // NOI18N

        // Check for duration after arrive
        if (stop.getDuration() > 0) {
            x = _graphLeft + ((departTime - _baseTime) * _sizeMinute) + _hourOffset;
            if (x < _trainLine.getCurrentPoint().getX()) {
                // The line wraps around to the beginning, do the line in two pieces
                _trainLine.lineTo(_graphRight - _hourOffset, y);
                drawTrainName(_graphRight - _hourOffset, y, "Right", false, false);  // NOI18N
                _trainLine.moveTo(_graphLeft + _hourOffset, y);
                _trainLine.lineTo(x, y);
                drawTrainName(_graphLeft + _hourOffset, y, "Left", false, false);  // NOI18N
                drawTrainTime(departTime, "depart", x, y);  // NOI18N
            } else {
                _trainLine.lineTo(x, y);
                drawTrainTime(departTime, "depart", x, y);  // NOI18N
            }
        }
    }

    void setEnd(Stop stop, boolean endSegment) {
        double x;
        double y;
        boolean skipLine = false;

        int arriveTime = stop.getArriveTime();
        if (_stops.size() == 1 || endSegment) {
            x = _trainLine.getCurrentPoint().getX();
            y = _trainLine.getCurrentPoint().getY();
            skipLine = true;
        } else {
            x = _graphLeft + ((arriveTime - _baseTime) * _sizeMinute) + _hourOffset;
            y = _stationGrid.get(stop.getStationId());
        }

        drawTrainName(x, y, "Center", true, false);  // NOI18N
//         if (_stops.size() > 1) {
//             drawTrainTime(arriveTime, "end", x, y);
//         }
        _g2.setColor(_trainColor);
        _g2.setStroke(stroke);
        if (!skipLine) {
            _trainLine.lineTo(x, y);
        }
        _g2.draw(_trainLine);

        // Process throttle line
        if (_trainThrottle > 0) {
            _g2.setFont(_throttleFont);
            double throttleY = (_trainThrottle * 14);
            if (x < _throttleX) {
                 _g2.draw(new Line2D.Double(_throttleX, throttleY, _graphRight - _hourOffset, throttleY));
                 _g2.draw(new Line2D.Double(_graphLeft + _hourOffset, throttleY, x, throttleY));
                drawTrainName(_throttleX + 10, throttleY + 5, "Left", false, true);  // NOI18N
                drawTrainName(_graphLeft + _hourOffset + 10, throttleY + 5, "Left", false, true);  // NOI18N
           } else {
                _g2.draw(new Line2D.Double(_throttleX, throttleY, x, throttleY));
                drawTrainName(_throttleX + 10, throttleY + 5, "Left", false, true);  // NOI18N
            }
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimeTableGraph.class);
}
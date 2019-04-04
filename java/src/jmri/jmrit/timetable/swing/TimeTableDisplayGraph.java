package jmri.jmrit.timetable.swing;

import java.awt.*;
import javax.swing.*;

/**
 * Display a timetable graph.
 * @author Dave Sand Copyright (c) 2019
 */
public class TimeTableDisplayGraph extends JPanel {

    /**
     * Initialize the data used by paint() and supporting methods when the
     * panel is displayed.
     * @param segmentId The segment to be displayed.  For multiple segment
     * layouts separate graphs are required.
     * @param scheduleId The schedule to be used for this graph.
     * @param showTrainTimes When true, include the minutes portion of the
     * train times at each station.
     */
    public TimeTableDisplayGraph(int segmentId, int scheduleId, boolean showTrainTimes) {
        _segmentId = segmentId;
        _scheduleId = scheduleId;
        _showTrainTimes = showTrainTimes;
    }

    final int _segmentId;
    final int _scheduleId;
    final boolean _showTrainTimes;
    Graphics2D _g2;

    @Override
    public void paint(Graphics g) {
        if (g instanceof Graphics2D) {
            _g2 = (Graphics2D) g;
        } else {
            throw new IllegalArgumentException();
        }
        Dimension dim = getSize();
        double dimHeight = dim.getHeight();
        double dimWidth = dim.getWidth();

        TimeTableGraphCommon graph = new TimeTableGraphCommon();
        graph.init(_segmentId, _scheduleId, _showTrainTimes, dimHeight, dimWidth, true);
        graph.doPaint(_g2);

    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimeTableDisplayGraph.class);
}
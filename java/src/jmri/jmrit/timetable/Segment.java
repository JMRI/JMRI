package jmri.jmrit.timetable;

/**
 * Define the content of a Segment record.
 *
 * @author Dave Sand Copyright (C) 2018
 */
public class Segment {

    public Segment(int segmentId, int layoutId, String segmentName) {
        _segmentId = segmentId;
        _layoutId = layoutId;
        _segmentName = segmentName;
    }

    private int _segmentId = 0;
    private int _layoutId = 0;
    private String _segmentName = "";

    public int getSegmentId() {
        return _segmentId;
    }

    public int getLayoutId() {
        return _layoutId;
    }

    public String getSegmentName() {
        return _segmentName;
    }

    public void setSegmentName(String newName) {
        _segmentName = newName;
    }

    public String toString() {
        return _segmentName;
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Segment.class);

}

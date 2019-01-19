package jmri.jmrix.internal;

import java.util.Deque;
import java.util.ArrayDeque;
import jmri.Report;
import jmri.implementation.AbstractReporter;
import jmri.CollectingReporter;

/**
 * Extension of the AbstractReporter class that implements CollectingReporter
 * and represents the contents of a track.  This is an internal construct that
 * does not correspond to a physical reporter.
 * <P>
 *
 * @author Paul Bender Copyright (C) 2019
 */
public class TrackReporter extends AbstractReporter implements CollectingReporter {

    private Deque<Report> collection = null;

    public TrackReporter(String systemName) {
        super(systemName.toUpperCase());
        collection = new ArrayDeque<>();
    }

    public TrackReporter(String systemName, String userName) {
        super(systemName.toUpperCase(), userName);
        collection = new ArrayDeque<>();
    }

    /**
     * Provide a general method for updating the report.
     */
    @Override
    public void setReport(Report r) {
        if (r == _currentReport) {
            return;
        }
        Report old = _currentReport;
        Report oldLast = _lastReport;
        _currentReport = r;
        if (r != null) {
            _lastReport = r;
            // notify
            firePropertyChange("lastReport", oldLast, _lastReport);
        }
        // notify
        firePropertyChange("currentReport", old, _currentReport);
    }
            
    @Override
    public int getState() {
       return state;
    }

    @Override
    public void setState(int s) {
       state = s;
    }
    int state = 0;

    //CollectingReporter Interface Method(s)
    /**
     * @return the collection of elements associated with this reporter.
     */
    public java.util.Collection getCollection(){
       return(collection);
    }

    // Special methods to set the report from the ends of the track
    // these methods record the order of reports seen.

    @SuppressWarnings("unchecked")
    public void pushEast(Report o){
         if(o != null) {
            collection.addFirst(o);
            setReport(o);
         }
    }

    @SuppressWarnings("unchecked")
    public void pushWest(Report o){
         if(o != null) {
            collection.addLast(o);
            setReport(o);
         }
    }

    public Report pullEast(){
       Report retval = collection.removeFirst();
       setReport(collection.peekFirst());
       return retval;
    }

    public Report pullWest(){
       Report retval = collection.removeLast();
       setReport(collection.peekLast());
       return retval;
    }

}

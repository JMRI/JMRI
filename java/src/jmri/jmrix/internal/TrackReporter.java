package jmri.jmrix.internal;

import java.util.Deque;
import java.util.ArrayDeque;
import jmri.implementation.AbstractReporter;
import jmri.CollectingReporter;

/**
 * Extension of the AbstractReporter class that implements CollectingReporter
 * and represents the contents of a track.  This is an internal construct that
 * does not correspond to a physical reporter.
 *
 * @author Paul Bender Copyright (C) 2019
 */
public class TrackReporter extends AbstractReporter implements CollectingReporter {

    private Deque collection = null;

    public TrackReporter(String systemName) {
        super(systemName);
        collection = new ArrayDeque<Object>();
    }

    public TrackReporter(String systemName, String userName) {
        super(systemName, userName);
        collection = new ArrayDeque<Object>();
    }

    /**
     * Provide a general method for updating the report.
     */
    @Override
    public void setReport(Object r) {
        if (r == _currentReport) {
            return;
        }
        Object old = _currentReport;
        Object oldLast = _lastReport;
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
    @Override
    public java.util.Collection getCollection(){
       return(collection);
    }

    // Special methods to set the report from the ends of the track
    // these methods record the order of reports seen.

    @SuppressWarnings("unchecked")
    public void pushEast(Object o){
         if(o != null) {
            collection.addFirst(o);
            setReport(o);
         }
    }

    @SuppressWarnings("unchecked")
    public void pushWest(Object o){
         if(o != null) {
            collection.addLast(o);
            setReport(o);
         }
    }

    public Object pullEast(){
       Object retval = collection.removeFirst();
       setReport(collection.peekFirst());
       return retval;
    }

    public Object pullWest(){
       Object retval = collection.removeLast();
       setReport(collection.peekLast());
       return retval;
    }

}

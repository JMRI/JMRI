package jmri.jmrix.internal;

import java.util.Deque;
import java.util.ArrayDeque;

import jmri.implementation.AbstractReporter;
import jmri.CollectingReporter;

/**
 * Extension of the AbstractReporter class that implements CollectingReporter
 * and represents the contents of a track.  This is an internal construct that
 * does not correspond to a physical reporter.
 * <P>
 * This reporter is not used by JMRI itself but by scripts. So it's not
 * refactored to use ExtendedReport.
 *
 * @author Paul Bender Copyright (C) 2019
 */
public class TrackReporter extends AbstractReporter implements CollectingReporter {

    private Deque<Object> collection = null;

    public TrackReporter(String systemName) {
        super(systemName);
        collection = new ArrayDeque<>();
    }

    public TrackReporter(String systemName, String userName) {
        super(systemName, userName);
        collection = new ArrayDeque<>();
    }

    @Override
    public boolean isExtendedReportsSupported() {
        return false;
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
    public java.util.Collection<Object> getCollection(){
       return(collection);
    }

    // Special methods to set the report from the ends of the track
    // these methods record the order of reports seen.

    public void pushEast(Object o){
         if(o != null) {
            collection.addFirst(o);
            setReport(o);
         }
    }

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

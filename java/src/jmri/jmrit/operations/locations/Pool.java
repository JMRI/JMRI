package jmri.jmrit.operations.locations;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a pool of tracks that share their length.
 * 
 * @author Daniel Boudreau Copyright (C) 2011
 * @version             $Revision$
 */
public class Pool{

	protected String _id = "";
	protected String _name = "";
	//protected String _comment = "";
	
	//	 stores tracks for this pool
	protected List<Track> _pool = new ArrayList<Track>();
	
	public static final String LISTCHANGE_CHANGED_PROPERTY = "listChange";
	public static final String DISPOSE = "dispose";
	

	public Pool(String id, String name){
		log.debug("New pool (" + name + ") id: " + id);
		_name = name;
		_id = id;
	}

	public String getId() {
		return _id;
	}

	public void setName(String name){
		String old = _name;
		_name = name;
		if (!old.equals(name)){
			firePropertyChange("name", old, name);
		}
	}
	
	// for combo boxes
	public String toString(){
		return _name;
	}

	public String getName(){
		return _name;
	}
	
	public int getSize(){
		return _pool.size();
	}

	/*
	public void setComment(String comment){
		_comment = comment;
	}
	
	public String getComment(){
		return _comment;
	}
	*/

    public void dispose(){
    	firePropertyChange (DISPOSE, null, DISPOSE);
    }
 
    /**
     * Adds a track to this pool
     * @param track to be added.
     */
    public void add(Track track){
    	if (!_pool.contains(track)){
    		_pool.add(track);
    		firePropertyChange(LISTCHANGE_CHANGED_PROPERTY, Integer.valueOf(_pool.size()-1), Integer.valueOf(_pool.size()));
    	}
    }
    
    /**
     * Removes a track from this pool
     * @param track to be removed.
     */
    public void remove(Track track){
    	if (_pool.contains(track)){
    		_pool.remove(track);
    		firePropertyChange(LISTCHANGE_CHANGED_PROPERTY, Integer.valueOf(_pool.size()+1), Integer.valueOf(_pool.size()));
    	}
    }
    
    public List<Track> getTracks(){
    	List<Track> tracks = new ArrayList<Track>();
    	for (int i=0; i<_pool.size(); i++)
    		tracks.add(_pool.get(i));
    	return tracks; 
    }
    
    /**
     * Request track length from one of the other tracks in this pool.
     * @param track the track requesting additional length
     * @param length the amount of track length requested
     * @return true if successful
     */
    public boolean requestTrackLength(Track track, int length){
    	int additionalLength = track.getUsedLength() + track.getReserved() + length - track.getLength();
    	List<Track> tracks = getTracks();
    	for (int i=0; i<tracks.size(); i++){
    		Track t = tracks.get(i);
    		// note that the reserved track length can be both positive and negative, that's the reason
    		// for the second check that doesn't include the reserve, this prevent overloading.
    		if (t != track && (t.getUsedLength() + t.getReserved() + additionalLength) <= t.getLength()
    				&& (t.getLength() - additionalLength) >= t.getMinimumLength()){
    			// allow overloading.  Even tracks out of pools experience overloading.
    				//&& (t.getUsedLength() + additionalLength) <= t.getLength()){
    			log.debug("Increasing track ("+track.getName()+") length ("+additionalLength+") decreasing ("+t.getName()+")");
    			t.setLength(t.getLength()-additionalLength);
    			track.setLength(track.getLength()+additionalLength);
    			return true;
    		}
    	}
    	return false;
    }

	java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(
			this);

	public synchronized void addPropertyChangeListener(
			java.beans.PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	public synchronized void removePropertyChangeListener(
			java.beans.PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	protected void firePropertyChange(String p, Object old, Object n) {
		pcs.firePropertyChange(p, old, n);
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(Pool.class.getName());

}

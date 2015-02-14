package jmri.jmrit.vsdecoder.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.vecmath.Vector3f;
import java.util.List;
import jmri.AudioManager;
import jmri.AudioException;
import jmri.util.PhysicalLocation;
import jmri.jmrit.audio.AudioListener;


public class VSDListener {

    public final static String ListenerSysNamePrefix = "IAL$VSD:";

    private AudioListener _listener;
    private String _sysname;
    private String _username;
    private ListeningSpot _location;

    public VSDListener(String sname) {
	this(sname, sname);
    }

    public VSDListener(String sname, String uname) {
	_sysname = ListenerSysNamePrefix + sname;
	_username = uname;

        AudioManager am = jmri.InstanceManager.audioManagerInstance();
	try {
	    _listener = (AudioListener) am.provideAudio(ListenerSysNamePrefix + _sysname);
	    log.debug("Listener Created: " + _listener);
	} catch (AudioException ae) {
	    log.debug("AudioException creating Listener: " + ae);
	    // Do nothing?
	}
    }

    public VSDListener(AudioListener l) {
	_listener = l;
	_sysname = l.getSystemName();
	_username = l.getUserName();
    }

    public VSDListener() {
	// Initialize the AudioManager (if it isn't already) and get the Listener.
	AudioManager am = jmri.InstanceManager.audioManagerInstance();
	am.init();
	_listener = am.getActiveAudioFactory().getActiveAudioListener();
	
	List<String> names = am.getSystemNameList('L');
	if (names.size() == 0) {
	    log.debug("No Listener yet. Creating one.");
	} else {
	    log.debug("Found name: " + names.get(0));
	}
	_sysname = _listener.getSystemName();
	_username = _listener.getUserName();
    }

    public String getSystemName() { return(_sysname); }
    public String getUserName() { return(_username); }
    public ListeningSpot getLocation() { return(_location); }
    public void setSystemName(String s) { _sysname = s; }
    public void setUserName(String u) { _username = u; }

    public void setLocation(ListeningSpot l) {
	_location = l;
	_listener.setPosition(new Vector3f(l.getLocation()));
	_listener.setOrientation(new Vector3f(l.getLookAtVector()), new Vector3f(l.getUpVector()));
	// Set position here
    }

    public void setPosition(PhysicalLocation p) {
	if (_location == null)
	    _location = new ListeningSpot();
	_location.setLocation(p);
	_listener.setPosition(p);
    }

    static Logger log = LoggerFactory.getLogger(VSDListener.class.getName());

}

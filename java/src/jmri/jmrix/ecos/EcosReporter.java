// EcosReporter.java

package jmri.jmrix.ecos;

import jmri.implementation.AbstractReporter;
import jmri.Sensor;
import jmri.IdTag;

/**
 * Extend jmri.AbstractReporter for Ecos Reporters
 * Implemenation for providing status of rail com decoders at this
 * reporter location.
 * <p>
 * The reporter will decode the rail com packets and add the information 
 * to the rail com tag.
 * <P>
 * @author			Kevin Dickerson Copyright (C) 2012
 * @version			$Revision: 17977 $
 */
 
 public class EcosReporter extends AbstractReporter{

    public EcosReporter(String systemName, String userName, EcosSystemConnectionMemo memo) {  // a human-readable Reporter number must be specified!
        super(systemName, userName);  // can't use prefix here, as still in construction
        this.memo=memo;
     }
    
    EcosSystemConnectionMemo memo;
    
	/**
	 * Provide an int value for use in scripts, etc.  This will be
	 * the numeric locomotive address last seen, unless the last 
	 * message said the loco was exiting. Note that there may still some
	 * other locomotive in the transponding zone!
	 * @return -1 if the last message specified exiting
	 */
	public int getState() {
	 	return lastLoco;
	}

	public void setState(int s) {
	 	lastLoco = s;
	}	 
	int lastLoco = -1;
    
    private int object;
    private int port;
    
    public int getObjectId(){
        return object;
    }
    
    public int getPort(){
        return port;
    }
    
    public void setObjectPort(int object, int port){
        this.object = object;
        this.port = port;
    }
    
    //This could possibly do with a debounce option being added
    public void decodeDetails(String msg){
        int start = msg.indexOf('[')+1;
        int end = msg.indexOf(']');
        String[] result = msg.substring(start, end).split(",");
        result[1]=result[1].trim();
        if(!result[1].equals("0000")){
            IdTag idTag = jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag(result[1]);
            setReport(idTag);
        } else {
            setReport(null);
        }
    
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EcosReporter.class.getName());

 }

/* @(#)EcosReporter.java */

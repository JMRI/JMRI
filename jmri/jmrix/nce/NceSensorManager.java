// NceSensorManager.java

package jmri.jmrix.nce;

import jmri.JmriException;
import jmri.Sensor;
import jmri.jmrix.AbstractMRReply;

/**
 * Manage the NCE-specific Sensor implementation.
 * <P>
 * System names are "NSnnn", where nnn is the sensor number without padding.
 * <P>
 * This class is responsible for generating polling messages
 * for the NceTrafficController,
 * see nextAiuPoll()
 * <P>
 * @author			Bob Jacobsen Copyright (C) 2003
 * @version			$Revision: 1.8 $
 */
public class NceSensorManager extends jmri.AbstractSensorManager
                            implements NceListener {

    public NceSensorManager() {
        super();
        mInstance = this;
        for (int i=MINAIU; i<=MAXAIU; i++)
            aiuArray[i] = null;
    }

    static public NceSensorManager instance() {
        if (mInstance == null) new NceSensorManager();
        return mInstance;
    }
    static private NceSensorManager mInstance = null;

    public char systemLetter() { return 'N'; }

    // to free resources when no longer used
    public void dispose() {
    }

    public Sensor createNewSensor(String systemName, String userName) {
        int number = Integer.parseInt(systemName.substring(2));

        Sensor s = new NceSensor(systemName);
        s.setUserName(userName);

        // ensure the AIU exists
        int index = (number/16)+1;
        if (aiuArray[index] == null) aiuArray[index] = new NceAIU();
        aiusPresent = true;

        // register this sensor with the AIU
        aiuArray[index].registerSensor((NceSensor)s, number-(index-1)*16);

        return s;
    }

    NceAIU[] aiuArray = new NceAIU[MAXAIU+1];  // element 0 isn't used


    private static int MINAIU =  1;
    private static int MAXAIU = 63;
    private int mNextIndex = MINAIU;
    private boolean aiusPresent = false;

    public NceMessage nextAiuPoll() {
        if (!aiusPresent) return null;

        // increment to next entry
        mNextIndex++;
        if (mNextIndex>MAXAIU) mNextIndex = MINAIU;

        // skip over undefined AIU entries
        while (aiuArray[mNextIndex]==null) {
            mNextIndex++;
            if (mNextIndex>MAXAIU) mNextIndex = MINAIU;
        }

        // format the message
        NceMessage m = new NceMessage(2);
        m.setBinary(true);
        m.setReplyLen(4);
        m.setElement(0, 0x8A);
        m.setElement(1, mNextIndex);

        return m;
    }

    public void message(NceMessage r) {
        log.warn("unexpected message");
    }
    public void reply(NceReply r) {
    	if (!r.isUnsolicited()) {
    		int bits = r.pollValue();  // bits is the value in hex from the message
    		aiuArray[mNextIndex].markChanges(bits);
    	}
    }
    
    /**
     * Handle an unsolicited sensor (AIU) state message
     * @param r
     */
    
    public void handleSensorMessage(AbstractMRReply r) {
        int index = r.getElement(1) - 0x30;
        int indicator = r.getElement(2);
        if (r.getElement(0)==0x61 && r.getElement(1)>=0x30 && r.getElement(1)<=0x6f &&
                ((indicator>=0x41 && indicator<=0x5e ) || (indicator>=0x61 && indicator<=0x7e ))) {
            if (aiuArray[index]==null) {
                log.debug("unsolicited message \"" + r.toString()+ "for unused sensor array");
            } else {
                int sensorNo;
                int newState;
                if (indicator >= 0x60) { 
                    sensorNo = indicator - 0x61;
                    newState = Sensor.ACTIVE;
                } else {
                    sensorNo = indicator - 0x41;
                    newState = Sensor.INACTIVE;
                }
                Sensor s = aiuArray[index].getSensor(sensorNo);
                if (log.isDebugEnabled()) {
                    String msg = "Handling sensor message \""+r.toString()+ "\" for ";
                    if (s != null) {
                        msg += s.getSystemName();
                    } else {
                        msg += "unmonitored sensor";
                    }
                    if (newState == Sensor.ACTIVE) {
                        msg += ": ACTIVE";
                    } else {
                        msg += ": INACTIVE";
                    }
                    log.debug(msg);
                }
                if (s!=null) {
                    aiuArray[index].sensorChange(sensorNo, newState);
                }
            }
        } else {
            log.warn("incorrect sensor message: "+r.toString());
        }
    }
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceSensorManager.class.getName());
}

/* @(#)NceSensorManager.java */

// NceSensorManager.java

package jmri.jmrix.nce;

import jmri.JmriException;
import jmri.Sensor;

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
 * @version			$Revision: 1.4 $
 */
public class NceSensorManager extends jmri.AbstractSensorManager
                            implements NceListener {

    public NceSensorManager() {
        super();
        for (int i=MINAIU; i<=MAXAIU; i++)
            aiuArray[i] = null;
    }

    public char systemLetter() { return 'N'; }

    // to free resources when no longer used
    public void dispose() {
    }

    public Sensor newSensor(String systemName, String userName) {
        if (log.isDebugEnabled()) log.debug("newSensor:"
                                            +( (systemName==null) ? "null" : systemName)
                                            +";"+( (userName==null) ? "null" : userName));

        // if system name is null, supply one from the number in userName
        if (systemName == null) systemName = "NS"+userName;

        // get number from name
        if (!systemName.startsWith("NS")) {
            log.error("Invalid system name for NCE sensor: "+systemName);
            return null;
        }
        int number = Integer.parseInt(systemName.substring(2));

        // return existing if there is one
        Sensor s;
        if ( (userName!=null) && ((s = getByUserName(userName)) != null)) return s;
        if ( (systemName!=null) && ((s = getBySystemName(systemName)) != null)) return s;

        // doesn't exist, make a new one
        s = new NceSensor(systemName);

        // save in the maps
        _tsys.put(systemName, s);
        if (userName!=null) _tuser.put(userName, s);

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
        String s;
        if (mNextIndex<10)
            s = "I 0"+mNextIndex;
        else
            s = "I "+mNextIndex;
        NceMessage m = new NceMessage(s);
        m.setBinary(false);

        return m;
    }

    public void message(NceMessage r) {
        log.warn("unexpected message");
    }
    public void reply(NceReply r) {
        int bits = r.pollValue();  // bits is the value in hex from the message
        aiuArray[mNextIndex].markChanges(bits);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceSensorManager.class.getName());
}

/* @(#)NceSensorManager.java */

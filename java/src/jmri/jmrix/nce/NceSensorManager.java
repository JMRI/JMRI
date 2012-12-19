// NceSensorManager.java

package jmri.jmrix.nce;

import jmri.Sensor;
import jmri.jmrix.AbstractMRReply;
import jmri.JmriException;

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
 * @version			$Revision$
 */
public class NceSensorManager extends jmri.managers.AbstractSensorManager
                            implements NceListener {

    public NceSensorManager(NceTrafficController tc, String prefix) {
        super();
        this.tc = tc;
        this.prefix = prefix;
        for (int i=MINAIU; i<=MAXAIU; i++)
            aiuArray[i] = null;
        listener = new NceListener() {
        	public void message(NceMessage m) { }
        	public void reply(NceReply r) {
        		if (r.isSensorMessage()) {
        			mInstance.handleSensorMessage(r); 
        		}
        	}
        };
        tc.addNceListener(listener);
    }
    
    NceTrafficController tc = null;
    String prefix = "N";

    private NceSensorManager mInstance = null;

    public String getSystemPrefix() { return prefix; }

    // to free resources when no longer used
    public void dispose() {
    	stopPolling = true;		// tell polling thread to go away
        tc.removeNceListener(listener);
        super.dispose();
    }

    public Sensor createNewSensor(String systemName, String userName) {
        int number = Integer.valueOf(systemName.substring(getSystemPrefix().length()+1)).intValue();

        Sensor s = new NceSensor(systemName);
        s.setUserName(userName);

        // ensure the AIU exists
        int index = (number/16)+1;
        if (aiuArray[index] == null) {
        	aiuArray[index] = new NceAIU();
            buildActiveAIUs();
        }

        // register this sensor with the AIU
        aiuArray[index].registerSensor(s, number-(index-1)*16);

        return s;
    }

    NceAIU[] aiuArray = new NceAIU[MAXAIU+1];  // element 0 isn't used
    int [] activeAIUs = new int[MAXAIU];		// keep track of those worth polling
    int activeAIUMax = 0;							// last+1 element used of activeAIUs
    private static int MINAIU =  1;
    private static int MAXAIU = 63;

    Thread pollThread;
    boolean stopPolling = false;
    NceListener listener;
    
    // polling parameters and variables
    
    private final int shortCycleInterval = 200;
    private final int longCycleInterval = 10000;		// when we know async messages are flowing
    private final long maxSilentInterval = 30000;		// max slow poll time without hearing an async message
    private final int pollTimeout = 20000;				// in case of lost response
    private int aiuCycleCount;
    private long lastMessageReceived;					// time of last async message
    private NceAIU currentAIU;
    private boolean awaitingReply = false;
    private boolean awaitingDelay = false;
    
    /**
     * Build the array of the indices of AIUs which have been polled,
     * and ensures that pollManager has all the information it needs to
     * work correctly.
     *
     */
    
    /* Some logic notes
     * 
     * Sensor polling normally happens on a short cycle - the NCE round-trip
     * response time (normally 50mS, set by the serial line timeout) plus
     * the "shortCycleInterval" defined above. If an async sensor message is received,
     * we switch to the longCycleInterval since really we don't need to poll at all.
     * 
     * We use the long poll only if the following conditions are satisified:
     * 
     * -- there have been at least two poll cycle completions since the last change
     * to the list of active sensor - this means at least one complete poll cycle,
     * so we are sure we know the states of all the sensors to begin with
     * 
     * -- we have received an async message in the last maxSilentInterval, so that
     * if the user turns off async messages (possible, though dumb in mid session)
     * the system will stumble back to life
     * 
     * The interaction between buildActiveAIUs and pollManager is designed so that
     * no explicit sync or locking is needed when the former changes the list of active
     * AIUs used by the latter. At worst, there will be one cycle which polls the same
     * sensor twice.
     * 
     * Be VERY CAREFUL if you change any of this.
     * 
     */
    private void buildActiveAIUs() {
    	activeAIUMax = 0;
    	for (int a=MINAIU; a<=MAXAIU; ++a) {
    		if (aiuArray[a] != null) {
    			activeAIUs[activeAIUMax++] = a;
    		}
    	}
    	aiuCycleCount = 0;				// force another polling cycle
    	lastMessageReceived = Long.MIN_VALUE;
    	if (activeAIUMax>0) {
    		if (pollThread==null) {
    			pollThread = new Thread(new Runnable()
    					{ public void run() { pollManager(); }});
    			pollThread.setName("NCE Sensor Poll");
    			pollThread.start();
    		} else {
    			synchronized (this) {
    				if (awaitingDelay) {		// interrupt long between-poll wait
    					notify();
    				}
    			}
    		}
    	}
    }
    
    public NceMessage makeAIUPoll(int aiuNo) {
    	// use old 4 byte read command if not USB
    	if (tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_NONE)
    		return makeAIUPoll4ByteReply(aiuNo);
    	else
    		return makeAIUPoll2ByteReply(aiuNo);
    }

    /**
     * construct a binary-formatted AIU poll message
     * @param aiuNo	number of AIU to poll
     * @return	message to be queued
     */
    private NceMessage makeAIUPoll4ByteReply(int aiuNo) {
        NceMessage m = new NceMessage(2);
        m.setBinary(true);
        m.setReplyLen(4);
        m.setElement(0, NceBinaryCommand.READ_AUI4_CMD);
        m.setElement(1, aiuNo);
        m.setTimeout(pollTimeout);
        return m;
    }
    
    /**
     * construct a binary-formatted AIU poll message
     * @param aiuNo	number of AIU to poll
     * @return	message to be queued
     */
    private NceMessage makeAIUPoll2ByteReply(int aiuNo) {
        NceMessage m = new NceMessage(2);
        m.setBinary(true);
        m.setReplyLen(2);
        m.setElement(0, NceBinaryCommand.READ_AUI2_CMD);
        m.setElement(1, aiuNo);
        m.setTimeout(pollTimeout);
        return m;
    }

    /**
     * pollManager - send poll messages for AIU sensors. Also interact with asynchronous 
     * sensor state messages. Adjust poll cycle according to whether any async messages have
     * been received recently. Also we require one poll of each sensor before squelching
     * active polls.
     *
     */
    private void pollManager() {
    	while (!stopPolling) {
    		for (int a=0; a<activeAIUMax; ++a) {
    			int aiuNo = activeAIUs[a];
    			currentAIU = aiuArray[aiuNo];
    			if (currentAIU != null) {				// in case it has gone away
    				NceMessage m = makeAIUPoll(aiuNo);
    				synchronized (this) {
    					if (log.isDebugEnabled()) {
    						log.debug("queueing poll request for AIU "+aiuNo);
    					}
    					tc.sendNceMessage(m, this);
    					awaitingReply = true;
    					try {
    						wait(pollTimeout);
    					} catch (InterruptedException e) {
    					    Thread.currentThread().interrupt(); // retain if needed later
    					}
    				}
     	    		int delay = shortCycleInterval;
    	    		if (aiuCycleCount>=2 &&
    	    				lastMessageReceived>=System.currentTimeMillis()-maxSilentInterval) {
    	    			delay = longCycleInterval;
    	    		}
    	    		synchronized (this){
    	    			if (awaitingReply) {
    	    				log.warn("timeout awaiting poll response for AIU "+aiuNo);
    	    				// slow down the poll since we're not getting responses
    	    				// this lets NceConnectionStatus to do its thing
    	    				delay = pollTimeout;
    	    			}
    	    			try {
    	    				awaitingDelay = true;
    	    				wait(delay);
    	    			} catch (InterruptedException e) {
    	    			    Thread.currentThread().interrupt(); // retain if needed later
    	    			} finally { awaitingDelay = false; }
    	    		}
    			}
    		}
    		++aiuCycleCount;
    	}
    }
    
    public void message(NceMessage r) {
        log.warn("unexpected message");
    }
    
    /**
     * Process single received reply from sensor poll
     */
    public void reply(NceReply r) {
    	if (!r.isUnsolicited()) {
    		int bits;
    		synchronized (this) {
    			bits = r.pollValue();  // bits is the value in hex from the message
    			awaitingReply = false;
    			this.notify();
    		}
    		currentAIU.markChanges(bits);
    		if (log.isDebugEnabled()) {
    			String str = jmri.util.StringUtil.twoHexFromInt((bits>>4)&0xf);
    			str += " ";
    			str = jmri.util.StringUtil.appendTwoHexFromInt(bits&0xf, str);
    			log.debug("sensor poll reply received: \""+str+"\"");
    		}
    	}
    }
    
    
    /**
     * Handle an unsolicited sensor (AIU) state message
     * @param r	sensor message
     */
    
    public void handleSensorMessage(AbstractMRReply r) {
        int index = r.getElement(1) - 0x30;
        int indicator = r.getElement(2);
        if (r.getElement(0)==0x61 && r.getElement(1)>=0x30 && r.getElement(1)<=0x6f &&
                ((indicator>=0x41 && indicator<=0x5e ) || (indicator>=0x61 && indicator<=0x7e ))) {
        	lastMessageReceived = System.currentTimeMillis();
        	if (aiuArray[index]==null) {
                log.debug("unsolicited message \"" + r.toString()+ "\" for unused sensor array");
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
                if (s.getInverted()){
        			if (newState == Sensor.ACTIVE) {
        				newState = Sensor.INACTIVE;
        			} else if (newState == Sensor.INACTIVE) {
        				newState = Sensor.ACTIVE;
        			}
                }

                if (log.isDebugEnabled()) {
                	String msg = "Handling sensor message \""+r.toString()+ "\" for ";
                	msg += s.getSystemName();

                	if (newState == Sensor.ACTIVE) {
                		msg += ": ACTIVE";
                	} else {
                		msg += ": INACTIVE";
                	}
                	log.debug(msg);
                }
                aiuArray[index].sensorChange(sensorNo, newState);
            }
        } else {
            log.warn("incorrect sensor message: "+r.toString());
        }
    }
    
    public boolean allowMultipleAdditions(String systemName) { return true;  }
    
    public String createSystemName(String curAddress, String prefix) throws JmriException{
        if(curAddress.contains(":")){
            //Sensor address is presented in the format AIU Cab Address:Pin Number On AIU
            //Should we be validating the values of aiucab address and pin number?
            int seperator = curAddress.indexOf(":");
            try {
                aiucab = Integer.valueOf(curAddress.substring(0,seperator)).intValue();
                pin = Integer.valueOf(curAddress.substring(seperator+1)).intValue();
            } catch (NumberFormatException ex) { 
                log.error("Unable to convert " + curAddress + " into the cab and pin format of nn:xx");
                throw new JmriException("Hardware Address passed should be a number");
            }
            iName = (aiucab-1)*16+pin-1;
        
        } else {
            //Entered in using the old format
            try {
                iName = Integer.parseInt(curAddress);
            } catch (NumberFormatException ex) { 
                log.error("Unable to convert " + curAddress + " Hardware Address to a number");
                throw new JmriException("Hardware Address passed should be a number");
            }
        }
        return prefix+typeLetter()+iName;
    
    }
    
    int aiucab = 0;
    int pin = 0;
    int iName = 0;
    
    public String getNextValidAddress(String curAddress, String prefix){

        String tmpSName = "";

        try {
            tmpSName = createSystemName(curAddress, prefix);
        } catch (JmriException ex) {
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    showInfoMessage("Error","Unable to convert " + curAddress + " to a valid Hardware Address",""+ex, "",true, false, org.apache.log4j.Level.ERROR);
            return null;
        }
        
        //Check to determine if the systemName is in use, return null if it is,
        //otherwise return the next valid address.
        Sensor s = getBySystemName(tmpSName);
        if(s!=null){
            for(int x = 1; x<10; x++){
                iName=iName+1;
                s = getBySystemName(prefix+typeLetter()+iName);
                if(s==null){
                    return Integer.toString(iName);
                }
            }
            return null;
        } else {
            return Integer.toString(iName);
        }
        
    }
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NceSensorManager.class.getName());
}

/* @(#)NceSensorManager.java */

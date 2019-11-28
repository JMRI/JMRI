package jmri.jmrit.ussctc;

import java.util.*;
import jmri.*;
/**
 * Drive the code line communications on a USS CTC panel.
 * <p>
 * Primary interactions are with a group of {@link Station} objects
 * that make up the panel.  Can also work with external 
 * hardware via Turnout/Sensor interfaces.
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
public class CodeLine {

    /**
     * Create and configure 
     *
     * @param startIndicateTO  Name for turnout that starts indication operations on the layout
     * @param startSendTO  Name for turnout that starts send operations on the layout
     * @param output1TO  Turnout name for 1st channel of code information
     * @param output2TO  Turnout name for 2nd channel of code information
     * @param output3TO  Turnout name for 3rd channel of code information
     * @param output4TO  Turnout name for 4th channel of code information
     */
    public CodeLine(String startIndicateTO, String startSendTO, String output1TO, String output2TO, String output3TO, String output4TO) {
        NamedBeanHandleManager hm = InstanceManager.getDefault(NamedBeanHandleManager.class);
        TurnoutManager tm = InstanceManager.getDefault(TurnoutManager.class);

        logMemory = InstanceManager.getDefault(MemoryManager.class).provideMemory(
                        Constants.commonNamePrefix+"CODELINE"+Constants.commonNameSuffix+"LOG");  // NOI18N
        log.debug("log memory name is {}", logMemory.getSystemName());  // NOI18N
        
        hStartIndicateTO = hm.getNamedBeanHandle(startIndicateTO, tm.provideTurnout(startIndicateTO));
        hStartSendTO = hm.getNamedBeanHandle(startSendTO, tm.provideTurnout(startSendTO));

        hOutput1TO = hm.getNamedBeanHandle(output1TO, tm.provideTurnout(output1TO));
        hOutput2TO = hm.getNamedBeanHandle(output2TO, tm.provideTurnout(output2TO));
        hOutput3TO = hm.getNamedBeanHandle(output3TO, tm.provideTurnout(output3TO));
        hOutput4TO = hm.getNamedBeanHandle(output4TO, tm.provideTurnout(output4TO));
    }

    final Memory logMemory;

    final NamedBeanHandle<Turnout> hStartIndicateTO;
    final NamedBeanHandle<Turnout> hStartSendTO;

    final NamedBeanHandle<Turnout> hOutput1TO;
    final NamedBeanHandle<Turnout> hOutput2TO;
    final NamedBeanHandle<Turnout> hOutput3TO;
    final NamedBeanHandle<Turnout> hOutput4TO;
    
    public static int START_PULSE_LENGTH = 500; // mSec
    public static int CODE_SEND_DELAY = 2500; // mSec
    public static int INTER_INDICATION_DELAY = 500; // mSec
    
    volatile Deque<Station> codeQueue = new ArrayDeque<>();
    volatile Deque<Station> indicationQueue = new ArrayDeque<>();
    
    volatile boolean active = false;
    
    synchronized void endAndCheckNext() {
        if (!active) log.error("endAndCheckNext with active false");
        active = false;
        checkForWork();
    }
    
    synchronized void checkForWork() {
        log.debug("checkForWork with active == {}", active);
        if (active) return;
        active = true;
        
        // indications have priority over code sends
        final Station indicatorStation = indicationQueue.pollFirst();
        if (indicatorStation != null) {
            // go inactive for just a bit before starting indication cycles
            jmri.util.ThreadingUtil.runOnGUIDelayed( ()->{
                    startSendIndication(indicatorStation);
                }, INTER_INDICATION_DELAY);
            return;
        }
        Station codeStation = codeQueue.pollFirst();
        if (codeStation != null) {
            startSendCode(codeStation);
            return;
        }
        active = false;
        logMemory.setValue("");
        log.debug("CodeLine goes inactive");  // NOI18N
    }
    
    /**
     * Request processing of an indication from the field
     */
    synchronized void requestSendCode(Station station) {
        log.debug("requestSendCode queued from {}", station.toString());
        // remove if present
        while (codeQueue.contains(station)) {
            codeQueue.remove(station);
            log.debug("     removed previous request");
        }
        codeQueue.addLast(station);
        checkForWork();
    }

    void startSendCode(Station station) {
        final Station s = station;
        log.debug("CodeLine startSendCode - Tell hardware to start sending code");  // NOI18N
        logMemory.setValue("Sending Code: Station "+station.getName());  // NOI18N
        startSendExternalCodeLine();
        
        // Wait time for sequence complete, then proceed to end of code send
        // ToDo: Allow an input to end this too
        jmri.util.ThreadingUtil.runOnGUIDelayed( ()->{
                    s.codeValueDelivered();
                    // and see if anything else needs to be done
                    log.debug("end of codeValueDelivered");  // NOI18N
                    endAndCheckNext();
                }, CODE_SEND_DELAY);
    }
    
    void startSendExternalCodeLine() {
        hStartSendTO.getBean().setCommandedState(Turnout.THROWN);
        jmri.util.TimerUtil.schedule(new TimerTask() {
            @Override
            public void run() {
                hStartSendTO.getBean().setCommandedState(Turnout.CLOSED);
            }
        }, START_PULSE_LENGTH);
    }
    
    /**
     * Request processing of an indication from the field
     */
    synchronized void requestIndicationStart(Station station) {
        log.debug("requestIndicationStart queued from {}", station.toString());
        // remove if present
        while (indicationQueue.contains(station)) {
            indicationQueue.remove(station);
            log.debug("     removed previous request");
        }
        indicationQueue.addLast(station);
        checkForWork();
    }
    
    void startSendIndication(Station station) {
        final Station s = station;
        log.debug("CodeLine startSendIndication - process indication from field");

        // light code light and gather values
        station.indicationStart();
    
        log.debug("Tell hardware to start sending indication");
        logMemory.setValue("Receiving Indication: Station "+station.getName());  // NOI18N
        startIndicationExternalCodeLine();
        
        // Wait time for sequence complete, then proceed to end of indication send
        // ToDo: Allow an input to end this too
        jmri.util.ThreadingUtil.runOnGUIDelayed( ()->{
                    log.debug("hardware delay done, receiving indication");
                    s.indicationComplete();
                    log.debug("end of indicationComplete");
                    // and see if anything else needs to be done
                    endAndCheckNext();
                }, CODE_SEND_DELAY);
    }

    void startIndicationExternalCodeLine() {
        hStartIndicateTO.getBean().setCommandedState(Turnout.THROWN);
        jmri.util.TimerUtil.schedule(new TimerTask() {
            @Override
            public void run() {
                hStartIndicateTO.getBean().setCommandedState(Turnout.CLOSED);
            }
        }, START_PULSE_LENGTH);
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CodeLine.class);
}

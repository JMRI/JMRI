package jmri.jmrit.ussctc;

import jmri.util.JUnitUtil;
import java.util.*;
import jmri.*;

/**
 * Demo of classes in jmri.jmrit.ussctc
 *
 * @author	Bob Jacobsen Copyright 2003, 2007, 2015, 2017
 */
public class PackageDemo {

    public PackageDemo(String s) {
    }

    // Main entry point
    static public void main(String[] args) {
        JUnitUtil.setUp();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initMemoryManager();  
        JUnitUtil.resetProfileManager();

        // load file that defines various NamedBeans and pops a demo panel
        try {
            InstanceManager.getDefault(ConfigureManager.class)
                    .load(new java.io.File("java/test/jmri/jmrit/ussctc/PackageDemo.xml"));
            InstanceManager.getDefault(LogixManager.class).activateAllLogixs();
        } catch (Exception e) { System.err.println(e); }
        
        // turn signals on for display
        SignalHeadManager shm = InstanceManager.getDefault(SignalHeadManager.class);
        shm.getSignalHead("2R Upper").setAppearance(SignalHead.RED);
        shm.getSignalHead("2R Lower").setAppearance(SignalHead.RED);
        shm.getSignalHead("2L Main").setAppearance(SignalHead.RED);
        shm.getSignalHead("2L Siding").setAppearance(SignalHead.RED);
        
        // create and wire USS CTC objects
        Bell bell = new PhysicalBell("CTC Bell");
        
        CodeLine line = new CodeLine("Code Indication Start", "Code Send Start", "IT101", "IT102", "IT103", "IT104");

        CodeButton button = new CodeButton("Sec1 Code", "Sec1 Code");
        Station station = new Station("1", line, button);

        TurnoutSection turnout = new TurnoutSection("Sec 1 Layout TO", "Sec1 TO 1 N", "Sec1 TO 1 R", "Sec1 TO 1 N", "Sec1 TO 1 R", station);
        station.add(turnout);

        SignalHeadSection signals = new SignalHeadSection(
                        Arrays.asList(new String[]{"2R Upper","2R Lower"}), Arrays.asList(new String[]{"2L Main", "2L Siding"}),
                        "Sec1 Sign 2 Left", "Sec1 Sign 2 Center", "Sec1 Sign 2 Right", 
                        "Sec1 Sig 2 Left", "Sec1 Sig 2 Right",
                        station);
        station.add(signals);
        
        Lock occupancyLock = new OccupancyLock("Sec1 Track OS");
        Lock routeLock = new RouteLock(new String[]{"2R Upper","2R Lower", "2L Main", "2L Siding"});
        turnout.addLocks(Arrays.asList(new Lock[]{occupancyLock, routeLock}));
        
        station.add(new TrackCircuitSection("Sec1 Track Between", "Sec1 Track Outside", station)); 
        station.add(new TrackCircuitSection("Sec1 Track OS", "Sec1 Track OS", station, bell)); 
        station.add(new TrackCircuitSection("Sec1 Track Main", "Sec1 Track Main", station)); 
        station.add(new TrackCircuitSection("Sec1 Track Siding", "Sec1 Track Siding", station)); 

        station.add(new MaintainerCallSection("Sec1 MC", "Sec 1 MC", station));

        // slow down delayed turnouts
        jmri.implementation.AbstractTurnout.DELAYED_FEEDBACK_INTERVAL = 10000;
        
        // show times
        log.debug("Code line start pulse: {}", CodeLine.START_PULSE_LENGTH);
        log.debug("Code line send duration: {}", CodeLine.CODE_SEND_DELAY);
        log.debug("Turnout motion delay: {}", jmri.implementation.AbstractTurnout.DELAYED_FEEDBACK_INTERVAL);
        log.debug("Signal setting pause: {}", SignalHeadSection.MOVEMENT_DELAY);
        
        // user interacts here
        
        // wait for Swing to end
        Thread.getAllStackTraces().keySet().forEach((t) -> 
            { 
                if (t.getName().startsWith("AWT-EventQueue")) {  
                    try {
                        t.join();  // Wait for AWT to end on last window deleted
                    } catch (Exception e) {}
                }
            });
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TurnoutSection.class);
}

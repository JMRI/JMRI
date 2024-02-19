package jmri.jmrit.etcs.dmi.swing;

import jmri.jmrit.etcs.CabMessage;
import jmri.jmrit.etcs.TrackCondition;
import jmri.jmrit.etcs.TrackSection;
import jmri.jmrit.etcs.StationTrackCondition;
import jmri.jmrit.etcs.MovementAuthority;

import java.util.ArrayList;

import org.apiguardian.api.API;

/**
 * Class to demonstrate features of the ERTMS ETCS DMI.
 * @author Steve Young Copyright (C) 2024
 */
@API(status=API.Status.EXPERIMENTAL)
public class DmiDemo {

    final DmiPanel dmiPanel;

    /**
     * Create a new Demo for a given DmiPanel.
     * @param mainPanel the mainPanel to run the demo on.
     */
    public DmiDemo(DmiPanel mainPanel) {
        dmiPanel = mainPanel;
    }

    /**
     * Run the Demonstration.
     */
    public void runDemo() {
        jmri.util.ThreadingUtil.newThread(this::doDemo, "DMI Demo").start();
    }

    private void doDemo() {
        dmiPanel.messageDriver(new CabMessage("Starting DMI GUI Demo", 1, false));
        waitFor(1000);
        pt1();
    }

    private void pt1(){

        CabMessage ackMsg = new CabMessage("Message which needs Acknowledge", 1, true);
        dmiPanel.messageDriver(ackMsg);
        waitFor(3000);

        dmiPanel.removeMessage(ackMsg.getMessageId());
        dmiPanel.messageDriver(new CabMessage("Ack Message removed, this is a normal CabMessage.", 1, false));
        waitFor(1000);


        dmiPanel.messageDriver(new CabMessage("Ramp speed, setSpeedHookSpeed 0 to 140", 1, false));
        for (int i = 0; i < 141; i++) {
            waitFor(50);
            dmiPanel.setActualSpeed(i);
            setSpeedHookSpeed(i, dmiPanel);
        }

        dmiPanel.messageDriver(new CabMessage("Ramp speed, "
            +"setSpeedHookSpeed 140 to 0", 1, false));
        for (int i = 140; i > -1; i-- ) {
            waitFor(50);
            dmiPanel.setActualSpeed(i);
            setSpeedHookSpeed(i, dmiPanel);
        }

        dmiPanel.messageDriver(new CabMessage("Max Speed to 180", 1, false));
        dmiPanel.setMaxDialSpeed(180);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("RampActualSpeed setSpeedHookSpeed 0 - 20", 1, false));
        for (int i = 0; i < 21; i++ ) {
            waitFor(150);
            dmiPanel.setActualSpeed(i);
            setSpeedHookSpeed(i, dmiPanel);
        }

        dmiPanel.messageDriver(new CabMessage("RampActualSpeed setSpeedHookSpeed 20 - 0", 1, false));
        for (int i = 20; i > -1; i-- ) {
            waitFor(150);
            dmiPanel.setActualSpeed(i);
            setSpeedHookSpeed(i, dmiPanel);
        }

        dmiPanel.messageDriver(new CabMessage("Ramp speed and hook to 20", 1, false));
        for (int i = 0; i < 21; i++ ) {
            waitFor(150);
            dmiPanel.setActualSpeed(i);
            setSpeedHookSpeed(i, dmiPanel);
        }

        dmiPanel.messageDriver(new CabMessage("Max Speed to 250", 1, false));
        dmiPanel.setMaxDialSpeed(250);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Max Speed to 400", 1, false));
        dmiPanel.setMaxDialSpeed(400);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Ramp speed and Target Advice Speed 0-400", 1, false));
        for (int i = 0; i < 401; i++ ) {
            waitFor(20);
            dmiPanel.setActualSpeed(i);
            dmiPanel.setTargetAdviceSpeed(i);
        }

        dmiPanel.messageDriver(new CabMessage("speed 70 , hook 80", 1, false));
        dmiPanel.setActualSpeed(70);
        setSpeedHookSpeed(80, dmiPanel);

        dmiPanel.setTargetAdviceSpeed(-1);

        dmiPanel.messageDriver(new CabMessage("Limited Supervision Speed 10", 1, false));
        dmiPanel.setLimitedSupervisionSpeed(10);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Limited Supervision Speed 0-400", 1, false));
        for (int i = 0; i < 401; i++ ) {
            waitFor(30);
            dmiPanel.setLimitedSupervisionSpeed(i);
        }

        waitFor(1000);
        dmiPanel.messageDriver(new CabMessage("Hide Limited Supervision Speed", 1, false));
        dmiPanel.setLimitedSupervisionSpeed(-1);

        dmiPanel.messageDriver(new CabMessage("Distance to Target 1500m to 0m", 1, false));
        for (int i = 1500; i > -100; i-- ) {
            dmiPanel.setDistanceToTarget(i);
            waitFor(5);
        }

        dmiPanel.setActualSpeed(96);
        setSpeedHookSpeed(100, dmiPanel);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Speed Unit to MPH", 1, false));
        dmiPanel.setDisplaySpeedUnit("MPH");
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Speed Unit Empty", 1, false));
        dmiPanel.setDisplaySpeedUnit("");
        dmiPanel.setCoasting(false);
        waitFor(1000);
        
        
        pt2();
    }


    private void pt2(){

        // dmiPanel.messageDriver(new CabMessage("Setting Level to NTC", 1, false));
        // dmiPanel.setLevel(-1);

        waitFor(1000);



        dmiPanel.messageDriver(new CabMessage("Track Ahead Free Question Visible", 1, false));
        dmiPanel.setTrackAheadFreeQuestionVisible(true);
        waitFor(2000);


        dmiPanel.messageDriver(new CabMessage("Track Ahead Free Question Hidden", 1, false));
        dmiPanel.setTrackAheadFreeQuestionVisible(false);
        waitFor(1000);


        dmiPanel.messageDriver(new CabMessage("Tunnel Stopping Icon Visible Ack", 1, false));
        dmiPanel.setTunnelStoppingIconVisible(true, true);
        waitFor(2000);


        dmiPanel.messageDriver(new CabMessage("Tunnel Stopping Icon No Ack", 1, false));
        dmiPanel.setTunnelStoppingIconVisible(true, false);
        waitFor(1000);


        dmiPanel.messageDriver(new CabMessage("Tunnel Stopping Distance 400 to 0", 1, false));
        for (int i = 401; i > -2; i--) {
            waitFor(50);
            dmiPanel.setTunnelStoppingDistance(i);
        }

        dmiPanel.messageDriver(new CabMessage("Tunnel Stopping Icon hidden", 1, false));
        dmiPanel.setTunnelStoppingIconVisible(false, false);
        waitFor(1000);


        dmiPanel.messageDriver(new CabMessage("Notification Level transition to NTC", 1, false));
        dmiPanel.setLevelTransition(-1, true);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Notification Level transition to 0", 1, false));
        dmiPanel.setLevelTransition(0, false);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Level to 0", 1, false));
        dmiPanel.setLevel(0);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Notification Level transition to 1", 1, false));
        dmiPanel.setLevelTransition(1, false);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Set Level 1", 1, false));
        dmiPanel.setLevel(1);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Set Level 2", 1, false));
        dmiPanel.setLevel(2);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Hide Notification Level transition", 1, false));
        dmiPanel.setLevelTransition(-2, false);
        waitFor(1000);

        // not valid for ERTMS4
        // dmiPanel.messageDriver(new CabMessage("Notification Level transition to 2", 1, false));
        // dmiPanel.setLevelTransition(2, false);
        // waitFor(1000);

        // ERTMS < 4
        // dmiPanel.messageDriver(new CabMessage("Notification Level transition to 3", 1, false));
        // dmiPanel.setLevelTransition(3, false);
        // waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Safe Radio Connection Lost", 1, false));
        dmiPanel.setSafeRadioConnection(0);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Safe Radio Connection Hidden", 1, false));
        dmiPanel.setSafeRadioConnection(-1);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Safe Radio Connection", 1, false));
        dmiPanel.setSafeRadioConnection(1);
        waitFor(1000);

        
        dmiPanel.messageDriver(new CabMessage("Adhesion Factor On", 1, false));
        dmiPanel.setAdhesionFactorOn(true);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Adhesion Factor Off", 1, false));
        dmiPanel.setAdhesionFactorOn(false);
        waitFor(1000);
        
        dmiPanel.messageDriver(new CabMessage("Display Intervetion Symbol", 1, false));
        dmiPanel.setIntervetionSymbol(true);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Hide Intervetion Symbol", 1, false));
        dmiPanel.setIntervetionSymbol(false);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Reversing Permitted", 1, false));
        dmiPanel.setReversingPermittedSymbol(true);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Reversing Permitted hidden", 1, false));
        dmiPanel.setReversingPermittedSymbol(false);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Shunting Mode", 1, false));
        dmiPanel.setMode(1); // shunting
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Mode set to Trip", 1, false));
        dmiPanel.setMode(4); // trip
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Mode set to Post Trip", 1, false));
        dmiPanel.setMode(6);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Mode set to On Sight", 1, false));
        dmiPanel.setMode(7);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Mode set to Staff Responsible", 1, false));
        dmiPanel.setMode(9);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Mode set to Full Supervision", 1, false));
        dmiPanel.setMode(11);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Mode set to Non-Leading", 1, false));
        dmiPanel.setMode(12);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Mode to Reversing", 1, false));
        dmiPanel.setMode(14);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Mode to Unfitted", 1, false));
        dmiPanel.setMode(16);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Mode to System Failure", 1, false));
        dmiPanel.setMode(18);
        waitFor(1000);

        // ERTMS < 4
        // dmiPanel.messageDriver(new CabMessage("Set Level 3", 1, false));
        // dmiPanel.setLevel(3);
        // waitFor(1000);


        dmiPanel.messageDriver(new CabMessage("Release speed to 50", 1, false));
        dmiPanel.setReleaseSpeed(50);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Mode to Limited Supervision", 1, false));
        dmiPanel.setMode(21);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Hide Release speed", 1, false));
        dmiPanel.setReleaseSpeed(-1);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Set Mode to Automatic Train Operation", 1, false));
        dmiPanel.setMode(23); // ato
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Display Coasting", 1, false));
        dmiPanel.setCoasting(true);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Speed Unit while coasting", 1, false));
        dmiPanel.setCoasting(true);
        dmiPanel.setDisplaySpeedUnit("mph");
        waitFor(1000);
        
        dmiPanel.messageDriver(new CabMessage("Hide Coasting", 1, false));
        dmiPanel.setCoasting(false);
        dmiPanel.setDisplaySpeedUnit("");
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Set Mode to Supervised Manoeuvre", 1, false));
        dmiPanel.setMode(24); // supervised manoeuvre
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Set Supervised Direction Forwards", 1, false));
        dmiPanel.setSupervisedDirection(1);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Set Supervised Direction Reverse", 1, false));
        dmiPanel.setSupervisedDirection(-1);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Hide Supervised Direction", 1, false));
        dmiPanel.setSupervisedDirection(0);
        waitFor(1000);

        checkModeAcknowledge(DmiPanel.PROP_CHANGE_MODE_SHUNTING_ACK, DmiPanel.MODE_SHUNTING, dmiPanel);
        checkModeAcknowledge(DmiPanel.PROP_CHANGE_MODE_TRIP_ACK, DmiPanel.MODE_TRIP, dmiPanel);
        checkModeAcknowledge(DmiPanel.PROP_CHANGE_MODE_ON_SIGHT_ACK, DmiPanel.MODE_ON_SIGHT, dmiPanel);
        checkModeAcknowledge(DmiPanel.PROP_CHANGE_MODE_STAFF_RESPONSIBLE_ACK, DmiPanel.MODE_STAFF_RESPONSIBLE, dmiPanel);
        checkModeAcknowledge(DmiPanel.PROP_CHANGE_MODE_REVERSING_ACK, DmiPanel.MODE_REVERSING, dmiPanel);
        checkModeAcknowledge(DmiPanel.PROP_CHANGE_MODE_UNFITTED_ACK, DmiPanel.MODE_UNFITTED, dmiPanel);
        checkModeAcknowledge(DmiPanel.PROP_CHANGE_MODE_NATIONAL_SYSTEM_ACK, DmiPanel.MODE_NATIONAL_SYSTEM, dmiPanel);
        checkModeAcknowledge(DmiPanel.PROP_CHANGE_MODE_LIMITED_SUPERVISION_ACK, DmiPanel.MODE_LIMITED_SUPERVISION, dmiPanel);

        pt3();

    }

    private void checkModeAcknowledge(String str, int mode, DmiPanel main){
        main.setModeAcknowledge(mode);
        main.messageDriver(new CabMessage(addSpaceBeforeCapital(str), 1, false));
        waitFor(1000);
        main.setModeAcknowledge(DmiPanel.MODE_NONE);
        waitFor(500);
    }

    private void pt3() {

        dmiPanel.setLevel(2);
        dmiPanel.setMode(DmiPanel.MODE_AUTOMATIC_DRIVING);
        dmiPanel.setSafeRadioConnection(1);

        waitFor(1000);

        dmiPanel.setAtoMode(1);
        dmiPanel.messageDriver(new CabMessage("ATO Selected", 1, false));

        waitFor(1000);

        dmiPanel.setAtoMode(2);
        dmiPanel.messageDriver(new CabMessage("ATO ready for engagement", 1, false));

        waitFor(1000);

        dmiPanel.setAtoMode(3);
        dmiPanel.messageDriver(new CabMessage("ATO Engaged", 1, false));

        waitFor(1000);

        dmiPanel.setAtoMode(4);
        dmiPanel.messageDriver(new CabMessage("ATO Disengaging", 1, false));

        waitFor(1000);

        dmiPanel.setAtoMode(5);
        dmiPanel.messageDriver(new CabMessage("ATO Failure", 1, false));
        waitFor(1000);

        dmiPanel.setAtoMode(0);
        dmiPanel.messageDriver(new CabMessage("ATO Off", 1, false));
        waitFor(1000);



        dmiPanel.setAtoMode(3);


        dmiPanel.messageDriver(new CabMessage("Undershot stopping window", 1, false));
        dmiPanel.setStoppingAccuracy(-1);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Accurate stopping window", 1, false));
        dmiPanel.setStoppingAccuracy(0);
        waitFor(1000);


        dmiPanel.messageDriver(new CabMessage("Overshotshot stopping window", 1, false));
        dmiPanel.setStoppingAccuracy(1);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Hide stopping window", 1, false));
        dmiPanel.setStoppingAccuracy(-2);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Set Stopping Point", 1, false));
        dmiPanel.setStoppingPointLabel("Welwyn North", "17:36:48");
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Skip Stopping Point Active", 1, false));
        dmiPanel.setSkipStoppingPoint(17);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Skip Stopping Point requested by ATO-TS", 1, false));
        dmiPanel.setSkipStoppingPoint(18);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Skip Stopping Point requested by driver", 1, false));
        dmiPanel.setSkipStoppingPoint(19);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Hide Skip Stopping Point", 1, false));
        dmiPanel.setSkipStoppingPoint(0);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Set Stopping Point Hidden", 1, false));
        dmiPanel.setStoppingPointLabel("", "");
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Request driver to open both sides doors", 1, false));
        dmiPanel.setDoorIcon(10);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Request driver to open left doors", 1, false));
        dmiPanel.setDoorIcon(11);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Request driver to open right doors", 1, false));
        dmiPanel.setDoorIcon(12);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Doors are open", 1, false));
        dmiPanel.setDoorIcon(13);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Request driver to close doors", 1, false));
        dmiPanel.setDoorIcon(14);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Doors are being closed by ATO", 1, false));
        dmiPanel.setDoorIcon(15);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Doors are closed", 1, false));
        dmiPanel.setDoorIcon(16);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Dwell Time set to 1min 9secs", 1, false));
        dmiPanel.setDwellTime(1, 9);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Dwell Time set to 1min 8secs", 1, false));
        dmiPanel.setDwellTime(1, 8);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Hide Dwell Time", 1, false));
        dmiPanel.setDwellTime(-1, -1);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Doors are closed", 1, false));
        dmiPanel.setDoorIcon(16);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Doors icon hidden", 1, false));
        dmiPanel.setDoorIcon(0);
        waitFor(1000);

        pt4();
    }

    private void pt4() {

        dmiPanel.messageDriver(new CabMessage("Add Movement Authority", 1, false));
        ArrayList<TrackSection> trackSectionList = new ArrayList<>();
        TrackSection s1 = new TrackSection(75,100,-4);
        TrackSection s2 = new TrackSection(50,140,-2);
        TrackSection s3 = new TrackSection(125,100,1);
        TrackSection s4 = new TrackSection(250,80,2);
        TrackSection s5 = new TrackSection(250,40,4);
        TrackSection s6 = new TrackSection(250,100,8);
        trackSectionList.add(s1);
        trackSectionList.add(s2);
        trackSectionList.add(s3);
        trackSectionList.add(s4);
        trackSectionList.add(s5);
        trackSectionList.add(s6);
        dmiPanel.extendMovementAuthorities(new MovementAuthority(trackSectionList)); // 1000m
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Advance Movement Authority", 1, false));
        for (int i = 0; i < 1100; i++ ) {
            dmiPanel.advance(1);
            waitFor(20);
        }

        s1 = new TrackSection(7700,100,1);

        int pos = 0;


        s1.addAnnouncement(TrackCondition.levelCrossing(pos+=200));
        s1.addAnnouncement(TrackCondition.radioHole(pos+=200));
        s1.addAnnouncement(TrackCondition.soundHorn(pos+=200));

        s1.addAnnouncement(TrackCondition.pantographLower(pos+=200, true));
        s1.addAnnouncement(TrackCondition.pantographLower(pos+=200, false));
        s1.addAnnouncement(TrackCondition.pantographRaise(pos+=200, true));
        s1.addAnnouncement(TrackCondition.pantographRaise(pos+=200, false));


        s1.addAnnouncement(TrackCondition.airConClose(pos+=200, true));
        s1.addAnnouncement(TrackCondition.airConClose(pos+=200, false));
        s1.addAnnouncement(TrackCondition.airConOpen(pos+=200, true));
        s1.addAnnouncement(TrackCondition.airConOpen(pos+=200, false));

        s1.addAnnouncement(TrackCondition.neutralSection(pos+=200, true));
        s1.addAnnouncement(TrackCondition.neutralSection(pos+=200, false));
        s1.addAnnouncement(TrackCondition.neutralSectionEnd(pos+=200, true));
        s1.addAnnouncement(TrackCondition.neutralSectionEnd(pos+=200, false));

        s1.addAnnouncement(TrackCondition.nonStoppingArea(pos+=200, true));

        s1.addAnnouncement(TrackCondition.inhibitMagShoeBrake(pos+=200, true));
        s1.addAnnouncement(TrackCondition.inhibitMagShoeBrake(pos+=200, false));
        s1.addAnnouncement(TrackCondition.inhibitEddyCurrentBrake(pos+=200, true));
        s1.addAnnouncement(TrackCondition.inhibitEddyCurrentBrake(pos+=200, false));
        s1.addAnnouncement(TrackCondition.inhibitRegenerativeBrake(pos+=200, true));
        s1.addAnnouncement(TrackCondition.inhibitRegenerativeBrake(pos+=200, false));

        s1.addAnnouncement(TrackCondition.tractionChange0(pos+=200, true));
        s1.addAnnouncement(TrackCondition.tractionChange0(pos+=200, false));
        s1.addAnnouncement(TrackCondition.tractionChange25000(pos+=200, true));
        s1.addAnnouncement(TrackCondition.tractionChange25000(pos+=200, false));
        s1.addAnnouncement(TrackCondition.tractionChange15000(pos+=200, true));
        s1.addAnnouncement(TrackCondition.tractionChange15000(pos+=200, false));
        s1.addAnnouncement(TrackCondition.tractionChange3000(pos+=200, true));
        s1.addAnnouncement(TrackCondition.tractionChange3000(pos+=200, false));
        s1.addAnnouncement(TrackCondition.tractionChange1500(pos+=200, true));
        s1.addAnnouncement(TrackCondition.tractionChange1500(pos+=200, false));
        s1.addAnnouncement(TrackCondition.tractionChange750(pos+=200, true));
        s1.addAnnouncement(TrackCondition.tractionChange750(pos+=200, false));
        s1.addAnnouncement(new StationTrackCondition(pos+=200,"Welwyn North"));

        trackSectionList = new ArrayList<>();
        trackSectionList.add(s1);
        dmiPanel.extendMovementAuthorities(new MovementAuthority(trackSectionList));

        waitFor(1000);

        dmiPanel.setScale(5);
        dmiPanel.messageDriver(new CabMessage("Set Planning Scale 0-3200", 1, false));
        waitFor(1000);

        dmiPanel.setScale(4);
        dmiPanel.messageDriver(new CabMessage("Set Planning Scale 0-1600", 1, false));
        waitFor(1000);

        dmiPanel.setScale(3);
        dmiPanel.messageDriver(new CabMessage("Set Planning Scale 0-8000", 1, false));
        waitFor(1000);

        dmiPanel.setScale(2);
        dmiPanel.messageDriver(new CabMessage("Set Planning Scale 0-4000", 1, false));
        waitFor(1000);

        dmiPanel.setScale(1);
        dmiPanel.messageDriver(new CabMessage("Set Planning Scale 0-2000", 1, false));
        waitFor(1000);

        dmiPanel.setScale(0);
        dmiPanel.messageDriver(new CabMessage("Set Planning Scale 0-1000", 1, false));
        waitFor(1000);


        String tcString = "";
        for (int i = 0; i < 7300; i++ ) {
            dmiPanel.advance(1);
            TrackCondition nextTc = dmiPanel.getNextAnnouncement(false);
            if ( nextTc != null ) {
                String newString = nextTc.getDescription();
                if (!( tcString.equals(newString) )) {
                    tcString = newString;
                    dmiPanel.messageDriver(new CabMessage(tcString, 1, false));
                }
            }
            waitFor(10);

        }

        dmiPanel.setMode(DmiPanel.MODE_SUPERVISED_MANOEUVRE);
        dmiPanel.messageDriver(new CabMessage("Indication Marker", 1, false));
        dmiPanel.setIndicationMarker(100,0);
        waitFor(1000);

        dmiPanel.messageDriver(new CabMessage("Indication Marker in ATO", 1, false));
        dmiPanel.setMode(DmiPanel.MODE_AUTOMATIC_DRIVING);
        waitFor(1000);

        pt5();
    }

    private void pt5(){

        dmiPanel.messageDriver(new CabMessage("Play Sound Too Fast", 1, false));
        dmiPanel.playDmiSound(1);
        waitFor(3000);

        dmiPanel.messageDriver(new CabMessage("Play Sound Info", 1, false));
        dmiPanel.playDmiSound(3);
        waitFor(3000);

        dmiPanel.messageDriver(new CabMessage("Play Sound Click", 1, false));
        dmiPanel.playDmiSound(4);
        waitFor(3000);

        dmiPanel.messageDriver(new CabMessage("Play Sound Warning", 1, false));
        dmiPanel.playDmiSound(2);
        waitFor(3000);

        dmiPanel.messageDriver(new CabMessage("Stop Sound Warning", 1, false));
        dmiPanel.stopDmiSound(2);

        waitFor(1000);
        dmiPanel.messageDriver(new CabMessage("Demo Complete", 1, false));
    }

    private static void setSpeedHookSpeed(int speedHookSpeed, DmiPanel p){

        java.util.ArrayList<DmiCircularSpeedGuideSection> csgSectionList = new java.util.ArrayList<>();

        csgSectionList.add(new DmiCircularSpeedGuideSection(DmiCircularSpeedGuideSection.CSG_TYPE_NORMAL,
            DmiPanel.DARK_GREY, -2, 1, false ));


        p.setCentreCircleAndDialColor(DmiPanel.YELLOW);

        if ( speedHookSpeed > 110 ) {

            csgSectionList.add(new DmiCircularSpeedGuideSection(DmiCircularSpeedGuideSection.CSG_TYPE_HOOK,
            DmiPanel.ORANGE, 110, speedHookSpeed, true ));

            p.setCentreCircleAndDialColor(DmiPanel.ORANGE);
        }

        if ( speedHookSpeed > 200 ) {

            csgSectionList.add(new DmiCircularSpeedGuideSection(DmiCircularSpeedGuideSection.CSG_TYPE_HOOK,
            DmiPanel.RED, 200, speedHookSpeed, true ));
            p.setCentreCircleAndDialColor(DmiPanel.RED);

        }

        if ( speedHookSpeed <= 50 ){

            csgSectionList.add(new DmiCircularSpeedGuideSection(DmiCircularSpeedGuideSection.CSG_TYPE_NORMAL,
            DmiPanel.GREY, 0, Math.min(speedHookSpeed, 110 ), false));


        csgSectionList.add(new DmiCircularSpeedGuideSection(DmiCircularSpeedGuideSection.CSG_TYPE_SUPERVISION,
            DmiPanel.YELLOW, 0, 50, false));
        } else {
            csgSectionList.add(new DmiCircularSpeedGuideSection(DmiCircularSpeedGuideSection.CSG_TYPE_NORMAL,
            DmiPanel.YELLOW, 0, Math.min(speedHookSpeed, 110 ), true));
            csgSectionList.add(new DmiCircularSpeedGuideSection(DmiCircularSpeedGuideSection.CSG_TYPE_RELEASE,
            DmiPanel.YELLOW, 0, 50, false));
        }


        p.setCsgSections(csgSectionList);

    }

    private static String addSpaceBeforeCapital(String input) {
        if (input == null || input.isEmpty()) {
            return input; // Return the input as is for empty or null strings
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);

            // Check if the current character is an uppercase letter
            if (Character.isUpperCase(currentChar)) {
                // Add a space before the uppercase letter
                result.append(' ');
            }

            // Append the current character to the result
            result.append(currentChar);
        }

        return result.toString();
    }

    private static final int DELAY_STEP = 11;
    private static int delayMultiplier = 1;

    protected static void setDelayMultiplier(int newVal){
        delayMultiplier = newVal;
    }

    /**
     * Wait for a specific amount of time
     * <p>
     * It's better to wait for a condition, but if you can't find a condition,
     * this will have to do.
     * <p>
     *
     * @param msec Delay in milliseconds
     */
    private static void waitFor(final int msec) {
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            log.error("Cannot use waitFor on Swing thread", new Exception());
            return;
        }
        int delay = 0;
        while (delay < msec * delayMultiplier) {
            int priority = Thread.currentThread().getPriority();
            try {
                Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                Thread.sleep(DELAY_STEP);
                delay += DELAY_STEP;
            } catch (InterruptedException e) {
                log.error("Interrupted");
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            } finally {
                Thread.currentThread().setPriority(priority);
            }
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DmiDemo.class);

}

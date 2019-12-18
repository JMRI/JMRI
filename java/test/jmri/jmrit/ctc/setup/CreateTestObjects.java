package jmri.jmrit.ctc.setup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.jmrit.blockboss.BlockBossLogic;
import jmri.jmrit.ctc.CTCFiles;

public class CreateTestObjects {

    public static void createTestObjects() {
        // Panel objects
        createSensor("IS101", "B-Left");
        createSensor("IS102", "O-Left");
        createSensor("IS103", "B-Main");
        createSensor("IS104", "B-Side");
        createSensor("IS105", "O-Right");
        createSensor("IS106", "B-Right");
        createSensor("IS107", "O-Stub");
        createSensor("IS108", "B-StubM");
        createSensor("IS109", "B-StubS");

        createTurnout("IT101", "T-Left");
        createTurnout("IT102", "T-Right");
        createTurnout("IT103", "T-Stub");
        createTurnout("IT104", "T-Spur");

        createSignalHead("IH01", "Left-U");
        createSignalHead("IH02", "Left-L");
        createSignalHead("IH03", "Left-M");
        createSignalHead("IH04", "Left-S");

        createSignalHead("IH05", "Right-U");
        createSignalHead("IH06", "Right-L");
        createSignalHead("IH07", "Right-M");
        createSignalHead("IH08", "Right-S");

        createSignalHead("IH09", "Stub-U");
        createSignalHead("IH10", "Stub-L");
        createSignalHead("IH11", "Stub-M");
        createSignalHead("IH12", "Stub-S");

        // Create SSL
        //        signal   mode   to      w1      wa      sp2       sen1    sen2
        createSSL("Left-U", 2, "IT101", "IH07", null, false, "IS102", "IS103");
        createSSL("Left-L", 3, "IT101", "IH08", null, true, "IS102", "IS104");
        createSSL("Left-M", 2, "IT101", null, null, false, "IS102", "IS101");
        createSSL("Left-S", 3, "IT101", null, null, true, "IS102", "IS101");

        createSSL("Right-U", 2, "IT102", "IH03", null, false, "IS105", "IS103");
        createSSL("Right-L", 3, "IT102", "IH04", null, true, "IS105", "IS104");
        createSSL("Right-M", 2, "IT102", "IH09", "IH10", false, "IS105", "IS106");
        createSSL("Right-S", 3, "IT102", "IH09", "IH10", true, "IS105", "IS106");

        createSSL("Stub-U", 2, "IT103", null, null, false, "IS107", "IS108");
        createSSL("Stub-L", 3, "IT103", null, null, true, "IS107", "IS109");
        createSSL("Stub-M", 2, "IT103", "IH05", "IH06", false, "IS107", "IS106");
        createSSL("Stub-S", 3, "IT103", "IH05", "IH06", true, "IS107", "IS106");

        // CTC objects
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS:RELOADCTC");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS:DEBUGCTC");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS:FLEETING");

        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS1:LEVER");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS1:SN");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS1:SR");

        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS2:CB");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS2:LK");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS2:NK");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS2:RK");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS2:LL");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS2:NL");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS2:RL");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS2:CALLON");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS2:LOCKTOGGLE");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS2:UNLOCKEDIND");

        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS3:LEVER");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS3:SN");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS3:SR");

        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS4:CB");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS4:LK");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS4:NK");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS4:RK");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS4:LL");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS4:NL");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS4:RL");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS4:CALLON");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS4:LOCKTOGGLE");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS4:UNLOCKEDIND");

        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS5:LEVER");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS5:SN");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS5:SR");

        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS6:CB");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS6:LK");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS6:NK");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS6:RK");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS6:LL");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS6:NL");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS6:RL");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS6:CALLON");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS6:LOCKTOGGLE");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS6:UNLOCKEDIND");

        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS7:LEVER");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS7:SN");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS7:SR");

        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS8:CB");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS8:LK");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS8:NK");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS8:RK");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS8:LL");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS8:NL");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS8:RL");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS8:CALLON");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS8:LOCKTOGGLE");
        InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS8:UNLOCKEDIND");
    }

    public static void createSensor(String sname, String uname) {
        Sensor sensor = InstanceManager.getDefault(jmri.SensorManager.class).provideSensor(sname);
        sensor.setUserName(uname);
        try {
            sensor.setKnownState(Sensor.INACTIVE);
        } catch (jmri.JmriException ex) {
            log.warn("sensor known exception");
        }
    }

    public static void createTurnout(String sname, String uname) {
        Turnout turnout = InstanceManager.getDefault(jmri.TurnoutManager.class).provideTurnout(sname);
        turnout.setUserName(uname);
        turnout.setCommandedState(Turnout.CLOSED);
    }

    public static void createSignalHead(String sname, String uname) {
        SignalHead signalhead = new jmri.implementation.VirtualSignalHead(sname, uname);
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(signalhead);
        signalhead.setAppearance(SignalHead.RED);
    }

    public static void createSSL(String signal, int mode, String watchedturnout, String watchedsignal1, String watchedsignal1alt,
            boolean limitspeed2, String sensor1, String sensor2) {
        BlockBossLogic bb = BlockBossLogic.getStoppedObject(signal);
        bb.setMode(mode);
        bb.setTurnout(watchedturnout);
        if (watchedsignal1 != null) {
            bb.setWatchedSignal1(watchedsignal1, false);
        }
        if (watchedsignal1alt != null) {
            bb.setWatchedSignal1Alt(watchedsignal1alt);
        }
        bb.setLimitSpeed2(limitspeed2);
        bb.setSensor1(sensor1);
        bb.setSensor2(sensor2);
        bb.retain();
        bb.start();
    }

    public static void createTestFiles() {
        // Copy ProgramProperties
        final File source = new File("java/test/jmri/jmrit/ctc/setup/");
        final String props = "ProgramProperties.xml";
        final String system = "CTCSystem.xml";

        File propsFile = new File(source, props);
        File systemFile = new File(source, system);
        try {
            log.debug("Copying from {} to {}", propsFile, CTCFiles.getFile(props));
            Files.copy(propsFile.toPath(), CTCFiles.getFile(props).toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            log.error("Copy CTC Properties demo file failed", ex);  // NOI18N
        }
        try {
            log.debug("Copying from {} to {}", systemFile, CTCFiles.getFile(system));
            Files.copy(systemFile.toPath(), CTCFiles.getFile(system).toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            log.error("Copy CTC System demo file failed", ex);  // NOI18N
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateTestObjects.class);
}

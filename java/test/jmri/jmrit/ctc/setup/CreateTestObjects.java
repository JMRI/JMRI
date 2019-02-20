package jmri.jmrit.ctc.setup;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

        createTurnout("IT101", "T-Left");
        createTurnout("IT102", "T-Right");

        createSignalHead("IH01", "Left-U");
        createSignalHead("IH02", "Left-L");
        createSignalHead("IH03", "Left-M");
        createSignalHead("IH04", "Left-S");

        createSignalHead("IH05", "Right-U");
        createSignalHead("IH06", "Right-L");
        createSignalHead("IH07", "Right-M");
        createSignalHead("IH08", "Right-S");

        // Create SSL
        createSSL("Left-M",  2, "IT101", null,   false, "IS101");
        createSSL("Left-S",  3, "IT101", null,   true,  "IS101");
        createSSL("Right-U", 2, "IT102", "IH03", false, "IS103");
        createSSL("Right-L", 3, "IT102", "IH04", true,  "IS104");
        createSSL("Right-M", 2, "IT102", null,   false, "IS106");
        createSSL("Right-S", 3, "IT102", null,   true,  "IS106");
        createSSL("Left-U",  2, "IT101", "IH07", false, "IS103");
        createSSL("Left-L",  3, "IT101", "IH08", true,  "IS104");

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

    public static void createSSL(String signal, int mode, String watchedturnout, String watchedsignal1, boolean limitspeed2, String sensor) {
        BlockBossLogic bb = BlockBossLogic.getStoppedObject(signal);
        bb.setMode(mode);
        bb.setTurnout(watchedturnout);
        if (watchedsignal1 != null) {
            bb.setWatchedSignal1(watchedsignal1, false);
        }
        bb.setLimitSpeed2(limitspeed2);
        bb.setSensor1(sensor);

//     signal="Left-M"  mode="2" watchedturnout="IT101"                       limitspeed1="false" limitspeed2="false" useflashyellow="false" distantsignal="false" sensorname=IS101
//     signal="Left-S"  mode="3" watchedturnout="IT101"                       limitspeed1="false" limitspeed2="true"  useflashyellow="false" distantsignal="false" sensorname=IS101
//     signal="Right-U" mode="2" watchedturnout="IT102" watchedsignal1="IH03" limitspeed1="false" limitspeed2="false" useflashyellow="false" distantsignal="false" sensorname=IS103
//     signal="Right-L" mode="3" watchedturnout="IT102" watchedsignal1="IH04" limitspeed1="false" limitspeed2="true"  useflashyellow="false" distantsignal="false" sensorname=IS104
//     signal="Right-M" mode="2" watchedturnout="IT102"                       limitspeed1="false" limitspeed2="false" useflashyellow="false" distantsignal="false" sensorname=IS106
//     signal="Right-S" mode="3" watchedturnout="IT102"                       limitspeed1="false" limitspeed2="true"  useflashyellow="false" distantsignal="false" sensorname=IS106
//     signal="Left-U"  mode="2" watchedturnout="IT101" watchedsignal1="IH07" limitspeed1="false" limitspeed2="false" useflashyellow="false" distantsignal="false" sensorname=IS103
//     signal="Left-L"  mode="3" watchedturnout="IT101" watchedsignal1="IH08" limitspeed1="false" limitspeed2="true"  useflashyellow="false" distantsignal="false" sensorname=IS104

    }
    public static void createTestFiles() {
        // Copy ProgramProperties
        final String SOURCE_PATH = "java/test/jmri/jmrit/ctc/setup/";
        final String PROP_FILE = "ProgramProperties.xml";
        final String SYS_FILE = "CTCSystem.xml";

        File fromFile;
        File toFile;
        Path fromPath;
        Path toPath;

        toFile = CTCFiles.getFile(PROP_FILE);
        if (!toFile.exists()) {
            fromFile = new File(SOURCE_PATH + PROP_FILE);
            toPath = toFile.toPath();
            fromPath = fromFile.toPath();
            try {
                Files.copy(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                log.error("Copy CTC Poperties demo file failed", ex);  // NOI18N
            }
        }

        // Copy CTCSystem.xml
        toFile = CTCFiles.getFile(SYS_FILE);
        if (!toFile.exists()) {
            fromFile = new File(SOURCE_PATH + SYS_FILE);
            toPath = toFile.toPath();
            fromPath = fromFile.toPath();
            try {
                Files.copy(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                log.error("Copy CTC Poperties demo file failed", ex);  // NOI18N
            }
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateTestObjects.class);
}

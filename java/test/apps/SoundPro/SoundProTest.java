package apps.SoundPro;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

/**
 * This is more of an acceptance test than a unit test, loading a series
 * of connection user profiles in SoundPro.
 * <p>
 * It confirms that the entire application can start up and configure itself.
 * <p>
 * When format of user configuration (profile) files is changed, update the
 * sets in java/test/apps/PanelPro/profiles/ to match.
 * Also check the required TESTMAXTIME in {@link apps.LaunchJmriAppBase} to
 * prevent timeouts on app startup tests if structure of data develops.
 * 
 * @author Paul Bender Copyright (C) 2017
 * @author Bob Jacobsen Copyright (C) 2017
 */
public class SoundProTest extends apps.LaunchJmriAppBase {

    protected void launch(String[] args) {
        SoundPro.main(args);
    }
    
    @Test
    public void testLaunchLocoNet() throws IOException {
        runOne("LocoNet_Simulator", "SoundPro", "SoundPro version"); // param 2 and 3 must match Console output
        jmri.util.JUnitAppender.suppressWarnMessage("passing to xmit: unexpected exception:  [LnPowerManager LnTrackStatusUpdateThread] jmri.jmrix.loconet.LnPacketizer.sendLocoNetMessage()");
        jmri.util.JUnitAppender.suppressWarnMessage("passing to xmit: unexpected exception:  [LnSensorUpdateThread] jmri.jmrix.loconet.LnPacketizer.sendLocoNetMessage()");
        jmri.util.JUnitAppender.suppressWarnMessage("passing to xmit: unexpected exception:  [LnSensorUpdateThread] jmri.jmrix.loconet.LnPacketizer.sendLocoNetMessage()");
    }

    @Test
    public void testLaunchEasyDcc() throws IOException {
        runOne("EasyDcc_Simulator", "SoundPro", "SoundPro version");
    }

    @Test
    public void testLaunchGrapevine() throws IOException {
        runOne("Grapevine_Simulator", "SoundPro", "SoundPro version");
        jmri.util.JUnitAppender.suppressWarnMessage("Timeout can't be handled due to missing node (index 1)");
        jmri.util.JUnitAppender.suppressWarnMessage("Timeout can't be handled due to missing node (index 0)");
    }

    @Test
    public void testLaunchTmcc() throws IOException {
        runOne("TMCC_Simulator", "SoundPro", "SoundPro version");
    }

    @Test
    public void testLaunchSprog() throws IOException {
        runOne("Sprog_Simulator", "PanelPro", "PanelPro version");
    }

    @Test
    public void testLaunchInitLoop() throws IOException {
        runOne("Prevent_Init_Loop", "SoundPro", "SoundPro version");
        jmri.util.JUnitAppender.suppressWarnMessage("passing to xmit: unexpected exception:  [LnPowerManager LnTrackStatusUpdateThread] jmri.jmrix.loconet.LnPacketizer.sendLocoNetMessage()");
        jmri.util.JUnitAppender.suppressWarnMessage("passing to xmit: unexpected exception:  [LnSensorUpdateThread] jmri.jmrix.loconet.LnPacketizer.sendLocoNetMessage()");
        jmri.util.JUnitAppender.suppressWarnMessage("passing to xmit: unexpected exception:  [LnSensorUpdateThread] jmri.jmrix.loconet.LnPacketizer.sendLocoNetMessage()");
    }
    
}

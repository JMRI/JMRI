package apps.DecoderPro;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

/**
 * This is more of an acceptance test than a unit test. It confirms that the entire
 * application can start up and configure itself.
 * 
 * @author Paul Bender Copyright (C) 2017
 * @author Bob Jacobsen Copyright (C) 2017
 */

public class DecoderProTest extends apps.LaunchJmriAppBase {

    protected void launch(String[] args) {
        DecoderPro.main(args);
    }
    
    @Test
    public void testLaunchLocoNet() throws IOException {
        runOne("LocoNet_Simulator", "DecoderPro", "DecoderPro version");
    }

    @Test
    public void testLaunchEasyDcc() throws IOException {
        runOne("EasyDcc_Simulator", "DecoderPro", "DecoderPro version");
    }

    @Test
    public void testLaunchSprog() throws IOException {
        runOne("Sprog_Simulator", "DecoderPro", "DecoderPro version");
    }

    @Test
    public void testLaunchTmcc() throws IOException {
        runOne("TMCC_Simulator", "DecoderPro", "DecoderPro version");
    }

    @Test
    public void testLaunchInitLoop() throws IOException {
        runOne("Prevent_Init_Loop", "DecoderPro", "DecoderPro version");
    }

}

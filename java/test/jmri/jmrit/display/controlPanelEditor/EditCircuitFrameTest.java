package jmri.jmrit.display.controlPanelEditor;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.logix.OBlock;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EditCircuitFrameTest extends jmri.util.JmriJFrameTestBase {

    ControlPanelEditor cpe;
    CircuitBuilder cb;
    OBlock ob;
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        if(!GraphicsEnvironment.isHeadless()){
           jmri.util.ThreadingUtil.runOnGUI(() -> {
               cpe = new ControlPanelEditor();
               cb = new CircuitBuilder(cpe);
               cpe.setVisible(true);
           });
           new org.netbeans.jemmy.QueueTool().waitEmpty(100);
           jmri.util.ThreadingUtil.runOnGUI(() -> {
               ob = new OBlock("OB01");
               frame = new EditCircuitFrame("Edit Circuit Frame", cb, ob);
           });
        
           new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        }
    }


    @After
    public void tearDown() {
        if(cpe!=null){
           cpe.setVisible(false);
           cpe.dispose();
        }
        cpe = null;
        cb = null;
        ob = null;
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EditCircuitFrameTest.class);

}

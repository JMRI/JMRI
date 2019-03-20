package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.Collection;
import javax.swing.JFrame;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runner.RunWith;

/**
 * This is a parameterized test for Editor object's getIconFrame
 * method.  It tests that the method returns a vaid value for all
 * expected parameters to the method. 
 *
 * @author Paul Bender Copyright (C) 2018
 */
@RunWith(Parameterized.class)
public class EditorIconFrameTest {

    private Editor e = null;
    private EditorFrameOperator jfo;
    private String inputString;
    private boolean expectNull;

    public EditorIconFrameTest(String input,boolean nullExpected){
       inputString = input;
       expectNull = nullExpected;
    }

    @Parameterized.Parameters
    public static Collection iconTypes() {
       return Arrays.asList(new Object[][] {
          {"Sensor",false},
          {"RightTurnout",false},
          {"LeftTurnout",false},
          {"SlipTOEditor",false},
          {"SignalHead",false},
          {"SignalMast",false},
          {"Memory",false},
          {"Reporter",false},
          {"Light",false},
          {"Background",false},
          {"MultiSensor",false},
          {"Icon",false},
          //{"Text",true},  //see note in test method.
          {"BlockLabel",false},
          {"bar",true},
       });
    }

    @Test
    public void checkGetIconFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if(expectNull) {
           if(inputString == "Text" ) {
              // "Text" pops up a modal JOptionPane, we need to close it.
           }
           JFrame frame = e.getIconFrame(inputString);
           Assert.assertNull(inputString + " Editor expects null return value", frame);
        } else {
           JFrame frame = e.getIconFrame(inputString);
           Assert.assertNotNull(inputString + " Editor available", frame );
           frame.dispose();
        }
    }

    // from here down is testing infrastructure
    @Before
    public void setUp(){
       JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
       if(!GraphicsEnvironment.isHeadless()) {
          e = new EditorScaffold(inputString + " IconAdder test Editor");
          e.setVisible(true);
          jfo = new EditorFrameOperator(e);
       }
    }

    @After
    public void tearDown(){
       if(!GraphicsEnvironment.isHeadless()) {
          jfo.requestClose();
          JUnitUtil.dispose(e);
       }
       e = null;
       JUnitUtil.tearDown();
    }

}

package jmri.jmrit.display;

import java.util.stream.Stream;

import javax.swing.JFrame;

import jmri.util.JUnitUtil;

import org.junit.Assert;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * This is a parameterized test for Editor object's getIconFrame
 * method.
 * It tests that the method returns a valid value for all
 * expected parameters to the method.
 *
 * @author Paul Bender Copyright (C) 2018
 */
public class EditorIconFrameTest {

    public static Stream<Arguments> iconTypes() {
       return Stream.of(
          arguments("Sensor",false),
          arguments("RightTurnout",false),
          arguments("LeftTurnout",false),
          arguments("SlipTOEditor",false),
          arguments("SignalHead",false),
          arguments("SignalMast",false),
          arguments("Memory",false),
          arguments("Reporter",false),
          arguments("Light",false),
          arguments("Background",false),
          arguments("MultiSensor",false),
          arguments("Icon",false),
          //arguments("Text",true),  //see note in test method.
          arguments("BlockLabel",false),
          arguments("bar",true)
        );
    }

    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    @ParameterizedTest
    @MethodSource("iconTypes")
    public void checkGetIconFrame(String inputString, boolean expectNull) {

        Editor e = new EditorScaffold(inputString + " IconAdder test Editor");
        e.setVisible(true);
        EditorFrameOperator jfo = new EditorFrameOperator(e);
        
        if(expectNull) {
           if("Text".equals(inputString) ) {
              // "Text" pops up a modal JOptionPane, we need to close it.
           }
           JFrame frame = e.getIconFrame(inputString);
           Assert.assertNull(inputString + " Editor expects null return value", frame);
        } else {
           JFrame frame = e.getIconFrame(inputString);
           Assert.assertNotNull(inputString + " Editor available", frame );
           frame.dispose();
        }
        
        jfo.requestClose();
        JUnitUtil.dispose(e);
    }

    // from here down is testing infrastructure
    @BeforeEach
    public void setUp(){
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalSignalHeadManager();
    }

    @AfterEach
    public void tearDown(){
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}

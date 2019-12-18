package jmri.jmrit.ussctc;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for Station classes in the jmri.jmrit.ussctc package
 *
 * @author	Bob Jacobsen Copyright 2007
 */
public class StationTest {

    @Test
    public void testConstruction() {
        Station s = new Station("test", codeline, button);
        s.add(new TurnoutSection());
        
        Assert.assertEquals("test", s.getName());        
        Assert.assertEquals("Station "+s.getName(), s.toString());        
    }

    @Test
    public void testSendCode() {
        Station s = new Station("tests", codeline, button);
        s.add(new Section<CodeGroupTwoBits, CodeGroupTwoBits>(){
            @Override
            public CodeGroupTwoBits  codeSendStart() { countCodeSend++; return CodeGroupTwoBits.Double00; }
            @Override
            public void codeValueDelivered(CodeGroupTwoBits value) { }
            @Override
            public CodeGroupTwoBits indicationStart() { return CodeGroupTwoBits.Double00; }
            @Override
            public void indicationComplete(CodeGroupTwoBits value) {}
            
            @Override
            public Station getStation() { return null; }
            @Override
            public String getName() { return ""; }
        });
        
        countCodeSend = 0;
        
        s.codeSendRequest();
        
        Assert.assertEquals("count of operations", 1, countCodeSend);
    }

    @Test
    public void testSendCodeSendAndImplementMultiSection() {
        Station s = new Station("test", codeline, button);
        s.add(new Section<CodeGroupTwoBits, CodeGroupTwoBits>(){
            @Override
            public CodeGroupTwoBits  codeSendStart() { countCodeSend++; return CodeGroupTwoBits.Double10; }
            @Override
            public void codeValueDelivered(CodeGroupTwoBits value) { 
                Assert.assertEquals("deliver 10", CodeGroupTwoBits.Double10, value);
                countCodeSend = 0;
            }
            @Override
            public CodeGroupTwoBits indicationStart() { return CodeGroupTwoBits.Double00; }
            @Override
            public void indicationComplete(CodeGroupTwoBits value) {}
            
            @Override
            public Station getStation() { return null; }
            @Override
            public String getName() { return ""; }
        });
        s.add(new Section<CodeGroupTwoBits, CodeGroupTwoBits>(){
            @Override
            public CodeGroupTwoBits codeSendStart() { countCodeSend2++; return CodeGroupTwoBits.Double01; }
            @Override
            public void codeValueDelivered(CodeGroupTwoBits value) { 
                Assert.assertEquals("deliver 01", CodeGroupTwoBits.Double01, value);
                countCodeSend2 = 0;
            }
            @Override
            public CodeGroupTwoBits indicationStart() { return CodeGroupTwoBits.Double00; }
            @Override
            public void indicationComplete(CodeGroupTwoBits value) {}
            
            @Override
            public Station getStation() { return null; }
            @Override
            public String getName() { return ""; }
        });
        
        countCodeSend = 0;
        countCodeSend2 = 0;
        
        s.codeSendRequest();
        
        Assert.assertEquals("count of operations 1", 1, countCodeSend);
        Assert.assertEquals("count of operations 2", 1, countCodeSend2);

        s.codeValueDelivered();
        
        Assert.assertEquals("delivered OK 1", 0, countCodeSend);
        Assert.assertEquals("delivered OK 2", 0, countCodeSend2);
    }

    int countCodeSend;
    int countCodeSend2;
    
    CodeLine codeline;
    CodeButton button;
        
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        
        codeline = new CodeLine("Code Indication Start", "Code Send Start", "IT101", "IT102", "IT103", "IT104");
        button = new CodeButton("IS21", "IS22");
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

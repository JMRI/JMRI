package jmri.jmrit.entryexit;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.SensorIcon;
import jmri.jmrit.display.SignalMastIcon;
import jmri.util.JUnitUtil;
import jmri.InstanceManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SourceTest {

    private LayoutEditor editor = null;
    private SensorIcon si = null; 
    private jmri.Sensor s = null; 
    private jmri.SignalMast sm = null;
    private SignalMastIcon to = null;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutBlock f = new LayoutBlock("test1","Facing Block");
        LayoutBlock p1 = new LayoutBlock("test2","Protecting Block 1");
        LayoutBlock p2 = new LayoutBlock("test3","Protecting Block 2");
        List<LayoutBlock> blockList = new ArrayList<>();
        blockList.add(p1);
        blockList.add(p2);

        PointDetails ptd = new PointDetails(f,blockList);
        ptd.setPanel(editor);
        ptd.setSensor(s); 
        ptd.setSignalMast(sm);
        Source t = new Source(ptd);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            editor = new LayoutEditor("Test Entry/Exit Source Panel");
            to = new SignalMastIcon(editor);
            to.setShowAutoText(true);
            sm = new jmri.implementation.VirtualSignalMast("IF$vsm:basic:one-searchlight($1)");
            to.setSignalMast(new jmri.NamedBeanHandle<>(sm.getSystemName(), sm));
        
            si = new SensorIcon(editor);
            s = jmri.InstanceManager.sensorManagerInstance().provideSensor("IS1");
            si.setSensor(new jmri.NamedBeanHandle<>("IS1", s));
            editor.setAllEditable(false);
        }
    }

    @After
    public void tearDown() {
        if(editor!=null) {
           editor.dispose();
        }
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SourceTest.class);

}

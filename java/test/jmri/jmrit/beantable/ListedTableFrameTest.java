package jmri.jmrit.beantable;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ListedTableFrameTest extends jmri.util.JmriJFrameTestBase {

    @Test
    @Override
    public void testShowAndClose() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ((ListedTableFrame)frame).initTables();
        frame.initComponents();
        ThreadingUtil.runOnLayout(() -> {
            frame.setVisible(true);
        });
        JFrameOperator fo = new JFrameOperator(frame);
        // It's up at this point, and can be manipulated
        // Ask to close window
        fo.requestClose();
    }
    
    @Test
    public void testNoInitTablesError() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ListedTableFrame.tabbedTableItemListArrayArray.clear(); // reset static BeanTable list
        frame.initComponents();
        JUnitAppender.assertErrorMessageStartsWith("No tables loaded: ");
    }
    
    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        if(!GraphicsEnvironment.isHeadless()) {
           jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
           frame = new ListedTableFrame<>();
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // should be converted to check of scheduled ShutDownActions
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ListedTableFrameTest.class);

}

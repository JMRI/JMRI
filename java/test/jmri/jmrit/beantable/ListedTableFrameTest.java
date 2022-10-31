package jmri.jmrit.beantable;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class ListedTableFrameTest extends jmri.util.JmriJFrameTestBase {

    @Test
    @Override
    public void testShowAndClose() {
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
        ListedTableFrame.tabbedTableItemListArrayArray.clear(); // reset static BeanTable list
        frame.initComponents();
        JUnitAppender.assertErrorMessageStartsWith("No tables loaded: ");
    }

    @Test
    @Override
    public void testAccessibleContent() {
        ((ListedTableFrame)frame).initTables();
        super.testAccessibleContent();
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        frame = new ListedTableFrame<>();
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // should be converted to check of scheduled ShutDownActions
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ListedTableFrameTest.class);

}

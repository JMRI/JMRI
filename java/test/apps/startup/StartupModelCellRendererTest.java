package apps.startup;

import java.awt.Component;

import javax.swing.JTable;

import jmri.util.JUnitUtil;
import jmri.util.startup.StartupModel;

import org.junit.jupiter.api.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class StartupModelCellRendererTest {

    @Test
    public void testCTor() {
        StartupModelCellRenderer t = new StartupModelCellRenderer();
        Assertions.assertNotNull(t, "exists");
    }

    private static StartupModel model(String name, boolean valid) {
        StartupModel m = mock(StartupModel.class);
        when(m.getName()).thenReturn(name);
        when(m.isValid()).thenReturn(valid);
        return m;
    }

    @Test
    public void testInvalidActionShownInRed() {
        StartupModelCellRenderer t = new StartupModelCellRenderer();
        JTable table = new JTable();
        Component c = t.getTableCellRendererComponent(table, model("skipped", false), false, false, 0, 0);
        Assertions.assertEquals(StartupModelCellRenderer.INVALID_COLOR, c.getForeground(), "invalid action in red");
    }

    @Test
    public void testValidActionAfterInvalidNotRed() {
        StartupModelCellRenderer t = new StartupModelCellRenderer();
        JTable table = new JTable();
        t.getTableCellRendererComponent(table, model("skipped", false), false, false, 0, 0);
        Component c = t.getTableCellRendererComponent(table, model("runs", true), false, false, 1, 0);
        Assertions.assertEquals(table.getForeground(), c.getForeground(), "valid action in the table's colour");
        c = t.getTableCellRendererComponent(table, model("runs", true), true, false, 1, 0);
        Assertions.assertEquals(table.getSelectionForeground(), c.getForeground(), "selected valid action in the selection colour");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(StartupModelCellRendererTest.class);

}

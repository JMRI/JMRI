package jmri.jmrit.beantable.beanedit;

import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JTextField;

import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.Path;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class BlockEditActionTest {

    @Test
    public void testCTor() {
        BlockEditAction t = new BlockEditAction();
        Assert.assertNotNull("exists",t);
    }

    private static String twoPlaces(double v) {
        NumberFormat f = NumberFormat.getNumberInstance();
        f.setMinimumFractionDigits(2);
        f.setMaximumFractionDigits(2);
        f.setGroupingUsed(false);
        return f.format(v);
    }

    private Block a;
    private Block b;
    private Block c;

    private void line() {
        BlockManager bm = InstanceManager.getDefault(BlockManager.class);
        a = bm.provideBlock("IB1");
        a.setUserName("West");
        b = bm.provideBlock("IB2");
        c = bm.provideBlock("IB3");
        c.setUserName("East");
        b.setLength(500f);
        b.addPath(new Path(a, Path.NONE, Path.NONE));
        b.addPath(new Path(c, Path.NONE, Path.NONE));
    }

    @Test
    public void testRoutesShownAndSaved() {
        line();
        b.setRouteLengthMm(a, c, 600f);
        b.setBrakingTowards(c, true);
        BlockEditAction t = new BlockEditAction();
        t.setBean(b);
        t.metricUi = true;
        BeanItemPanel routes = t.physicalDetails();
        routes.resetField();

        Assertions.assertEquals(1, t.routesTable.getRowCount(), "one way through");
        Assertions.assertEquals("West - East", t.routesTable.getValueAt(0, 0));
        Assertions.assertEquals("Length (cm)", t.routesTable.getColumnName(1));
        Assertions.assertEquals(twoPlaces(60.0), t.routesTable.getValueAt(0, 1), "600 mm, in cm");
        Assertions.assertTrue(t.brakingBoxes.get(c).isSelected(), "braking towards East");
        Assertions.assertFalse(t.brakingBoxes.get(a).isSelected());

        t.routesTable.setValueAt(twoPlaces(72.5), 0, 1);
        t.brakingBoxes.get(c).setSelected(false);
        t.brakingBoxes.get(a).setSelected(true);
        routes.saveItem();
        Assertions.assertEquals(725f, b.getRouteLengthMm(a, c), 0.01f);
        Assertions.assertFalse(b.isBrakingTowards(c));
        Assertions.assertTrue(b.isBrakingTowards(a));

        t.routesTable.setValueAt("", 0, 1);
        routes.saveItem();
        Assertions.assertFalse(b.hasRouteLength(a, c), "blank: the Block's own length");
        Assertions.assertEquals(500f, b.getRouteLengthMm(a, c));
        routes.resetField();
        Assertions.assertEquals("", t.routesTable.getValueAt(0, 1), "shown blank when none is set");

        t.routesTable.setValueAt("long", 0, 1);
        Assertions.assertEquals("", t.routesTable.getValueAt(0, 1), "not a length: ignored");

        // typed into the table's editor
        Assertions.assertTrue(t.routesTable.editCellAt(0, 1));
        ((JTextField) t.routesTable.getEditorComponent()).setText(twoPlaces(55.0));
        Assertions.assertTrue(t.routesTable.getCellEditor().stopCellEditing(), "a length is taken");
        Assertions.assertEquals(twoPlaces(55.0), t.routesTable.getValueAt(0, 1));

        // Reset while typing drops what was typed
        Assertions.assertTrue(t.routesTable.editCellAt(0, 1));
        ((JTextField) t.routesTable.getEditorComponent()).setText(twoPlaces(99.0));
        routes.resetField();
        Assertions.assertFalse(t.routesTable.isEditing());
        Assertions.assertEquals("", t.routesTable.getValueAt(0, 1), "back to the Block's: none set");
    }

    @Test
    public void testRouteLengthInInches() {
        line();
        b.setRouteLengthMm(a, c, 254f);
        BlockEditAction t = new BlockEditAction();
        t.setBean(b);
        t.metricUi = false;
        BeanItemPanel routes = t.physicalDetails();
        routes.resetField();
        Assertions.assertEquals("Length (inches)", t.routesTable.getColumnName(1));
        Assertions.assertEquals(twoPlaces(10.0), t.routesTable.getValueAt(0, 1));
        t.routesTable.setValueAt(twoPlaces(20.0), 0, 1);
        routes.saveItem();
        Assertions.assertEquals(508f, b.getRouteLengthMm(a, c), 0.01f);
    }

    @Test
    public void testWhatIsNotALengthIsRefused() throws ParseException {
        Assertions.assertNull(BlockEditAction.parseLength("  "), "blank");
        Assertions.assertEquals(12.5f, BlockEditAction.parseLength(twoPlaces(12.5)));
        Assertions.assertThrows(ParseException.class, () -> BlockEditAction.parseLength("long"));
        Assertions.assertThrows(ParseException.class, () -> BlockEditAction.parseLength("-1"));
        Assertions.assertThrows(ParseException.class, () -> BlockEditAction.parseLength("0"), "a length is more than 0");

        line();
        b.setRouteLengthMm(a, c, 600f);
        BlockEditAction t = new BlockEditAction();
        t.setBean(b);
        t.metricUi = true;
        BeanItemPanel routes = t.physicalDetails();
        routes.resetField();
        Assertions.assertTrue(t.routesTable.editCellAt(0, 1));
        ((JTextField) t.routesTable.getEditorComponent()).setText("long");
        Assertions.assertFalse(t.routesTable.getCellEditor().stopCellEditing(), "refused");
        routes.saveItem();
        Assertions.assertFalse(t.routesTable.isEditing(), "the refused entry is dropped on saving");
        Assertions.assertEquals(600f, b.getRouteLengthMm(a, c), "and the length before it kept");
    }

    @Test
    public void testBlockWithNoNeighbours() {
        Block alone = InstanceManager.getDefault(BlockManager.class).provideBlock("IB9");
        BlockEditAction t = new BlockEditAction();
        t.setBean(alone);
        BeanItemPanel routes = t.physicalDetails();
        routes.resetField();
        Assertions.assertEquals(0, t.routesTable.getRowCount());
        Assertions.assertTrue(t.brakingBoxes.isEmpty());
        routes.saveItem();
        Assertions.assertTrue(alone.getPropertyKeys().isEmpty(), "nothing stored");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(BlockEditActionTest.class);

}

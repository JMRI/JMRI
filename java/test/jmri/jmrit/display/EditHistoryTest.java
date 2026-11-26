package jmri.jmrit.display;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import jmri.jmrit.display.EditState.Property;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link EditHistory}, on a small panel of items with one value
 * each.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class EditHistoryTest {

    /** An item on the test panel. */
    private static final class Item {

        int value;
        boolean disposed = false;

        Item(int value) {
            this.value = value;
        }

        int getValue() {
            return value;
        }

        void setValue(int v) {
            value = v;
        }
    }

    private static final List<Property<Item, ?>> PROPERTIES = List.of(Property.ofInt(Item::getValue, Item::setValue));

    /** The test panel: a list of items. */
    private final class Panel implements EditHistory.Model {

        final List<Item> items = new ArrayList<>();
        int dirtied = 0;

        @Override
        public List<EditState<?>> capture() {
            List<EditState<?>> l = new ArrayList<>();
            for (Item i : items) {
                l.add(new EditState<>(i, PROPERTIES));
            }
            return l;
        }

        @Override
        public void restore(List<Object> list, List<EditState<?>> states) {
            items.clear();
            for (Object o : list) {
                items.add((Item) o);
            }
            for (EditState<?> s : states) {
                s.restore();
            }
        }

        @Override
        public void disposeItem(Object item) {
            ((Item) item).disposed = true;
        }

        @Override
        public void setDirty() {
            dirtied++;
        }
    }

    private Panel panel;
    private boolean editMode;
    private EditHistory history;

    @Test
    public void testOneActionIsOneStep() {
        Item a = new Item(1);
        panel.items.add(a);
        history.begin("Move");
        a.value = 2;
        history.begin("inner");     // nested: part of the outer step
        a.value = 3;
        history.end();
        assertFalse(history.canUndo(), "no step until the action ends");
        history.end();
        assertTrue(history.canUndo());
        assertEquals("Move", history.getUndoName(), "named by the outer action");

        assertTrue(history.undo());
        assertEquals(1, a.value, "undo goes back to before the whole action");
        assertTrue(history.canRedo());
        assertTrue(history.redo());
        assertEquals(3, a.value);
        assertEquals(2, panel.dirtied, "undo and redo mark the panel changed");
    }

    @Test
    public void testNoChangeNoStep() {
        panel.items.add(new Item(1));
        history.begin("Click");
        history.end();
        assertFalse(history.canUndo());
    }

    @Test
    public void testRemovedItemComesBackAsTheSameObject() {
        Item a = new Item(1);
        Item b = new Item(2);
        panel.items.add(a);
        panel.items.add(b);
        history.begin("Delete");
        panel.items.remove(a);
        history.retireLater(a);
        history.end();
        assertFalse(a.disposed, "kept while a step can put it back");

        history.undo();
        assertEquals(2, panel.items.size());
        assertSame(a, panel.items.get(0), "the same object, in its place");
        history.redo();
        assertEquals(List.of(b), panel.items);

        history.discardAll();
        assertTrue(a.disposed, "disposed once no step can put it back");
        assertFalse(b.disposed, "items on the panel are not disposed");
    }

    @Test
    public void testAddedItemUndone() {
        Item a = new Item(1);
        history.begin("Add");
        panel.items.add(a);
        history.end();
        history.undo();
        assertTrue(panel.items.isEmpty());
        history.redo();
        assertSame(a, panel.items.get(0));
    }

    @Test
    public void testItemRemovedAndNotRecordedIsDisposedAtEnd() {
        Item a = new Item(1);
        history.begin("Add and remove");
        panel.items.add(a);
        panel.items.remove(a);
        history.retireLater(a);
        history.end();
        assertFalse(history.canUndo(), "nothing changed");
        assertTrue(a.disposed, "no step holds it");
    }

    @Test
    public void testNewStepDropsRedo() {
        Item a = new Item(1);
        panel.items.add(a);
        step(a, 2, "First");
        history.undo();
        assertTrue(history.canRedo());
        step(a, 5, "Second");
        assertFalse(history.canRedo(), "a new step drops the steps undone");
        assertEquals("Second", history.getUndoName());
    }

    @Test
    public void testOnlyInEditMode() {
        Item a = new Item(1);
        panel.items.add(a);
        step(a, 2, "Move");
        editMode = false;
        assertFalse(history.undo(), "not out of edit mode");
        assertEquals(2, a.value);
        editMode = true;
        assertTrue(history.undo());
        assertEquals(1, a.value);
    }

    @Test
    public void testNotRecordedOutOfEditMode() {
        Item a = new Item(1);
        panel.items.add(a);
        editMode = false;
        step(a, 2, "Move");
        history.beginOpen("Menu");
        a.value = 3;
        editMode = true;
        assertFalse(history.canUndo(), "nothing recorded out of edit mode");
        assertFalse(history.undo());
    }

    @Test
    public void testChangedSinceIsDropped() {
        Item a = new Item(1);
        Item b = new Item(10);
        panel.items.add(a);
        panel.items.add(b);
        step(b, 11, "Edit b");
        step(a, 2, "Edit a");
        a.value = 7;    // changed by something else (a script)
        assertFalse(history.undo(), "the step no longer matches the panel");
        JUnitAppender.assertWarnMessageStartingWith("Panel changed since \"Edit a\" was done");
        assertEquals(7, a.value, "nothing is changed");
        assertEquals("Edit b", history.getUndoName(), "the step is dropped, earlier ones kept");
        assertTrue(history.undo());
        assertEquals(10, b.value);
    }

    @Test
    public void testLimit() {
        Item a = new Item(0);
        panel.items.add(a);
        for (int i = 1; i <= EditHistory.DEFAULT_LIMIT + 5; i++) {
            step(a, i, "Step " + i);
        }
        int n = 0;
        while (history.undo()) {
            n++;
        }
        assertEquals(EditHistory.DEFAULT_LIMIT, n, "only the last steps are kept");
        assertEquals(5, a.value);
    }

    @Test
    public void testOpenStepEndsAtNextAction() {
        Item a = new Item(1);
        panel.items.add(a);
        history.beginOpen("Menu command");
        history.rename("Hidden");
        a.value = 2;    // as after a question the command asked
        assertFalse(history.canUndo(), "still open");
        step(a, 3, "Move");
        assertEquals("Move", history.getUndoName());
        history.undo();
        assertEquals("Hidden", history.getUndoName(), "the open step was closed by the next action");
        history.undo();
        assertEquals(1, a.value);
    }

    @Test
    public void testOpenStepRequestedDuringAnAction() {
        Item a = new Item(1);
        panel.items.add(a);
        history.begin("Press");
        history.beginOpen("Popup");     // a pop-up menu shown during the press
        history.end();                  // the press changed nothing
        a.value = 2;                    // the pop-up command
        assertTrue(history.undo(), "Undo closes the open step first");
        assertEquals(1, a.value);
    }

    @Test
    public void testUndoClosesOpenStep() {
        Item a = new Item(1);
        panel.items.add(a);
        history.beginOpen("Delete");
        a.value = 4;
        assertTrue(history.undo());
        assertEquals(1, a.value);
    }

    @Test
    public void testNothingRecordedWhileRestoring() {
        Item a = new Item(1);
        panel.items.add(a);
        step(a, 2, "Move");
        // a model whose restore itself begins an action, as setters may do
        EditHistory h2 = new EditHistory(new EditHistory.Model() {
            @Override
            public List<EditState<?>> capture() {
                return panel.capture();
            }

            @Override
            public void restore(List<Object> items, List<EditState<?>> states) {
                assertTrue(holder[0].isRestoring());
                holder[0].begin("from a setter");
                panel.restore(items, states);
                holder[0].end();
            }

            @Override
            public void disposeItem(Object item) {
            }

            @Override
            public void setDirty() {
            }
        }, () -> true);
        holder[0] = h2;
        h2.begin("Move");
        a.value = 3;
        h2.end();
        assertTrue(h2.undo());
        assertEquals(2, a.value);
        assertFalse(h2.canUndo(), "the restore recorded no step");
        assertTrue(h2.canRedo());
    }

    private final EditHistory[] holder = new EditHistory[1];

    @Test
    public void testMenuNames() {
        assertEquals(Bundle.getMessage("MenuItemUndo"), history.createUndoMenuItem().getText());
        Item a = new Item(1);
        panel.items.add(a);
        step(a, 2, "Move");
        assertEquals(Bundle.getMessage("MenuItemUndoStep", "Move"), history.createUndoMenuItem().getText());
        assertTrue(history.createUndoMenuItem().isEnabled());
        assertFalse(history.createRedoMenuItem().isEnabled());
        editMode = false;
        history.updateActions();
        assertFalse(history.createUndoMenuItem().isEnabled(), "off outside edit mode");
    }

    @Test
    public void testSameValue() {
        assertTrue(EditState.sameValue(1.0, 1.0 + 1.0E-9), "doubles within the tolerance");
        assertFalse(EditState.sameValue(1.0, 1.001));
        assertTrue(EditState.sameValue(new java.awt.geom.Point2D.Double(1, 2),
                new java.awt.geom.Point2D.Double(1 + 1.0E-9, 2)));
        assertTrue(EditState.sameValue(List.of(1, 2), List.of(1, 2)));
        assertFalse(EditState.sameValue(List.of(1, 2), List.of(1)));
        assertTrue(EditState.sameValue(null, null));
        assertFalse(EditState.sameValue(null, "a"));
    }

    private void step(Item item, int value, String name) {
        history.begin(name);
        item.value = value;
        history.end();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        panel = new Panel();
        editMode = true;
        history = new EditHistory(panel, () -> editMode);
    }

    @AfterEach
    public void tearDown() {
        history.discardAll();
        JUnitUtil.tearDown();
    }
}

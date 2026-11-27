package jmri.jmrit.display;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import jmri.InvokeOnGuiThread;
import jmri.util.SystemType;

/**
 * Undo and redo for the edits made to a panel in an editor's edit mode.
 * <p>
 * An editor brackets each user action (a drag, a delete, a dialog's OK, a
 * menu command) with {@link #begin(String)} and {@link #end()}. At
 * {@code begin} the history records the state of every item on the panel; at
 * {@code end} it records it again and keeps, as one step, only the items whose
 * state or presence changed. Undo writes the earlier states back and puts back
 * the very same objects that were removed, so other parts of JMRI that hold
 * them (Signal Mast Logic, Entry/Exit, LogixNG) still find them on the panel.
 * Redo does the reverse.
 * <p>
 * Before a step is undone or redone, each of its items must still be as the
 * step left it. If something else (a script, LogixNG) has changed one of them
 * since, the step is dropped from the history, with a log message, and
 * nothing is changed.
 * <p>
 * Actions bracketed inside another action are part of the outer step. While
 * a step is being undone or redone, no new step is recorded.
 * <p>
 * The history is kept while the panel is open (storing the panel doesn't
 * clear it) and is cleared by {@link #discardAll()} when the panel closes.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public class EditHistory {

    /**
     * The number of steps kept. Each step holds only the items it changed.
     */
    public static final int DEFAULT_LIMIT = 100;

    /**
     * What the history needs from an editor: the panel's items with their
     * states, and a way to put a set of items and states back.
     */
    public interface Model {

        /**
         * Record every item on the panel, in panel order, with its state.
         *
         * @return the items' states
         */
        @Nonnull
        List<EditState<?>> capture();

        /**
         * Make the panel hold exactly the given items, in that order, adding
         * back (the same objects) any that are missing and removing any that
         * are not listed, then write back the given states and redraw.
         *
         * @param items  the items the panel is to hold
         * @param states the states to write back, for items that are in
         *               {@code items}
         */
        void restore(@Nonnull List<Object> items, @Nonnull List<EditState<?>> states);

        /**
         * Dispose of an item that has left the panel and that no step can put
         * back any more.
         *
         * @param item the item
         */
        void disposeItem(@Nonnull Object item);

        /**
         * Mark the panel as changed.
         */
        void setDirty();
    }

    private final Model model;
    private final BooleanSupplier enabled;
    private final History manager = new History();
    private final Action undoAction;
    private final Action redoAction;

    private int depth = 0;
    private boolean open = false;
    private String openRequested = null;
    private boolean restoring = false;
    private String stepName = "";
    private List<EditState<?>> beforeStates = null;
    private final Set<Object> retiring = Collections.newSetFromMap(new IdentityHashMap<>());

    /**
     * Create the history for an editor.
     *
     * @param model   the editor's panel
     * @param enabled true when undo and redo may be used (the editor is in
     *                edit mode)
     */
    public EditHistory(@Nonnull Model model, @Nonnull BooleanSupplier enabled) {
        this.model = model;
        this.enabled = enabled;
        manager.setLimit(DEFAULT_LIMIT);
        undoAction = new AbstractAction(Bundle.getMessage("MenuItemUndo")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                undo();
            }
        };
        redoAction = new AbstractAction(Bundle.getMessage("MenuItemRedo")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                redo();
            }
        };
        updateActions();
    }

    /**
     * @return a new Undo menu item, kept up to date by this history, with
     *         its shortcut (Ctrl+Z, Command+Z on macOS)
     */
    @Nonnull
    public JMenuItem createUndoMenuItem() {
        JMenuItem item = new JMenuItem(undoAction);
        if (!GraphicsEnvironment.isHeadless()) {
            int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, shortcut));
        }
        return item;
    }

    /**
     * @return a new Redo menu item, kept up to date by this history, with
     *         its shortcut (Ctrl+Y, Command+Shift+Z on macOS)
     */
    @Nonnull
    public JMenuItem createRedoMenuItem() {
        JMenuItem item = new JMenuItem(redoAction);
        if (!GraphicsEnvironment.isHeadless()) {
            int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
            item.setAccelerator(SystemType.isMacOSX()
                    ? KeyStroke.getKeyStroke(KeyEvent.VK_Z, shortcut | InputEvent.SHIFT_DOWN_MASK)
                    : KeyStroke.getKeyStroke(KeyEvent.VK_Y, shortcut));
        }
        return item;
    }

    /**
     * Start a user action. Every change to the panel until the matching
     * {@link #end()} is one step. Calls may be nested; the outermost names
     * the step.
     *
     * @param name the step's name, as shown after "Undo" and "Redo"
     */
    @InvokeOnGuiThread
    public void begin(@Nonnull String name) {
        if (restoring) {
            return;
        }
        if (depth == 0) {
            closeOpenStep();
            stepName = name;
            // only edits made in edit mode are recorded
            beforeStates = enabled.getAsBoolean() ? model.capture() : null;
        }
        depth++;
    }

    /**
     * Start a user action whose end isn't seen: a command chosen from a menu
     * or pop-up menu, which may ask a question or open a window before it
     * changes anything. The step stays open until the next user action
     * begins, or until Undo or Redo is used.
     * <p>
     * Called during another action, the open step starts when that action
     * ends.
     *
     * @param name the step's name, as shown after "Undo" and "Redo"
     */
    @InvokeOnGuiThread
    public void beginOpen(@Nonnull String name) {
        if (restoring) {
            return;
        }
        if (depth > 0) {
            openRequested = name;
            return;
        }
        if (!enabled.getAsBoolean()) {
            closeOpenStep();
            return;
        }
        closeOpenStep();
        startOpenStep(name);
    }

    private void startOpenStep(String name) {
        open = true;
        stepName = name;
        beforeStates = model.capture();
    }

    /**
     * Close the open step begun by {@link #beginOpen(String)}, keeping its
     * changes, if any, as a step.
     */
    @InvokeOnGuiThread
    public void closeOpenStep() {
        if (open) {
            open = false;
            record();
        }
    }

    /**
     * Rename the step being recorded, for an action whose kind is known only
     * once it has started (a press of the mouse that turns into a drag).
     *
     * @param name the step's name
     */
    @InvokeOnGuiThread
    public void rename(@Nonnull String name) {
        if (depth > 0 || open) {
            stepName = name;
        }
    }

    /**
     * End a user action begun with {@link #begin(String)}. At the outermost
     * end, the changes made since the begin are kept as one step, if there
     * are any.
     */
    @InvokeOnGuiThread
    public void end() {
        if (restoring || depth == 0) {
            return;
        }
        depth--;
        if (depth == 0) {
            if (beforeStates != null) {
                record();
            }
            if (openRequested != null) {
                String name = openRequested;
                openRequested = null;
                beginOpen(name);
            }
        }
    }

    /*
     * Keep the changes since the step began as a step, if there are any.
     */
    private void record() {
        List<EditState<?>> before = beforeStates;
        beforeStates = null;
        List<EditState<?>> after = model.capture();
        Step step = Step.between(this, stepName, before, after);
        if (step != null) {
            manager.addEdit(step);
            updateActions();
        }
        // items removed during the action that the new step can't put back
        Set<Object> onPanel = index(after).keySet();
        for (Object item : retiring) {
            if (!onPanel.contains(item) && (step == null || !step.items.contains(item))) {
                model.disposeItem(item);
            }
        }
        retiring.clear();
    }

    /**
     * Note an item that has been removed from the panel during the user
     * action being recorded. It is disposed of by
     * {@link Model#disposeItem(Object)} once no step can put it back.
     *
     * @param item the item
     */
    public void retireLater(@Nonnull Object item) {
        retiring.add(item);
    }

    /**
     * @return true while a user action is being recorded
     */
    public boolean isRecording() {
        return (depth > 0 && beforeStates != null) || open;
    }

    /**
     * @return true while a step is being undone or redone
     */
    public boolean isRestoring() {
        return restoring;
    }

    /**
     * @return true if there is a step to undo
     */
    public boolean canUndo() {
        return manager.canUndo();
    }

    /**
     * @return true if there is a step to redo
     */
    public boolean canRedo() {
        return manager.canRedo();
    }

    /**
     * @return the name of the step Undo would undo, or "" if none
     */
    @Nonnull
    public String getUndoName() {
        Step s = manager.nextUndo();
        return (s == null) ? "" : s.getPresentationName();
    }

    /**
     * @return the name of the step Redo would redo, or "" if none
     */
    @Nonnull
    public String getRedoName() {
        Step s = manager.nextRedo();
        return (s == null) ? "" : s.getPresentationName();
    }

    /**
     * Undo the last step, if the panel is in edit mode.
     *
     * @return true if a step was undone
     */
    @InvokeOnGuiThread
    public boolean undo() {
        if (depth > 0 || !enabled.getAsBoolean()) {
            return false;
        }
        closeOpenStep();
        Step s = manager.nextUndo();
        if (s == null) {
            return false;
        }
        boolean done = apply(s, true);
        if (done) {
            manager.undo();
        } else {
            manager.drop(s);
        }
        updateActions();
        return done;
    }

    /**
     * Redo the last step undone, if the panel is in edit mode.
     *
     * @return true if a step was redone
     */
    @InvokeOnGuiThread
    public boolean redo() {
        if (depth > 0 || !enabled.getAsBoolean()) {
            return false;
        }
        closeOpenStep();
        Step s = manager.nextRedo();
        if (s == null) {
            return false;
        }
        boolean done = apply(s, false);
        if (done) {
            manager.redo();
        } else {
            manager.drop(s);
        }
        updateActions();
        return done;
    }

    /**
     * Discard every step, for example when the panel closes. Items kept only
     * for undo are disposed of.
     */
    @InvokeOnGuiThread
    public void discardAll() {
        stopWatchingMenus();
        manager.discardAllEdits();
        if (!retiring.isEmpty()) {
            Set<Object> onPanel = index(model.capture()).keySet();
            for (Object item : retiring) {
                if (!onPanel.contains(item)) {
                    model.disposeItem(item);
                }
            }
            retiring.clear();
        }
        depth = 0;
        open = false;
        openRequested = null;
        beforeStates = null;
        updateActions();
    }

    /**
     * Update the Undo and Redo menu items, for example when the editor enters
     * or leaves edit mode.
     */
    public void updateActions() {
        boolean on = enabled.getAsBoolean();
        String u = getUndoName();
        String r = getRedoName();
        undoAction.putValue(Action.NAME, u.isEmpty() ? Bundle.getMessage("MenuItemUndo")
                : Bundle.getMessage("MenuItemUndoStep", u));
        redoAction.putValue(Action.NAME, r.isEmpty() ? Bundle.getMessage("MenuItemRedo")
                : Bundle.getMessage("MenuItemRedoStep", r));
        undoAction.setEnabled(on && !u.isEmpty());
        redoAction.setEnabled(on && !r.isEmpty());
    }

    private javax.swing.event.ChangeListener menuWatcher = null;

    /**
     * Record each command chosen from a window's menus (its menu bar, and the
     * pop-up menus shown over it) as a step, with
     * {@link #beginOpen(String)} when a menu opens, named after the command
     * chosen. Call {@link #stopWatchingMenus()} when the window closes.
     *
     * @param window the editor's window
     */
    @InvokeOnGuiThread
    public void watchMenus(@Nonnull Window window) {
        stopWatchingMenus();
        menuWatcher = new javax.swing.event.ChangeListener() {
            private MenuElement root = null;
            private boolean ours = false;

            @Override
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                MenuElement[] path = MenuSelectionManager.defaultManager().getSelectedPath();
                if (path.length == 0) {
                    root = null;
                    return;
                }
                if (path[0] != root) {
                    root = path[0];
                    ours = belongsTo(root, window);
                    if (ours) {
                        beginOpen(Bundle.getMessage("StepEdit"));
                    }
                }
                if (ours) {
                    MenuElement leaf = path[path.length - 1];
                    if (leaf instanceof JMenuItem && !(leaf instanceof JMenu)) {
                        String text = ((JMenuItem) leaf).getText();
                        if (text != null && !text.isEmpty()) {
                            rename(text.endsWith("...") ? text.substring(0, text.length() - 3) : text);
                        }
                    }
                }
            }
        };
        MenuSelectionManager.defaultManager().addChangeListener(menuWatcher);
    }

    /**
     * Stop recording a window's menu commands.
     */
    @InvokeOnGuiThread
    public void stopWatchingMenus() {
        if (menuWatcher != null) {
            MenuSelectionManager.defaultManager().removeChangeListener(menuWatcher);
            menuWatcher = null;
        }
    }

    private static boolean belongsTo(MenuElement root, Window window) {
        Component c;
        if (root instanceof JPopupMenu) {
            c = ((JPopupMenu) root).getInvoker();
        } else if (root instanceof Component) {
            c = (Component) root;
        } else {
            return false;
        }
        return c != null && (c == window || SwingUtilities.getWindowAncestor(c) == window);
    }

    /*
     * Put back one side of a step: the before side to undo, the after side to
     * redo.
     */
    private boolean apply(Step step, boolean toBefore) {
        Map<Object, EditState<?>> target = toBefore ? step.before : step.after;
        Map<Object, EditState<?>> expected = toBefore ? step.after : step.before;
        List<EditState<?>> current = model.capture();
        Map<Object, EditState<?>> now = index(current);
        for (Object item : step.items) {
            EditState<?> e = expected.get(item);
            EditState<?> c = now.get(item);
            if ((e == null) ? (c != null) : !e.sameAs(c)) {
                log.warn("Panel changed since \"{}\" was done; that step can't be {} and has been dropped",
                        step.getPresentationName(), toBefore ? "undone" : "redone");
                return false;
            }
        }
        List<Object> order = new ArrayList<>();
        Set<Object> listed = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Object item : toBefore ? step.beforeOrder : step.afterOrder) {
            if (target.get(item) != null || (now.containsKey(item) && !step.items.contains(item))) {
                order.add(item);
                listed.add(item);
            }
        }
        for (EditState<?> s : current) {
            Object item = s.getItem();
            if (!listed.contains(item) && !(step.items.contains(item) && target.get(item) == null)) {
                order.add(item);
            }
        }
        List<EditState<?>> states = new ArrayList<>();
        for (Object item : step.items) {
            EditState<?> t = target.get(item);
            if (t != null) {
                states.add(t);
            }
        }
        restoring = true;
        try {
            model.restore(order, states);
            model.setDirty();
        } finally {
            restoring = false;
        }
        return true;
    }

    private static Map<Object, EditState<?>> index(List<EditState<?>> states) {
        Map<Object, EditState<?>> m = new IdentityHashMap<>();
        for (EditState<?> s : states) {
            m.put(s.getItem(), s);
        }
        return m;
    }

    /*
     * Dispose of the items of a step that has died which are off the panel and
     * which no live step can put back.
     */
    private void retire(Step dead) {
        Set<Object> onPanel = index(model.capture()).keySet();
        for (Object item : dead.items) {
            if (!onPanel.contains(item) && !manager.isHeldByLiveStep(item, dead)) {
                model.disposeItem(item);
            }
        }
    }

    /**
     * One user action: the items it changed, with their states before and
     * after (no state where the item was not on the panel), and the order of
     * the panel's items before and after.
     */
    private static final class Step extends AbstractUndoableEdit {

        private final EditHistory owner;
        private final String name;
        private final Set<Object> items;
        private final Map<Object, EditState<?>> before;
        private final Map<Object, EditState<?>> after;
        private final List<Object> beforeOrder;
        private final List<Object> afterOrder;

        private Step(EditHistory owner, String name, Set<Object> items, Map<Object, EditState<?>> before,
                Map<Object, EditState<?>> after, List<Object> beforeOrder, List<Object> afterOrder) {
            this.owner = owner;
            this.name = name;
            this.items = items;
            this.before = before;
            this.after = after;
            this.beforeOrder = beforeOrder;
            this.afterOrder = afterOrder;
        }

        /*
         * The step between two captures, or null if nothing changed.
         */
        @CheckForNull
        static Step between(EditHistory owner, String name, List<EditState<?>> b, List<EditState<?>> a) {
            Map<Object, EditState<?>> bm = index(b);
            Map<Object, EditState<?>> am = index(a);
            Set<Object> changed = Collections.newSetFromMap(new IdentityHashMap<>());
            Map<Object, EditState<?>> before = new IdentityHashMap<>();
            Map<Object, EditState<?>> after = new IdentityHashMap<>();
            for (EditState<?> s : b) {
                EditState<?> other = am.get(s.getItem());
                if (!s.sameAs(other)) {
                    changed.add(s.getItem());
                    before.put(s.getItem(), s);
                    if (other != null) {
                        after.put(s.getItem(), other);
                    }
                }
            }
            for (EditState<?> s : a) {
                if (!bm.containsKey(s.getItem())) {
                    changed.add(s.getItem());
                    after.put(s.getItem(), s);
                }
            }
            if (changed.isEmpty()) {
                return null;
            }
            return new Step(owner, name, changed, before, after, items(b), items(a));
        }

        @Override
        public String getPresentationName() {
            return name;
        }

        @Override
        public void die() {
            super.die();
            owner.retire(this);
        }
    }

    private static List<Object> items(List<EditState<?>> states) {
        List<Object> list = new ArrayList<>(states.size());
        for (EditState<?> s : states) {
            list.add(s.getItem());
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * The undo manager, with access to its steps.
     */
    private static final class History extends UndoManager {

        @CheckForNull
        Step nextUndo() {
            UndoableEdit e = editToBeUndone();
            return (e instanceof Step) ? (Step) e : null;
        }

        @CheckForNull
        Step nextRedo() {
            UndoableEdit e = editToBeRedone();
            return (e instanceof Step) ? (Step) e : null;
        }

        /*
         * Remove one step (and so it dies), keeping the others.
         */
        void drop(Step s) {
            int i = edits.indexOf(s);
            if (i >= 0) {
                trimEdits(i, i);
            }
        }

        boolean isHeldByLiveStep(Object item, Step except) {
            for (UndoableEdit e : edits) {
                if (e != except && e instanceof Step && ((Step) e).items.contains(item)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EditHistory.class);
}

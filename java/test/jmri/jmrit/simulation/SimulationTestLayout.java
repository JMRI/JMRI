package jmri.jmrit.simulation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import jmri.Block;
import jmri.Turnout;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JUnitUtil;

/**
 * A layout built by {@link SimulationLayoutBuilder}: its Layout Editor panel,
 * and its Blocks and Turnouts by name.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public final class SimulationTestLayout {

    private final LayoutEditor editor;
    private final Map<String, Block> blocks;
    private final Map<String, Turnout> turnouts;

    SimulationTestLayout(@Nonnull LayoutEditor editor, @Nonnull Map<String, Block> blocks,
            @Nonnull Map<String, Turnout> turnouts) {
        this.editor = editor;
        this.blocks = Collections.unmodifiableMap(new LinkedHashMap<>(blocks));
        this.turnouts = Collections.unmodifiableMap(new LinkedHashMap<>(turnouts));
    }

    /**
     * @return the Layout Editor panel the layout is drawn on
     */
    @Nonnull
    public LayoutEditor getEditor() {
        return editor;
    }

    /**
     * @param name a Block's name
     * @return the Block
     * @throws IllegalArgumentException if the layout has no Block of that name
     */
    @Nonnull
    public Block block(@Nonnull String name) {
        Block b = blocks.get(name);
        if (b == null) {
            throw new IllegalArgumentException("no Block called " + name + " in this layout");
        }
        return b;
    }

    /**
     * @param name a turnout's name, without the "IT"
     * @return the Turnout
     * @throws IllegalArgumentException if the layout has no turnout of that
     *                                  name
     */
    @Nonnull
    public Turnout turnout(@Nonnull String name) {
        Turnout t = turnouts.get(name);
        if (t == null) {
            throw new IllegalArgumentException("no turnout called " + name + " in this layout");
        }
        return t;
    }

    /**
     * @return the Blocks by name, in the order they were added
     */
    @Nonnull
    public Map<String, Block> getBlocks() {
        return blocks;
    }

    /**
     * @return the Turnouts by name, in the order they were added
     */
    @Nonnull
    public Map<String, Turnout> getTurnouts() {
        return turnouts;
    }

    /**
     * Close the panel, on the GUI thread. Call in a test's tear-down, after
     * {@link SimulationTestUtil#removeTrains()}.
     */
    public void dispose() {
        JUnitUtil.dispose(editor);
    }
}

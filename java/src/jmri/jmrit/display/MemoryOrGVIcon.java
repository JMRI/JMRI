package jmri.jmrit.display;

import jmri.jmrit.catalog.NamedIcon;

/**
 * An icon to display a status of a Memory.
 * <p>
 * The value of the memory can't be changed with this icon.
 *
 * @author Bob Jacobsen     Copyright (c) 2004
 * @author Daniel Bergqvist Copyright (C) 2022
 * @since 5.3.1
 */
public abstract class MemoryOrGVIcon extends PositionableLabel {

    public MemoryOrGVIcon(String s, Editor editor) {
        super(s, editor);
    }

    public MemoryOrGVIcon(NamedIcon s, Editor editor) {
        super(s, editor);
    }

    public abstract void displayState();

    public abstract int getOriginalX();

    public abstract int getOriginalY();

}

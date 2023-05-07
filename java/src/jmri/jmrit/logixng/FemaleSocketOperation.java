package jmri.jmrit.logixng;

import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * An enum with the values "is" and "is not"
 *
 * @author Daniel Bergqvist 2019
 */
public enum FemaleSocketOperation {

    Remove(Bundle.getMessage("FemaleSocketOperation_Remove"), KeyEvent.VK_R,
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() + InputEvent.SHIFT_DOWN_MASK),
    InsertBefore(Bundle.getMessage("FemaleSocketOperation_InsertBefore")),
    InsertAfter(Bundle.getMessage("FemaleSocketOperation_InsertAfter")),
    MoveUp(Bundle.getMessage("FemaleSocketOperation_MoveUp")),
    MoveDown(Bundle.getMessage("FemaleSocketOperation_MoveDown"));

    private final String _text;
    private final int _keyCode;
    private final int _modifiers;

    private FemaleSocketOperation(String text) {
        this._text = text;
        this._keyCode = 0;
        this._modifiers = 0;
    }

    private FemaleSocketOperation(String text, int keyCode, int modifiers) {
        this._text = text;
        this._keyCode = keyCode;
        this._modifiers = modifiers;
    }

    public boolean hasKey() {
        return _keyCode != 0;
    }

    public int getKeyCode() {
        return _keyCode;
    }

    public int getModifiers() {
        return _modifiers;
    }

    @Override
    public String toString() {
        return _text;
    }

}

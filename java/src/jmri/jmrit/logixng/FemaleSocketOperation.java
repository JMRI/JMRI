package jmri.jmrit.logixng;

/**
 * An enum with the values "is" and "is not"
 * 
 * @author Daniel Bergqvist 2019
 */
public enum FemaleSocketOperation {

    Remove(Bundle.getMessage("FemaleSocketOperation_Remove")),
    InsertBefore(Bundle.getMessage("FemaleSocketOperation_InsertBefore")),
    InsertAfter(Bundle.getMessage("FemaleSocketOperation_InsertAfter")),
    MoveUp(Bundle.getMessage("FemaleSocketOperation_MoveUp")),
    MoveDown(Bundle.getMessage("FemaleSocketOperation_MoveDown"));

    private final String _text;

    private FemaleSocketOperation(String text) {
        this._text = text;
    }

    @Override
    public String toString() {
        return _text;
    }
    
}

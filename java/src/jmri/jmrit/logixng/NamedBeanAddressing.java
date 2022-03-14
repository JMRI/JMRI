package jmri.jmrit.logixng;

/**
 * How should a named bean be addressed by an action or expression?
 *
 * @author Daniel Bergqvist Copyright 2020
 */
public enum NamedBeanAddressing {

    /**
     * Direct addressing, by entering the name of the named bean
     */
    Direct(Bundle.getMessage("NamedBeanAddressing_Direct")),

    /**
     * Addresssing by reference, by entering a reference that points to the named bean.
     */
    Reference(Bundle.getMessage("NamedBeanAddressing_Reference")),

    /**
     * Addresssing by local variable, by entering a local variable that points to the named bean.
     */
    LocalVariable(Bundle.getMessage("NamedBeanAddressing_LocalVariable")),

    /**
     * Addresssing by formula, by entering a formula that points to the named bean.
     */
    Formula(Bundle.getMessage("NamedBeanAddressing_Formula")),

    /**
     * Addresssing by formula, by entering a formula that points to the named bean.
     */
    Table(Bundle.getMessage("NamedBeanAddressing_Table"));

    private final String _text;

    private NamedBeanAddressing(String text) {
        this._text = text;
    }

    @Override
    public String toString() {
        return _text;
    }

}

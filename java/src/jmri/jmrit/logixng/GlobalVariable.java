package jmri.jmrit.logixng;

import jmri.JmriException;
import jmri.NamedBean;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;

/**
 * LogixNG Global Variable.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public interface GlobalVariable extends Base, NamedBean {

    /**
     * Initialize this global variable to the initial value.
     * @throws jmri.JmriException in case of an error
     */
    void initialize() throws JmriException;

    /**
     * Set the value.
     * @param value the value
     */
    void setValue(Object value);

    /**
     * Get the value.
     * @return the value
     */
    Object getValue();

    /**
     * Set the initial value type.
     * @param value the type
     */
    void setInitialValueType(InitialValueType value);

    /**
     * Get the initial value type.
     * @return the type
     */
    InitialValueType getInitialValueType();

    /**
     * Set the initial value.
     * @param value the value
     */
    void setInitialValueData(String value);

    /**
     * Get the initial value.
     * @return the value
     */
    String getInitialValueData();

}

package jmri.jmrit.logixng;

import java.util.Collection;

import jmri.NamedBean;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;
import jmri.jmrit.logixng.SymbolTable.VariableData;

/**
 * Represent a LogixNG module.
 * A module is similar to a ConditionalNG, except that it can be used by
 * both ConditionalNGs and modules.
 *
 * @author Daniel Bergqvist Copyright (C) 2020
 */
public interface Module extends Base, NamedBean {

    /**
     * Is the module visible to the user?
     * @return true if the module is visible, false otherwise.
     */
    boolean isVisible();

    /**
     * Makes the module visible or not visible to the user.
     * @param value true to make the module visible, false to make it invisible
     */
    void setVisible(boolean value);

    /**
     * Is the module stored in the tables and panels file if the module is empty?
     * @return true if it's always stored, false if it's not stored if empty
     */
    boolean isStoreIfEmpty();

    /**
     * Set whenether the module should be stored in the tables and panels file
     * if the module is empty.
     * @param value true if it's always stored, false if it's not stored if empty
     */
    void setStoreIfEmpty(boolean value);

    /**
     * Get the type of the root socket of the module.
     * @return the type
     */
    FemaleSocketManager.SocketType getRootSocketType();

    /**
     * Get the root socket of the module.
     * @return the root socket
     */
    FemaleSocket getRootSocket();

    /**
     * Set the current ConditionalNG of the module.
     * This method is called on all modules before a ConditionalNG is executed
     * to let the modules know which ConditionalNG is running in which thread.
     * 
     * @param conditionalNG the ConditionalNG
     */
    void setCurrentConditionalNG(ConditionalNG conditionalNG);

    void addParameter(String name, boolean isInput, boolean isOutput);

    void addParameter(Parameter parameter);

    void addLocalVariable(
            String name,
            SymbolTable.InitialValueType initialValueType,
            String initialValueData);

    Collection<Parameter> getParameters();

    Collection<VariableData> getLocalVariables();


    /**
     * The definition of a parameter.
     */
    interface Parameter {

        /**
         * The name of the parameter
         * @return the name
         */
        String getName();

        /**
         * Answer whenether or not the parameter is input to the module.
         * @return true if the parameter is input, false otherwise
         */
        boolean isInput();

        /**
         * Answer whenether or not the parameter is output to the module.
         * @return true if the parameter is output, false otherwise
         */
        boolean isOutput();

    }


    /**
     * Data for a parameter.
     */
    public static class ParameterData extends VariableData {

        public ReturnValueType _returnValueType = ReturnValueType.None;
        public String _returnValueData;

        public ParameterData(
                String name,
                InitialValueType initialValueType,
                String initialValueData,
                ReturnValueType returnValueType,
                String returnValueData) {

            super(name, initialValueType, initialValueData);

            _returnValueType = returnValueType;
            _returnValueData = returnValueData;
        }

        public ParameterData(ParameterData data) {
            super(data._name, data._initialValueType, data._initialValueData);
            _returnValueType = data._returnValueType;
            _returnValueData = data._returnValueData;
        }

        public ReturnValueType getReturnValueType() {
            return _returnValueType;
        }

        public String getReturnValueData() {
            return _returnValueData;
        }

    }


    /**
     * An enum that defines the types of initial value.
     */
    enum ReturnValueType {

        None(Bundle.getMessage("ReturnValueType_None")),
        LocalVariable(Bundle.getMessage("ReturnValueType_LocalVariable")),
        Memory(Bundle.getMessage("ReturnValueType_Memory"));

        private final String _descr;

        private ReturnValueType(String descr) {
            _descr = descr;
        }

        public String getDescr() {
            return _descr;
        }
    }


}

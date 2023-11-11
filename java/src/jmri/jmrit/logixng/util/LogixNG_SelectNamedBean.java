package jmri.jmrit.logixng.util;

import java.beans.*;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.AbstractBase;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.TypeConversionUtil;

/**
 * Select namedBean for LogixNG actions and expressions.
 *
 * @param <E> the type of the named bean
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class LogixNG_SelectNamedBean<E extends NamedBean> implements VetoableChangeListener {

    private final AbstractBase _base;
    private final InUse _inUse;
    private final Class<E> _class;
    private final Manager<E> _manager;
    private final LogixNG_SelectTable _selectTable;
    private final PropertyChangeListener _listener;
    private boolean _listenToMemory;
    private boolean _listenersAreRegistered;
    private boolean _onlyDirectAddressingAllowed;

    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private NamedBeanHandle<E> _handle;
    private String _reference = "";
    private NamedBeanHandle<Memory> _memoryHandle;
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;

    private String _delayedNamedBean;


    public LogixNG_SelectNamedBean(AbstractBase base, Class<E> clazz, Manager<E> manager, PropertyChangeListener listener) {
        _base = base;
        _inUse = () -> true;
        _class = clazz;
        _manager = manager;
        _selectTable = new LogixNG_SelectTable(_base, _inUse);
        _listener = listener;
    }

    public LogixNG_SelectNamedBean(AbstractBase base, Class<E> clazz, Manager<E> manager, InUse inUse, PropertyChangeListener listener) {
        _base = base;
        _inUse = inUse;
        _class = clazz;
        _manager = manager;
        _selectTable = new LogixNG_SelectTable(_base, _inUse);
        _listener = listener;
    }

    public void setOnlyDirectAddressingAllowed() {
        _onlyDirectAddressingAllowed = true;
    }

    public void copy(LogixNG_SelectNamedBean<E> copy) throws ParserException {
        copy.setAddressing(_addressing);
        if (_handle != null) copy.setNamedBean(_handle);
        copy.setLocalVariable(_localVariable);
        copy.setReference(_reference);
        copy.setMemory(_memoryHandle);
        copy.setListenToMemory(_listenToMemory);
        copy.setFormula(_formula);
        _selectTable.copy(copy._selectTable);
    }

    public Manager<E> getManager() {
        return _manager;
    }

    public void setAddressing(@Nonnull NamedBeanAddressing addressing) throws ParserException {
        if (_onlyDirectAddressingAllowed && (addressing != NamedBeanAddressing.Direct)) {
            throw new IllegalArgumentException("Addressing must be Direct");
        }
        this._addressing = addressing;
        parseFormula();
    }

    public boolean isDirectAddressing() {
        return _addressing == NamedBeanAddressing.Direct;
    }

    public NamedBeanAddressing getAddressing() {
        return _addressing;
    }

    public void setDelayedNamedBean(@Nonnull String name) {
        _delayedNamedBean = name;
    }

    public void setup() {
        if (_delayedNamedBean != null) setNamedBean(_delayedNamedBean);
    }

    public void setNamedBean(@Nonnull String name) {
        _base.assertListenersAreNotRegistered(log, "setNamedBean");
        E namedBean = _manager.getNamedBean(name);
        if (namedBean != null) {
            setNamedBean(name, namedBean);
        } else {
            removeNamedBean();
            log.error("{} \"{}\" is not found", _manager.getBeanTypeHandled(), name);
        }
    }

    public void setNamedBean(@Nonnull NamedBeanHandle<E> handle) {
        _base.assertListenersAreNotRegistered(log, "setNamedBean");
        _handle = handle;
        _manager.addVetoableChangeListener(this);
    }

    public void setNamedBean(@Nonnull E namedBean) {
        setNamedBean(namedBean.getDisplayName(), namedBean);
    }

    public void setNamedBean(@Nonnull String name, @Nonnull E namedBean) {
        _base.assertListenersAreNotRegistered(log, "setNamedBean");
        setNamedBean(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(name, namedBean));
    }

    public void removeNamedBean() {
        _base.assertListenersAreNotRegistered(log, "setNamedBean");
        if (_handle != null) {
            _manager.removeVetoableChangeListener(this);
            _handle = null;
        }
    }

    public E getBean() {
        if (_handle != null) {
            return _handle.getBean();
        } else {
            return null;
        }
    }

    public NamedBeanHandle<E> getNamedBean() {
        return _handle;
    }

    public E getNamedBeanIfDirectAddressing() {
        if ((_handle != null) && (this._addressing == NamedBeanAddressing.Direct)) {
            return _handle.getBean();
        }
        return null;
    }

    public void setReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _reference = reference;
    }

    public String getReference() {
        return _reference;
    }

    public void setMemory(@Nonnull String memoryName) {
        Memory memory = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName);
        if (memory != null) {
            setMemory(memory);
        } else {
            removeMemory();
            log.warn("memory \"{}\" is not found", memoryName);
        }
    }

    public void setMemory(@Nonnull NamedBeanHandle<Memory> handle) {
        _memoryHandle = handle;
        InstanceManager.memoryManagerInstance().addVetoableChangeListener(this);
        addRemoveVetoListener();
    }

    public void setMemory(@Nonnull Memory memory) {
        setMemory(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(memory.getDisplayName(), memory));
    }

    public void removeMemory() {
        if (_memoryHandle != null) {
            _memoryHandle = null;
            addRemoveVetoListener();
        }
    }

    public NamedBeanHandle<Memory> getMemory() {
        return _memoryHandle;
    }

    public void setListenToMemory(boolean listenToMemory) {
        _listenToMemory = listenToMemory;
    }

    public boolean getListenToMemory() {
        return _listenToMemory;
    }

    public void setLocalVariable(@Nonnull String localVariable) {
        _localVariable = localVariable;
    }

    public String getLocalVariable() {
        return _localVariable;
    }

    public void setFormula(@Nonnull String formula) throws ParserException {
        _formula = formula;
        parseFormula();
    }

    public String getFormula() {
        return _formula;
    }

    private void parseFormula() throws ParserException {
        if (_addressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _expressionNode = parser.parseExpression(_formula);
        } else {
            _expressionNode = null;
        }
    }

    public LogixNG_SelectTable getSelectTable() {
        return _selectTable;
    }

    private void addRemoveVetoListener() {
        if (_memoryHandle != null) {
            InstanceManager.getDefault(MemoryManager.class).addVetoableChangeListener(this);
        } else {
            InstanceManager.getDefault(MemoryManager.class).removeVetoableChangeListener(this);
        }
    }

    public E evaluateNamedBean(ConditionalNG conditionalNG) throws JmriException {

        if (_addressing == NamedBeanAddressing.Direct) {
            return _handle != null ? _handle.getBean() : null;
        } else {
            String name;

            switch (_addressing) {
                case Reference:
                    name = ReferenceUtil.getReference(
                            conditionalNG.getSymbolTable(), _reference);
                    break;

                case LocalVariable:
                    SymbolTable symbolNamedBean = conditionalNG.getSymbolTable();
                    name = TypeConversionUtil
                            .convertToString(symbolNamedBean.getValue(_localVariable), false);
                    break;

                case Memory:
                    name = TypeConversionUtil
                            .convertToString(_memoryHandle.getBean().getValue(), false);
                    break;

                case Formula:
                    name = _expressionNode  != null
                            ? TypeConversionUtil.convertToString(
                                    _expressionNode.calculate(
                                            conditionalNG.getSymbolTable()), false)
                            : null;
                    break;

                case Table:
                    name = TypeConversionUtil.convertToString(
                            _selectTable.evaluateTableData(conditionalNG), false);
                    break;

                default:
                    throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
            }

            E namedBean = null;
            if (name != null) {
                namedBean = _manager.getNamedBean(name);
            }
            return namedBean;
        }
    }

    public String getDescription(Locale locale) {
        String namedBean;

        String memoryName;
        if (_memoryHandle != null) {
            memoryName = _memoryHandle.getName();
        } else {
            memoryName = Bundle.getMessage(locale, "BeanNotSelected");
        }

        switch (_addressing) {
            case Direct:
                String namedBeanName;
                if (_handle != null) {
                    namedBeanName = _handle.getBean().getDisplayName();
                } else {
                    namedBeanName = Bundle.getMessage(locale, "BeanNotSelected");
                }
                namedBean = Bundle.getMessage(locale, "AddressByDirect", namedBeanName);
                break;

            case Reference:
                namedBean = Bundle.getMessage(locale, "AddressByReference", _reference);
                break;

            case Memory:
                namedBean = Bundle.getMessage(locale, "AddressByMemory_Listen", memoryName, Base.getListenString(_listenToMemory));
                break;

            case LocalVariable:
                namedBean = Bundle.getMessage(locale, "AddressByLocalVariable", _localVariable);
                break;

            case Formula:
                namedBean = Bundle.getMessage(locale, "AddressByFormula", _formula);
                break;

            case Table:
                namedBean = Bundle.getMessage(
                        locale,
                        "AddressByTable",
                        _selectTable.getTableNameDescription(locale),
                        _selectTable.getTableRowDescription(locale),
                        _selectTable.getTableColumnDescription(locale));
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing: " + _addressing.name());
        }
        return namedBean;
    }

    /**
     * Register listeners if this object needs that.
     */
    public void registerListeners() {
        if (!_listenersAreRegistered
                && (_addressing == NamedBeanAddressing.Memory)
                && (_memoryHandle != null)
                && _listenToMemory) {
            _memoryHandle.getBean().addPropertyChangeListener("value", _listener);
            _listenersAreRegistered = true;
        }
    }

    /**
     * Unregister listeners if this object needs that.
     */
    public void unregisterListeners() {
        if (_listenersAreRegistered
                && (_addressing == NamedBeanAddressing.Memory)
                && (_memoryHandle != null)
                && _listenToMemory) {
            _memoryHandle.getBean().removePropertyChangeListener("value", _listener);
            _listenersAreRegistered = false;
        }
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName()) && _inUse.isInUse()) { // No I18N
            if (_inUse.isInUse() && (_class.isAssignableFrom(evt.getOldValue().getClass()))) {
                if (evt.getOldValue().equals(getNamedBean().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("InUseVeto", _base.getDisplayName(), _base.getShortDescription()), e);
                }
            }
            if (evt.getOldValue() instanceof Memory) {
                boolean doVeto = false;
                if ((_addressing == NamedBeanAddressing.Memory) && (_memoryHandle != null) && evt.getOldValue().equals(_memoryHandle.getBean())) {
                    doVeto = true;
                }
                if (doVeto) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("MemoryInUseMemoryExpressionVeto", _base.getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (_class.isAssignableFrom(evt.getOldValue().getClass())) {
                if (evt.getOldValue().equals(getNamedBean().getBean())) {
                    removeNamedBean();
                }
            }
            if (evt.getOldValue() instanceof Memory) {
                if ((_memoryHandle != null) && evt.getOldValue().equals(_memoryHandle.getBean())) {
                    removeMemory();
                }
            }
        }
    }

    /**
     * Add a {@link java.beans.PropertyChangeListener} for a specific property.
     *
     * @param listener The PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(
            @CheckForNull PropertyChangeListener listener) {
        if ((_addressing == NamedBeanAddressing.Direct) && (_handle != null)) {
            _handle.getBean().addPropertyChangeListener(listener);
        }
    }

    /**
     * Add a {@link java.beans.PropertyChangeListener} for a specific property.
     *
     * @param propertyName The name of the property to listen on.
     * @param listener     The PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(
            @CheckForNull String propertyName,
            @CheckForNull PropertyChangeListener listener) {
        if ((_addressing == NamedBeanAddressing.Direct) && (_handle != null)) {
            _handle.getBean().addPropertyChangeListener(propertyName, listener);
        }
    }

    /**
     * Remove the specified listener of the specified property from this object.
     *
     * @param listener The {@link java.beans.PropertyChangeListener} to
     *                 remove.
     */
    public void removePropertyChangeListener(
            @CheckForNull PropertyChangeListener listener) {
        if (_handle != null) {
            _handle.getBean().removePropertyChangeListener(listener);
        }
    }

    /**
     * Remove the specified listener of the specified property from this object.
     *
     * @param propertyName The name of the property to stop listening to.
     * @param listener     The {@link java.beans.PropertyChangeListener} to
     *                     remove.
     */
    public void removePropertyChangeListener(
            @CheckForNull String propertyName,
            @CheckForNull PropertyChangeListener listener) {
        if (_handle != null) {
            _handle.getBean().removePropertyChangeListener(propertyName, listener);
        }
    }

    public void getUsageDetail(int level, NamedBean bean, List<NamedBeanUsageReport> report, NamedBean cdl, Base base, Type type) {
        log.debug("getUsageReport :: {}: bean = {}, report = {}", base.getShortDescription(), cdl, report);
        if (_handle != null && bean.equals(_handle.getBean())) {
            report.add(new NamedBeanUsageReport(type.toString(), cdl, base.getLongDescription()));
        }
    }

    public static enum Type {
        Action("LogixNGAction"),
        Expression("LogixNGExpression");

        private final String _descr;

        private Type(String descr) {
            this._descr = descr;
        }

        @Override
        public String toString() {
            return _descr;
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNG_SelectNamedBean.class);
}

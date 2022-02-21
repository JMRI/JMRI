package jmri.jmrit.logixng.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

    public static interface InUse {
        public boolean isInUse();
    }

    private final AbstractBase _base;
    private final Class<E> _class;
    private final Manager<E> _manager;
    private final InUse _inUse;

    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private NamedBeanHandle<E> _handle;
    private String _reference = "";
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;


    public LogixNG_SelectNamedBean(AbstractBase base, Class<E> clazz, Manager<E> manager) {
        _base = base;
        _inUse = () -> true;
        _class = clazz;
        _manager = manager;
    }

    public LogixNG_SelectNamedBean(AbstractBase base, Class<E> clazz, Manager<E> manager, InUse inUse) {
        _base = base;
        _inUse = inUse;
        _class = clazz;
        _manager = manager;
    }


    public void copy(LogixNG_SelectNamedBean<E> copy) throws ParserException {
        copy.setAddressing(_addressing);
        if (_handle != null) copy.setNamedBean(_handle);
        copy.setLocalVariable(_localVariable);
        copy.setReference(_reference);
        copy.setFormula(_formula);
    }

    public Manager<E> getManager() {
        return _manager;
    }

    public void setAddressing(@Nonnull NamedBeanAddressing addressing) throws ParserException {
        this._addressing = addressing;
        parseFormula();
    }

    public NamedBeanAddressing getAddressing() {
        return _addressing;
    }

    public void setNamedBean(@Nonnull String name) {
        _base.assertListenersAreNotRegistered(log, "setNamedBean");
        E namedBean = _manager.getNamedBean(name);
        if (namedBean != null) {
            setNamedBean(namedBean);
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
        _base.assertListenersAreNotRegistered(log, "setNamedBean");
        setNamedBean(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(namedBean.getDisplayName(), namedBean));
    }

    public void removeNamedBean() {
        _base.assertListenersAreNotRegistered(log, "setNamedBean");
        if (_handle != null) {
            _manager.removeVetoableChangeListener(this);
            _handle = null;
        }
    }

    public NamedBeanHandle<E> getNamedBean() {
        return _handle;
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



    public E getNamedBean(ConditionalNG conditionalNG) throws JmriException {

        System.out.format("getNamedBean: Addr: %s%n", _addressing.name());
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

                case Formula:
                    name = _expressionNode  != null
                            ? TypeConversionUtil.convertToString(
                                    _expressionNode.calculate(
                                            conditionalNG.getSymbolTable()), false)
                            : null;
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

            case LocalVariable:
                namedBean = Bundle.getMessage(locale, "AddressByLocalVariable", _localVariable);
                break;

            case Formula:
                namedBean = Bundle.getMessage(locale, "AddressByFormula", _formula);
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing: " + _addressing.name());
        }
        return namedBean;
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (_inUse.isInUse() && (_class.isAssignableFrom(evt.getOldValue().getClass()))) {
                if (evt.getOldValue().equals(getNamedBean().getBean())) {
                    throw new PropertyVetoException(_base.getDisplayName(), evt);
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (_class.isAssignableFrom(evt.getOldValue().getClass())) {
                if (evt.getOldValue().equals(getNamedBean().getBean())) {
                    removeNamedBean();
                }
            }
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNG_SelectNamedBean.class);
}

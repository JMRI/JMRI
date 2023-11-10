package jmri.jmrit.logixng.util;

import java.beans.PropertyChangeListener;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.AbstractBase;
import jmri.jmrit.logixng.util.parser.ParserException;

/**
 * Select a charset for LogixNG actions and expressions.
 *
 * @author Daniel Bergqvist (C) 2023
 */
public class LogixNG_SelectCharset {

    public static final List<Charset> STANDARD_CHARSETS = getStandardCharsets();

    private final AbstractBase _base;
    private final InUse _inUse;
//    private final PropertyChangeListener _listener;

    private Addressing _addressing = Addressing.Standard;
    private Charset _standardValue = StandardCharsets.UTF_8;
    private Charset _allValue = StandardCharsets.UTF_8;
    private final LogixNG_SelectString _selectUserSpecifiedCharset;


    public LogixNG_SelectCharset(AbstractBase base, PropertyChangeListener listener) {
        _base = base;
        _inUse = () -> _addressing == Addressing.UserSpecified;
//        _listener = listener;
        _selectUserSpecifiedCharset = new LogixNG_SelectString(base, _inUse, listener);
    }

    private static List<Charset> getStandardCharsets() {
        List<Charset> standardCharsets = new ArrayList<>();
        standardCharsets.add(StandardCharsets.US_ASCII);
        standardCharsets.add(StandardCharsets.ISO_8859_1);
        standardCharsets.add(StandardCharsets.UTF_8);
        standardCharsets.add(StandardCharsets.UTF_16);
        standardCharsets.add(StandardCharsets.UTF_16BE);
        standardCharsets.add(StandardCharsets.UTF_16LE);
        return Collections.unmodifiableList(standardCharsets);
    }

    public void copy(LogixNG_SelectCharset copy) throws ParserException {
        copy.setAddressing(_addressing);
        copy.setStandardValue(_standardValue);
        copy.setAllValue(_standardValue);
        _selectUserSpecifiedCharset.copy(copy._selectUserSpecifiedCharset);
    }

    public void setAddressing(@Nonnull Addressing addressing) {
        this._addressing = addressing;
    }

    public Addressing getAddressing() {
        return _addressing;
    }

    public void setStandardValue(@Nonnull Charset charset) {
        _base.assertListenersAreNotRegistered(log, "setStandardValue");
        _standardValue = charset;
    }

    public Charset getStandardValue() {
        return _standardValue;
    }

    public void setAllValue(@Nonnull Charset charset) {
        _base.assertListenersAreNotRegistered(log, "setListValue");
        _allValue = charset;
    }

    public Charset getAllValue() {
        return _allValue;
    }

    public LogixNG_SelectString getSelectUserSpecified() {
        return _selectUserSpecifiedCharset;
    }

    public Charset evaluateCharset(ConditionalNG conditionalNG) throws JmriException {

        switch (_addressing) {
            case Standard:
                return _standardValue;

            case All:
                return _standardValue;

            case UserSpecified:
                String charsetName = _selectUserSpecifiedCharset.evaluateValue(conditionalNG);
                return Charset.forName(charsetName);

            default:
                throw new IllegalArgumentException("_addressing has unknown value: "+_addressing.name());
        }
    }

    public String getDescription(Locale locale) {
        return "ERROR !!!!";
/*
        String enumName;

        String memoryName;
        if (_memoryHandle != null) {
            memoryName = _memoryHandle.getName();
        } else {
            memoryName = Bundle.getMessage(locale, "BeanNotSelected");
        }

        switch (_addressing) {
            case Direct:
                enumName = Bundle.getMessage(locale, "AddressByDirect", _value);
                break;

            case Reference:
                enumName = Bundle.getMessage(locale, "AddressByReference", _reference);
                break;

            case Memory:
                enumName = Bundle.getMessage(locale, "AddressByMemory_Listen", memoryName, Base.getListenString(_listenToMemory));
                break;

            case LocalVariable:
                enumName = Bundle.getMessage(locale, "AddressByLocalVariable", _localVariable);
                break;

            case Formula:
                enumName = Bundle.getMessage(locale, "AddressByFormula", _formula);
                break;

            case Table:
                enumName = Bundle.getMessage(
                        locale,
                        "AddressByTable",
                        _selectTable.getTableNameDescription(locale),
                        _selectTable.getTableRowDescription(locale),
                        _selectTable.getTableColumnDescription(locale));
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing: " + _addressing.name());
        }
        return enumName;
*/
    }

    /**
     * Register listeners if this object needs that.
     */
    public void registerListeners() {
    }

    /**
     * Unregister listeners if this object needs that.
     */
    public void unregisterListeners() {
    }


    public enum Addressing {

        Standard(Bundle.getMessage("LogixNG_SelectCharset_Addressing_Standard")),
        All(Bundle.getMessage("LogixNG_SelectCharset_Addressing_All")),
        UserSpecified(Bundle.getMessage("LogixNG_SelectCharset_Addressing_UserSpecified"));

        private final String _text;

        private Addressing(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNG_SelectCharset.class);
}

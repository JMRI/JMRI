package jmri.jmrit.logixng.actions;

import java.beans.*;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.script.ScriptOutput;

/**
 * This action logs some data.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class LogData extends AbstractDigitalAction
        implements PropertyChangeListener, VetoableChangeListener {

    private boolean _logToLog = true;
    private boolean _logToScriptOutput = false;
    private FormatType _formatType = FormatType.OnlyText;
    private String _format = "";
    private final List<Data> _dataList = new ArrayList<>();

    public LogData(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        LogData copy = new LogData(sysName, userName);
        copy.setComment(getComment());
        copy.setLogToLog(_logToLog);
        copy.setLogToScriptOutput(_logToScriptOutput);
        copy.setFormat(_format);
        copy.setFormatType(_formatType);
        for (Data data : _dataList) {
            copy.getDataList().add(new Data(data));
        }
        return manager.registerAction(copy);
    }

    public void setLogToLog(boolean logToLog) {
        _logToLog = logToLog;
    }

    public boolean getLogToLog() {
        return _logToLog;
    }

    public void setLogToScriptOutput(boolean logToScriptOutput) {
        _logToScriptOutput = logToScriptOutput;
    }

    public boolean getLogToScriptOutput() {
        return _logToScriptOutput;
    }

    public void setFormatType(FormatType formatType) {
        _formatType = formatType;
    }

    public FormatType getFormatType() {
        return _formatType;
    }

    public void setFormat(String format) {
        _format = format;
    }

    public String getFormat() {
        return _format;
    }

    public List<Data> getDataList() {
        return _dataList;
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
/*
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(getMemory().getBean())) {
                    throw new PropertyVetoException(getDisplayName(), evt);
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(getMemory().getBean())) {
                    setMemory((Memory)null);
                }
            }
        }
*/
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.OTHER;
    }

    private List<Object> getDataValues() throws JmriException {
        List<Object> values = new ArrayList<>();
        for (Data _data : _dataList) {
            switch (_data._dataType) {
                case LocalVariable:
                    values.add(getConditionalNG().getSymbolTable().getValue(_data._data));
                    break;

                case Memory:
                    MemoryManager memoryManager = InstanceManager.getDefault(MemoryManager.class);
                    Memory memory = memoryManager.getMemory(_data._data);
                    if (memory == null) throw new IllegalArgumentException("Memory '" + _data._data + "' not found");
                    values.add(memory.getValue());
                    break;

                case Reference:
                    values.add(ReferenceUtil.getReference(
                            getConditionalNG().getSymbolTable(), _data._data));
                    break;

                case Formula:
                    if (_data._expressionNode != null) {
                        values.add(_data._expressionNode.calculate(getConditionalNG().getSymbolTable()));
                    }
                    
                    break;

                default:
                    throw new IllegalArgumentException("_formatType has invalid value: "+_formatType.name());
            }
        }
        return values;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {

        String str;

        switch (_formatType) {
            case OnlyText:
                str = _format;
                break;

            case CommaSeparatedList:
                StringBuilder sb = new StringBuilder();
                for (Object value : getDataValues()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(value != null ? value.toString() : "null");
                }
                str = sb.toString();
                break;

            case StringFormat:
                str = String.format(_format, getDataValues().toArray());
                break;

            default:
                throw new IllegalArgumentException("_formatType has invalid value: "+_formatType.name());
        }

        if (_logToLog) log.warn(str);
        if (_logToScriptOutput) ScriptOutput.getDefault().getOutputArea().append(str+"\n");
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "LogData_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String bundleKey;
        switch (_formatType) {
            case OnlyText:
                bundleKey = "LogData_Long_TextOnly";
                break;
            case CommaSeparatedList:
                bundleKey = "LogData_Long_CommaSeparatedList";
                break;
            case StringFormat:
                bundleKey = "LogData_Long_StringFormat";
                break;
            default:
                throw new RuntimeException("_formatType has unknown value: "+_formatType.name());
        }
        return Bundle.getMessage(locale, bundleKey, _format);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }


    /** {@inheritDoc} */
    @Override
    public void getUsageDetail(int level, NamedBean bean, List<NamedBeanUsageReport> report, NamedBean cdl) {
/*        
        log.debug("getUsageReport :: LogData: bean = {}, report = {}", cdl, report);
        for (NamedBeanReference namedBeanReference : _namedBeanReferences.values()) {
            if (namedBeanReference._handle != null) {
                if (bean.equals(namedBeanReference._handle.getBean())) {
                    report.add(new NamedBeanUsageReport("LogixNGAction", cdl, getLongDescription()));
                }
            }
        }
*/
    }


    public enum FormatType {
        OnlyText(Bundle.getMessage("LogData_FormatType_TextOnly"), true, false),
        CommaSeparatedList(Bundle.getMessage("LogData_FormatType_CommaSeparatedList"), false, true),
        StringFormat(Bundle.getMessage("LogData_FormatType_StringFormat"), true, true);

        private final String _text;
        private final boolean _useFormat;
        private final boolean _useData;

        private FormatType(String text, boolean useFormat, boolean useData) {
            this._text = text;
            this._useFormat = useFormat;
            this._useData = useData;
        }

        @Override
        public String toString() {
            return _text;
        }

        public boolean getUseFormat() {
            return _useFormat;
        }

        public boolean getUseData() {
            return _useData;
        }

    }


    public enum DataType {
        LocalVariable(Bundle.getMessage("LogData_Operation_LocalVariable")),
        Memory(Bundle.getMessage("LogData_Operation_Memory")),
        Reference(Bundle.getMessage("LogData_Operation_Reference")),
        Formula(Bundle.getMessage("LogData_Operation_Formula"));

        private final String _text;

        private DataType(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }


    public static class Data {

        private DataType _dataType = DataType.LocalVariable;
        private String _data = "";
        private ExpressionNode _expressionNode;

        public Data(Data data) throws ParserException {
            _dataType = data._dataType;
            _data = data._data;
            calculateFormula();
        }

        public Data(DataType dataType, String data) throws ParserException {
            _dataType = dataType;
            _data = data;
            calculateFormula();
        }

        private void calculateFormula() throws ParserException {
            if (_dataType == DataType.Formula) {
                Map<String, Variable> variables = new HashMap<>();
                RecursiveDescentParser parser = new RecursiveDescentParser(variables);
                _expressionNode = parser.parseExpression(_data);
            } else {
                _expressionNode = null;
            }
        }

        public void setDataType(DataType dataType) { _dataType = dataType; }
        public DataType getDataType() { return _dataType; }

        public void setData(String data) { _data = data; }
        public String getData() { return _data; }

    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogData.class);

}

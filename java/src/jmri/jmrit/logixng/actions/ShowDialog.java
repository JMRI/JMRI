package jmri.jmrit.logixng.actions;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.beans.*;
import java.util.*;

import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.ThreadingUtil;
import jmri.util.TypeConversionUtil;

/**
 * This action show a dialog.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ShowDialog extends AbstractDigitalAction
        implements FemaleSocketListener, PropertyChangeListener, VetoableChangeListener {

    private static final ResourceBundle rbx =
            ResourceBundle.getBundle("jmri.jmrit.logixng.implementation.ImplementationBundle");

    private String _socketSystemName;
    private final FemaleDigitalActionSocket _socket;
    private Set<Button> _enabledButtons = new HashSet<>();
    private String _localVariableForSelectedButton = "";
    private String _localVariableForInputString = "";
    private boolean _modal = true;
    private boolean _multiLine = false;
    private FormatType _formatType = FormatType.OnlyText;
    private String _format = "";
    private final List<Data> _dataList = new ArrayList<>();
    private final InternalFemaleSocket _internalSocket = new InternalFemaleSocket();
    private JDialog _dialog;


    public ShowDialog(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        _socket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, "A");
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ShowDialog copy = new ShowDialog(sysName, userName);
        copy.setComment(getComment());
        for (Button button : _enabledButtons) {
            copy.getEnabledButtons().add(button);
        }
        copy.setLocalVariableForSelectedButton(_localVariableForSelectedButton);
        copy.setLocalVariableForInputString(_localVariableForInputString);
        copy.setModal(_modal);
        copy.setMultiLine(_multiLine);
        copy.setFormat(_format);
        copy.setFormatType(_formatType);
        for (Data data : _dataList) {
            copy.getDataList().add(new Data(data));
        }
        return manager.registerAction(copy);
    }

    /**
     * Return the list of buttons.
     * @return the list of buttons.
     */
    public Set<Button> getEnabledButtons() {
        return this._enabledButtons;
    }

    public void setModal(boolean modal) {
        _modal = modal;
    }

    public boolean getModal() {
        return _modal;
    }

    public void setMultiLine(boolean multiLine) {
        _multiLine = multiLine;
    }

    public boolean getMultiLine() {
        return _multiLine;
    }

    public void setLocalVariableForSelectedButton(String localVariable) {
        _localVariableForSelectedButton = localVariable;
    }

    public String getLocalVariableForSelectedButton() {
        return _localVariableForSelectedButton;
    }

    public void setLocalVariableForInputString(String localVariableForInputString) {
        _localVariableForInputString = localVariableForInputString;
    }

    public String getLocalVariableForInputString() {
        return _localVariableForInputString;
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
        String strMultiLine;

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

        final ConditionalNG conditionalNG = getConditionalNG();
        final DefaultSymbolTable newSymbolTable = new DefaultSymbolTable(conditionalNG.getSymbolTable());

        if (_multiLine) strMultiLine = "<html>" + str + "</html>";
        else strMultiLine = str;

        ThreadingUtil.runOnGUIEventually(() -> {

            if (_dialog != null) _dialog.dispose();

            _dialog = new JDialog(
                    (JFrame)null,
                    Bundle.getMessage("ShowDialog_Title"),
                    _modal);

            _dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    _dialog = null;
                }
            });

            JPanel panel = new JPanel();
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            _dialog.getContentPane().add(panel);

            panel.add(new JLabel(strMultiLine));

            JTextField textField = new JTextField(20);
            if (!_localVariableForInputString.isEmpty()) {
                Object currentValue = newSymbolTable.getValue(_localVariableForInputString);
                if (currentValue != null) {
                    String strValue = TypeConversionUtil.convertToString(currentValue, false);
                    textField.setText(strValue);
                }
                panel.add(textField);
            }

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout());

            for (Button button : Button.values()) {
                if (_enabledButtons.contains(button)) {
                    JButton jbutton = new JButton(button._text);
                    jbutton.addActionListener((ActionEvent e) -> {
                        _dialog.dispose();

                        synchronized(ShowDialog.this) {
                            _internalSocket.conditionalNG = conditionalNG;
                            _internalSocket.newSymbolTable = newSymbolTable;
                            _internalSocket.selectedButton = button._value;
                            _internalSocket.inputValue = textField.getText();
                            conditionalNG.execute(_internalSocket);
                        }
                    });
                    buttonPanel.add(jbutton);
                }
            }
            panel.add(buttonPanel);

            _dialog.pack();
            _dialog.setLocationRelativeTo(null);
            _dialog.setVisible(true);
        });
    }
    
    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        switch (index) {
            case 0:
                return _socket;
                
            default:
                throw new IllegalArgumentException(
                        String.format("index has invalid value: %d", index));
        }
    }

    @Override
    public int getChildCount() {
        return 1;
    }

    @Override
    public void connected(FemaleSocket socket) {
        if (socket == _socket) {
            _socketSystemName = socket.getConnectedSocket().getSystemName();
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        if (socket == _socket) {
            _socketSystemName = null;
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "ShowDialog_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String bundleKey;
        switch (_formatType) {
            case OnlyText:
                bundleKey = "ShowDialog_Long_TextOnly";
                break;
            case CommaSeparatedList:
                bundleKey = "ShowDialog_Long_CommaSeparatedList";
                break;
            case StringFormat:
                bundleKey = "ShowDialog_Long_StringFormat";
                break;
            default:
                throw new RuntimeException("_formatType has unknown value: "+_formatType.name());
        }
        return Bundle.getMessage(locale, bundleKey, _format);
    }

    public FemaleDigitalActionSocket getSocket() {
        return _socket;
    }

    public String getSocketSystemName() {
        return _socketSystemName;
    }

    public void setSocketSystemName(String systemName) {
        _socketSystemName = systemName;
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        try {
            if (!_socket.isConnected()
                    || !_socket.getConnectedSocket().getSystemName()
                            .equals(_socketSystemName)) {
                
                String socketSystemName = _socketSystemName;
                
                _socket.disconnect();
                
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalActionManager.class)
                                    .getBySystemName(socketSystemName);
                    if (maleSocket != null) {
                        _socket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load analog action " + socketSystemName);
                    }
                }
            } else {
                _socket.getConnectedSocket().setup();
            }
        } catch (SocketAlreadyConnectedException ex) {
            // This shouldn't happen and is a runtime error if it does.
            throw new RuntimeException("socket is already connected");
        }
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
        log.debug("getUsageReport :: ShowDialog: bean = {}, report = {}", cdl, report);
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
        OnlyText(Bundle.getMessage("ShowDialog_FormatType_TextOnly"), true, false),
        CommaSeparatedList(Bundle.getMessage("ShowDialog_FormatType_CommaSeparatedList"), false, true),
        StringFormat(Bundle.getMessage("ShowDialog_FormatType_StringFormat"), true, true);

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


    public enum Button {
        Ok(1, Bundle.getMessage("ButtonOK")),
        Cancel(2, Bundle.getMessage("ButtonCancel")),
        Yes(3, Bundle.getMessage("ButtonYes")),
        No(4, Bundle.getMessage("ButtonNo"));

        private final int _value;
        private final String _text;

        private Button(int value, String text) {
            this._value = value;
            this._text = text;
        }

        public int getValue() {
            return _value;
        }

        @Override
        public String toString() {
            return _text;
        }

    }


    public enum DataType {
        LocalVariable(Bundle.getMessage("ShowDialog_Operation_LocalVariable")),
        Memory(Bundle.getMessage("ShowDialog_Operation_Memory")),
        Reference(Bundle.getMessage("ShowDialog_Operation_Reference")),
        Formula(Bundle.getMessage("ShowDialog_Operation_Formula"));

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
            if (dataType != null) {
                _dataType = dataType;
            } else {
                // Sometimes data entered in a JTable is not updated correctly
                log.warn("dataType is null");
            }
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

        public void setDataType(DataType dataType) {
            if (dataType != null) {
                _dataType = dataType;
            } else {
                // Sometimes data entered in a JTable is not updated correctly
                log.warn("dataType is null");
            }
        }

        public DataType getDataType() { return _dataType; }

        public void setData(String data) { _data = data; }
        public String getData() { return _data; }

    }


    private class InternalFemaleSocket extends jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket {

        private ConditionalNG conditionalNG;
        private SymbolTable newSymbolTable;
        private int selectedButton;
        private String inputValue;

        public InternalFemaleSocket() {
            super(null, new FemaleSocketListener(){
                @Override
                public void connected(FemaleSocket socket) {
                    // Do nothing
                }

                @Override
                public void disconnected(FemaleSocket socket) {
                    // Do nothing
                }
            }, "A");
        }

        @Override
        public void execute() throws JmriException {
            if (_socket != null) {
                MaleSocket maleSocket = (MaleSocket)ShowDialog.this.getParent();
                try {
                    SymbolTable oldSymbolTable = conditionalNG.getSymbolTable();
                    conditionalNG.setSymbolTable(newSymbolTable);
                    if (!_localVariableForSelectedButton.isEmpty()) {
                        newSymbolTable.setValue(_localVariableForSelectedButton, selectedButton);
                    }
                    if (!_localVariableForInputString.isEmpty()) {
                        newSymbolTable.setValue(_localVariableForInputString, inputValue);
                    }
                    _socket.execute();
                    conditionalNG.setSymbolTable(oldSymbolTable);
                } catch (JmriException e) {
                    if (e.getErrors() != null) {
                        maleSocket.handleError(ShowDialog.this, rbx.getString("ExceptionExecuteMulti"), e.getErrors(), e, log);
                    } else {
                        maleSocket.handleError(ShowDialog.this, Bundle.formatMessage(rbx.getString("ExceptionExecuteAction"), e.getLocalizedMessage()), e, log);
                    }
                } catch (RuntimeException e) {
                    maleSocket.handleError(ShowDialog.this, Bundle.formatMessage(rbx.getString("ExceptionExecuteAction"), e.getLocalizedMessage()), e, log);
                }
            }
        }

    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ShowDialog.class);

}

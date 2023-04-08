package jmri.jmrit.logixng.actions.swing;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.table.TableColumn;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;
import jmri.jmrit.logixng.actions.WebRequest;
import jmri.jmrit.logixng.actions.WebRequest.RequestMethodType;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectCharsetSwing;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectEnumSwing;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectStringSwing;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Configures an WebRequest object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright (C) 2021
 */
public class WebRequestSwing extends AbstractDigitalActionSwing {

    private JTabbedPane _tabbedPane;
    private JPanel _panelUrl;
    private JPanel _panelCharset;
    private JPanel _panelRequestMethod;
    private JPanel _panelUserAgent;

    private LogixNG_SelectStringSwing _selectUrlSwing;
    private LogixNG_SelectCharsetSwing _selectCharsetSwing;
    private LogixNG_SelectEnumSwing<RequestMethodType> _selectRequestMethodSwing;
    private LogixNG_SelectStringSwing _selectUserAgentSwing;

    private JTextField _localVariableForResponseCodeTextField;
    private JTextField _localVariableForReplyContentTextField;
    private JTextField _localVariableForCookiesTextField;

    private JTable _logDataTable;
    private WebRequestTableModel _logDataTableModel;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof WebRequest)) {
            throw new IllegalArgumentException("object must be an WebRequest but is a: "+object.getClass().getName());
        }

        _tabbedPane = new JTabbedPane();
        _panelUrl = new JPanel();
        _panelCharset = new JPanel();
        _panelRequestMethod = new JPanel();
        _panelUserAgent = new JPanel();

        _selectUrlSwing = new LogixNG_SelectStringSwing(getJDialog(), this);
        _selectCharsetSwing = new LogixNG_SelectCharsetSwing(getJDialog(), this);
        _selectRequestMethodSwing = new LogixNG_SelectEnumSwing<>(getJDialog(), this);
        _selectUserAgentSwing = new LogixNG_SelectStringSwing(getJDialog(), this);

        WebRequest action = (WebRequest)object;

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel tabbedPaneUrl;
        JPanel tabbedPaneCharset;
        JPanel tabbedPaneRequestMethod;
        JPanel tabbedPaneUserAgent;

        if (action != null) {
            tabbedPaneUrl = _selectUrlSwing.createPanel(action.getSelectUrl());
            tabbedPaneCharset = _selectCharsetSwing.createPanel(action.getSelectCharset());
            tabbedPaneRequestMethod = _selectRequestMethodSwing.createPanel(action.getSelectRequestMethod(), RequestMethodType.values());
            tabbedPaneUserAgent = _selectUserAgentSwing.createPanel(action.getSelectUserAgent());
        } else {
            tabbedPaneUrl = _selectUrlSwing.createPanel(null);
            tabbedPaneCharset = _selectCharsetSwing.createPanel(null);
            tabbedPaneRequestMethod = _selectRequestMethodSwing.createPanel(null, RequestMethodType.values());
            tabbedPaneUserAgent = _selectUserAgentSwing.createPanel(null);
        }


        JLabel selectUrlLabel = new JLabel(Bundle.getMessage("WebRequestSwing_Url"));
        selectUrlLabel.setLabelFor(tabbedPaneUrl);

        JLabel selectCharsetLabel = new JLabel(Bundle.getMessage("WebRequestSwing_Charset"));
        selectCharsetLabel.setLabelFor(tabbedPaneCharset);

        JLabel selectRequestMethodLabel = new JLabel(Bundle.getMessage("WebRequestSwing_RequestMethod"));
        selectRequestMethodLabel.setLabelFor(tabbedPaneRequestMethod);

        JLabel selectUserAgentLabel = new JLabel(Bundle.getMessage("WebRequestSwing_UserAgent"));
        selectUserAgentLabel.setLabelFor(tabbedPaneUserAgent);

        _panelUrl.add(selectUrlLabel);
        _panelUrl.add(tabbedPaneUrl);

        _panelCharset.add(selectCharsetLabel);
        _panelCharset.add(tabbedPaneCharset);

        _panelRequestMethod.add(selectRequestMethodLabel);
        _panelRequestMethod.add(tabbedPaneRequestMethod);

        _panelUserAgent.add(selectUserAgentLabel);
        _panelUserAgent.add(tabbedPaneUserAgent);

        _tabbedPane.addTab(Bundle.getMessage("WebRequestSwing_Url"), _panelUrl);
        _tabbedPane.addTab(Bundle.getMessage("WebRequestSwing_Charset"), _panelCharset);
        _tabbedPane.addTab(Bundle.getMessage("WebRequestSwing_RequestMethod"), _panelRequestMethod);
        _tabbedPane.addTab(Bundle.getMessage("WebRequestSwing_UserAgent"), _panelUserAgent);

        JLabel localVariableForResponseCodeLabel = new JLabel(Bundle.getMessage("WebRequestSwing_LocalVariableForResponseCode"));
        _localVariableForResponseCodeTextField = new JTextField();
        _localVariableForResponseCodeTextField.setColumns(30);

        JLabel localVariableForReplyContentLabel = new JLabel(Bundle.getMessage("WebRequestSwing_LocalVariableForReplyContent"));
        _localVariableForReplyContentTextField = new JTextField();
        _localVariableForReplyContentTextField.setColumns(30);

        JLabel localVariableForCookiesLabel = new JLabel(Bundle.getMessage("WebRequestSwing_LocalVariableForCookies"));
        _localVariableForCookiesTextField = new JTextField();
        _localVariableForCookiesTextField.setColumns(30);


        _logDataTable = new JTable();

        if (action != null) {
            List<WebRequest.Parameter> dataList
                    = new ArrayList<>(action.getParameters());

            _logDataTableModel = new WebRequestTableModel(dataList);
        } else {
            _logDataTableModel = new WebRequestTableModel(null);
        }

        _logDataTable.setModel(_logDataTableModel);
        _logDataTable.setDefaultRenderer(InitialValueType.class,
                new WebRequestTableModel.CellRenderer());
        _logDataTable.setDefaultEditor(InitialValueType.class,
                new WebRequestTableModel.DataTypeCellEditor());
        _logDataTableModel.setColumnsForComboBoxes(_logDataTable);
        _logDataTable.setDefaultRenderer(JButton.class, new ButtonRenderer());
        _logDataTable.setDefaultEditor(JButton.class, new ButtonEditor(new JButton()));

        JButton testButton = new JButton("XXXXXX");  // NOI18N
        _logDataTable.setRowHeight(testButton.getPreferredSize().height);
        TableColumn deleteColumn = _logDataTable.getColumnModel()
                .getColumn(WebRequestTableModel.COLUMN_DUMMY);
        deleteColumn.setMinWidth(testButton.getPreferredSize().width);
        deleteColumn.setMaxWidth(testButton.getPreferredSize().width);
        deleteColumn.setResizable(false);

        // The dummy column is used to be able to force update of the
        // other columns when the panel is closed.
        TableColumn dummyColumn = _logDataTable.getColumnModel()
                .getColumn(WebRequestTableModel.COLUMN_DUMMY);
        dummyColumn.setMinWidth(0);
        dummyColumn.setPreferredWidth(0);
        dummyColumn.setMaxWidth(0);

        JScrollPane scrollpane = new JScrollPane(_logDataTable);
        scrollpane.setPreferredSize(new Dimension(400, 200));

        // Add parameter
        JButton add = new JButton(Bundle.getMessage("WebRequest_TableAdd"));
        buttonPanel.add(add);
        add.addActionListener((ActionEvent e) -> {
            _logDataTableModel.add();
        });

        JLabel parametersLabel = new JLabel(Bundle.getMessage("WebRequest_Parameters"));
        parametersLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollpane.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.PAGE_AXIS));
        tablePanel.add(parametersLabel);
        tablePanel.add(scrollpane);



        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraint = new GridBagConstraints();
        Insets defaultInsets = constraint.insets;
        constraint.gridwidth = 1;
        constraint.gridheight = 1;
        constraint.gridx = 0;
        constraint.gridy = 0;
        constraint.gridwidth = 2;
        constraint.anchor = GridBagConstraints.CENTER;
        panel.add(_tabbedPane, constraint);
        constraint.gridy = 1;
        constraint.insets = new Insets(10,0,10,0);
        panel.add(tablePanel, constraint);

        constraint.gridwidth = 1;
        constraint.gridy = 2;
        constraint.anchor = GridBagConstraints.EAST;
        constraint.insets = defaultInsets;
        panel.add(localVariableForResponseCodeLabel, constraint);
        localVariableForResponseCodeLabel.setLabelFor(_localVariableForResponseCodeTextField);
        constraint.gridy = 3;
        panel.add(localVariableForReplyContentLabel, constraint);
        localVariableForReplyContentLabel.setLabelFor(_localVariableForReplyContentTextField);
        constraint.gridy = 4;
        panel.add(localVariableForCookiesLabel, constraint);
        localVariableForCookiesLabel.setLabelFor(_localVariableForCookiesTextField);

        constraint.gridx = 1;
        constraint.gridy = 2;
        constraint.anchor = GridBagConstraints.EAST;
        panel.add(_localVariableForResponseCodeTextField, constraint);
        constraint.gridy = 3;
        panel.add(_localVariableForReplyContentTextField, constraint);
        constraint.gridy = 4;
        panel.add(_localVariableForCookiesTextField, constraint);


        if (action != null) {
            _localVariableForResponseCodeTextField.setText(action.getLocalVariableForResponseCode());
            _localVariableForReplyContentTextField.setText(action.getLocalVariableForReplyContent());
            _localVariableForCookiesTextField.setText(action.getLocalVariableForCookies());
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        WebRequest action = new WebRequest("IQDA1", null);

        _selectUrlSwing.validate(action.getSelectUrl(), errorMessages);
        _selectCharsetSwing.validate(action.getSelectCharset(), errorMessages);
        _selectRequestMethodSwing.validate(action.getSelectRequestMethod(), errorMessages);
        _selectUserAgentSwing.validate(action.getSelectUserAgent(), errorMessages);

        for (WebRequest.Parameter data : _logDataTableModel.getDataList()) {
            if (data.getType() == InitialValueType.Formula) {
                try {
                    Map<String, Variable> variables = new HashMap<>();
                    RecursiveDescentParser parser = new RecursiveDescentParser(variables);
                    parser.parseExpression(data.getData());
                } catch (ParserException e) {
                    errorMessages.add(e.getLocalizedMessage());
                }
            }
        }

        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        WebRequest action = new WebRequest(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (!(object instanceof WebRequest)) {
            throw new IllegalArgumentException("object must be an WebRequest but is a: "+object.getClass().getName());
        }

        WebRequest action = (WebRequest)object;

        _selectUrlSwing.updateObject(action.getSelectUrl());
        _selectCharsetSwing.updateObject(action.getSelectCharset());
        _selectRequestMethodSwing.updateObject(action.getSelectRequestMethod());
        _selectUserAgentSwing.updateObject(action.getSelectUserAgent());

        action.setLocalVariableForResponseCode(_localVariableForResponseCodeTextField.getText());
        action.setLocalVariableForReplyContent(_localVariableForReplyContentTextField.getText());
        action.setLocalVariableForCookies(_localVariableForCookiesTextField.getText());

        // Do this to force update of the table
        _logDataTable.editCellAt(0, 2);

        action.getParameters().clear();

        for (WebRequest.Parameter data : _logDataTableModel.getDataList()) {
            action.getParameters().add(data);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("WebRequest_Short");
    }

    @Override
    public void dispose() {
        _selectUrlSwing.dispose();
        _selectCharsetSwing.dispose();
        _selectRequestMethodSwing.dispose();
        _selectUserAgentSwing.dispose();
    }

}

package jmri.jmrit.logixng.actions.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.WebRequest;
import jmri.jmrit.logixng.actions.WebRequest.RequestMethodType;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.TimerUnit;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectEnumSwing;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectStringSwing;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an WebRequest object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright (C) 2021
 */
public class WebRequestSwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectStringSwing _selectUrlSwing;
    private LogixNG_SelectEnumSwing<RequestMethodType> _selectRequestMethodSwing;
    private LogixNG_SelectStringSwing _selectUserAgentSwing;

    private JTextField _localVariableForPostContentTextField;
    private JTextField _localVariableForResponseCodeTextField;
    private JTextField _localVariableForReplyContentTextField;
    private JTextField __localVariableForCookiesTextField;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof WebRequest)) {
            throw new IllegalArgumentException("object must be an WebRequest but is a: "+object.getClass().getName());
        }

        _selectUrlSwing = new LogixNG_SelectStringSwing(getJDialog(), this);
        _selectRequestMethodSwing = new LogixNG_SelectEnumSwing<>(getJDialog(), this);
        _selectUserAgentSwing = new LogixNG_SelectStringSwing(getJDialog(), this);

        WebRequest action = (WebRequest)object;

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel tabbedPaneUrl;
        JPanel tabbedPaneRequestMethod;
        JPanel tabbedPaneUserAgent;

        if (action != null) {
            tabbedPaneUrl = _selectUrlSwing.createPanel(action.getSelectUrl());
            tabbedPaneRequestMethod = _selectRequestMethodSwing.createPanel(action.getSelectRequestMethod(), RequestMethodType.values());
            tabbedPaneUserAgent = _selectUserAgentSwing.createPanel(action.getSelectUserAgent());
        } else {
            tabbedPaneUrl = _selectUrlSwing.createPanel(null);
            tabbedPaneRequestMethod = _selectRequestMethodSwing.createPanel(null, RequestMethodType.values());
            tabbedPaneUserAgent = _selectUserAgentSwing.createPanel(null);
        }


        JLabel selectUrlLabel = new JLabel(Bundle.getMessage("WebRequestSwing_Url"));
        JLabel selectRequestMethodLabel = new JLabel(Bundle.getMessage("WebRequestSwing_RequestMethod"));
        JLabel selectUserAgentLabel = new JLabel(Bundle.getMessage("WebRequestSwing_UserAgent"));

        JLabel localVariableForPostContentLabel = new JLabel(Bundle.getMessage("WebRequestSwing_LocalVariableForPostContent"));
        _localVariableForPostContentTextField = new JTextField();
        _localVariableForPostContentTextField.setColumns(30);

        JLabel localVariableForResponseCodeLabel = new JLabel(Bundle.getMessage("WebRequestSwing_LocalVariableForResponseCode"));
        _localVariableForResponseCodeTextField = new JTextField();
        _localVariableForResponseCodeTextField.setColumns(30);

        JLabel localVariableForReplyContentLabel = new JLabel(Bundle.getMessage("WebRequestSwing_LocalVariableForReplyContent"));
        _localVariableForReplyContentTextField = new JTextField();
        _localVariableForReplyContentTextField.setColumns(30);

        JLabel localVariableForCookiesLabel = new JLabel(Bundle.getMessage("WebRequestSwing_LocalVariableForCookies"));
        __localVariableForCookiesTextField = new JTextField();
        __localVariableForCookiesTextField.setColumns(30);




        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraint = new GridBagConstraints();
        constraint.gridwidth = 1;
        constraint.gridheight = 1;
        constraint.gridx = 0;
        constraint.gridy = 0;
        constraint.anchor = GridBagConstraints.EAST;
        panel.add(selectUrlLabel, constraint);
        selectUrlLabel.setLabelFor(tabbedPaneUrl);
        constraint.gridy = 1;
        panel.add(selectRequestMethodLabel, constraint);
        selectRequestMethodLabel.setLabelFor(tabbedPaneRequestMethod);
        constraint.gridy = 2;
        panel.add(selectUserAgentLabel, constraint);
        selectUserAgentLabel.setLabelFor(tabbedPaneUserAgent);

        constraint.gridy = 3;
        panel.add(localVariableForPostContentLabel, constraint);
        localVariableForPostContentLabel.setLabelFor(_localVariableForPostContentTextField);
        constraint.gridy = 4;
        panel.add(localVariableForResponseCodeLabel, constraint);
        localVariableForResponseCodeLabel.setLabelFor(_localVariableForResponseCodeTextField);
        constraint.gridy = 5;
        panel.add(localVariableForReplyContentLabel, constraint);
        localVariableForReplyContentLabel.setLabelFor(_localVariableForReplyContentTextField);
        constraint.gridy = 6;
        panel.add(localVariableForCookiesLabel, constraint);
        localVariableForCookiesLabel.setLabelFor(__localVariableForCookiesTextField);

        constraint.gridx = 1;
        constraint.gridy = 0;
        constraint.anchor = GridBagConstraints.WEST;
        panel.add(tabbedPaneUrl, constraint);
        constraint.gridy = 1;
        panel.add(tabbedPaneRequestMethod, constraint);
        constraint.gridy = 2;
        panel.add(tabbedPaneUserAgent, constraint);

        constraint.gridy = 3;
        panel.add(_localVariableForPostContentTextField, constraint);
        constraint.gridy = 4;
        panel.add(_localVariableForResponseCodeTextField, constraint);
        constraint.gridy = 5;
        panel.add(_localVariableForReplyContentTextField, constraint);
        constraint.gridy = 6;
        panel.add(__localVariableForCookiesTextField, constraint);



        if (action != null) {
/*
            switch (action.getDelayAddressing()) {
                case Direct: _tabbedPaneDelay.setSelectedComponent(_panelDelayDirect); break;
                case Reference: _tabbedPaneDelay.setSelectedComponent(_panelDelayReference); break;
                case LocalVariable: _tabbedPaneDelay.setSelectedComponent(_panelDelayLocalVariable); break;
                case Formula: _tabbedPaneDelay.setSelectedComponent(_panelDelayFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getDelayAddressing().name());
            }
            _timerDelay.setText(Integer.toString(action.getDelay()));
            _delayReferenceTextField.setText(action.getDelayReference());
            _delayLocalVariableTextField.setText(action.getDelayLocalVariable());
            _delayFormulaTextField.setText(action.getDelayFormula());
*/
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        WebRequest action = new WebRequest("IQDA1", null);
/*
        try {
            if (_tabbedPaneDelay.getSelectedComponent() == _panelDelayReference) {
                action.setDelayReference(_delayReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            action.setDelayFormula(_delayFormulaTextField.getText());
            if (_tabbedPaneDelay.getSelectedComponent() == _panelDelayDirect) {
                action.setDelayAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneDelay.getSelectedComponent() == _panelDelayReference) {
                action.setDelayAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneDelay.getSelectedComponent() == _panelDelayLocalVariable) {
                action.setDelayAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneDelay.getSelectedComponent() == _panelDelayFormula) {
                action.setDelayAddressing(NamedBeanAddressing.Formula);
            } else {
                throw new IllegalArgumentException("_tabbedPane has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
            return false;
        }
*/
        return true;
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
/*
        action.setUnit(_unitComboBox.getItemAt(_unitComboBox.getSelectedIndex()));
        action.setResetIfAlreadyStarted(_resetIfAlreadyStarted.isSelected());
        action.setUseIndividualTimers(_useIndividualTimers.isSelected());



        try {
            if (_tabbedPaneDelay.getSelectedComponent() == _panelDelayDirect) {
                action.setDelayAddressing(NamedBeanAddressing.Direct);
                action.setDelay(Integer.parseInt(_timerDelay.getText()));
            } else if (_tabbedPaneDelay.getSelectedComponent() == _panelDelayReference) {
                action.setDelayAddressing(NamedBeanAddressing.Reference);
                action.setDelayReference(_delayReferenceTextField.getText());
            } else if (_tabbedPaneDelay.getSelectedComponent() == _panelDelayLocalVariable) {
                action.setDelayAddressing(NamedBeanAddressing.LocalVariable);
                action.setDelayLocalVariable(_delayLocalVariableTextField.getText());
            } else if (_tabbedPaneDelay.getSelectedComponent() == _panelDelayFormula) {
                action.setDelayAddressing(NamedBeanAddressing.Formula);
                action.setDelayFormula(_delayFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneDelay has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
*/
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("WebRequest_Short");
    }

    @Override
    public void dispose() {
    }

}

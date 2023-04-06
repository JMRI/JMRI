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
import jmri.jmrit.logixng.util.swing.LogixNG_SelectCharsetSwing;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectEnumSwing;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectStringSwing;

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

    private JTextField _localVariableForPostContentTextField;
    private JTextField _localVariableForResponseCodeTextField;
    private JTextField _localVariableForReplyContentTextField;
    private JTextField _localVariableForCookiesTextField;


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
        _localVariableForCookiesTextField = new JTextField();
        _localVariableForCookiesTextField.setColumns(30);

        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraint = new GridBagConstraints();
        constraint.gridwidth = 1;
        constraint.gridheight = 1;
        constraint.gridx = 0;
        constraint.gridy = 0;
        constraint.anchor = GridBagConstraints.EAST;

        constraint.gridwidth = 2;
        panel.add(_tabbedPane, constraint);
        constraint.gridwidth = 1;
/*
        panel.add(selectUrlLabel, constraint);
        selectUrlLabel.setLabelFor(tabbedPaneUrl);
        constraint.gridy = 1;
        panel.add(selectCharsetLabel, constraint);
        selectCharsetLabel.setLabelFor(tabbedPaneCharset);
        constraint.gridy = 2;
        panel.add(selectRequestMethodLabel, constraint);
        selectRequestMethodLabel.setLabelFor(tabbedPaneRequestMethod);
        constraint.gridy = 3;
        panel.add(selectUserAgentLabel, constraint);
        selectUserAgentLabel.setLabelFor(tabbedPaneUserAgent);
*/
        constraint.gridy = 4;
        panel.add(localVariableForPostContentLabel, constraint);
        localVariableForPostContentLabel.setLabelFor(_localVariableForPostContentTextField);
        constraint.gridy = 5;
        panel.add(localVariableForResponseCodeLabel, constraint);
        localVariableForResponseCodeLabel.setLabelFor(_localVariableForResponseCodeTextField);
        constraint.gridy = 6;
        panel.add(localVariableForReplyContentLabel, constraint);
        localVariableForReplyContentLabel.setLabelFor(_localVariableForReplyContentTextField);
        constraint.gridy = 7;
        panel.add(localVariableForCookiesLabel, constraint);
        localVariableForCookiesLabel.setLabelFor(_localVariableForCookiesTextField);

        constraint.gridx = 1;
/*
        constraint.gridy = 0;
        constraint.anchor = GridBagConstraints.WEST;
        panel.add(tabbedPaneUrl, constraint);
        constraint.gridy = 1;
        panel.add(tabbedPaneCharset, constraint);
        constraint.gridy = 2;
        panel.add(tabbedPaneRequestMethod, constraint);
        constraint.gridy = 3;
        panel.add(tabbedPaneUserAgent, constraint);
*/
        constraint.gridy = 4;
        panel.add(_localVariableForPostContentTextField, constraint);
        constraint.gridy = 5;
        panel.add(_localVariableForResponseCodeTextField, constraint);
        constraint.gridy = 6;
        panel.add(_localVariableForReplyContentTextField, constraint);
        constraint.gridy = 7;
        panel.add(_localVariableForCookiesTextField, constraint);

        if (action != null) {
            _localVariableForPostContentTextField.setText(action.getLocalVariableForPostContent());
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

        _selectUrlSwing.updateObject(action.getSelectUrl());
        _selectCharsetSwing.updateObject(action.getSelectCharset());
        _selectRequestMethodSwing.updateObject(action.getSelectRequestMethod());
        _selectUserAgentSwing.updateObject(action.getSelectUserAgent());

        action.setLocalVariableForPostContent(_localVariableForPostContentTextField.getText());
        action.setLocalVariableForResponseCode(_localVariableForResponseCodeTextField.getText());
        action.setLocalVariableForReplyContent(_localVariableForReplyContentTextField.getText());
        action.setLocalVariableForCookies(_localVariableForCookiesTextField.getText());
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

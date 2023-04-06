package jmri.jmrit.logixng.util.swing;

import java.nio.charset.Charset;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.LogixNG_SelectCharset;
import jmri.jmrit.logixng.util.LogixNG_SelectCharset.Addressing;
import jmri.util.swing.JComboBoxUtil;

/**
 * Swing class for jmri.jmrit.logixng.util.LogixNG_SelectCharset.
 *
 * @author Daniel Bergqvist (C) 2023
 */
public class LogixNG_SelectCharsetSwing {

    private final JDialog _dialog;
    private final LogixNG_SelectStringSwing _selectUserSpecified;

    private JTabbedPane _tabbedPane;
    private JPanel _panelStandard;
    private JPanel _panelAll;
    private JPanel _panelUserSpecified;
    private JComboBox<Charset> _standardValueComboBox;
    private JComboBox<Charset> _allValueComboBox;


    public LogixNG_SelectCharsetSwing(
            @Nonnull JDialog dialog,
            @Nonnull SwingConfiguratorInterface swi) {
        _dialog = dialog;
        _selectUserSpecified = new LogixNG_SelectStringSwing(_dialog, swi);
    }

    public JPanel createPanel(@CheckForNull LogixNG_SelectCharset selectCharset) {

        JPanel panel = new JPanel();

        _tabbedPane = new JTabbedPane();
        _panelStandard = new javax.swing.JPanel();
        _panelAll = new javax.swing.JPanel();

        _standardValueComboBox = new JComboBox<>();
        _standardValueComboBox = new JComboBox<>();
        for (Charset charset : LogixNG_SelectCharset.STANDARD_CHARSETS) {
            _standardValueComboBox.addItem(charset);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_standardValueComboBox);
        _panelStandard.add(_standardValueComboBox);

        _allValueComboBox = new JComboBox<>();
        _allValueComboBox = new JComboBox<>();
        for (Charset charset : Charset.availableCharsets().values()) {
            _allValueComboBox.addItem(charset);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_allValueComboBox);
        _panelAll.add(_allValueComboBox);

        if (selectCharset != null) {
            _panelUserSpecified = _selectUserSpecified.createPanel(selectCharset.getSelectUserSpecified());
        } else {
            _panelUserSpecified = _selectUserSpecified.createPanel(null);
        }

        _tabbedPane.addTab(Addressing.Standard.toString(), _panelStandard);
        _tabbedPane.addTab(Addressing.All.toString(), _panelAll);
        _tabbedPane.addTab(Addressing.UserSpecified.toString(), _panelUserSpecified);


        if (selectCharset != null) {
            switch (selectCharset.getAddressing()) {
                case Standard: _tabbedPane.setSelectedComponent(_panelStandard); break;
                case All: _tabbedPane.setSelectedComponent(_panelAll); break;
                case UserSpecified: _tabbedPane.setSelectedComponent(_panelUserSpecified); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + selectCharset.getAddressing().name());
            }
            if (selectCharset.getStandardValue() != null) {
                _standardValueComboBox.setSelectedItem(selectCharset.getStandardValue());
            }
            if (selectCharset.getAllValue() != null) {
                _allValueComboBox.setSelectedItem(selectCharset.getAllValue());
            }
        }

        panel.add(_tabbedPane);
        return panel;
    }

    public boolean validate(
            @Nonnull LogixNG_SelectCharset selectCharset,
            @Nonnull List<String> errorMessages) {

        _selectUserSpecified.validate(selectCharset.getSelectUserSpecified(), errorMessages);

        return errorMessages.isEmpty();
    }

    public void updateObject(@Nonnull LogixNG_SelectCharset selectCharset) {

        if (_tabbedPane.getSelectedComponent() == _panelStandard) {
            selectCharset.setAddressing(Addressing.Standard);
            selectCharset.setStandardValue(_standardValueComboBox.getItemAt(_standardValueComboBox.getSelectedIndex()));
        } else if (_tabbedPane.getSelectedComponent() == _panelAll) {
            selectCharset.setAddressing(Addressing.All);
            selectCharset.setAllValue(_allValueComboBox.getItemAt(_allValueComboBox.getSelectedIndex()));
        } else if (_tabbedPane.getSelectedComponent() == _panelUserSpecified) {
            selectCharset.setAddressing(Addressing.UserSpecified);
        } else {
            throw new IllegalArgumentException("_tabbedPaneEnum has unknown selection");
        }

        _selectUserSpecified.updateObject(selectCharset.getSelectUserSpecified());
    }

    public void dispose() {
        _selectUserSpecified.dispose();
    }

}

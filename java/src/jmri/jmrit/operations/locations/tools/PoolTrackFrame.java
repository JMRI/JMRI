package jmri.jmrit.operations.locations.tools;

import java.awt.*;

import javax.swing.*;

import jmri.jmrit.operations.*;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.swing.JmriJOptionPane;

/**
 *
 * @author Daniel Boudreau Copyright (C) 2011, 2025
 * @author Gregory Madsen Copyright (C) 2012
 */
class PoolTrackFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    // labels
    JLabel name = new JLabel(Bundle.getMessage("Name"));
    JLabel minimum = new JLabel(Bundle.getMessage("Minimum"));
    JLabel maximum = new JLabel(Bundle.getMessage("Maximum"));
    JLabel maxUser = new JLabel(Bundle.getMessage("Maximum") + "*");
    JLabel length = new JLabel(Bundle.getMessage("Length"));

    // text field
    JTextField trackPoolNameTextField = new JTextField(20);
    JTextField trackMinLengthTextField = new JTextField(5);
    JTextField trackMaxLengthTextField = new JTextField(5);

    // checkbox
    JCheckBox maxLengthCheckBox = new JCheckBox(Bundle.getMessage("EnableMax"));

    // combo box
    JComboBox<Pool> comboBoxPools = new JComboBox<>();
    
    // train departure order out of staging
    JRadioButton orderNormal = new JRadioButton(Bundle.getMessage("Normal"));
    JRadioButton orderFIFO = new JRadioButton(Bundle.getMessage("DescriptiveFIFO"));
    JRadioButton orderLIFO = new JRadioButton(Bundle.getMessage("DescriptiveLIFO"));

    // major buttons
    JButton addButton = new JButton(Bundle.getMessage("Add"));
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));

    // pool status
    JPanel poolStatus = new JPanel();

    protected Track _track;
    protected Pool _pool;

    public PoolTrackFrame(Track track) {
        super();
        _track = track;
    }

    @Override
    public void initComponents() {
        if (_track == null) {
            log.debug("track is null, pools can not be created");
            return;
        }
        // the following code sets the frame's initial state
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        _track.addPropertyChangeListener(this);
        _track.getLocation().addPropertyChangeListener(this);

        _pool = _track.getPool();

        // load the panel
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
        JScrollPane p1Pane = new JScrollPane(p1);
        p1Pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        p1Pane.setBorder(BorderFactory.createTitledBorder(""));
        
        // row 1
        JPanel pt = new JPanel();
        pt.setLayout(new BoxLayout(pt, BoxLayout.X_AXIS));
        pt.setMaximumSize(new Dimension(2000, 250));

        // row 1a
        JPanel pTrackName = new JPanel();
        pTrackName.setLayout(new GridBagLayout());
        pTrackName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Track")));
        addItem(pTrackName, new JLabel(_track.getName()), 0, 0);

        // row 1b
        JPanel pLocationName = new JPanel();
        pLocationName.setLayout(new GridBagLayout());
        pLocationName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Location")));
        addItem(pLocationName, new JLabel(_track.getLocation().getName()), 0, 0);

        pt.add(pTrackName);
        pt.add(pLocationName);

        JPanel poolName = new JPanel();
        poolName.setLayout(new GridBagLayout());
        poolName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("PoolName")));
        addItem(poolName, trackPoolNameTextField, 0, 0);
        addItem(poolName, addButton, 1, 0);

        JPanel selectPool = new JPanel();
        selectPool.setLayout(new GridBagLayout());
        selectPool.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("PoolSelect")));
        addItem(selectPool, comboBoxPools, 0, 0);
        OperationsPanel.padComboBox(comboBoxPools);

        JPanel minLengthTrack = new JPanel();
        minLengthTrack.setLayout(new GridBagLayout());
        minLengthTrack
                .setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("PoolTrackMinimum", _track.getName())));
        addItem(minLengthTrack, trackMinLengthTextField, 0, 0);

        trackMinLengthTextField.setText(Integer.toString(_track.getPoolMinimumLength()));
        
        JPanel maxLengthTrack = new JPanel();
        maxLengthTrack.setLayout(new GridBagLayout());
        maxLengthTrack
                .setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("PoolTrackMaximum", _track.getName())));
        addItemLeft(maxLengthTrack, maxLengthCheckBox, 0, 0);
        addItemLeft(maxLengthTrack, trackMaxLengthTextField, 1, 0);

        maxLengthCheckBox.setToolTipText(Bundle.getMessage("EnableMaxTrackTip"));
        updateMaxLengthFields();

        // row 4, train service order panel
        JPanel panelOrder = new JPanel();
        panelOrder.setLayout(new GridBagLayout());
        panelOrder.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TrainPickupOrder")));
        panelOrder.add(orderNormal);
        panelOrder.add(orderFIFO);
        panelOrder.add(orderLIFO);
        
        ButtonGroup orderGroup = new ButtonGroup();
        orderGroup.add(orderNormal);
        orderGroup.add(orderFIFO);
        orderGroup.add(orderLIFO);
        
        updateRadioButtons();
        
        addRadioButtonAction(orderNormal);
        addRadioButtonAction(orderFIFO);
        addRadioButtonAction(orderLIFO);

        JPanel savePool = new JPanel();
        savePool.setLayout(new GridBagLayout());
        savePool.setBorder(BorderFactory.createTitledBorder(""));
        addItem(savePool, saveButton, 0, 0);

        p1.add(pt);
        p1.add(poolName);
        p1.add(selectPool);
        p1.add(minLengthTrack);
        p1.add(maxLengthTrack);
        if (_track.isStaging()) {
            p1.add(panelOrder);
        }

        // pool status panel
        poolStatus.setLayout(new GridBagLayout());

        p1.add(poolStatus);
        p1.add(savePool);

        getContentPane().add(p1Pane);
        setTitle(Bundle.getMessage("MenuItemPoolTrack"));

        // load comboBox
        updatePoolsComboBox();
        updatePoolStatus();

        addCheckBoxAction(maxLengthCheckBox);
        addButtonAction(addButton);
        addButtonAction(saveButton);
        
        addPropertyChangeListeners();

        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_Pools", true); // NOI18N
        
        initMinimumSize(new Dimension(Control.panelWidth600, Control.panelHeight400));
    }

    private void updatePoolsComboBox() {
        _track.getLocation().updatePoolComboBox(comboBoxPools);
        comboBoxPools.setSelectedItem(_track.getPool());
    }

    private void updateMaxLengthFields() {
        maxLengthCheckBox.setSelected(false);
        if (_track != null && _track.getPool() != null) {
            boolean enable = _track.getPool().isMaxLengthOptionEnabled();
            maxLengthCheckBox.setEnabled(enable);
            if (enable) {
                if (_track.getPoolMaximumLength() != Integer.MAX_VALUE) {
                    maxLengthCheckBox.setSelected(true);
                    trackMaxLengthTextField.setText(Integer.toString(_track.getPoolMaximumLength()));
                } else {
                    trackMaxLengthTextField.setText(Integer.toString(_track.getPool().getMaxLengthTrack(_track)));
                }
            }

        } else {
            maxLengthCheckBox.setEnabled(false);
            trackMaxLengthTextField.setText("");
        }
        trackMaxLengthTextField.setEnabled(maxLengthCheckBox.isSelected());
    }

    private void updatePoolStatus() {
        // This shows the details of the current member tracks in the Pool.
        poolStatus.removeAll();

        addItemLeft(poolStatus, name, 0, 0);
        addItem(poolStatus, maximum, 1, 0);
        addItem(poolStatus, minimum, 3, 0);
        addItem(poolStatus, length, 4, 0);

        String poolName = "";
        if (_track.getPool() != null) {
            Pool pool = _track.getPool();
            if (pool.isThereMaxLengthRestrictions()) {
                addItem(poolStatus, maxUser, 2, 0);
            }
            poolName = pool.getName();

            int totalMinLength = 0;
            int totalLength = 0;
            int i = 0;
            for (Track track : pool.getTracks()) {
                i++;

                JLabel name = new JLabel();
                name.setText(track.getName());
                
                JLabel maximum = new JLabel();
                maximum.setText(Integer.toString(pool.getMaxLengthTrack(track)));

                JLabel maxUser = new JLabel();
                if (track.getPoolMaximumLength() == Integer.MAX_VALUE) {
                    maxUser.setText("");
                } else {
                    maxUser.setText(Integer.toString(track.getPoolMaximumLength()));
                }

                JLabel minimum = new JLabel();
                minimum.setText(Integer.toString(track.getPoolMinimumLength()));
                totalMinLength = totalMinLength + track.getPoolMinimumLength();

                JLabel length = new JLabel();
                length.setText(Integer.toString(track.getLength()));
                totalLength = totalLength + track.getLength();

                addItemLeft(poolStatus, name, 0, i);
                addItem(poolStatus, maximum, 1, i);
                addItem(poolStatus, minimum, 3, i);
                addItem(poolStatus, length, 4, i);

                if (pool.isThereMaxLengthRestrictions()) {
                    addItem(poolStatus, maxUser, 2, i);
                }
            }
            i++;
            // Summary
            JLabel total = new JLabel(Bundle.getMessage("Totals"));
            addItem(poolStatus, total, 0, i);
            if (totalMinLength > totalLength) {
                JLabel error = new JLabel(Bundle.getMessage("ErrorMinLen"));
                error.setForeground(Color.RED);
                addItem(poolStatus, error, 1, i);
            }
            JLabel totalMin = new JLabel();
            totalMin.setText(Integer.toString(totalMinLength));
            addItem(poolStatus, totalMin, 3, i);
            JLabel totalLen = new JLabel();
            totalLen.setText(Integer.toString(totalLength));
            addItem(poolStatus, totalLen, 4, i);
        }
        poolStatus.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("PoolTracks", poolName)));
        poolStatus.repaint();
        poolStatus.revalidate();
        setPreferredSize(null); // kill JMRI window size
        pack();
    }

    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == addButton) {
            Location location = _track.getLocation();
            Pool pool = location.addPool(trackPoolNameTextField.getText().trim());
            if (_pool == null) {
                comboBoxPools.setSelectedItem(pool);
            }
        }
        if (ae.getSource() == saveButton) {
            save();
            // save location file
            OperationsXml.save();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
    }
    
    @Override
    public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
        trackMaxLengthTextField.setEnabled(maxLengthCheckBox.isSelected());
    }

    private void save() {
        Pool pool = (Pool) comboBoxPools.getSelectedItem();
        if (pool != _pool) {
            maxLengthCheckBox.setSelected(false);
            removePropertyChangeListeners();
            _pool = pool;
            addPropertyChangeListeners();
        }
        try {
            _track.setPoolMinimumLength(Integer.parseInt(trackMinLengthTextField.getText()));
            if (maxLengthCheckBox.isSelected() && _pool != null && _pool.isMaxLengthOptionEnabled()) {
                _track.setPoolMaximumLength(Integer.parseInt(trackMaxLengthTextField.getText()));
            } else {
                _track.setPoolMaximumLength(Integer.MAX_VALUE); // default
            }
        } catch (NumberFormatException e) {
            JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("TrackMustBeNumber"), Bundle
                    .getMessage("ErrorTrackLength"), JmriJOptionPane.ERROR_MESSAGE);
            return;
        }
        _track.setPool(_pool); // this causes a property change to this frame
        updateServiceOrder();
    }

    @Override
    public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("radio button activated");
        if (_track.getPool() != null) {
            for (Track track : _track.getPool().getTracks()) {
                if (ae.getSource() == orderNormal) {
                    track.setServiceOrder(Track.NORMAL);
                }
                if (ae.getSource() == orderFIFO) {
                    track.setServiceOrder(Track.FIFO);
                }
                if (ae.getSource() == orderLIFO) {
                    track.setServiceOrder(Track.LIFO);
                }
            }
        }
    }
    
    private void updateServiceOrder() {
        if (_track.isStaging() && _track.getPool() != null) {
            for (Track track : _track.getPool().getTracks()) {
                if (track != _track) {
                    _track.setServiceOrder(track.getServiceOrder());
                    updateRadioButtons();
                    break;
                }
            }
        }
    }
    
    private void updateRadioButtons() {
        orderNormal.setSelected(_track.getServiceOrder().equals(Track.NORMAL));
        orderFIFO.setSelected(_track.getServiceOrder().equals(Track.FIFO));
        orderLIFO.setSelected(_track.getServiceOrder().equals(Track.LIFO));
    }

    private void addPropertyChangeListeners() {
        if (_pool != null) {
            _pool.addPropertyChangeListener(this);
            for (Track t : _pool.getTracks()) {
                if (t != _track) {
                    t.addPropertyChangeListener(this);
                }
            }
        }
    }

    private void removePropertyChangeListeners() {
        if (_pool != null) {
            _pool.removePropertyChangeListener(this);
            for (Track t : _pool.getTracks()) {
                if (t != _track) {
                    t.removePropertyChangeListener(this);
                }
            }
        }
    }

    @Override
    public void dispose() {
        if (_track != null) {
            _track.removePropertyChangeListener(this);
            _track.getLocation().removePropertyChangeListener(this);
        }
        removePropertyChangeListeners();
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(Location.POOL_LENGTH_CHANGED_PROPERTY)) {
            updatePoolsComboBox();
        }
        if (e.getPropertyName().equals(Pool.LISTCHANGE_CHANGED_PROPERTY)) {
            removePropertyChangeListeners();
            addPropertyChangeListeners();
        }
        if (e.getPropertyName().equals(Pool.LISTCHANGE_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Location.LENGTH_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Track.POOL_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Track.MIN_LENGTH_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Track.MAX_LENGTH_CHANGED_PROPERTY)) {
            updatePoolStatus();
        }
        if (e.getPropertyName().equals(Track.POOL_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Track.MAX_LENGTH_CHANGED_PROPERTY)) {
            updateMaxLengthFields();
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PoolTrackFrame.class);
}

package jmri.jmrix.mqtt;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.implementation.*;
import jmri.jmrit.beantable.signalmast.SignalMastAddPane;

import org.openide.util.lookup.ServiceProvider;

/**
 * A pane for configuring VirtualSignalMast objects.
 *
 * @see jmri.jmrit.beantable.signalmast.SignalMastAddPane
 * @author Bob Jacobsen Copyright (C) 2018, 2021
 */
public class MqttSignalMastAddPane extends SignalMastAddPane {

    public MqttSignalMastAddPane() {
        initPanel();
    }

    final void initPanel() {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // lit/unlit controls
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel(Bundle.getMessage("AllowUnLitLabel") + ": "));
        p.add(allowUnLit);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(p);

        // Address part
        add(addressDataPane());

        // disabled aspects controls
        TitledBorder disableborder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        disableborder.setTitle(Bundle.getMessage("DisableAspectsLabel"));
        JScrollPane disabledAspectsScroll = new JScrollPane(disabledAspectsPanel);
        disabledAspectsScroll.setBorder(disableborder);
        add(disabledAspectsScroll);
    }

    JPanel addressDataPane() {
        JPanel p = new JPanel();

        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        //border.setTitle("Mast Number");
        p.setBorder(border);

         p.setLayout(new jmri.util.javaworld.GridLayout2(1, 2));

        p.add(aspectAddressLabel);

        aspectAddressField.setText(paddedNumber.format(MqttSignalMast.getLastRef() + 1));
        aspectAddressField.setEnabled(true);
        p.add(aspectAddressField);

        return p;
    }
    JLabel aspectAddressLabel = new JLabel(Bundle.getMessage("TopicSuffix"));
    JTextField aspectAddressField = new JTextField(5);

    /** {@inheritDoc} */
    @Override
    @Nonnull public String getPaneName() {
        return Bundle.getMessage("MqttMast");
    }

    JCheckBox allowUnLit = new JCheckBox();

    LinkedHashMap<String, JCheckBox> disabledAspects = new LinkedHashMap<>(NOTIONAL_ASPECT_COUNT);
    JPanel disabledAspectsPanel = new JPanel();

    MqttSignalMast currentMast = null;

    /** {@inheritDoc} */
    @Override
    public void setAspectNames(@Nonnull SignalAppearanceMap map, @Nonnull SignalSystem sigSystem) {
        Enumeration<String> aspects = map.getAspects();
        // update immediately
        disabledAspects = new LinkedHashMap<>(NOTIONAL_ASPECT_COUNT);
        disabledAspectsPanel.removeAll();
        while (aspects.hasMoreElements()) {
            String aspect = aspects.nextElement();
            JCheckBox disabled = new JCheckBox(aspect);
            disabledAspects.put(aspect, disabled);
        }

        for (Map.Entry<String, JCheckBox> entry : disabledAspects.entrySet()) {
            disabledAspectsPanel.add(entry.getValue());
        }

        disabledAspectsPanel.setLayout(new jmri.util.javaworld.GridLayout2(0, 1)); // 0 means enough

        disabledAspectsPanel.revalidate();
    }

    /** {@inheritDoc} */
    @Override
    public boolean canHandleMast(@Nonnull SignalMast mast) {
        return mast instanceof MqttSignalMast;
    }

    /** {@inheritDoc} */
    @Override
    public void setMast(SignalMast mast) {
        log.trace("setMast {} start", mast);
        if (mast == null) {
            currentMast = null;
            return;
        }

        if (! (mast instanceof MqttSignalMast) ) {
            log.error("mast was wrong type: {} {}", mast.getSystemName(), mast.getClass().getName());
            return;
        }

        aspectAddressField.setEnabled(false);
        String[] pieces = mast.getSystemName().split("\\(");
        if (pieces.length == 2) {
            String number = pieces[1].substring(1, pieces[1].length()-1); // starts with ($)
            aspectAddressField.setText(number);
        } else {
            log.warn("not just one '(' in {}",mast.getSystemName());
        }

        currentMast = (MqttSignalMast) mast;
        List<String> disabled = currentMast.getDisabledAspects();
        if (disabled != null) {
            for (String aspect : disabled) {
                if (disabledAspects.containsKey(aspect)) {
                    disabledAspects.get(aspect).setSelected(true);
                }
            }
        }

        allowUnLit.setSelected(currentMast.allowUnLit());

        log.trace("setMast {} end", mast);
    }

    DecimalFormat paddedNumber = new DecimalFormat("0000");

    /** {@inheritDoc} */
    @Override
    public boolean createMast(@Nonnull
            String sigsysname, @Nonnull
                    String mastname, @Nonnull
                            String username) {
        log.trace("createMast {} {} {} start with currentMast: {}", sigsysname, mastname, username, currentMast);
        if (currentMast == null) {
            // create a mast
            String name = "IF$mqm:"
                    + sigsysname
                    + ":" + mastname.substring(11, mastname.length() - 4);
            name += "($" + aspectAddressField.getText()+ ")";
            currentMast = new MqttSignalMast(name);
            if (!username.isEmpty()) {
                currentMast.setUserName(username);
            }
            currentMast.setMastType(mastname.substring(11, mastname.length() - 4));
            try {
                InstanceManager.getDefault(SignalMastManager.class).register(currentMast);
            } catch (jmri.NamedBean.DuplicateSystemNameException e) {
                // clear the signal mast and rethrow
                currentMast = null;
                throw e;
            }

        }

        // load a new or existing mast
        for (Map.Entry<String, JCheckBox> entry : disabledAspects.entrySet()) {
            if (entry.getValue().isSelected()) {
                currentMast.setAspectDisabled(entry.getKey());
            } else {
                currentMast.setAspectEnabled(entry.getKey());
            }
        }
        currentMast.setAllowUnLit(allowUnLit.isSelected());

        // update to next option
        aspectAddressField.setText((paddedNumber.format(MqttSignalMast.getLastRef() + 1)));

        log.trace("createMast {} {} {} end", sigsysname, mastname, username);
        return true;
    }

    @ServiceProvider(service = SignalMastAddPane.SignalMastAddPaneProvider.class)
    static public class SignalMastAddPaneProvider extends SignalMastAddPane.SignalMastAddPaneProvider {
        /**
         * {@inheritDoc}
         * Requires a valid MQTT connection
         */
        @Override
        public boolean isAvailable() {
            for (SystemConnectionMemo memo : InstanceManager.getList(SystemConnectionMemo.class)) {
                if (memo instanceof jmri.jmrix.mqtt.MqttSystemConnectionMemo) {
                    return true;
                }
            }
            return false;
        }

        /** {@inheritDoc} */
        @Override
        @Nonnull public String getPaneName() {
            return Bundle.getMessage("MqttMast");
        }
        /** {@inheritDoc} */
        @Override
        @Nonnull public SignalMastAddPane getNewPane() {
            return new MqttSignalMastAddPane();
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MqttSignalMastAddPane.class);

}

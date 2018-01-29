package jmri.jmrit.beantable.signalmast;

import java.awt.*;
import java.awt.event.*;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import org.openide.util.lookup.ServiceProvider;

/**
 * A pane for configuring MatrixSignalMast objects
 * <P>
 * @see jmri.jmrit.beantable.signalmast.SignalMastAddPane
 * @author Bob Jacobsen Copyright (C) 2018
 * @since 4.11.2
 */
public class DccSignalMastAddPane extends SignalMastAddPane {

    /** {@inheritDoc} */
    @Override
    @Nonnull public String getPaneName() {
        return Bundle.getMessage("DCCMast");
    }

    public JPanel getLitPanel() {
        JPanel dccUnLitPanel = new JPanel();
        JTextField unLitAspectField = new JTextField(5);
        
        dccUnLitPanel.setLayout(new BoxLayout(dccUnLitPanel, BoxLayout.Y_AXIS));
        JPanel dccDetails = new JPanel();
        dccDetails.add(new JLabel(Bundle.getMessage("DCCMastSetAspectId") + ":"));
        dccDetails.add(unLitAspectField);
        unLitAspectField.setText("31");
        dccUnLitPanel.add(dccDetails);
        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        border.setTitle(Bundle.getMessage("DCCUnlitAspectNumber"));
        dccUnLitPanel.setBorder(border);
        unLitAspectField.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
                if (unLitAspectField.getText().equals("")) {
                    return;
                }
                if (!validateAspectId(unLitAspectField.getText())) {
                    unLitAspectField.requestFocusInWindow();
                }
            }

            @Override
            public void focusGained(FocusEvent e) {
            }

        });
        
        return dccUnLitPanel;
    }

    static boolean validateAspectId(String strAspect) {
        int aspect;
        try {
            aspect = Integer.parseInt(strAspect.trim());
        } catch (java.lang.NumberFormatException e) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("DCCMastAspectNumber"));
            return false;
        }
        if (aspect < 0 || aspect > 31) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("DCCMastAspectOutOfRange"));
            log.error("invalid aspect {}", aspect);
            return false;
        }
        return true;
    }

    @ServiceProvider(service = SignalMastAddPane.SignalMastAddPaneProvider.class)
    static public class SignalMastAddPaneProvider extends SignalMastAddPane.SignalMastAddPaneProvider {
        /** {@inheritDoc} */
        @Override
        @Nonnull public String getPaneName() {
            return Bundle.getMessage("DCCMast");
        }
        /** {@inheritDoc} */
        @Override
        @Nonnull public SignalMastAddPane getNewPane() {
            return new DccSignalMastAddPane();
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DccSignalMastAddPane.class);
}

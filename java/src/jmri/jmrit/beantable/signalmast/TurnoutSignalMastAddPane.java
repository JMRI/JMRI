package jmri.jmrit.beantable.signalmast;

import java.awt.*;
import java.awt.event.*;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import jmri.*;
import jmri.util.swing.BeanSelectCreatePanel;

import org.openide.util.lookup.ServiceProvider;

/**
 * A pane for configuring TurnoutSignalMast objects
 * <P>
 * @see jmri.jmrit.beantable.signalmast.SignalMastAddPane
 * @author Bob Jacobsen Copyright (C) 2018
 * @since 4.11.2
 */
public class TurnoutSignalMastAddPane extends SignalMastAddPane {

    /** {@inheritDoc} */
    @Override
    @Nonnull public String getPaneName() {
        return Bundle.getMessage("TurnCtlMast");
    }

    String stateThrown = InstanceManager.turnoutManagerInstance().getThrownText();
    String stateClosed = InstanceManager.turnoutManagerInstance().getClosedText();
    String[] turnoutStates = new String[]{stateClosed, stateThrown};
    int[] turnoutStateValues = new int[]{Turnout.CLOSED, Turnout.THROWN};

    BeanSelectCreatePanel<Turnout> turnoutUnLitBox = new BeanSelectCreatePanel<>(InstanceManager.turnoutManagerInstance(), null);
    JComboBox<String> turnoutUnLitState = new JComboBox<>(turnoutStates);

    public JPanel getLitPanel() {

        JPanel turnoutUnLitPanel = new JPanel();

        turnoutUnLitPanel.setLayout(new BoxLayout(turnoutUnLitPanel, BoxLayout.Y_AXIS));
        JPanel turnDetails = new JPanel();
        turnDetails.add(turnoutUnLitBox);
        turnDetails.add(new JLabel(Bundle.getMessage("SetState")));
        turnDetails.add(turnoutUnLitState);
        turnoutUnLitPanel.add(turnDetails);
        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        border.setTitle(Bundle.getMessage("TurnUnLitDetails"));
        turnoutUnLitPanel.setBorder(border);
        
        return turnoutUnLitPanel;
    }

    @ServiceProvider(service = SignalMastAddPane.SignalMastAddPaneProvider.class)
    static public class SignalMastAddPaneProvider extends SignalMastAddPane.SignalMastAddPaneProvider {
        /** {@inheritDoc} */
        @Override
        @Nonnull public String getPaneName() {
            return Bundle.getMessage("TurnCtlMast");
        }
        /** {@inheritDoc} */
        @Override
        @Nonnull public SignalMastAddPane getNewPane() {
            return new TurnoutSignalMastAddPane();
        }
    }
}

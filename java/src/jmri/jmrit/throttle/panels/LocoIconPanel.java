package jmri.jmrit.throttle.panels;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.jmrit.consisttool.ConsistListCellRenderer;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterIconFactory;
import jmri.jmrit.throttle.interfaces.AddressListener;

public class LocoIconPanel extends JPanel implements AddressListener {

    private static final int IMAGE_HEIGHT = 32;
    private static final RosterIconFactory ICN_FACT = new RosterIconFactory(IMAGE_HEIGHT);

    AddressPanel addressPanel = null;
    JLabel iconLabel = new JLabel();

    public LocoIconPanel() {
        super();
        setLayout(new BorderLayout());
        iconLabel.setHorizontalAlignment(JLabel.CENTER);
        iconLabel.setVerticalAlignment(JLabel.CENTER);
        updateLabel();
        add(iconLabel, BorderLayout.CENTER);
    }

    public void setAddressPanel(AddressPanel addressPanel) {
        if (this.addressPanel != null) {
            this.addressPanel.removeAddressListener(this);
        }
        this.addressPanel = addressPanel;
        if (this.addressPanel != null) {
            this.addressPanel.addAddressListener(this);
        }
        updateLabel();
    }

    public JLabel getLabel() {
        return iconLabel;
    }

    private void updateLabel() {
        String ltext = Bundle.getMessage("ThrottleNotAssigned");
        ImageIcon licon = null;
        if (addressPanel != null && (addressPanel.getThrottle() != null)) {
            if (addressPanel.getConsistAddress() != null) { 
                // consists                
                licon = ConsistListCellRenderer.getConsistIcon(addressPanel.getConsistAddress(), ICN_FACT);
                ltext = addressPanel.getConsistAddress().toString();
            } else if (addressPanel.getRosterEntry() != null) {  
                // regular locomotive                                   
                licon = ICN_FACT.getIcon(addressPanel.getRosterEntry());
                ltext = addressPanel.getRosterEntry().getId();
            } else if (addressPanel.getCurrentAddress() != null)  {
                switch (addressPanel.getCurrentAddress().getNumber()) {
                    case 0:
                        ltext = Bundle.getMessage("ThrottleDCControl") + " - " + addressPanel.getCurrentAddress();
                        break;
                    case 3:
                        ltext = Bundle.getMessage("ThrottleDCCControl") + " - " + addressPanel.getCurrentAddress();
                        break;
                    default:
                        ltext = Bundle.getMessage("ThrottleAddress") + " " + addressPanel.getCurrentAddress();
                        break;
                }
            }
        }
        iconLabel.setIcon(licon);
        iconLabel.setText(ltext);
    }

    @Override
    public void notifyAddressThrottleFound(DccThrottle t) {
        updateLabel();
    }

    @Override
    public void notifyRosterEntrySelected(RosterEntry re) {
        updateLabel();
    }

    @Override
    public void notifyAddressReleased(LocoAddress la) {
        updateLabel();
    }

    @Override
    public void notifyAddressChosen(LocoAddress l) {
        updateLabel();
    }

    @Override
    public void notifyConsistAddressChosen(LocoAddress l) {
        updateLabel();
    }

    @Override
    public void notifyConsistAddressReleased(LocoAddress l) {
        updateLabel();
    }

    @Override
    public void notifyConsistAddressThrottleFound(DccThrottle t) {
        updateLabel();
    }

    public void destroy() {        
        if (addressPanel != null) {
            addressPanel.removeAddressListener(this);
            addressPanel = null;
        }
    }
}

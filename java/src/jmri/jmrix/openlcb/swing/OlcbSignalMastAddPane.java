package jmri.jmrix.openlcb.swing;

import jmri.*;
import jmri.jmrit.beantable.signalmast.SignalMastAddPane;
import jmri.jmrit.catalog.NamedIcon;
import jmri.SystemConnectionMemo;
import jmri.util.ConnectionNameFromSystemName;

import jmri.jmrix.openlcb.*;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.annotation.Nonnull;

import org.openide.util.lookup.ServiceProvider;
import org.openlcb.swing.EventIdTextField;

/**
 * A pane for configuring OlcbSignalMast objects
 *
 * @see jmri.jmrit.beantable.signalmast.SignalMastAddPane
 * @author Bob Jacobsen Copyright (C) 2018
 * @since 4.11.2
 */
public class OlcbSignalMastAddPane extends SignalMastAddPane {

    public OlcbSignalMastAddPane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        litEventID.setText("00.00.00.00.00.00.00.00");
        notLitEventID.setText("00.00.00.00.00.00.00.00");
        heldEventID.setText("00.00.00.00.00.00.00.00");
        notHeldEventID.setText("00.00.00.00.00.00.00.00");

        // populate the OpenLCB connections list before creating GUI components.
        getOlcbConnections();

        // If the connections list has less than 2 items, don't show a selector.
        // This maintains backward compatibility with previous versions.
        if (olcbConnections != null && olcbConnections.size() > 1) {
            // Connection selector
            JPanel p = new JPanel();
            TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
            border.setTitle(Bundle.getMessage("OlcbConnection"));
            p.setBorder(border);
            p.setLayout(new jmri.util.javaworld.GridLayout2(3, 1));

            p.add(connSelectionBox);
            add(p);
        }

        // lit/unlit controls
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel(Bundle.getMessage("AllowUnLitLabel") + ": "));
        p.add(allowUnLit);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(p);
        
        // aspects controls
        TitledBorder aspectsBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        aspectsBorder.setTitle(Bundle.getMessage("EnterAspectsLabel"));
        JScrollPane allAspectsScroll = new JScrollPane(allAspectsPanel);
        allAspectsScroll.setBorder(aspectsBorder);
        add(allAspectsScroll);
        
        JPanel p5;

        // Lit
        TitledBorder litborder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        litborder.setTitle(Bundle.getMessage("LitUnLit"));
        JPanel pLit = new JPanel();
        pLit.setBorder(litborder);
        pLit.setLayout(new BoxLayout(pLit, BoxLayout.Y_AXIS));
        
        p5 = new JPanel();
        p5.setLayout(new BoxLayout(p5, BoxLayout.X_AXIS));
        p5.add(new JLabel(Bundle.getMessage("LitLabel")));
        p5.add(Box.createHorizontalGlue());
        pLit.add(p5);
        pLit.add(litEventID);
        
        p5 = new JPanel();
        p5.setLayout(new BoxLayout(p5, BoxLayout.X_AXIS));
        p5.add(new JLabel(Bundle.getMessage("NotLitLabel")));
        p5.add(Box.createHorizontalGlue());
        pLit.add(p5);
        pLit.add(notLitEventID);
        
        add(pLit);
       
        // Held
        TitledBorder heldborder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        heldborder.setTitle(Bundle.getMessage("HeldUnHeld"));
        JPanel pHeld= new JPanel();
        pHeld.setBorder(heldborder);
        pHeld.setLayout(new BoxLayout(pHeld, BoxLayout.Y_AXIS));
        
        p5 = new JPanel();
        p5.setLayout(new BoxLayout(p5, BoxLayout.X_AXIS));
        p5.add(new JLabel(Bundle.getMessage("HeldLabel")));
        p5.add(Box.createHorizontalGlue());
        pHeld.add(p5);
        pHeld.add(heldEventID);
        
        p5 = new JPanel();
        p5.setLayout(new BoxLayout(p5, BoxLayout.X_AXIS));
        p5.add(new JLabel(Bundle.getMessage("NotHeldLabel")));
        p5.add(Box.createHorizontalGlue());
        pHeld.add(p5);
        pHeld.add(notHeldEventID);
        
        add(pHeld);

        // set up selection of connections, if needed
        populateConnSelectionBox();

    }

    /** {@inheritDoc} */
    @Override
    @Nonnull public String getPaneName() {
        return Bundle.getMessage("OlcbSignalMastPane");
    }

    final JCheckBox allowUnLit = new JCheckBox();

    // This used to be called "disabledAspects", but that's misleading: it's actually a map of
    // ALL aspects' "disabled" checkboxes, regardless of their enabled/disabled state.
    LinkedHashMap<String, JCheckBox> allAspectsCheckBoxes = new LinkedHashMap<>(NOTIONAL_ASPECT_COUNT);
    final LinkedHashMap<String, EventIdTextField> aspectEventIDs = new LinkedHashMap<>(NOTIONAL_ASPECT_COUNT);
    final JPanel allAspectsPanel = new JPanel();
    final EventIdTextField litEventID = new EventIdTextField();
    final EventIdTextField notLitEventID = new EventIdTextField();
    final EventIdTextField heldEventID = new EventIdTextField();
    final EventIdTextField notHeldEventID = new EventIdTextField();

    JComboBox<String> connSelectionBox = new JComboBox<String>();

    OlcbSignalMast currentMast = null;

    // Support for multiple OpenLCB connections with different prefixes
    ArrayList<String> olcbConnections = null;

    /** {@inheritDoc} */
    @Override
    public void setAspectNames(@Nonnull SignalAppearanceMap map, @Nonnull SignalSystem sigSystem) {
        Enumeration<String> aspectNames = map.getAspects();
        // update immediately
        allAspectsCheckBoxes = new LinkedHashMap<>(NOTIONAL_ASPECT_COUNT);
        allAspectsPanel.removeAll();
        while (aspectNames.hasMoreElements()) {
            String aspectName = aspectNames.nextElement();
            JCheckBox disabled = new JCheckBox(aspectName);
            allAspectsCheckBoxes.put(aspectName, disabled);
            EventIdTextField eventID = new EventIdTextField();
            eventID.setText("00.00.00.00.00.00.00.00");
            aspectEventIDs.put(aspectName, eventID);
        }
        allAspectsPanel.setLayout(new BoxLayout(allAspectsPanel, BoxLayout.Y_AXIS));
        for (Map.Entry<String, JCheckBox> entry : allAspectsCheckBoxes.entrySet()) {
            JPanel p1 = new JPanel();
            TitledBorder p1border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
            p1border.setTitle(entry.getKey()); // Note this is not I18N'd: as-is from xml file
            p1.setBorder(p1border);
            p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));

            // Attempt to load an icon for display
            String iconLink = map.getImageLink(entry.getKey(), "default");
            if (iconLink == null || iconLink.isEmpty()) {
                log.debug("Got empty image link for {}", entry.getKey());
            } else {
                log.debug("Image link for {} is {}", entry.getKey(), iconLink);
                if (!iconLink.contains("preference:")) {
                    // This logic copied from SignalMastItemPanel.java
                    iconLink = iconLink.substring(iconLink.indexOf("resources"));
                }
                NamedIcon n = null;
                try {
                    n = new NamedIcon(iconLink, iconLink);
                    log.debug("Loaded icon {}", iconLink);
                } catch (Exception e) {
                    log.debug("Got exception trying to load icon link {}: {}", iconLink, e.getMessage());
                }
                // display icon
                if (n != null) {
                    p1.add(new JLabel(n));
                }
            }

            JPanel p2 = new JPanel();
            p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
            // event ID text box
            p2.add(aspectEventIDs.get(entry.getKey()));
            // "Disable" checkbox
            p2.add(entry.getValue());
            entry.getValue().setName(entry.getKey());
            entry.getValue().setText(Bundle.getMessage("DisableAspect"));
            p1.add(p2);
            allAspectsPanel.add(p1);
        }

        populateConnSelectionBox();

        litEventID.setText("00.00.00.00.00.00.00.00");
        notLitEventID.setText("00.00.00.00.00.00.00.00");
        heldEventID.setText("00.00.00.00.00.00.00.00");
        notHeldEventID.setText("00.00.00.00.00.00.00.00");

        allAspectsPanel.revalidate();
    }

    /*
     * Populate the list of valid OpenLCB connection names.
     * Only connections that have "OpenLCB" or "LCC" as their manufacturer are added.
     */
    private void getOlcbConnections() {
        olcbConnections = null;
        ConnectionConfig[] conns = null;
        try {
            conns = InstanceManager.getDefault(ConnectionConfigManager.class).getConnections();
        } catch (Exception e) {
            log.info("No ConnectionConfigManager installed: Using default Olcb Connections");
        }
        if (conns == null || conns.length == 0) {
            log.debug("Found null or empty connections list");
            return;
        }
        for (int x = 0; x < conns.length; x++) {
            ConnectionConfig cc = conns[x];
            log.debug("conns[{}]: name={} info={} adapter={} conn={}  man={}",
                      x, cc.name(), cc.getInfo(), cc.getAdapter(),
                      cc.getConnectionName(), cc.getManufacturer());
            /* As this is the Olcb signal mast add pane, only show OpenLCB/LCC connections */
            String man = cc.getManufacturer();
            String name = cc.getConnectionName();
            if (man != null && name != null && !name.isEmpty() &&
                (man.equals("OpenLCB") || man.equals("LCC"))) {
                if (olcbConnections == null) {
                    olcbConnections = new ArrayList<String>();
                }
                olcbConnections.add(name);
            }
        }
    }

    /*
     * Populate the GUI connection selection box, if there are
     * multiple OpenLCB connections.
     */
    private void populateConnSelectionBox() {
        connSelectionBox.removeAllItems();
        if (olcbConnections == null || olcbConnections.size() < 2) {
            return;
        }
        for (String conn : olcbConnections) {
            connSelectionBox.addItem(conn);
        }
        if (currentMast == null) {
            connSelectionBox.setEnabled(true);
            return;
        }
        // set the selected connection based on the current mast
        String mastPrefix = currentMast.getSystemPrefix();
        if (mastPrefix != null) {
            for (String conn : olcbConnections) {
                String connectionPrefix = ConnectionNameFromSystemName.getPrefixFromName(conn);
                if (connectionPrefix != null && connectionPrefix.equals(mastPrefix)) {
                    connSelectionBox.setSelectedItem(conn);
                    break;
                }
            }
        }
        // Can't change connection on existing masts
        connSelectionBox.setEnabled(false);
    }

    /** {@inheritDoc} */
    @Override
    public boolean canHandleMast(@Nonnull SignalMast mast) {
        return mast instanceof OlcbSignalMast;
    }

    /** {@inheritDoc} */
    @Override
    public void setMast(SignalMast mast) { 
        if (mast == null) { 
            currentMast = null; 
            // re-enable connection selector
            populateConnSelectionBox();
            return; 
        }
        
        if (! (mast instanceof OlcbSignalMast) ) {
            log.error("mast was wrong type: {} {}", mast.getSystemName(), mast.getClass().getName());
            return;
        }

        currentMast = (OlcbSignalMast) mast;
        List<String> disabledList = currentMast.getDisabledAspects();
        if (disabledList != null) {
            for (String aspect : disabledList) {
                if (allAspectsCheckBoxes.containsKey(aspect)) {
                    allAspectsCheckBoxes.get(aspect).setSelected(true);
                }
            }
         }
        for (String aspect : currentMast.getAllKnownAspects()) {
            if (aspectEventIDs.get(aspect) == null) {
                EventIdTextField eventID = new EventIdTextField();
                eventID.setText("00.00.00.00.00.00.00.00");
                aspectEventIDs.put(aspect, eventID);
            }
            if (currentMast.isOutputConfigured(aspect)) {
                aspectEventIDs.get(aspect).setText(currentMast.getOutputForAppearance(aspect));
            } else {
                aspectEventIDs.get(aspect).setText("00.00.00.00.00.00.00.00");
            }
        }

        litEventID.setText(currentMast.getLitEventId());
        notLitEventID.setText(currentMast.getNotLitEventId());
        heldEventID.setText(currentMast.getHeldEventId());
        notHeldEventID.setText(currentMast.getNotHeldEventId());        

        allowUnLit.setSelected(currentMast.allowUnLit());

        // show current connection in selector
        populateConnSelectionBox();

        log.debug("setMast({})", mast);
    }

    final DecimalFormat paddedNumber = new DecimalFormat("0000");

    /** {@inheritDoc} */
    @Override
    public boolean createMast(@Nonnull String sigsysname,
                              @Nonnull String mastname,
                              @Nonnull String username) {
        if (currentMast == null) {
            // create a mast
            String selItem = (String)connSelectionBox.getSelectedItem();
            String connectionPrefix = ConnectionNameFromSystemName.getPrefixFromName(selItem);
            log.debug("selected name={}, prefix={}", selItem, connectionPrefix);
            // if prefix is null, use default
            if (connectionPrefix == null || connectionPrefix.isEmpty()) {
                connectionPrefix = "M";
            }

            String type = mastname.substring(11, mastname.length() - 4);
            String name = connectionPrefix + "F$olm:" + sigsysname + ":" + type;
            name += "($" + (paddedNumber.format(OlcbSignalMast.getLastRef() + 1)) + ")";
            log.debug("Creating mast: {}", name);
            currentMast = new OlcbSignalMast(name);
            if (!username.equals("")) {
                currentMast.setUserName(username);
            }
            currentMast.setMastType(type);
            InstanceManager.getDefault(jmri.SignalMastManager.class).register(currentMast);
        }
        
        // load a new or existing mast
        for (Map.Entry<String, JCheckBox> entry : allAspectsCheckBoxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                currentMast.setAspectDisabled(entry.getKey());
            } else {
                currentMast.setAspectEnabled(entry.getKey());
            }
            currentMast.setOutputForAppearance(entry.getKey(), aspectEventIDs.get(entry.getKey()).getText());
        }
        
        currentMast.setLitEventId(litEventID.getText());
        currentMast.setNotLitEventId(notLitEventID.getText());
        currentMast.setHeldEventId(heldEventID.getText());
        currentMast.setNotHeldEventId(notHeldEventID.getText());

        currentMast.setAllowUnLit(allowUnLit.isSelected());
        return true;
    }


    @ServiceProvider(service = SignalMastAddPane.SignalMastAddPaneProvider.class)
    static public class SignalMastAddPaneProvider extends SignalMastAddPane.SignalMastAddPaneProvider {

        /**
         * {@inheritDoc}
         * Requires a valid OpenLCB connection
         */
        @Override
        public boolean isAvailable() {
            for (SystemConnectionMemo memo : InstanceManager.getList(SystemConnectionMemo.class)) {
                if (memo instanceof jmri.jmrix.can.CanSystemConnectionMemo) {
                    return true;
                }
            }
            return false;
        }

        /** {@inheritDoc} */
        @Override
        @Nonnull public String getPaneName() {
            return Bundle.getMessage("OlcbSignalMastPane");
        }

        /** {@inheritDoc} */
        @Override
        @Nonnull public SignalMastAddPane getNewPane() {
            return new OlcbSignalMastAddPane();
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OlcbSignalMastAddPane.class);

}

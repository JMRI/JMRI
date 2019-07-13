package jmri.jmrix.openlcb.swing;

import jmri.*;
import jmri.jmrit.beantable.signalmast.SignalMastAddPane;
import jmri.jmrix.SystemConnectionMemo;

import jmri.jmrix.openlcb.*;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
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

        // lit/unlit controls
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel(Bundle.getMessage("AllowUnLitLabel") + ": "));
        p.add(allowUnLit);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(p);
        
        // aspects controls
        TitledBorder disableborder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        disableborder.setTitle(Bundle.getMessage("EnterAspectsLabel"));
        JScrollPane disabledAspectsScroll = new JScrollPane(disabledAspectsPanel);
        disabledAspectsScroll.setBorder(disableborder);
        add(disabledAspectsScroll);
        
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

    }

    /** {@inheritDoc} */
    @Override
    @Nonnull public String getPaneName() {
        return Bundle.getMessage("OlcbSignalMastPane");
    }


    JCheckBox allowUnLit = new JCheckBox();

    LinkedHashMap<String, JCheckBox> disabledAspects = new LinkedHashMap<>(NOTIONAL_ASPECT_COUNT);
    LinkedHashMap<String, EventIdTextField> aspectEventIDs = new LinkedHashMap<>(NOTIONAL_ASPECT_COUNT);
    JPanel disabledAspectsPanel = new JPanel();
    EventIdTextField litEventID = new EventIdTextField();
    EventIdTextField notLitEventID = new EventIdTextField();
    EventIdTextField heldEventID = new EventIdTextField();
    EventIdTextField notHeldEventID = new EventIdTextField();

    OlcbSignalMast currentMast = null;

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
            EventIdTextField eventID = new EventIdTextField();
            eventID.setText("00.00.00.00.00.00.00.00");
            aspectEventIDs.put(aspect, eventID);
        }
        disabledAspectsPanel.setLayout(new BoxLayout(disabledAspectsPanel, BoxLayout.Y_AXIS));
        for (Map.Entry<String, JCheckBox> entry : disabledAspects.entrySet()) {
            JPanel p1 = new JPanel();
            TitledBorder p1border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
            p1border.setTitle(entry.getKey());
            p1.setBorder(p1border);
            p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
            p1.add(aspectEventIDs.get(entry.getKey()));
            p1.add(entry.getValue());
            entry.getValue().setName(entry.getKey());
            entry.getValue().setText(Bundle.getMessage("DisableAspect"));
            disabledAspectsPanel.add(p1);
        }

        litEventID.setText("00.00.00.00.00.00.00.00");
        notLitEventID.setText("00.00.00.00.00.00.00.00");
        heldEventID.setText("00.00.00.00.00.00.00.00");
        notHeldEventID.setText("00.00.00.00.00.00.00.00");

        disabledAspectsPanel.revalidate();
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
            return; 
        }
        
        if (! (mast instanceof OlcbSignalMast) ) {
            log.error("mast was wrong type: {} {}", mast.getSystemName(), mast.getClass().getName());
            return;
        }

        currentMast = (OlcbSignalMast) mast;
        List<String> disabled = currentMast.getDisabledAspects();
        if (disabled != null) {
            for (String aspect : disabled) {
                if (disabledAspects.containsKey(aspect)) {
                    disabledAspects.get(aspect).setSelected(true);
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

        log.debug("setMast({})", mast);
    }

    DecimalFormat paddedNumber = new DecimalFormat("0000");

    /** {@inheritDoc} */
    @Override
    public boolean createMast(@Nonnull String sigsysname,
                              @Nonnull String mastname,
                              @Nonnull String username) {
        if (currentMast == null) {
            // create a mast
            String type = mastname.substring(11, mastname.length() - 4);
            String name = "MF$olm:" + sigsysname + ":" + type;
            name += "($" + (paddedNumber.format(OlcbSignalMast.getLastRef() + 1)) + ")";
            currentMast = new OlcbSignalMast(name);
            if (!username.equals("")) {
                currentMast.setUserName(username);
            }
            currentMast.setMastType(type);
            InstanceManager.getDefault(jmri.SignalMastManager.class).register(currentMast);
        }
        
        // load a new or existing mast
        for (Map.Entry<String, JCheckBox> entry : disabledAspects.entrySet()) {
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

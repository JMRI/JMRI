package jmri.jmrit.beantable.signalmast;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import org.openide.util.lookup.ServiceProvider;

import jmri.InstanceManager;
import jmri.SignalAppearanceMap;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.SignalSystem;
import jmri.implementation.DefaultSignalAppearanceMap;
import jmri.implementation.SignalHeadSignalMast;
import jmri.swing.NamedBeanComboBox;
import jmri.util.StringUtil;
import jmri.util.javaworld.GridLayout2;

/**
 * A pane for configuring SignalHeadSignalMast objects.
 *
 * @see jmri.jmrit.beantable.signalmast.SignalMastAddPane
 * @author Bob Jacobsen Copyright (C) 2018
 * @since 4.11.2
 */
public class SignalHeadSignalMastAddPane extends SignalMastAddPane {

    public SignalHeadSignalMastAddPane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // lit/unlit controls
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel(Bundle.getMessage("AllowUnLitLabel") + ": "));
        p.add(allowUnLit);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(p);

        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        border.setTitle(Bundle.getMessage("MenuItemSignalTable")); // Signal Heads
        signalHeadPanel.setBorder(border);
        add(signalHeadPanel);
        
        includeUsed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshHeadComboBox();
            }
        });

        // disabled aspects controls
        TitledBorder disableborder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        disableborder.setTitle(Bundle.getMessage("DisableAspectsLabel"));
        JScrollPane disabledAspectsScroll = new JScrollPane(disabledAspectsPanel);
        disabledAspectsScroll.setBorder(disableborder);
        add(disabledAspectsScroll);
    }
    
    /** {@inheritDoc} */
    @Override
    @Nonnull public String getPaneName() {
        return Bundle.getMessage("HeadCtlMast");
    }
    
    
    JPanel signalHeadPanel = new JPanel();
    ArrayList<NamedBeanComboBox<SignalHead>> headList = new ArrayList<>(5);
    JCheckBox includeUsed = new JCheckBox(Bundle.getMessage("IncludeUsedHeads"));

    JCheckBox allowUnLit = new JCheckBox();

    LinkedHashMap<String, JCheckBox> disabledAspects = new LinkedHashMap<>(NOTIONAL_ASPECT_COUNT);
    JPanel disabledAspectsPanel = new JPanel();

    SignalHeadSignalMast currentMast = null;
    Set<SignalHead> alreadyUsed = new HashSet<>();
    DefaultSignalAppearanceMap map; 

    /** {@inheritDoc} */
    @Override
    public void setAspectNames(@Nonnull SignalAppearanceMap newMap, @Nonnull SignalSystem sigSystem) {
        log.debug("setAspectNames(...)");

        map = (DefaultSignalAppearanceMap)newMap;
        
        int count = map.getAspectSettings(map.getAspects().nextElement()).length;
        log.trace(" head count is {}", count);

        Enumeration<String> aspects = map.getAspects();

        headList = new ArrayList<>(count);

        signalHeadPanel.removeAll();
        for (int i = 0; i < count; i++) {
            NamedBeanComboBox<SignalHead> head = new NamedBeanComboBox<>(InstanceManager.getDefault(SignalHeadManager.class));
            head.setExcludedItems(alreadyUsed);
            headList.add(head);
            signalHeadPanel.add(head);
        }
        signalHeadPanel.add(includeUsed);

        signalHeadPanel.setLayout(new GridLayout2(0, 1)); // 0 means enough
        signalHeadPanel.revalidate();

        disabledAspects = new LinkedHashMap<>(10);
        disabledAspectsPanel.removeAll();
        while (aspects.hasMoreElements()) {
            String aspect = aspects.nextElement();
            JCheckBox disabled = new JCheckBox(aspect);
            disabledAspects.put(aspect, disabled);
        }

        for (Map.Entry<String, JCheckBox> entry : disabledAspects.entrySet()) {
            disabledAspectsPanel.add(entry.getValue());
        }

        disabledAspectsPanel.setLayout(new GridLayout2(0, 1)); // 0 means enough
        disabledAspectsPanel.revalidate();
    }

    /** {@inheritDoc} */
    @Override
    public boolean canHandleMast(@Nonnull SignalMast mast) {
        return mast instanceof SignalHeadSignalMast;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setMast(SignalMast mast) { 
        log.debug("setMast({})", mast);
        if (mast == null) { 
            currentMast = null; 
            return; 
        }
        
        if (! (mast instanceof SignalHeadSignalMast) ) {
            log.error("mast was wrong type: {} {}", mast.getSystemName(), mast.getClass().getName());
            return;
        }

        currentMast = (SignalHeadSignalMast) mast;

        // can't actually edit the heads in this kind of mast
        int count = map.getAspectSettings(map.getAspects().nextElement()).length;
        log.trace(" head count is {}", count);
        signalHeadPanel.removeAll();
        signalHeadPanel.setLayout(new GridLayout2(count + 1, 1));
        for (int i = 0; i < count; i++) {
            NamedBeanComboBox<SignalHead> head = new NamedBeanComboBox<>(InstanceManager.getDefault(SignalHeadManager.class));
            head.setExcludedItems(alreadyUsed);
            headList.add(head);

            head.setEnabled(false);
            head.setSelectedItem(currentMast.getHeadsUsed().get(i).getBean().getDisplayName()); // must match NamedBeanComboBox above
            signalHeadPanel.add(head);
        }
        signalHeadPanel.add(includeUsed);
        signalHeadPanel.revalidate();

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

    /** {@inheritDoc} */
    @Override
    public boolean createMast(@Nonnull String sigsysname, @Nonnull String mastname, @Nonnull String username) {
        log.debug("createMast({},{})", sigsysname, mastname);
        String name;
        if (currentMast == null) {
                StringBuilder build = new StringBuilder();
                build.append("IF$shsm:").append(sigsysname).append(":").append(mastname.substring(11, mastname.length() - 4));
                for (NamedBeanComboBox<SignalHead> head : headList) {
                    if (head != null && head.getSelectedItemDisplayName() != null) {
                        build.append("(").append(StringUtil.parenQuote(head.getSelectedItemDisplayName())).append(")");
                    }
                }
                name = build.toString();
                log.debug("add signal: {}", name);
                
                // see if it exists (remember, we're not handling a current mast)
                SignalMast m = InstanceManager.getDefault(SignalMastManager.class).getSignalMast(name);
                if (m != null) {
                    JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(Bundle.getMessage("DuplicateMast"),
                            new Object[]{m.getDisplayName()}), Bundle.getMessage("DuplicateMastTitle"), JOptionPane.INFORMATION_MESSAGE);
                    return false;
                }
                try {
                    // now create it
                    currentMast = (SignalHeadSignalMast)InstanceManager.getDefault(SignalMastManager.class).provideSignalMast(name);
                } catch (IllegalArgumentException ex) {
                    // user input no good
                    handleCreateException(name);
                    return false; // without creating
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

        // heads are attached via the system name
        if (!username.equals("")) {
            currentMast.setUserName(username);
        }
        
        currentMast.setAllowUnLit(allowUnLit.isSelected());
        
        return true;
    }

    protected void refreshHeadComboBox() {
        log.trace("refreshHeadComboBox");
        if (includeUsed.isSelected()) {
            alreadyUsed = new HashSet<>();
        } else {
            List<SignalHead> alreadyUsedHeads = SignalHeadSignalMast.getSignalHeadsUsed();
            alreadyUsed = new HashSet<>();
            log.trace("   found {}", alreadyUsedHeads.size());
            for (SignalHead head : alreadyUsedHeads) {
                alreadyUsed.add(head);
            }
        }

        for (NamedBeanComboBox<SignalHead> head : headList) {
            head.setExcludedItems(alreadyUsed);
        }
    }
    
    void handleCreateException(String sysName) {
        JOptionPane.showMessageDialog(null,
                Bundle.getMessage("ErrorSignalMastAddFailed", sysName) + "\n" + Bundle.getMessage("ErrorAddFailedCheck"),
                Bundle.getMessage("ErrorTitle"),
                JOptionPane.ERROR_MESSAGE);
    }

    @ServiceProvider(service = SignalMastAddPane.SignalMastAddPaneProvider.class)
    static public class SignalMastAddPaneProvider extends SignalMastAddPane.SignalMastAddPaneProvider {
        /** {@inheritDoc} */
        @Override
        @Nonnull public String getPaneName() {
            return Bundle.getMessage("HeadCtlMast");
        }
        /** {@inheritDoc} */
        @Override
        @Nonnull public SignalMastAddPane getNewPane() {
            return new SignalHeadSignalMastAddPane();
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SignalHeadSignalMastAddPane.class);

}

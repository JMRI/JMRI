package jmri.jmrit.beantable.signalmast;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import jmri.*;
import jmri.implementation.*;
import jmri.util.*;
import jmri.util.swing.*;

import org.openide.util.lookup.ServiceProvider;

/**
 * A pane for configuring SignalHeadSignalMast objects
 * <P>
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
    ArrayList<JmriBeanComboBox> headList = new ArrayList<>(5);
    JCheckBox includeUsed = new JCheckBox(Bundle.getMessage("IncludeUsedHeads"));

    JCheckBox allowUnLit = new JCheckBox();

    LinkedHashMap<String, JCheckBox> disabledAspects = new LinkedHashMap<>(NOTIONAL_ASPECT_COUNT);
    JPanel disabledAspectsPanel = new JPanel();

    SignalHeadSignalMast currentMast = null;
    List<NamedBean> alreadyUsed = new ArrayList<>();
    DefaultSignalAppearanceMap map; 

    /** {@inheritDoc} */
    @Override
    public void setAspectNames(@Nonnull
            SignalAppearanceMap newMap, SignalSystem sigSystem) {
        log.debug("setAspectNames(...)");

        map = (DefaultSignalAppearanceMap)newMap;
        
        int count = map.getAspectSettings(map.getAspects().nextElement()).length;
        log.trace(" head count is {}", count);

        Enumeration<String> aspects = map.getAspects();

        headList = new ArrayList<>(count);

        signalHeadPanel.removeAll();
        for (int i = 0; i < count; i++) {
            JmriBeanComboBox head = new JmriBeanComboBox(InstanceManager.getDefault(jmri.SignalHeadManager.class));
            head.excludeItems(alreadyUsed);
            headList.add(head);
            signalHeadPanel.add(head);
        }
        signalHeadPanel.add(includeUsed);

        signalHeadPanel.setLayout(new jmri.util.javaworld.GridLayout2(0, 1)); // 0 means enough
        signalHeadPanel.revalidate();

        disabledAspects = new LinkedHashMap<>(10);
        disabledAspectsPanel.removeAll();
        while (aspects.hasMoreElements()) {
            String aspect = aspects.nextElement();
            JCheckBox disabled = new JCheckBox(aspect);
            disabledAspects.put(aspect, disabled);
        }

        for (String aspect : disabledAspects.keySet()) {
            disabledAspectsPanel.add(disabledAspects.get(aspect));
        }

        disabledAspectsPanel.setLayout(new jmri.util.javaworld.GridLayout2(0, 1)); // 0 means enough
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
        signalHeadPanel.setLayout(new jmri.util.javaworld.GridLayout2(count + 1, 1));
        for (int i = 0; i < count; i++) {
            JmriBeanComboBox head = new JmriBeanComboBox(InstanceManager.getDefault(jmri.SignalHeadManager.class));
            head.excludeItems(alreadyUsed);
            headList.add(head);

            head.setEnabled(false);
            head.setSelectedItem(currentMast.getHeadsUsed().get(i).getBean().getDisplayName()); // must match JmriBeanComboBox above
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
                for (JmriBeanComboBox head : headList) {
                    if (head != null && head.getSelectedDisplayName() != null) {
                        build.append("(").append(StringUtil.parenQuote(head.getSelectedDisplayName())).append(")");
                    }
                }
                name = build.toString();
                log.debug("add signal: {}", name);
                
                // see if it exists (remember, we're not handling a current mast)
                SignalMast m = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(name);
                if (m != null) {
                    JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(Bundle.getMessage("DuplicateMast"),
                            new Object[]{m.getDisplayName()}), Bundle.getMessage("DuplicateMastTitle"), JOptionPane.INFORMATION_MESSAGE);
                    return false;
                }
                try {
                    // now create it
                    currentMast = (SignalHeadSignalMast)InstanceManager.getDefault(jmri.SignalMastManager.class).provideSignalMast(name);
                } catch (IllegalArgumentException ex) {
                    // user input no good
                    handleCreateException(name);
                    return false; // without creating
                }
        }
        
        // heads are attached via the system name

        for (String aspect : disabledAspects.keySet()) {
            if (disabledAspects.get(aspect).isSelected()) {
                currentMast.setAspectDisabled(aspect);
            } else {
                currentMast.setAspectEnabled(aspect);
            }
        }

        if (!username.equals("")) {
            currentMast.setUserName(username);
        }
        
        currentMast.setAllowUnLit(allowUnLit.isSelected());
        
        return true;
    }

    protected void refreshHeadComboBox() {
        log.trace("refreshHeadComboBox");
        if (includeUsed.isSelected()) {
            alreadyUsed = new ArrayList<>();
        } else {
            List<SignalHead> alreadyUsedHeads = SignalHeadSignalMast.getSignalHeadsUsed();
            alreadyUsed = new ArrayList<>();
            log.trace("   found {}", alreadyUsedHeads.size());
            for (SignalHead head : alreadyUsedHeads) {
                alreadyUsed.add(head);
            }
        }

        for (JmriBeanComboBox head : headList) {
            head.excludeItems(alreadyUsed);
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

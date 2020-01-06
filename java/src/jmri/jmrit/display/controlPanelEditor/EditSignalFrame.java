package jmri.jmrit.display.controlPanelEditor;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.SignalHeadManager;
import jmri.SignalMastManager;
import jmri.NamedBean.DisplayOptions;
import jmri.jmrit.display.PositionableIcon;
import jmri.jmrit.display.SignalHeadIcon;
import jmri.jmrit.display.SignalMastIcon;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.Portal;
import jmri.jmrit.picker.PickListModel;
//import jmri.swing.NamedBeanComboBox;

/**
 *
 * @author Pete Cressman Copyright: Copyright (c) 2019
 *
 */
public class EditSignalFrame extends EditFrame implements ListSelectionListener {

    private PortalIcon _portalIcon;

    private JTextField _mastName;
    private PortalList _portalList;
    private LengthPanel _lengthPanel;
    private Portal _currentPortal;
    OpenPickListButton<SignalMast> _pickMast;
    OpenPickListButton<SignalHead> _pickHead;

    public EditSignalFrame(String title, CircuitBuilder parent, OBlock block) {
        super(title, parent, block);
        pack();
        String msg = _parent.checkForTrackIcons(block, "BlockSignal");
        if (msg != null) {
            _canEdit = false;
        } else {
            msg = _parent.checkForPortals(block, "ItemTypeSignalMast");
        }
        if (msg != null) {
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("incompleteCircuit"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Override
    protected JPanel makeContentPanel() {
        JPanel signalPanel = new JPanel();
        signalPanel.setLayout(new BoxLayout(signalPanel, BoxLayout.Y_AXIS));

        JPanel panel = new JPanel();
        panel.add(new JLabel(Bundle.getMessage("PortalTitle", _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME))));
        signalPanel.add(panel);

        _portalList = new PortalList(_homeBlock, this);
        _portalList.addListSelectionListener(this);
        signalPanel.add(new JScrollPane(_portalList));
        signalPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        JButton clearButton = new JButton(Bundle.getMessage("buttonClearSelection"));
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                clearListSelection();
            }
        });
        panel = new JPanel();
        panel.add(clearButton);
        signalPanel.add(panel);
        signalPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        panel = new JPanel();
        _mastName = new JTextField();
        panel.add(CircuitBuilder.makeTextBoxPanel(false, _mastName, "mastName", true, null));
        _mastName.setPreferredSize(new Dimension(300, _mastName.getPreferredSize().height));
        _mastName.setToolTipText(Bundle.getMessage("ToolTipMastName", _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME)));
        signalPanel.add(panel);

        panel = new JPanel();
        JButton addButton = new JButton(Bundle.getMessage("ButtonAddMast"));
        addButton.addActionListener((ActionEvent a) -> {
            Portal portal = _portalList.getSelectedValue();
            if (portal != null) {
                addMast(portal, null, null);
            } else {
                JOptionPane.showMessageDialog(this, 
                        Bundle.getMessage("selectPortalProtection", _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME)),
                        Bundle.getMessage("configureSignal"), JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        });
        addButton.setToolTipText(Bundle.getMessage("ToolTipAddMast", _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME)));
        panel.add(addButton);

        JButton removeButton = new JButton(Bundle.getMessage("ButtonRemoveMast"));
        removeButton.addActionListener((ActionEvent a) -> {
            removeMast();
        });
        removeButton.setToolTipText(Bundle.getMessage("ToolTipRemoveMast", _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME)));
        panel.add(removeButton);
        signalPanel.add(panel);
        signalPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        String[] blurbLines = {Bundle.getMessage("DragMast", Bundle.getMessage("mastName"))};
        
        
        _pickMast = new OpenPickListButton<SignalMast>(blurbLines, PickListModel.signalMastPickModelInstance(), this);
        _pickHead = new OpenPickListButton<SignalHead>(blurbLines, PickListModel.signalHeadPickModelInstance(), this);
        panel = new JPanel();
        panel.add(_pickMast.getButtonPanel());
        panel.add(_pickHead.getButtonPanel());
        signalPanel.add(panel);
        signalPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        _lengthPanel = new LengthPanel(_homeBlock, "entranceSpace");
        _lengthPanel.changeUnits();
        signalPanel.add(_lengthPanel);
        signalPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(Bundle.getMessage("selectPortalProtection", _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME)));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        l = new JLabel(Bundle.getMessage("selectSignalMast"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        l = new JLabel(Bundle.getMessage("pressAddButton", Bundle.getMessage("ButtonAddMast")));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        l = new JLabel(Bundle.getMessage("positionMast"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        JPanel p = new JPanel();
        p.add(panel);
        signalPanel.add(p);

        signalPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        signalPanel.add(makeDoneButtonPanel());
        return signalPanel;
    }

    @Override
    protected void clearListSelection() {
        _portalList.clearSelection();
        _parent._editor.highlight(null);
        _mastName.setText(null);
    }

    protected void setSelected(PositionableIcon icon) {
        if (!canEdit()) {
            return;
        }
        String name = null;
        NamedBean mast = null;
        Portal portal = null;
        OBlock protectedBlock;
        if (icon instanceof PortalIcon) {
            portal = ((PortalIcon)icon).getPortal();
        } else if (icon instanceof SignalMastIcon) {
            mast = ((SignalMastIcon)icon).getSignalMast();
        } else if (icon instanceof SignalHeadIcon) {
            mast = ((SignalHeadIcon)icon).getSignalHead();
        } else {
            return;
        }
        _portalIcon = null;
        if (mast != null) {
            portal =_parent.getSignalPortal(mast);
            if (portal != null) {
                protectedBlock = portal.getProtectedBlock(mast);
                if (_homeBlock.equals(protectedBlock)) {
                    setPortalSelected(portal);
                    return;
                }
            }
        } else if (portal != null) {
            mast = portal.getSignalProtectingBlock(_homeBlock);
            if (mast == null) {
                if (_homeBlock.getPortals().contains(portal)) {
                    setPortalSelected(portal);
                }
            } else {
                setPortalSelected(portal);
            }
            return;
        }
        if (portal != null) {
            List<PortalIcon> piArray = _parent.getPortalIconMap(portal);
            if (!piArray.isEmpty()) {
                _portalIcon = piArray.get(0);
            }
        }
        if (mast !=null) {
            name = mast.getDisplayName();
            List<PositionableIcon> sigArray = _parent.getSignalIconMap(mast);
            if (sigArray.isEmpty()) {
                _parent._editor.highlight(_portalIcon);
            } else {
                _parent._editor.highlight(sigArray.get(0));
            }
            portal =_parent.getSignalPortal(mast);
            if (portal == null) {
                StringBuffer sb = new StringBuffer(Bundle.getMessage("unattachedMast", name, _homeBlock.getDisplayName()));
                sb.append("\n");
                sb.append(Bundle.getMessage("attachMast",
                        _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME)));
                int answer = JOptionPane.showConfirmDialog(this, sb.toString(), 
                        Bundle.getMessage("configureSignal"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (answer == JOptionPane.YES_OPTION) {
                    addUnattachedMast(mast, icon);
                } else {
                    _parent._editor.highlight(null);
                    return;
                }
            } else if (replaceQuestion(mast, portal, null)) {
                    addUnattachedMast(mast, icon);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("setSelected: signal {}", name);
        }
    }

    private void setPortalSelected(Portal portal) {
        List<Portal> list = _homeBlock.getPortals();
        for (int i = 0; i < list.size(); i++) {
            if (portal.equals(list.get(i))) {
                _portalList.setSelectedIndex(i);
                break;
            }
        }   // selection within currently configured _homeBlock
    }

    private void addUnattachedMast(NamedBean mast, PositionableIcon icon) {
        JDialog portalDialog = new JDialog(this, true);
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(Bundle.getMessage("PortalTitle", _homeBlock.getDisplayName()));
        contentPane.add(l);
        JPanel panel = new JPanel();
        JComboBox<Portal> box = new JComboBox<Portal>(_homeBlock.getPortals().toArray(new Portal[0]));
        panel.add(box);
        contentPane.add(panel);
        panel = new JPanel();
        l = new JLabel(Bundle.getMessage("selectPortalProtection", _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME)));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        contentPane.add(panel);
        panel = new JPanel();
        JButton addButton = new JButton(Bundle.getMessage("ButtonAddMast"));
        addButton.addActionListener(new ActionListener() {
            NamedBean mast;
            ActionListener init(NamedBean m) {
                mast = m;
                return this;
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                Portal p = (Portal)box.getSelectedItem();
                if (p != null) {
                    addMast(p, mast, icon);
                }
                portalDialog.dispose();
            }
            
        }.init(mast));
        panel.add(addButton);
        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
        cancelButton.addActionListener((ActionEvent a) -> {
            portalDialog.dispose();
        });
        panel.add(cancelButton);
        contentPane.add(panel);
        portalDialog.setContentPane(contentPane);
        portalDialog.pack();
        portalDialog.setLocation(jmri.util.PlaceWindow. nextTo(this, null, portalDialog));
        portalDialog.setVisible(true);
        
    }
    /**
     * *********************** end setup *************************
     */
    /*
     * Listener on list of Portals
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    @Override
//    @SuppressFBWarnings(value="NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification="NonNull mast implies portal is also NonNull")
    public void valueChanged(ListSelectionEvent e) {
        Portal portal = _portalList.getSelectedValue();
        if (log.isDebugEnabled()) {
            log.debug("valueChanged: portal = {}, _currentPortal = {}", (portal==null?"null":portal.getName()), 
                    (_currentPortal==null?"null":_currentPortal.getName()));
        }
        NamedBean mast = null;
        if (portal != null) {
            mast = portal.getSignalProtectingBlock(_homeBlock);
            _lengthPanel.setLength(portal.getEntranceSpaceForBlock(_homeBlock));
            if (_currentPortal != null) {
                checkForSaveMast(portal);
            }
        }
        if (_portalIcon == null) {
            _parent._editor.highlight(null);
        }
        _currentPortal = portal;
        if (portal != null) {
            _lengthPanel.setLength(portal.getEntranceSpaceForBlock(_homeBlock));
           List<PortalIcon> piArray = _parent.getPortalIconMap(portal);
           if (!piArray.isEmpty()) {
               _portalIcon = piArray.get(0);
           }
        }
        if (mast != null) {
            _mastName.setText(mast.getDisplayName(DisplayOptions.DISPLAYNAME));
           List<PositionableIcon> sigArray = _parent.getSignalIconMap(mast);
            if (sigArray.isEmpty()) {
                _parent._editor.highlight(null);
            } else {
                PositionableIcon icon = sigArray.get(0);
                _parent._editor.highlight(icon);
            }
        } else {
            _mastName.setText(null);
            if (portal != null) {
                _parent._editor.highlight(_portalIcon);
            }
            _lengthPanel.setLength(0);
        }
    }

    private boolean replaceQuestion(@Nonnull NamedBean mast, Portal portal, Portal newPortal) {
        OBlock b = portal.getProtectedBlock(mast);
        StringBuffer sb = new StringBuffer(Bundle.getMessage("mastProtectsPortal", 
                mast.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME), 
                b.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME),
                portal.getName()));
        sb.append("\n");
        if (newPortal != null) {
            sb.append(Bundle.getMessage("switchProtection",
                    _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME),
                    newPortal.getName()));
        } else {
            sb.append(Bundle.getMessage("attachMast",
                    _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME)));
        }
        int answer = JOptionPane.showConfirmDialog(this,  sb.toString(),
                Bundle.getMessage("configureSignal"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) {
            return false;
        }
        portal.setProtectSignal(null, 0, b);
        _parent.putSignalPortal(mast, null);
        return true;
    }

    private void addMast(@Nonnull Portal portal, NamedBean newMast, PositionableIcon icon) {
        if (newMast == null) {
            String name = _mastName.getText();
            newMast = InstanceManager.getDefault(SignalMastManager.class).getSignalMast(name);
            if (newMast == null) {
                newMast = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(name);
                if (newMast == null) {
                    JOptionPane.showMessageDialog(this, Bundle.getMessage("selectSignalMast"),
                            Bundle.getMessage("configureSignal"), JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("addMast \"{}\" icon ={}", newMast.getDisplayName(), (icon==null?"null":icon.getName()));
        }
        
        Portal p = _parent.getSignalPortal(newMast);
        if (p != null && !p.equals(portal)) {
            if (!replaceQuestion(newMast, p, portal)) {
                return;
            }
        }
        NamedBean oldMast = portal.getSignalProtectingBlock(_homeBlock);
        if (oldMast != null) {
            if (oldMast.equals(newMast)) {
                portal.setEntranceSpaceForBlock(_homeBlock, _lengthPanel.getLength());
            } else {
                int answer = JOptionPane.showConfirmDialog(this, 
                        Bundle.getMessage("replaceSignalMast", oldMast.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME), 
                                newMast.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME), 
                                portal.getName()), 
                        Bundle.getMessage("configureSignal"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (answer != JOptionPane.YES_OPTION) {
                    return;
                }
            }
        }
        if (newMast instanceof SignalMast) {
            SignalMast mast = (SignalMast)newMast;
            if (mast.getAspect() == null) {
                mast.setAspect(mast.getValidAspects().get(0));
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("addMast state = {}", newMast.getState());
            if (newMast instanceof SignalHead) {
                log.debug("appearance = {}", ((SignalHead)newMast).getAppearanceName());
            } else {
                log.debug("aspect = {}", ((SignalMast)newMast).getAspect());
            }
        }
        portal.setProtectSignal(newMast, _lengthPanel.getLength(), _homeBlock);
        _parent.putSignalPortal(newMast, portal);

        List<PositionableIcon> mastIcons = _parent.getSignalIconMap(newMast);
        if (icon == null) {
            if (!mastIcons.isEmpty()) {
                icon = mastIcons.get(0);
            } else if (newMast instanceof SignalMast) {
                icon = new SignalMastIcon(_parent._editor);
                ((SignalMastIcon)icon).setSignalMast(newMast.getDisplayName());
            } else if (newMast instanceof SignalHead) {
                icon = new SignalHeadIcon(_parent._editor);
                ((SignalHeadIcon)icon).setSignalHead(newMast.getDisplayName());
            } else {
                return;
            }
            _parent._editor.putItem(icon);
        }
        List<PortalIcon> portalIcons = _parent.getPortalIconMap(portal);
        if (!portalIcons.isEmpty()) {
            _portalIcon = portalIcons.get(0);
            icon.setDisplayLevel(_portalIcon.getDisplayLevel());
            icon.setLocation(_portalIcon.getLocation());
        }
        _parent._editor.highlight(icon);
        icon.updateSize();
        if (!mastIcons.contains(icon)) {
            mastIcons.add(icon);
        }
        _parent.getCircuitIcons(_homeBlock).add(icon);
    }

    private void removeMast() {
        Portal portal = _portalList.getSelectedValue();
        NamedBean oldMast = null;
        if (portal != null) {
            oldMast = portal.getSignalProtectingBlock(_homeBlock);
        } else {
            JOptionPane.showMessageDialog(this, 
                    Bundle.getMessage("selectPortalProtection", _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME)),
                    Bundle.getMessage("configureSignal"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (oldMast != null) {
            int answer = JOptionPane.showConfirmDialog(this, 
                    Bundle.getMessage("removeSignalMast", oldMast.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME), 
                            portal.getName()), 
                    Bundle.getMessage("configureSignal"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (answer == JOptionPane.YES_OPTION) {
                portal.setProtectSignal(null, 0, _homeBlock);
                _parent.putSignalPortal(oldMast, null);
                _mastName.setText(null);
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                    Bundle.getMessage("noPortalProtection", _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME),
                            portal.getName()),
                    Bundle.getMessage("configureSignal"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void checkForSaveMast(@Nonnull Portal portal) {
        String name = _mastName.getText();
        if (name.trim().length() == 0) {
            return;
        }
        NamedBean newMast = InstanceManager.getDefault(SignalMastManager.class).getSignalMast(name);
        if (newMast == null) {
            newMast = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(name);
            if (newMast == null) {
                return;
            }
        }
        NamedBean currentMast =_currentPortal.getSignalProtectingBlock(_homeBlock);
        String msg = null;
        String mastName = null;
        if (currentMast != null) {
            mastName = currentMast.getDisplayName(DisplayOptions.DISPLAYNAME);
        }
        if (mastName == null) {
            msg = Bundle.getMessage("noMast", portal.getName(),
                    newMast.getDisplayName(DisplayOptions.DISPLAYNAME));
        } else {
            if (!newMast.equals(currentMast)) {
                msg = Bundle.getMessage("differentSignals", mastName, portal.getName(),
                        newMast.getDisplayName(DisplayOptions.DISPLAYNAME));
            } else if (!name.equals(mastName)) {
                msg = Bundle.getMessage("differentName", mastName, name);
            } else {
                if (_lengthPanel.isChanged()) {
                    msg = Bundle.getMessage("spaceChanged", mastName);
                }
            }
        }
        if (msg != null) {
            StringBuilder sb = new StringBuilder(msg);
            sb.append("\n");
            sb.append(Bundle.getMessage("saveChanges"));
            int answer = JOptionPane.showConfirmDialog(this, sb.toString(), Bundle.getMessage("configureSignal"),
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (answer == JOptionPane.YES_OPTION) {
                addMast(_currentPortal, newMast, null);
            }
            _lengthPanel.setChanged(false);
        }
    }

    @Override
    protected void closingEvent(boolean close) {
        if (_currentPortal != null) {
            checkForSaveMast(_currentPortal);
        }
        String msg = _parent.checkForPortals(_homeBlock, "ItemTypeSignalMast");
        closingEvent(close, msg);
        if (_pickMast != null) {
            _pickMast.closePickList();
        }
        if (_pickHead != null) {
            _pickHead.closePickList();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(EditSignalFrame.class);
}

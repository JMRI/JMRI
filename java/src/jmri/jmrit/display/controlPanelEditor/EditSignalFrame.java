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
import javax.swing.JComponent;
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
                _portalList.clearSelection();
                _parent._editor.highlight(null);
                _mastName.setText(null);
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
            String msg = null;
            if (portal != null) {
                NamedBean signal = getSignal();
                if (signal != null) {
                    if (addMastQuestion(signal, portal)) {
                        addMast(portal, signal);
                    }
                } else {
                    String name = _mastName.getText().trim();
                    if ( name.length()==0) {
                        msg = Bundle.getMessage("selectSignalMast", Bundle.getMessage("mastName"));
                    } else {
                        msg = Bundle.getMessage("NotFound", name);
                    }
                }
            } else {
                msg = Bundle.getMessage("selectPortalProtection", _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME));
            }
            if (msg != null) {
                JOptionPane.showMessageDialog(this, msg,
                        Bundle.getMessage("configureSignal"), JOptionPane.INFORMATION_MESSAGE);
            }
        });
        addButton.setToolTipText(Bundle.getMessage("ToolTipAddMast", _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME)));
        panel.add(addButton);

        JButton removeButton = new JButton(Bundle.getMessage("ButtonRemoveMast"));
        removeButton.addActionListener((ActionEvent a) -> {
            removeMast(true, _portalList.getSelectedValue());
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

    protected void setSelected(PositionableIcon icon) {
        if (!canEdit()) {
            return;
        }
        NamedBean mast = null;
        Portal portal = null;
        if (icon instanceof PortalIcon) {
            portal = ((PortalIcon)icon).getPortal();
        } else if (icon instanceof SignalMastIcon) {
            mast = ((SignalMastIcon)icon).getSignalMast();
        } else if (icon instanceof SignalHeadIcon) {
            mast = ((SignalHeadIcon)icon).getSignalHead();
        } else {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("setSelected portal= \"{}\" mast ={}", (portal!=null?portal.getName():"null"),(mast!=null?mast.getDisplayName():"null"));
        }
        _portalIcon = null;
        if (mast != null) {
            portal =_parent.getSignalPortal(mast);
            if (portal != null) {
                OBlock protectedBlock = portal.getProtectedBlock(mast);
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
            }
            return;
        }
        if (portal != null) {
            List<PortalIcon> piArray = _parent.getPortalIconMap(portal);
            if (!piArray.isEmpty()) {
                _portalIcon = piArray.get(0);
                _parent._editor.highlight(_portalIcon);
            }
        }
        if (mast !=null) {
            setMastNameAndIcon(mast);
        }
    }
    
    private void setMastNameAndIcon(NamedBean mast) {
        _mastName.setText(mast.getDisplayName(DisplayOptions.DISPLAYNAME));
       List<PositionableIcon> sigArray = _parent.getSignalIconMap(mast);
        if (sigArray.isEmpty()) {
            _parent._editor.highlight(null);
        } else {
            PositionableIcon icon = sigArray.get(0);
            _parent._editor.highlight(icon);
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

    /**
     * *********************** end setup *************************
     */
    /*
     * Listener on list of Portals
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        Portal portal = _portalList.getSelectedValue();
        if (log.isDebugEnabled()) {
            log.debug("valueChanged: portal = {}, _currentPortal = {}", (portal==null?"null":portal.getName()), 
                    (_currentPortal==null?"null":_currentPortal.getName()));
        }
        NamedBean mast = null;
        if (portal != null) {
            if (!portal.equals(_currentPortal)) {
                
                String msg = checkMastForSave(false);
                if (msg != null && msg.length() > 0) {
                    StringBuffer sb = new StringBuffer(msg);
                    sb.append("\n");
                    sb.append(Bundle.getMessage("saveChanges"));
                    int answer = JOptionPane.showConfirmDialog(this, sb.toString(), Bundle.getMessage("configureSignal"),
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (answer == JOptionPane.YES_OPTION) {
                        mast = getSignal();
                        if (mast != null) {
                            addMast(_currentPortal, mast);
                        } else {
                            removeMast(false, _currentPortal);
                        }
                    }
                }
            }
            mast = portal.getSignalProtectingBlock(_homeBlock);
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
            setMastNameAndIcon(mast);
        } else {
            _mastName.setText(null);
            if (portal != null) {
                _parent._editor.highlight(_portalIcon);
            }
            _lengthPanel.setLength(0);
        }
    }

    // Called from ButtonAddMast
    private boolean addMastQuestion(@Nonnull NamedBean mast, @Nonnull Portal homePortal) {
        StringBuffer sb = new StringBuffer();
        Portal portal = _parent.getSignalPortal(mast);
        OBlock blk = null;
        if (portal != null) {
            blk = portal.getProtectedBlock(mast);
            if (blk != null && !blk.equals(_homeBlock)) {
                sb.append(Bundle.getMessage("mastProtectsPortal", 
                        mast.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME), 
                        blk.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME),
                        portal.getName()));
                sb.append("\n");
            }
        }
        NamedBean homeMast = homePortal.getSignalProtectingBlock(_homeBlock);
        if (homeMast != null) {
            if (homeMast.equals(mast)) {
                // no changes needed except for length.  So do it now and skip the rest of AddMast()
                homePortal.setEntranceSpaceForBlock(_homeBlock, _lengthPanel.getLength());
                return false;
            } else {
                sb.append(Bundle.getMessage("replaceSignalMast", homeMast.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME), 
                                mast.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME), homePortal.getName()));
                sb.append("\n");
            }
        } else if (sb.length() > 0) {
            sb.append(Bundle.getMessage("attachMast", mast.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME),
                    _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME), homePortal.getName()));
        }
        if (sb.length() > 0) {
            int answer = JOptionPane.showConfirmDialog(this,  sb.toString(),
                    Bundle.getMessage("configureSignal"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (answer != JOptionPane.YES_OPTION) {
                return false;   // Skip the rest of AddMast()
            }
        }
        if (homeMast != null) {
            homePortal.setProtectSignal(null, 0, blk);
        }
        _parent.putSignalPortal(mast, null);
        return true;
    }

    private NamedBean getSignal() {
        String name = _mastName.getText();
        if (name.trim().length() == 0) {
            return null;
        }
        NamedBean signal = InstanceManager.getDefault(SignalMastManager.class).getSignalMast(name);
        if (signal == null) {
            signal = InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(name);
        }
        return signal;
    }
    // Called from: 
    // ConfigureButton -    addMast(portal, mast); portal from portal list, mast from name field
    private void addMast(@Nonnull Portal portal, @Nonnull NamedBean newMast) {
        if (log.isDebugEnabled()) {
            log.debug("addMast \"{}\" icon ={}", newMast.getDisplayName());
        }
        if (newMast instanceof SignalMast) {
            SignalMast mast = (SignalMast)newMast;
            if (mast.getAspect() == null) {
                mast.setAspect(mast.getValidAspects().get(0));
            }
        }
        if (log.isDebugEnabled()) {
            if (newMast instanceof SignalHead) {
                log.debug("addMast SignalHead state= {}, appearance= {}", ((SignalHead)newMast).getAppearanceName());
            } else {
                log.debug("addMast SignalMast state= {}, aspect= {}", ((SignalMast)newMast).getAspect());
            }
        }
        portal.setProtectSignal(newMast, _lengthPanel.getLength(), _homeBlock);
        _parent.putSignalPortal(newMast, portal);
        setMastIcon(newMast, portal);
    }

    private void setMastIcon(NamedBean newMast,  Portal portal) {
        List<PositionableIcon> mastIcons = _parent.getSignalIconMap(newMast);
        PositionableIcon icon = null;
        if (!mastIcons.isEmpty()) {
            icon = mastIcons.get(0);
        } else if (newMast instanceof SignalMast) {
            icon = new SignalMastIcon(_parent._editor);
            ((SignalMastIcon)icon).setSignalMast(newMast.getDisplayName());
            _parent._editor.putItem(icon);
        } else if (newMast instanceof SignalHead) {
            icon = new SignalHeadIcon(_parent._editor);
            ((SignalHeadIcon)icon).setSignalHead(newMast.getDisplayName());
            _parent._editor.putItem(icon);
        } else {
            return;
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

    private void removeMast(boolean confirm,  Portal portal) {
        NamedBean oldMast = null;
        if (portal != null) {
            oldMast = portal.getSignalProtectingBlock(_homeBlock);
        } else {
            _mastName.setText(null);
            return;
        }
        if (confirm) {
            if (oldMast != null) {
                int answer = JOptionPane.showConfirmDialog(this, 
                        Bundle.getMessage("removeSignalMast", oldMast.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME), 
                                portal.getName()), 
                        Bundle.getMessage("configureSignal"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (answer == JOptionPane.YES_OPTION) {
                    _mastName.setText(null);    // do before portal triggers propertyChange
                    removeMastAtPortal(oldMast, portal);
                } else {
                    _mastName.setText(oldMast.getDisplayName(DisplayOptions.DISPLAYNAME));
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                        Bundle.getMessage("noPortalProtection", _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME),
                                portal.getName()),
                        Bundle.getMessage("configureSignal"), JOptionPane.INFORMATION_MESSAGE);
            }
        } else if (oldMast != null) {
            removeMastAtPortal(oldMast, portal);
        }
    }
    private void removeMastAtPortal(NamedBean oldMast, Portal portal) {
        portal.setProtectSignal(null, 0, _homeBlock);
        _parent.putSignalPortal(oldMast, null);
        _mastName.setText(null);
    }

    /**
     * Check for questions about configuring this signal 
     * @param textMast = getSignal() (results of _mastName.getText())
     * @param portal = _portalList.getSelectedValue()
     * @return message of any concerns.
     */
    private String checkMastForSave(boolean atClose) {
        Portal listPortal;
        if (atClose) {
            listPortal = _portalList.getSelectedValue();
        } else {
            listPortal = _currentPortal;
        }
        if (listPortal == null) {
            return null;
        }
        NamedBean textMast = getSignal();
        NamedBean listMast = listPortal.getSignalProtectingBlock(_homeBlock);
        StringBuffer sb = new StringBuffer();
        if (textMast == null) {
            if (listMast != null) {
                sb.append(Bundle.getMessage("removeMastQuery", 
                        listMast.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME), listPortal.getName()));
            }
            return sb.toString();
        }
        Portal textPortal = _parent.getSignalPortal(textMast);
        if (log.isDebugEnabled()) {
            log.debug("textMast= {}, textMastPortal= {}, portalSelection= {}, currentMast= {}", 
                    textMast.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME),
                    (textPortal==null?"null":textPortal.getName()), listPortal.getName(),
                    (listMast==null?"null":listMast.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME)));
        }
        if (!listPortal.equals(textPortal)) {
            listMastMessage(sb, listMast, listPortal, textMast, textPortal);
            if (listMast != null)  {
                sb.append(Bundle.getMessage("replaceSignalMast", listMast.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME), 
                        textMast.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME), listPortal.getName()));
            }
        } else if (_lengthPanel.isChanged(listPortal.getEntranceSpaceForBlock(_homeBlock))) {
            sb.append(Bundle.getMessage("spaceChanged", textMast.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME), listPortal.getName()));
        }
        return sb.toString();
    }
    private void listMastMessage(StringBuffer sb, 
            NamedBean listMast, @Nonnull Portal listPortal, 
            @Nonnull NamedBean textMast, Portal textPortal) {
        if (listMast != null) {
            textMastMessage(sb, textMast, textPortal);
            sb.append(Bundle.getMessage("mastProtectsPortal", listMast.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME),
                    _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME), listPortal.getName()));
            sb.append("\n");
        } else {
            textMastMessage(sb, textMast, textPortal);
            sb.append(Bundle.getMessage("noPortalProtection",
                    _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME), listPortal.getName()));            
            sb.append("\n");
            sb.append(Bundle.getMessage("attachMast", textMast.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME),
                    _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME), listPortal.getName()));
        }
    }
    private void textMastMessage(StringBuffer sb, @Nonnull NamedBean textMast, Portal textPortal) {
        if (textPortal != null) {
            OBlock blk = textPortal.getProtectedBlock(textMast);
            if (blk != null) {
                sb.append(Bundle.getMessage("mastProtectsPortal", 
                        textMast.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME), 
                        blk.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME), textPortal.getName()));
                sb.append("\n");
            }                
        }
    }

    @Override
    protected void closingEvent(boolean close) {
        StringBuffer sb = new StringBuffer();
        String msg = _parent.checkForPortals(_homeBlock, "ItemTypeSignalMast");
        if (msg != null) {
            sb.append(msg);
            sb.append("\n");
        }
        msg = checkMastForSave(true);
        if (msg != null && msg.length() > 0) {
            sb.append(msg);
            sb.append("\n");
        }
        closingEvent(close, sb.toString());
        if (_pickMast != null) {
            _pickMast.closePickList();
        }
        if (_pickHead != null) {
            _pickHead.closePickList();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(EditSignalFrame.class);
}

package jmri.jmrit.display.controlPanelEditor;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
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
import jmri.SignalAppearanceMap;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.SignalHeadManager;
import jmri.SignalMastManager;
import jmri.NamedBean.DisplayOptions;
import jmri.jmrit.beantable.AbstractTableAction;
import jmri.jmrit.beantable.BeanTableFrame;
import jmri.jmrit.beantable.SignalHeadTableAction;
import jmri.jmrit.beantable.SignalMastTableAction;
import jmri.jmrit.catalog.DragJLabel;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.PositionableIcon;
import jmri.jmrit.display.SignalHeadIcon;
import jmri.jmrit.display.SignalMastIcon;
import jmri.jmrit.display.palette.ItemPalette;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.Portal;
import jmri.jmrit.picker.PickListModel;

/**
 *
 * @author Pete Cressman Copyright: Copyright (c) 2019
 *
 */
public class EditSignalFrame extends EditFrame {

    private PortalIcon _portalIcon;

    private JTextField _mastName;
    private PortalList _portalList;
    private SignalList _signalList;
    private LengthPanel _lengthPanel;
    private Portal _currentPortal;
    OpenPickListButton<SignalMast> _pickMast;
    OpenPickListButton<SignalHead> _pickHead;
    AbstractTableAction<SignalMast> _mastTableAction;
    AbstractTableAction<SignalHead> _headTableAction;
    JPanel _dndPanel;
    private IconDragJLabel _dragLabel;

    public EditSignalFrame(String title, CircuitBuilder parent, OBlock block) {
        super(title, parent, block);
        checkCircuitIcons("BlockSignal");
        pack();
    }

    @Override
    protected JPanel makeContentPanel() {
        JPanel signalPanel = new JPanel();
        signalPanel.setLayout(new BoxLayout(signalPanel, BoxLayout.Y_AXIS));

        JPanel panel = new JPanel();
        panel.add(new JLabel(Bundle.getMessage("PortalTitle", _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME))));
        signalPanel.add(panel);

        _portalList = new PortalList(_homeBlock, this);
        _portalList.addListSelectionListener(new PortalListListener(this));
        signalPanel.add(new JScrollPane(_portalList));
        signalPanel.add(Box.createVerticalStrut(STRUT_SIZE / 2));

        panel = new JPanel();
        panel.add(new JLabel(Bundle.getMessage("SignalTitle", _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME))));
        signalPanel.add(panel);

        _signalList = new SignalList(_homeBlock, this);
        _signalList.addListSelectionListener(new SignalListListener(this));
        signalPanel.add(new JScrollPane(_signalList));
        signalPanel.add(Box.createVerticalStrut(STRUT_SIZE / 2));

        JButton clearButton = new JButton(Bundle.getMessage("buttonClearSelection"));
        clearButton.addActionListener(a -> {
            _portalList.clearSelection();
            _signalList.clearSelection();
            _parent._editor.highlight(null);
            _mastName.setText(null);
        });

        panel = new JPanel();
        panel.add(clearButton);
        signalPanel.add(panel);
        signalPanel.add(Box.createVerticalStrut(STRUT_SIZE / 2));

        JPanel framingPanel = new JPanel();
        JPanel mastConfigPanel = new JPanel();
        // set border to group items in UI
        mastConfigPanel.setBorder(BorderFactory.createEtchedBorder());
        mastConfigPanel.setLayout(new BoxLayout(mastConfigPanel, BoxLayout.Y_AXIS));

        panel = new JPanel();
        _mastName = new JTextField();
        panel.add(CircuitBuilder.makeTextBoxPanel(false, _mastName, "mastName", true, null));
        _mastName.setPreferredSize(new Dimension(300, _mastName.getPreferredSize().height));
        _mastName.setToolTipText(Bundle.getMessage("ToolTipMastName", _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME)));
        mastConfigPanel.add(panel);

        _lengthPanel = new LengthPanel(_homeBlock, LengthPanel.ENTRANCE_SPACE, "OffsetToolTip");
        _lengthPanel.changeUnits();
        mastConfigPanel.add(_lengthPanel);

        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton(Bundle.getMessage("ButtonAddMast"));
        addButton.addActionListener((ActionEvent a) -> addMast());
        addButton.setToolTipText(Bundle.getMessage("ToolTipAddMast", _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME)));
        buttonPanel.add(addButton);
/*
        JButton button = new JButton(Bundle.getMessage("buttonChangeName"));
        button.addActionListener((ActionEvent a) -> changeName(null));
        button.setToolTipText(Bundle.getMessage("ToolTipChangeName", _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME)));
        panel.add(button);*/

        JButton buttonRemove = new JButton(Bundle.getMessage("ButtonRemoveMast"));
        buttonRemove.addActionListener((ActionEvent a) -> removeMast());
        buttonRemove.setToolTipText(Bundle.getMessage("ToolTipRemoveMast", _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME)));
        buttonPanel.add(buttonRemove);

        mastConfigPanel.add(buttonPanel);
        // border up to here
        framingPanel.add(mastConfigPanel);
        signalPanel.add(framingPanel);

        signalPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(Bundle.getMessage("addSignalConfig"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        l = new JLabel(Bundle.getMessage("selectSignalMast"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        l = new JLabel(Bundle.getMessage("pressConfigure", Bundle.getMessage("ButtonAddMast")));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        panel.add(Box.createVerticalStrut(STRUT_SIZE / 2));
        l = new JLabel(Bundle.getMessage("addSignalIcon"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        l = new JLabel(Bundle.getMessage("positionMast"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        JPanel p = new JPanel();
        p.add(panel);
        signalPanel.add(p);
        signalPanel.add(Box.createVerticalStrut(STRUT_SIZE / 2));

        panel = new JPanel();
        l = new JLabel(Bundle.getMessage("recommendMasts"));
        panel.add(l);
        signalPanel.add(panel);
        signalPanel.add(Box.createVerticalStrut(STRUT_SIZE / 2));

        String[] blurbLines = {Bundle.getMessage("DragMast", Bundle.getMessage("mastName"))};
        
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        _pickMast = new OpenPickListButton<>(blurbLines, PickListModel.signalMastPickModelInstance(), 
                this, Bundle.getMessage("OpenPicklist", Bundle.getMessage("BeanNameSignalMast")));
        _mastTableAction = new SignalMastTableAction(Bundle.getMessage("ButtonCreateMast"));
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(_pickMast.getButtonPanel());
        JPanel pp = new JPanel();
        pp.setLayout(new FlowLayout());
        JButton buttonCreate = new JButton(Bundle.getMessage("ButtonCreateMast"));
        buttonCreate.addActionListener(_mastTableAction);
        buttonCreate.setToolTipText(Bundle.getMessage("ToolTipAddToTable"));
        pp.add(buttonCreate);
        p.add(pp);
        panel.add(p);
        
        _pickHead = new OpenPickListButton<>(blurbLines, PickListModel.signalHeadPickModelInstance(),
                this, Bundle.getMessage("OpenPicklist", Bundle.getMessage("BeanNameSignalHead")));
        _headTableAction = new SignalHeadTableAction(Bundle.getMessage("ButtonCreateHead"));
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(_pickHead.getButtonPanel());
        pp = new JPanel();
        pp.setLayout(new FlowLayout());
        buttonCreate = new JButton(Bundle.getMessage("ButtonCreateHead"));
        buttonCreate.addActionListener(_headTableAction);
        buttonCreate.setToolTipText(Bundle.getMessage("ToolTipAddToTable"));
        pp.add(buttonCreate);
        p.add(pp);
        panel.add(p);
        signalPanel.add(panel);
        signalPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        panel = new JPanel();
        l = new JLabel(Bundle.getMessage("modifySignal"));
        panel.add(l);
        signalPanel.add(panel);
        signalPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        _dndPanel = makeDndIconPanel();
        signalPanel.add(_dndPanel);
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
        if (portal != null) {
            mast = portal.getSignalProtectingBlock(_homeBlock);
            if (mast !=null) {
                setMastNameAndIcon(mast, portal);
            }
        } else if (mast !=null) {
            portal =_parent.getSignalPortal(mast);
            if (portal != null) {
                OBlock protectedBlock = portal.getProtectedBlock(mast);
                if (_homeBlock.equals(protectedBlock)) {
                    setMastNameAndIcon(mast, portal);
                }
            }
            _portalList.setSelected(portal);
            _signalList.setSelected(portal);
            _parent._editor.highlight(icon);
        }
    }
    
    private void setMastNameAndIcon(NamedBean mast, Portal portal) {
        _mastName.setText(mast.getDisplayName(DisplayOptions.DISPLAYNAME));
        _parent._editor.highlight(null);
        List<PositionableIcon> siArray = _parent.getSignalIconMap(mast);
        for (PositionableIcon si : siArray) {
            _parent._editor.highlight(si);
        }
        List<PortalIcon> piArray = _parent.getPortalIcons(portal);
        for (PortalIcon pi : piArray) {
            _parent._editor.highlight(pi);
        }
    }

    /**
     * *********************** end setup *************************
     */
    
    class PortalListListener implements ListSelectionListener {
        EditFrame _frame;
        PortalListListener(EditFrame parent) {
            _frame = parent;
        }
        @Override
        public void valueChanged(ListSelectionEvent e) {
            Portal portal = _portalList.getSelectedValue();
            if (log.isDebugEnabled()) {
                log.debug("PortalList: valueChanged: portal = {}, _currentPortal = {}", (portal==null?"null":portal.getName()), 
                        (_currentPortal==null?"null":_currentPortal.getName()));
            }
            NamedBean mast = null;
            if (portal != null) {
                mast = portal.getSignalProtectingBlock(_homeBlock);
                if (!portal.equals(_currentPortal)) {
                    String msg = checkMastForSave();
                    if (msg.length() > 0) {
                        StringBuffer sb = new StringBuffer(msg);
                        NamedBean bean = getSignal();
//                        if (bean != null) {
                            sb.append("\n");
                            sb.append(Bundle.getMessage("saveChanges"));
//                        }
                        int answer = JOptionPane.showConfirmDialog(_frame, sb.toString(), Bundle.getMessage("configureSignal"),
                                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                        if (answer == JOptionPane.YES_OPTION) {
                            if (bean != null) {
                                addMast(_currentPortal, bean);                            
//                            } else {
//                                changeName(_currentPortal);
                            }
                            return;
                        }
                    }
                }
            }
            if (_portalIcon == null) {
                _parent._editor.highlight(null);
            }
            _currentPortal = portal;
            if (portal != null) {
                _lengthPanel.setLength(portal.getEntranceSpaceForBlock(_homeBlock));
               List<PortalIcon> piArray = _parent.getPortalIcons(portal);
               if (!piArray.isEmpty()) {
                   _portalIcon = piArray.get(0);
               }
            }
            
            if (mast != null) {
                setMastNameAndIcon(mast, portal);
            } else {
                _parent._editor.highlight(null);
                _mastName.setText(null);
                if (portal != null) {
                    _parent._editor.highlight(_portalIcon);
                }
                _lengthPanel.setLength(0);
            }
            _signalList.setSelected(portal);
            setDragIcon(mast);
        }
        
    }

    class SignalListListener  implements ListSelectionListener {
        EditFrame _frame;
        SignalListListener(EditFrame parent) {
            _frame = parent;
        }
        @Override
        public void valueChanged(ListSelectionEvent e) {
            SignalPair sp = _signalList.getSelectedValue();
            if (log.isDebugEnabled()) {
                if (sp != null) {
                    log.debug("SignalList: valueChanged: portal = {}, signal = {}", 
                            sp._portal.getName(), sp._signal.getDisplayName());
                } else {
                    log.debug("SignalList: valueChanged: signalPair null"); 
                }
            }
            NamedBean signal;
            if (sp != null) {
                _portalList.setSelected(sp._portal);
                signal = sp._signal;
            } else {
                signal = null;
            }
            setDragIcon(signal);
        }
    }

    // Called from ButtonAddMast
    private boolean replaceQuestion(@Nonnull NamedBean mast, @Nonnull Portal homePortal) {
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
        String mastName = mast.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME);
        String homeName = _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME);
        if (homeMast != null) {
            if (homeMast.equals(mast)) {
                // no changes needed except for length.  So do it now and skip the rest of AddMast()
                homePortal.setEntranceSpaceForBlock(_homeBlock, _lengthPanel.getLength());
                return false;
            } else {
                String homeMastName = homeMast.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME);
                sb.append(Bundle.getMessage("mastProtectsPortal", homeMastName, homeName, homePortal.getName()));
                sb.append("\n");
                sb.append(Bundle.getMessage("replaceSignalMast", homeMast.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME), 
                                mastName, homePortal.getName()));
            }
        } else if (sb.length() > 0) {
            sb.append(Bundle.getMessage("noMast", homePortal.getName(), homeName));
            sb.append("\n");                    
            sb.append(Bundle.getMessage("setSignal", mastName));
            sb.append("\n");                    
            sb.append(Bundle.getMessage("attachMast", mastName, homeName, homePortal.getName()));
        }
        if (sb.length() > 0) {
            int answer = JOptionPane.showConfirmDialog(this,  sb.toString(),
                    Bundle.getMessage("configureSignal"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (answer != JOptionPane.YES_OPTION) {
                return false;   // Skip the rest
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
            log.debug("addMast \"{}\"", newMast.getDisplayName());
        }
        if (newMast instanceof SignalMast) {
            SignalMast mast = (SignalMast)newMast;
            if (mast.getAspect() == null) {
                mast.setAspect(mast.getValidAspects().get(0));
            }
        }
        portal.setProtectSignal(newMast, _lengthPanel.getLength(), _homeBlock);
        _parent.putSignalPortal(newMast, portal);
        setDragIcon(newMast);
    }

    private boolean addMast() {
        Portal portal = _portalList.getSelectedValue();
        String msg = null;
        if (portal != null) {
            NamedBean signal = getSignal();
            if (signal != null) {
                if (replaceQuestion(signal, portal)) {
                    addMast(portal, signal);
                }
            } else {
                String name = _mastName.getText().trim();
                if ( name.length()==0) {
                    msg = Bundle.getMessage("selectSignalMast");
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
            return false;
        }
        return true;
    }
/*
    private void changeName(Portal portal) {
        if (portal == null) {
            portal = _portalList.getSelectedValue();
        }
        String msg = null;
        if (portal != null) {
            NamedBean signal = portal.getSignalProtectingBlock(_homeBlock);
            if (signal != null) {
                String name = _mastName.getText().trim();
                if ( name.length()==0) {
                    msg = Bundle.getMessage("selectSignalMast", Bundle.getMessage("mastName"));
                } else {
                    NamedBean nb;
                    if (signal instanceof SignalMast) {
                        nb = InstanceManager.getDefault(SignalMastManager.class).getByUserName(name);
                    } else {
                        nb = InstanceManager.getDefault(SignalHeadManager.class).getByUserName(name);
                    }
                    if (nb != null) {
                        msg = Bundle.getMessage("signalExists", name, signal.getDisplayName());
                    } else {
                        // TODO!!!! patch up references to name change!
                        signal.setUserName(name);
                    }
                }
            } else {
                msg = Bundle.getMessage("noMast", portal.getName(), 
                        _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME));
            }
        } else {
            msg = Bundle.getMessage("selectPortalProtection", _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME));
        }
        if (msg != null) {
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("configureSignal"), JOptionPane.INFORMATION_MESSAGE);
        }
    }*/

    private void removeMast() {
        Portal portal = _portalList.getSelectedValue();
        NamedBean oldMast = null;
        if (portal != null) {
            oldMast = portal.getSignalProtectingBlock(_homeBlock);
        } else {
            _mastName.setText(null);
            return;
        }
        if (oldMast != null) {
            _mastName.setText(null);    // do before portal triggers propertyChange
            portal.setProtectSignal(null, 0, _homeBlock);
            _parent.putSignalPortal(oldMast, null);
        } else {
            JOptionPane.showMessageDialog(this, 
                    Bundle.getMessage("noPortalProtection", _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME),
                            portal.getName()),
                    Bundle.getMessage("configureSignal"), JOptionPane.INFORMATION_MESSAGE);
        }
        _mastName.setText(null);
    }

    /**
     * Check for questions about configuring this signal 
     * @return message of any concerns. But ALWAYS non-null.
     */
    private String checkMastForSave() {
        if (_currentPortal == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        NamedBean selectedMast = getSignal();
        NamedBean currentMast = _currentPortal.getSignalProtectingBlock(_homeBlock);

        if (selectedMast == null) {
            if (currentMast != null) {
                String curMastName = currentMast.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME);
                String curPortalName = _currentPortal.getName();
                sb.append(Bundle.getMessage("mastProtectsPortal", curMastName,
                        _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME), curPortalName));
                sb.append("\n");                    
                String name = _mastName.getText();
                if (name.trim().length() > 0) {
                    sb.append(Bundle.getMessage("NotFound", name));
                    sb.append("\n");
                    String type;
                    if (currentMast instanceof SignalMast) {
                        type = Bundle.getMessage("BeanNameSignalMast");
                    } else {
                        type = Bundle.getMessage("BeanNameSignalHead");
                    }
                    sb.append(Bundle.getMessage("changeOrCancel", curMastName, name, type));
                } else {
                    sb.append(Bundle.getMessage("removeSignalMast", curMastName, curPortalName));
                }
            }
        } else {
            String selMastName = selectedMast.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME);
            String curPortalName = _currentPortal.getName();
            String homeName = _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME);
            if (!selectedMast.equals(currentMast)) {
                if (currentMast != null) {
                    Portal selectedPortal = _parent.getSignalPortal(selectedMast);
                    if (selectedPortal != null) {
                        OBlock blk = selectedPortal.getProtectedBlock(selectedMast);
                        if (blk != null) {
                            sb.append(Bundle.getMessage("mastProtectsPortal", selMastName,
                                    blk.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME), selectedPortal.getName()));
                            sb.append("\n");
                        }
                    }
                    String curMastName = currentMast.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME);
                    sb.append(Bundle.getMessage("mastProtectsPortal", curMastName, homeName, curPortalName));
                    sb.append("\n");                    
                    sb.append(Bundle.getMessage("replaceSignalMast", curMastName, selMastName, curPortalName));
                    sb.append("\n");
                    if (_lengthPanel.isChanged(_currentPortal.getEntranceSpaceForBlock(_homeBlock))) {
                        sb.append(Bundle.getMessage("spaceChanged", selMastName, _currentPortal.getName()));
                    }
                } else {
                    sb.append(Bundle.getMessage("noMast", curPortalName, homeName));
                    sb.append("\n");                    
                    sb.append(Bundle.getMessage("setSignal", selMastName));
                    sb.append("\n");                    
                    sb.append(Bundle.getMessage("attachMast", selMastName,
                            homeName, _currentPortal.getName()));
                }
            }
        }
        return sb.toString();
    }

    @Override
    protected void closingEvent(boolean close) {
        StringBuffer sb = new StringBuffer();
        String msg = _parent.checkForPortals(_homeBlock, "ItemTypeSignalMast");
        if (msg.length() > 0) {
            sb.append(msg);
            sb.append("\n");
        }
        msg = checkMastForSave();
        if  (msg.length() > 0) {
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
        if (_mastTableAction != null) {
            _mastTableAction.dispose();
            BeanTableFrame<SignalMast> frame = _mastTableAction.getFrame();
            if (frame != null) {
                frame.dispose();
            }
        }
        if (_headTableAction != null) {
            _headTableAction.dispose();
            BeanTableFrame<SignalHead> frame = _headTableAction.getFrame();
            if (frame != null) {
                frame.dispose();
            }
        }
    }

    private void setDragIcon(NamedBean signal) {
        NamedIcon icon = null;
        if (signal != null) {
            if (signal instanceof SignalMast) {
                icon = setDragMastIcon((SignalMast)signal);
            } else if (signal instanceof SignalHead) {
                icon = setDragHeadIcon((SignalHead)signal);
            }
        }
        if (icon == null) {
            _dragLabel.setText(Bundle.getMessage("noIcon"));
        } else {
            _dragLabel.setText(null);
        }
        _dragLabel.setIcon(icon);
        _dndPanel.invalidate();
        invalidate();
        pack();
    }

    private NamedIcon setDragMastIcon(SignalMast mast) {
        String family = mast.getSignalSystem().getSystemName();
        SignalAppearanceMap appMap = mast.getAppearanceMap();
        Enumeration<String> e = mast.getAppearanceMap().getAspects();
        String s = appMap.getImageLink("Clear", family);
        if ( s == null || s.equals("")) {
            s = appMap.getImageLink("Stop", family);
        }
        if ( s == null || s.equals("")) {
            s = appMap.getImageLink(e.nextElement(), family);
        }
        if (s !=null && !s.equals("")) {
            if (!s.contains("preference:")) {
                s = s.substring(s.indexOf("resources"));
            }
            return new NamedIcon(s, s);
        }
        log.error("SignalMast icon cannot be found for {}", mast.getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME));
        return null;
    }

    private NamedIcon setDragHeadIcon(SignalHead mast) {
        _dragHeadIcon = null;
        // find icon from other icons displaying this head, if any
        List<PositionableIcon> iArray = _parent.getSignalIconMap(mast);
        if (!iArray.isEmpty()) {
            PositionableIcon pos = iArray.get(0);
            if (pos instanceof SignalHeadIcon) {
                _dragHeadIcon = (SignalHeadIcon)pos;
            }
        }
        if (_dragHeadIcon == null) { // find icon from icons of other heads on this panel
            HashMap<NamedBean, ArrayList<PositionableIcon>> icons = _parent.getSignalIconMap();
            if (icons != null && !icons.isEmpty()) {
                for (List<PositionableIcon> ia : icons.values()) {
                    if (!ia.isEmpty()) {
                        PositionableIcon pos = ia.get(0);
                        if (pos instanceof SignalHeadIcon) {
                            _dragHeadIcon = (SignalHeadIcon)pos;
                            break;
                        }
                    }
                }
            }
        }
        if (_dragHeadIcon == null) { // find icon from any set in ItemPalette
            _dragHeadIcon = new SignalHeadIcon(_parent._editor);
            _dragHeadIcon.setSignalHead(mast.getDisplayName());
            HashMap<String, HashMap<String, NamedIcon>> maps = ItemPalette.getFamilyMaps("SignalHead");
            if (maps.isEmpty()) {
                log.error("SignalHead icon cannot be found for {}", mast.getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME));
            } else {
                for (Entry<String, HashMap<String, NamedIcon>> entry : maps.entrySet()) {
                    HashMap<String, NamedIcon> map = entry.getValue();
                    for (Entry<String, NamedIcon> ent : map.entrySet()) {
                        _dragHeadIcon.setIcon(ent.getKey(), new NamedIcon(ent.getValue()));
                    }
                    _dragHeadIcon.setFamily(entry.getKey());
                    break;
                }
            }
        } else {
            _dragHeadIcon = (SignalHeadIcon)_dragHeadIcon.deepClone();
        }
        _dragHeadIcon.setDisplayLevel(SignalHead.RED);
        return (NamedIcon)_dragHeadIcon.getIcon();
    }

    SignalHeadIcon _dragHeadIcon;
    
    //////////////////////////// DnD ////////////////////////////
    protected JPanel makeDndIconPanel() { 
        JPanel dndPanel = new JPanel();
        dndPanel.setLayout(new BoxLayout(dndPanel, BoxLayout.Y_AXIS));

        JPanel p = new JPanel();
        JLabel l = new JLabel(Bundle.getMessage("dragIcon"));
        p.add(l);
        dndPanel.add(p);

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                Bundle.getMessage("signal")));
        try {
            _dragLabel = new IconDragJLabel(new DataFlavor(Editor.POSITIONABLE_FLAVOR));
            _dragLabel.setIcon(null);
            _dragLabel.setText(Bundle.getMessage("noIcon"));
            _dragLabel.setName(Bundle.getMessage("signal"));
            panel.add(_dragLabel);
        } catch (java.lang.ClassNotFoundException cnfe) {
            log.error("Unable to find class supporting {}", Editor.POSITIONABLE_FLAVOR, cnfe);
        }
        dndPanel.add(panel);
        dndPanel.setVisible(true);
        return dndPanel;
    }

    public class IconDragJLabel extends DragJLabel {

        NamedBean signal;

        public IconDragJLabel(DataFlavor flavor) {
            super(flavor);
        }

        @Override
        protected boolean okToDrag() {
            String msg;
            if (_currentPortal == null) {
                msg = Bundle.getMessage("selectPortalProtection", _homeBlock.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME));
            } else {
                signal = getSignal();
                if (signal != null) {
                    msg = checkMastForSave();
                } else {
                    String name = _mastName.getText().trim();
                    if ( name.length()==0) {
                        msg = Bundle.getMessage("selectSignalMast");
                    } else {
                        msg = Bundle.getMessage("NotFound", name);
                    }
                }
            }
            if (msg.length() > 0) {
                JOptionPane.showMessageDialog(this, msg,
                        Bundle.getMessage("configureSignal"), JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
            return true;
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            if (DataFlavor.stringFlavor.equals(flavor)) {
                return null;
            }

            if (signal == null || _currentPortal == null) {
                return null;
            }
            PositionableIcon icon;
            if (signal instanceof SignalMast) {
                icon = new SignalMastIcon(_parent._editor);
                ((SignalMastIcon)icon).setSignalMast(signal.getDisplayName());
            } else if (signal instanceof SignalHead) {
                icon = _dragHeadIcon;
           } else {
                log.error("Signal icon cannot be created for {}", signal.getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME));
                return null;
            }
            _parent.getCircuitIcons(_homeBlock).add(icon);
            List<PositionableIcon> siArray = _parent.getSignalIconMap(signal);
            siArray.add(icon);
            _parent._editor.highlight(icon);
            icon.setLevel(Editor.SIGNALS);
            log.debug("getTransferData for {}", signal.getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME));
            return icon;
        }

        @Override
        public void dragDropEnd(DragSourceDropEvent e) {
            setMastNameAndIcon(signal, _currentPortal);
            log.debug("DragJLabel.dragDropEnd ");
        }
    }

    private final static Logger log = LoggerFactory.getLogger(EditSignalFrame.class);

}

package jmri.jmrit.display.controlPanelEditor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import jmri.NamedBeanHandle;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.CoordinateEdit;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.PositionableIcon;
import jmri.jmrit.display.ToolTip;
import jmri.jmrit.logix.Portal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Pete Cressman Copyright (C) 2011
 */
public class PortalIcon extends PositionableIcon implements java.beans.PropertyChangeListener {

    public static final String HIDDEN = "hidden";
    public static final String VISIBLE = "block";
    public static final String PATH = "path";
    public static final String TO_ARROW = "toArrow";
    public static final String FROM_ARROW = "fromArrow";

    private NamedBeanHandle<Portal> _portalHdl;
    private String _status;
    private boolean _regular = true; // true when TO_ARROW shows entry into ToBlock
    private boolean _hide = false; // true when arrow should NOT show entry into ToBlock

    public PortalIcon(Editor editor) {
        // super ctor call to make sure this is an icon label
        super(editor);
        initMap();
    }

    public PortalIcon(Editor editor, Portal portal) {
        this(editor);
        setPortal(portal);
    }

    public void initMap() {
        ControlPanelEditor ed = (ControlPanelEditor) _editor;
        int deg = getDegrees();
        _iconMap = PositionableIcon.cloneMap(ed.getPortalIconMap(), this);
        if (!_regular) {
            NamedIcon a = _iconMap.get(TO_ARROW);
            NamedIcon b = _iconMap.get(FROM_ARROW);
            _iconMap.put(FROM_ARROW, a);
            _iconMap.put(TO_ARROW, b);
        }
        setScale(getScale());
        rotate(deg);
        setIcon(_iconMap.get(HIDDEN));
    }

    @Override
    public Positionable deepClone() {
        PortalIcon pos = new PortalIcon(_editor, getPortal());
        return finishClone(pos);
    }

    protected Positionable finishClone(PortalIcon pos) {
        pos._iconMap = cloneMap(_iconMap, pos);
        pos._regular = _regular;
        pos._hide = _hide;
        pos._status = _status;
        return super.finishClone(pos);
    }

    protected void setIcon(String name, NamedIcon ic) {
        if (log.isDebugEnabled()) {
            log.debug("Icon " + getPortal().getName() + " put icon key= \"" + name + "\" icon= " + ic);
        }
        NamedIcon icon = cloneIcon(ic, this);
        icon.scale(getScale(), this);
        icon.rotate(getDegrees(), this);
        _iconMap.put(name, icon);
    }

    public void setArrowOrientatuon(boolean set) {
        if (log.isDebugEnabled()) {
            log.debug("Icon " + getPortal().getName() + " setArrowOrientatuon regular=" + set + " from " + _regular);
        }
        _regular = set;
    }

    public void setHideArrows(boolean set) {
        if (log.isDebugEnabled()) {
            log.debug("Icon " + getPortal().getName() + " setHideArrows hide=" + set + " from " + _hide);
        }
        _hide = set;
    }

    public boolean getArrowSwitch() {
        return _regular;
    }

    public boolean getArrowHide() {
        return _hide;
    }

    public Portal getPortal() {
        if (_portalHdl == null) {
            return null;
        }
        return _portalHdl.getBean();
    }

    public void setPortal(Portal portal) {
        if (portal == null) {
            return;
        }
        if (_portalHdl != null) {
            Portal port = getPortal();
            if (port.equals(portal)) {
                return;
            } else {
                port.removePropertyChangeListener(this);
            }
        }
        _portalHdl = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class)
                .getNamedBeanHandle(portal.getUserName(), portal);
        portal.addPropertyChangeListener(this);
        setName(portal.getName());
        setTooltip(new ToolTip(portal.getDescription(), 0, 0));
    }

    public void setStatus(String status) {
//        if (log.isDebugEnabled()) log.debug("Icon "+getPortal().getName()+" setStatus("+status+") regular="+_regular+" icon= "+_iconMap.get(status));
        setIcon(_iconMap.get(status));
        _status = status;
        updateSize();
        repaint();
    }

    public String getStatus() {
        return _status;
    }

    /* currently Portals do not have an instance manager - !!!todo? */
    @Override
    public jmri.NamedBean getNamedBean() {
        return getPortal();
    }

    @Override
    public void displayState(int state) {
        switch (state) {
            case 0x02:
                if (_hide) {
                    setStatus(HIDDEN);
                } else {
                    setStatus(TO_ARROW);
                }
                break;
            case 0x04:
                if (_hide) {
                    setStatus(HIDDEN);
                } else {
                    setStatus(FROM_ARROW);
                }
                break;
            case 0x10:
                setStatus(VISIBLE);
                break;
            case 0x20:
                setStatus(PATH);
                break;
            default:
                setStatus(HIDDEN);
                break;
        }
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        Object source = e.getSource();
//        if (log.isDebugEnabled()) log.debug("Icon "+getPortal().getName()+" PropertyChange= "+e.getPropertyName()+
//          " oldValue= "+e.getOldValue().toString()+" newValue= "+e.getNewValue().toString());
        if (source instanceof Portal) {
            if ("Direction".equals(e.getPropertyName())) {
                if (_hide) {
                    setStatus(HIDDEN);
                    return;
                }
                switch (((Integer) e.getNewValue())) {
                    case Portal.UNKNOWN:
                        setStatus(HIDDEN);
                        break;
                    case Portal.ENTER_TO_BLOCK:
                        setStatus(TO_ARROW);
                        break;
                    case Portal.ENTER_FROM_BLOCK:
                        setStatus(FROM_ARROW);
                        break;
                    default:
                        log.warn("Unhandled portal value: {}", e.getNewValue() );
                }
            } else if ("UserName".equals(e.getPropertyName())) {
                setName((String) e.getNewValue());
                ((ControlPanelEditor) getEditor()).getCircuitBuilder().changePortalName(
                        (String) e.getOldValue(), (String) e.getNewValue());
            }
        }
    }

    @Override
    public String getNameString() {
        return getPortal().getDescription();
    }

    /*
     * Disable popup items that apply to whole selection Group
     * @see jmri.jmrit.display.PositionableLabel#doViemMenu()
     */
    @Override
    public boolean doViemMenu() {
        return false;
    }

    private void setPositionableMenu(JPopupMenu popup) {
        JCheckBoxMenuItem lockItem = new JCheckBoxMenuItem(Bundle.getMessage("LockPosition"));
        lockItem.setSelected(!isPositionable());
        lockItem.addActionListener(new ActionListener() {
            Positionable comp;
            JCheckBoxMenuItem checkBox;

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                comp.setPositionable(!checkBox.isSelected());
            }

            ActionListener init(Positionable pos, JCheckBoxMenuItem cb) {
                comp = pos;
                checkBox = cb;
                return this;
            }
        }.init(this, lockItem));
        popup.add(lockItem);
    }

    private void setShowCoordinatesMenu(JPopupMenu popup) {
        JMenu edit = new JMenu(Bundle.getMessage("EditLocation"));

        edit.add("x = " + getX());
        edit.add("y = " + getY());

        edit.add(CoordinateEdit.getCoordinateEditAction(this));
        popup.add(edit);
    }

    private void setDisplayLevelMenu(JPopupMenu popup) {
        JMenu edit = new JMenu(Bundle.getMessage("EditLevel"));
        edit.add("level= " + getDisplayLevel());

        edit.add(CoordinateEdit.getLevelEditAction(this));
        popup.add(edit);
    }

    private void setRemoveMenu(JPopupMenu popup) {
        popup.add(new AbstractAction(Bundle.getMessage("Remove")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                remove();
            }
        });
    }

    /*
     * Use this call to set actions that will not effect whole selection Group
     * @see jmri.jmrit.display.PositionableLabel#setEditItemMenu(javax.swing.JPopupMenu)
     */
    @Override
    public boolean showPopUp(JPopupMenu popup) {
        popup.add(getNameString());
        setPositionableMenu(popup);
        if (isPositionable()) {
            setShowCoordinatesMenu(popup);
        }
        setDisplayLevelMenu(popup);
        popup.addSeparator();
        popup.add(CoordinateEdit.getScaleEditAction(this));
        popup.add(CoordinateEdit.getRotateEditAction(this));
        popup.addSeparator();
        setRemoveMenu(popup);
        return true;
    }

    @Override
    public boolean setRotateMenu(JPopupMenu popup) {
        return false;
    }

    @Override
    public boolean setScaleMenu(JPopupMenu popup) {
        return false;
    }

    @Override
    public boolean setEditItemMenu(JPopupMenu popup) {
        return false;
    }

    private final static Logger log = LoggerFactory.getLogger(PortalIcon.class.getName());
}

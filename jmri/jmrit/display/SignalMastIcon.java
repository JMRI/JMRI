// SignalMastIcon.java

package jmri.jmrit.display;

import jmri.*;

import jmri.jmrit.display.palette.SignalMastItemPanel;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.picker.PickListModel;
import jmri.util.NamedBeanHandle;

import java.util.Iterator;
import java.util.Set;
import java.awt.event.*;

import javax.swing.*;

/**
 * An icon to display a status of a SignalMast.
 * <P>
 * For now, this is done via text.
 *
 * @see jmri.SignalMastManager
 * @see jmri.InstanceManager
 * @author Bob Jacobsen Copyright (C) 2009
 * @version $Revision: 1.26 $
 */

public class SignalMastIcon extends PositionableIcon implements java.beans.PropertyChangeListener {

    public SignalMastIcon(Editor editor) {
        // super ctor call to make sure this is an icon label
        super(editor);
        debug = log.isDebugEnabled();
    }
    
    private SignalMast mMast;
    private NamedBeanHandle<SignalMast> namedMast;
    private boolean debug;

    public void setShowAutoText(boolean state) {
        _text = state;
        _icon = !_text;
    }
    
    public Positionable deepClone() {
        SignalMastIcon pos = new SignalMastIcon(_editor);
        return finishClone(pos);
    }

    public Positionable finishClone(Positionable p) {
        SignalMastIcon pos = (SignalMastIcon)p;
        pos.setSignalMast(getPName());        
        return super.finishClone(pos);
    }

    /**
     * Attached a signalmast element to this display item
     * @param sh Specific SignalMast handle
     */
    public void setSignalMast(NamedBeanHandle<SignalMast> sh) {
        if (mMast != null) {
            mMast.removePropertyChangeListener(this);
        }
        mMast = sh.getBean();
        if (mMast != null) {
            getIcons();
            displayState(mastState());
            mMast.addPropertyChangeListener(this);
            namedMast = sh;
            pName=sh.getName();
        }
    }
    
     /**
     * Taken from the layout editor
     * Attached a numbered element to this display item
     * @param pName Used as a system/user name to lookup the SignalMast object
     */
    public void setSignalMast(String pName) {
        this.pName = pName;
        mMast = InstanceManager.signalMastManagerInstance().provideSignalMast(pName);
        if (mMast == null) log.warn("did not find a SignalMast named "+pName);
        else {
            namedMast = new NamedBeanHandle<SignalMast>(pName, mMast);
            getIcons();
            displayState(mastState());
            mMast.addPropertyChangeListener(this);
        }
    }

    private void getIcons() {
        _iconMap = new java.util.Hashtable<String, NamedIcon>();
        java.util.Enumeration<String> e = mMast.getAppearanceMap().getAspects();
        while (e.hasMoreElements()) {
            String s = mMast.getAppearanceMap().getProperty(e.nextElement(), "imagelink");
            s = s.substring(s.indexOf("resources"));
            NamedIcon n = new NamedIcon(s,s);
            _iconMap.put(s, n);
            if(_rotate!=0){
                n.rotate(_rotate, this);
            }
            if (_scale!=1.0) {
                n.scale(_scale, this);
            }
        }
    }

    String pName;
    
    public NamedBeanHandle<SignalMast> getNamedSignalMast() {
        return namedMast;
    }

    public SignalMast getSignalMast(){
        if (namedMast==null)
            return null;
        return namedMast.getBean();
    }

    /**
     * Get current appearance of the mast
     * @return An aspect from the SignalMast
     */
    public String mastState() {
        if (mMast==null) return "<empty>";
        else return mMast.getAspect();
    }

    // update icon as state of turnout changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (debug) log.debug("property change: "+e.getPropertyName()
                                            +" current state: "+mastState());
        displayState(mastState());
        _editor.getTargetPanel().repaint(); 
    }

    public String getPName() { return pName; }
    
    public String getNameString() {
        String name;
        if (mMast == null) name = rb.getString("NotConnected");
        else if (mMast.getUserName() == null)
            name = mMast.getSystemName();
        else
            name = mMast.getUserName()+" ("+mMast.getSystemName()+")";
        return name;
    }

    /**
     * Pop-up just displays the name
     */
    public boolean showPopUp(JPopupMenu popup) {
        if (isEditable()) {
            popup.add(new AbstractAction(rb.getString("EditLogic")) {
                public void actionPerformed(ActionEvent e) {
                    jmri.jmrit.blockboss.BlockBossFrame f = new jmri.jmrit.blockboss.BlockBossFrame();
                    String name;
                    if (mMast.getUserName()==null || mMast.getUserName().equals(""))
                        name = mMast.getSystemName();
                    else
                        name = mMast.getUserName();
                    f.setTitle(java.text.MessageFormat.format(rb.getString("SignalLogic"), name));
                    f.setSignal(name);
                    f.setVisible(true);
                }
            });
            JMenu aspect = new JMenu("Set Aspect");
            final java.util.Vector <String> aspects = mMast.getValidAspects();
            for (int i=0; i<aspects.size(); i++){
                final int index = i;
                aspect.add(new AbstractAction(aspects.elementAt(index)){
                    public void actionPerformed(ActionEvent e) {
                        mMast.setAspect(aspects.elementAt(index));
                    }
                });
            }
            popup.add(aspect);
        }
        else {
            final java.util.Vector <String> aspects = mMast.getValidAspects();
            for (int i=0; i<aspects.size(); i++){
                final int index = i;
                popup.add(new AbstractAction(aspects.elementAt(index)){
                    public void actionPerformed(ActionEvent e) {
                        mMast.setAspect(aspects.elementAt(index));
                    }
                });
            }
        }
        return true;
    }

    SignalMastItemPanel _itemPanel;

    public boolean setEditItemMenu(JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(rb.getString("EditItem"), rb.getString("SignalMast"));
        popup.add(new AbstractAction(txt) {
                public void actionPerformed(ActionEvent e) {
                    editItem();
                }
            });
        return true;
    }
    
    protected void editItem() {
        makePalettteFrame(java.text.MessageFormat.format(rb.getString("EditItem"), rb.getString("SignalMast")));
        _itemPanel = new SignalMastItemPanel(_paletteFrame, "SignalMast", getFamily(),
                                       PickListModel.signalMastPickModelInstance(), _editor);
        ActionListener updateAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                updateItem();
            }
        };
        // _iconMap keys with local names - Let SignalHeadItemPanel figure this out
        _itemPanel.init(updateAction, _iconMap);
        _itemPanel.setSelection(getSignalMast());
        _paletteFrame.add(_itemPanel);
        _paletteFrame.pack();
        _paletteFrame.setVisible(true);
    }

    void updateItem() {
        setSignalMast(_itemPanel.getTableSelection().getSystemName());
        setFamily(_itemPanel.getFamilyName());
        _paletteFrame.dispose();
        _paletteFrame = null;
        _itemPanel.dispose();
        _itemPanel = null;
        invalidate();
    }

    /**
     * Change the SignalMast aspect when the icon is clicked.
     * @param e
     */
    public void doMouseClicked(java.awt.event.MouseEvent e) {
        if (e.isMetaDown() || e.isAltDown() ) return;
        if (!_editor.getFlag(Editor.OPTION_CONTROLS, isControlling())) return;
        java.util.Vector <String> aspects = mMast.getValidAspects();
        int idx = aspects.indexOf(mMast.getAspect()) + 1;
        if (idx >= aspects.size()) {
            idx = 0;
        }
        mMast.setAspect(aspects.elementAt(idx));
    }
    
    
    /**
     * Drive the current state of the display from the state of the
     * underlying SignalMast object.
     */
    public void displayState(String state) {
        updateSize();
        if (debug) {
            if (mMast == null) {
                log.debug("Display state "+state+", disconnected");
            } else {
                log.debug("Display state "+state+" for "+mMast.getSystemName());
            }
        }
        
        if (isText())
            super.setText(state);
            
        if (isIcon()) {
            if (state !=null ) {
                String s = mMast.getAppearanceMap().getProperty(state, "imagelink");
                s = s.substring(s.indexOf("resources"));
                
                // tiny global cache, due to number of icons
                if (_iconMap==null) getIcons();
                NamedIcon n = _iconMap.get(s);
                super.setIcon(n);
                setSize(n.getIconWidth(), n.getIconHeight());
            }
        } else {
            super.setIcon(null);
        }
        
        return;
    }
    
    public boolean setEditIconMenu(JPopupMenu popup) {
        return false;
    }
    
    public boolean setRotateOrthogonalMenu(JPopupMenu popup) {
        return false;
    }

    public void rotate(int deg){
        super.rotate(deg);
        displayState(mastState());
    }
    
    public void setScale(double s) {
        super.setScale(s);
        displayState(mastState());
    }

    public void dispose() {
        mMast.removePropertyChangeListener(this);        
        super.dispose();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignalMastIcon.class.getName());
}

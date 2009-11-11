// SignalHeadIcon.java

package jmri.jmrit.display;

import jmri.SignalHead;
import jmri.InstanceManager;
import jmri.jmrit.catalog.NamedIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButtonMenuItem;

/**
 * An icon to display a status of a SignalHead.
 * <P>
 * SignalHeads are located via the SignalHeadManager, which in turn is located
 * via the InstanceManager.
 *
 * @see jmri.SignalHeadManager
 * @see jmri.InstanceManager
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 * @version $Revision: 1.51 $
 */

public class SignalHeadIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

    public SignalHeadIcon() {
        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/smallschematics/searchlights/left-red-marker.gif",
                            "resources/icons/smallschematics/searchlights/left-red-marker.gif"));
        icon = true;
        text = false;
        setDisplayLevel(PanelEditor.SIGNALS);
        displayState(SignalHead.RED);
    }
    
    public SignalHeadIcon(LayoutEditor panel){
        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/smallschematics/searchlights/left-red-short.gif",
                            "resources/icons/smallschematics/searchlights/left-red-short.gif"));
        icon = true;
        text = false;

        //Set the default signal heads for the layout editor
        redName = "resources/icons/smallschematics/searchlights/left-red-short.gif";
        flashRedName = "resources/icons/smallschematics/searchlights/left-flashred-short.gif";
        yellowName = "resources/icons/smallschematics/searchlights/left-yellow-short.gif";
        flashYellowName = "resources/icons/smallschematics/searchlights/left-flashyellow-short.gif";
        greenName = "resources/icons/smallschematics/searchlights/left-green-short.gif";
        flashGreenName = "resources/icons/smallschematics/searchlights/left-flashgreen-short.gif";
        darkName = "resources/icons/smallschematics/searchlights/left-dark-short.gif";
        heldName = "resources/icons/smallschematics/searchlights/left-held-short.gif";
        lunarName = "resources/icons/smallschematics/searchlights/left-lunar-short-marker.gif";
        flashLunarName = "resources/icons/smallschematics/searchlights/left-flashlunar-short-marker.gif";
        
        red = new NamedIcon(redName, redName);
        flashRed = new NamedIcon(flashRedName, flashRedName);
        yellow = new NamedIcon(yellowName, yellowName);
        flashYellow = new NamedIcon(flashYellowName, flashYellowName);
        green = new NamedIcon(greenName, greenName);
        flashGreen = new NamedIcon(flashGreenName, flashGreenName);
        lunar = new NamedIcon(lunarName, lunarName);
        flashLunar = new NamedIcon(flashLunarName, flashLunarName);
        dark = new NamedIcon(darkName, darkName);
        held = new NamedIcon(heldName, heldName);

        setDisplayLevel(LayoutEditor.SIGNALS);
        displayState(SignalHead.RED);
    }

    private SignalHead mHead;

    /**
     * Attached a signalhead element to this display item
     * @param sh Specific SignalHead object
     */
    public void setSignalHead(SignalHead sh) {
        if (mHead != null) {
            mHead.removePropertyChangeListener(this);
        }
        mHead = sh;
        if (mHead != null) {
            displayState(headState());
            mHead.addPropertyChangeListener(this);
            setProperToolTip();
        }
    }
    
     /**
     * Taken from the layout editor
     * Attached a numbered element to this display item
     * @param pName Used as a system/user name to lookup the SignalHead object
     */
    public void setSignalHead(String pName) {
        mHead = InstanceManager.signalHeadManagerInstance().getBySystemName(pName);
        if (mHead == null) mHead = InstanceManager.signalHeadManagerInstance().getByUserName(pName);
        if (mHead == null) log.warn("did not find a SignalHead named "+pName);
        else {
            displayState(headState());
            mHead.addPropertyChangeListener(this);
            setProperToolTip();
        }
    }

    public SignalHead getSignalHead() {
        return mHead;
    }

    // display icons
    String redName = "resources/icons/smallschematics/searchlights/left-red-marker.gif";
    NamedIcon red = new NamedIcon(redName, redName);

    String flashRedName = "resources/icons/smallschematics/searchlights/left-flashred-marker.gif";
    NamedIcon flashRed = new NamedIcon(flashRedName, flashRedName);

    String yellowName = "resources/icons/smallschematics/searchlights/left-yellow-marker.gif";
    NamedIcon yellow = new NamedIcon(yellowName, yellowName);

    String flashYellowName = "resources/icons/smallschematics/searchlights/left-flashyellow-marker.gif";
    NamedIcon flashYellow = new NamedIcon(flashYellowName, flashYellowName);

    String greenName = "resources/icons/smallschematics/searchlights/left-green-marker.gif";
    NamedIcon green = new NamedIcon(greenName, greenName);

    String flashGreenName = "resources/icons/smallschematics/searchlights/left-flashgreen-marker.gif";
    NamedIcon flashGreen = new NamedIcon(flashGreenName, flashGreenName);

    String lunarName = "resources/icons/smallschematics/searchlights/left-lunar-marker.gif";
    NamedIcon lunar = new NamedIcon(lunarName, lunarName);

    String flashLunarName = "resources/icons/smallschematics/searchlights/left-flashlunar-marker.gif";
    NamedIcon flashLunar = new NamedIcon(flashLunarName, flashLunarName);

    String darkName = "resources/icons/smallschematics/searchlights/left-dark-marker.gif";
    NamedIcon dark = new NamedIcon(darkName, darkName);

    String heldName = "resources/icons/smallschematics/searchlights/left-held-marker.gif";
    NamedIcon held = new NamedIcon(heldName, heldName);

    public NamedIcon getHeldIcon() { return held; }
    public void setHeldIcon(NamedIcon i) {
        held = i;
        displayState(headState());
    }

    public NamedIcon getDarkIcon() { return dark; }
    public void setDarkIcon(NamedIcon i) {
        dark = i;
        displayState(headState());
    }

    public NamedIcon getRedIcon() { return red; }
    public void setRedIcon(NamedIcon i) {
        red = i;
        displayState(headState());
    }

    public NamedIcon getFlashRedIcon() { return flashRed; }
    public void setFlashRedIcon(NamedIcon i) {
        flashRed = i;
        displayState(headState());
    }

    public NamedIcon getYellowIcon() { return yellow; }
    public void setYellowIcon(NamedIcon i) {
        yellow = i;
        displayState(headState());
    }

    public NamedIcon getFlashYellowIcon() { return flashYellow; }
    public void setFlashYellowIcon(NamedIcon i) {
        flashYellow = i;
        displayState(headState());
    }

    public NamedIcon getGreenIcon() { return green; }
    public void setGreenIcon(NamedIcon i) {
        green = i;
        displayState(headState());
    }

    public NamedIcon getFlashGreenIcon() { return flashGreen; }
    public void setFlashGreenIcon(NamedIcon i) {
        flashGreen = i;
        displayState(headState());
    }

    public NamedIcon getLunarIcon() { return lunar; }
    public void setLunarIcon(NamedIcon i) {
        lunar = i;
        displayState(headState());
    }

    public NamedIcon getFlashLunarIcon() { return flashLunar; }
    public void setFlashLunarIcon(NamedIcon i) {
        flashLunar = i;
        displayState(headState());
    }

    protected int maxHeight() {
        int max = 0;
        max = Math.max((red!=null) ? red.getIconHeight() : 0, max);
        max = Math.max((yellow!=null) ? yellow.getIconHeight() : 0, max);
        max = Math.max((green!=null) ? green.getIconHeight() : 0, max);
        max = Math.max((lunar!=null) ? lunar.getIconHeight() : 0, max);
        max = Math.max((flashRed!=null) ? flashRed.getIconHeight() : 0, max);
        max = Math.max((flashYellow!=null) ? flashYellow.getIconHeight() : 0, max);
        max = Math.max((flashGreen!=null) ? flashGreen.getIconHeight() : 0, max);
        max = Math.max((flashLunar!=null) ? flashLunar.getIconHeight() : 0, max);
        max = Math.max((held!=null) ? held.getIconHeight() : 0, max);
        max = Math.max((dark!=null) ? dark.getIconHeight() : 0, max);
        return max;
    }
    protected int maxWidth() {
        int max = 0;
        max = Math.max((red!=null) ? red.getIconWidth() : 0, max);
        max = Math.max((yellow!=null) ? yellow.getIconWidth() : 0, max);
        max = Math.max((green!=null) ? green.getIconWidth() : 0, max);
        max = Math.max((lunar!=null) ? lunar.getIconWidth() : 0, max);
        max = Math.max((flashRed!=null) ? flashRed.getIconWidth() : 0, max);
        max = Math.max((flashYellow!=null) ? flashYellow.getIconWidth() : 0, max);
        max = Math.max((flashGreen!=null) ? flashGreen.getIconWidth() : 0, max);
        max = Math.max((flashLunar!=null) ? flashLunar.getIconWidth() : 0, max);
        max = Math.max((held!=null) ? held.getIconWidth() : 0, max);
        max = Math.max((dark!=null) ? dark.getIconWidth() : 0, max);
        return max;
    }

    /**
     * Get current appearance of the head
     * @return An appearance variable from a SignalHead, e.g. SignalHead.RED
     */
    public int headState() {
        if (mHead==null) return 0;
        else return mHead.getAppearance();
    }

    // update icon as state of turnout changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) log.debug("property change: "+e.getPropertyName()
                                            +" current state: "+headState());
        displayState(headState());
        if (getLayoutPanel()!=null){
            //super.layoutPanel.resetAwaitingIconChange();
            getLayoutPanel().resetAwaitingIconChange();
            getLayoutPanel().redrawPanel();
        }
    }

    public void setProperToolTip() {
        setToolTipText(getNameString());
    }

    public String getNameString() {
        String name;
        if (mHead == null) name = rb.getString("NotConnected");
        else if (mHead.getUserName() == null)
            name = mHead.getSystemName();
        else
            name = mHead.getUserName()+" ("+mHead.getSystemName()+")";
        return name;
    }

    ButtonGroup clickButtonGroup = null;
    ButtonGroup litButtonGroup = null;

    /**
     * Pop-up just displays the name
     */
    protected void showPopUp(MouseEvent e) {
        if (!getEditable()) return;
        ours = this;
        popup = new JPopupMenu();
        popup.add(new JMenuItem(getNameString()));

        if (icon) popup.add(new AbstractAction(rb.getString("Rotate")) {
                public void actionPerformed(ActionEvent e) {
                    setRotation(green.getRotation()+1);
                }
            });
        checkLocationEditable(popup, getNameString());

        addFixedItem(popup);
        addDisableMenuEntry(popup);
        if (layoutPanel!=null){
            popup.add(new AbstractAction("Set x & y") {
                public void actionPerformed(ActionEvent e) {
                    String name = getText();
                    displayCoordinateEdit(name);
                }
            });
        }

        // add menu to select action on click
        JMenu clickMenu = new JMenu(rb.getString("WhenClicked"));
        clickButtonGroup = new ButtonGroup();
        JRadioButtonMenuItem r;
        r = new JRadioButtonMenuItem(rb.getString("ChangeAspect"));
        r.addActionListener(new ActionListener() {
            final int desired = 0;
            public void actionPerformed(ActionEvent e) { setClickMode(desired); }
        });
        clickButtonGroup.add(r);
        if (clickMode == 0)  r.setSelected(true);
        else r.setSelected(false);
        clickMenu.add(r);
        r = new JRadioButtonMenuItem(rb.getString("AlternateLit"));
        r.addActionListener(new ActionListener() {
            final int desired = 1;
            public void actionPerformed(ActionEvent e) { setClickMode(desired); }
        });
        clickButtonGroup.add(r);
        if (clickMode == 1)  r.setSelected(true);
        else r.setSelected(false);
        clickMenu.add(r);
        r = new JRadioButtonMenuItem(rb.getString("AlternateHeld"));
        r.addActionListener(new ActionListener() {
            final int desired = 2;
            public void actionPerformed(ActionEvent e) { setClickMode(desired); }
        });
        clickButtonGroup.add(r);
        if (clickMode == 2)  r.setSelected(true);
        else r.setSelected(false);
        clickMenu.add(r);
        popup.add(clickMenu);


        // add menu to select handling of lit parameter
        JMenu litMenu = new JMenu(rb.getString("WhenNotLit"));
        litButtonGroup = new ButtonGroup();
        r = new JRadioButtonMenuItem(rb.getString("ShowAppearance"));
        r.setIconTextGap(10);
        r.addActionListener(new ActionListener() {
            final boolean desired = false;
            public void actionPerformed(ActionEvent e) { setLitMode(desired); }
        });
        litButtonGroup.add(r);
        if (!litMode)  r.setSelected(true);
        else r.setSelected(false);
        litMenu.add(r);
        r = new JRadioButtonMenuItem(rb.getString("ShowDarkIcon"));
        r.setIconTextGap(10);
        r.addActionListener(new ActionListener() {
            final boolean desired = true;
            public void actionPerformed(ActionEvent e) { setLitMode(desired); }
        });
        litButtonGroup.add(r);
        if (litMode)  r.setSelected(true);
        else r.setSelected(false);
        litMenu.add(r);
        popup.add(litMenu);

        popup.add(new AbstractAction(rb.getString("Remove")) {
            public void actionPerformed(ActionEvent e) {
                remove();
                dispose();
            }
        });

        popup.add(new AbstractAction(rb.getString("EditIcon")) {
                public void actionPerformed(ActionEvent e) {
                    edit();
                }
            });

        popup.add(new AbstractAction(rb.getString("EditLogic")) {
            public void actionPerformed(ActionEvent e) {
                jmri.jmrit.blockboss.BlockBossFrame f = new jmri.jmrit.blockboss.BlockBossFrame();
                String name;
                if (mHead.getUserName()==null || mHead.getUserName().equals(""))
                    name = mHead.getSystemName();
                else
                    name = mHead.getUserName();
                f.setTitle(java.text.MessageFormat.format(rb.getString("SignalLogic"), name));
                f.setSignal(name);
                f.setVisible(true);
            }
        });

        popup.add(setHiddenMenu());
        
        popup.show(e.getComponent(), e.getX(), e.getY());
    }
    
    public void setRotation(int x){
        ours = this;
        green.setRotation(x, ours);
        red.setRotation(x, ours);
        yellow.setRotation(x, ours);
        lunar.setRotation(x, ours);
        if (flashGreen !=null) flashGreen.setRotation(x, ours);
        if (flashRed !=null) flashRed.setRotation(x, ours);
        if (flashYellow !=null) flashYellow.setRotation(x, ours);
        if (flashLunar !=null) flashLunar.setRotation(x, ours);
        if (dark !=null) dark.setRotation(x, ours);
        if (held !=null) held.setRotation(x, ours);
        displayState(headState());
        // bug fix, must repaint icons that have same width and height
        repaint();
    
    }

    void scale(int s) {
        green.scale(s, this);
        red.scale(s, this);
        yellow.scale(s, this);
        lunar.scale(s, this);
        if (flashGreen !=null) flashGreen.scale(s, this);
        if (flashYellow !=null) flashYellow.scale(s, this);
        if (flashRed !=null) flashRed.scale(s, this);
        if (flashLunar !=null) flashLunar.scale(s, this);
        if (dark !=null) dark.scale(s, this);
        if (held !=null) held.scale(s, this);
        displayState(headState());
    }

    void rotate(int deg) {
        green.rotate(deg, this);
        red.rotate(deg, this);
        yellow.rotate(deg, this);
        lunar.rotate(deg, this);
        if (flashGreen !=null) flashGreen.rotate(deg, this);
        if (flashYellow !=null) flashYellow.rotate(deg, this);
        if (flashRed !=null) flashRed.rotate(deg, this);
        if (flashLunar !=null) flashLunar.rotate(deg, this);
        if (dark !=null) dark.rotate(deg, this);
        if (held !=null) held.rotate(deg, this);
        displayState(headState());
    }

    /**
     * Drive the current state of the display from the state of the
     * underlying SignalHead object.
     * <UL>
     * <LI>If the signal is held, display that.
     * <LI>If set to monitor the status of the lit parameter
     *     and lit is false, show the dark icon ("dark", when
     *     set as an explicit appearance, is displayed anyway)
     * <LI>Show the icon corresponding to one of the seven appearances.
     * </UL>
     */
    public void displayState(int state) {
        updateSize();
        if (mHead == null) {
            log.debug("Display state "+state+", disconnected");
        } else {
            log.debug("Display state "+state+" for "+mHead.getSystemName());
            if (mHead.getHeld()) {
                if (text) super.setText(rb.getString("Held"));
                if (icon) super.setIcon(held);
                return;
            }
            else if (getLitMode() && !mHead.getLit()) {
                if (text) super.setText(rb.getString("Dark"));
                if (icon) super.setIcon(dark);
                return;
            }
        }
        switch (state) {
        case SignalHead.RED:
            if (text) super.setText(rb.getString("red"));
            if (icon) super.setIcon(red);
            break;
        case SignalHead.FLASHRED:
            if (text) super.setText(rb.getString("FlashRed"));
            if (icon) super.setIcon(flashRed);
            break;
        case SignalHead.YELLOW:
            if (text) super.setText(rb.getString("yellow"));
            if (icon) super.setIcon(yellow);
            break;
        case SignalHead.FLASHYELLOW:
            if (text) super.setText(rb.getString("FlashYellow"));
            if (icon) super.setIcon(flashYellow);
            break;
        case SignalHead.GREEN:
            if (text) super.setText(rb.getString("green"));
            if (icon) super.setIcon(green);
            break;
        case SignalHead.FLASHGREEN:
            if (text) super.setText(rb.getString("FlashGreen"));
            if (icon) super.setIcon(flashGreen);
            break;
        case SignalHead.LUNAR:
            if (text) super.setText(rb.getString("lunar"));
            if (icon) super.setIcon(lunar);
            break;
        case SignalHead.FLASHLUNAR:
            if (text) super.setText(rb.getString("FlashGreen"));
            if (icon) super.setIcon(flashLunar);
            break;
        case SignalHead.DARK:
            if (text) super.setText(rb.getString("Dark"));
            if (icon) super.setIcon(dark);
            break;

        default:
            log.error("unexpected state during display: "+state);
        }
        return;
    }

    void edit() {
        if (_editorFrame != null) {
            _editorFrame.setLocationRelativeTo(null);
            _editorFrame.toFront();
            return;
        }
        _editor = new IconAdder();
        _editor.setIcon(0, "SignalHeadStateRed", red);
        _editor.setIcon(1, "SignalHeadStateYellow", yellow);
        _editor.setIcon(2, "SignalHeadStateGreen", green);
        _editor.setIcon(3, "SignalHeadStateDark", dark);
        _editor.setIcon(4, "SIgnalHeadStateHeld", held);
        _editor.setIcon(5, "SignalHeadStateLunar", lunar);
        _editor.setIcon(6, "SignalHeadStateFlashingRed", flashRed);
        _editor.setIcon(7, "SignalHeadStateFlashingYellow", flashYellow);
        _editor.setIcon(8, "SignalHeadStateFlashingGreen", flashGreen);
        _editor.setIcon(9, "SignalHeadStateFlashingLunar", flashLunar);
        makeAddIconFrame("EditSignal", "addIconsToPanel", 
                                           "SelectSignal", _editor);
        _editor.makeIconPanel();
        _editor.setPickList(PickListModel.signalPickModelInstance());

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                updateSignal();
            }
        };
        ActionListener changeIconAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    _editor.addCatalog();
                    _editorFrame.pack();
                }
        };
        _editor.complete(addIconAction, changeIconAction, false, true);
        _editor.setSelection(mHead);
    }
    void updateSignal() {
        setRedIcon(_editor.getIcon("SignalHeadStateRed"));
        setFlashRedIcon(_editor.getIcon("SignalHeadStateFlashingRed"));
        setYellowIcon(_editor.getIcon("SignalHeadStateYellow"));
        setFlashYellowIcon(_editor.getIcon("SignalHeadStateFlashingYellow"));
        setGreenIcon(_editor.getIcon("SignalHeadStateGreen"));
        setFlashGreenIcon(_editor.getIcon("SignalHeadStateFlashingGreen"));
        setLunarIcon(_editor.getIcon("SignalHeadStateLunar"));
        setFlashLunarIcon(_editor.getIcon("SignalHeadStateFlashingLunar"));
        setDarkIcon(_editor.getIcon("SignalHeadStateDark"));
        setHeldIcon(_editor.getIcon("SIgnalHeadStateHeld"));
        setSignalHead((SignalHead)_editor.getTableSelection());
        _editorFrame.dispose();
        _editorFrame = null;
        _editor = null;
        invalidate();
    }


    /**
     * What to do on click? 0 means 
     * sequence through aspects; 1 means 
     * alternate the "lit" aspect; 2 means
     * alternate the "held" aspect.
     */
    protected int clickMode = 0;
    
    public void setClickMode(int mode) {
        clickMode = mode;
    }
    public int getClickMode() {
        return clickMode;
    }
    
    /**
     * How to handle lit vs not lit?
     * <P>
     * False means ignore (always show R/Y/G/etc appearance on screen);
     * True means show "dark" if lit is set false.
     * <P>
     * Note that setting the appearance "DARK" explicitly
     * will show the dark icon regardless of how this is set.
     */
    protected boolean litMode = false;
    
    public void setLitMode(boolean mode) {
        litMode = mode;
    }
    public boolean getLitMode() {
        return litMode;
    }
    
    /**
     * Change the SignalHead state when the icon is clicked.
     * Note that this change may not be permanent if there is
     * logic controlling the signal head.
     * @param e
     */
     // Was mouseClicked, changed to mouseRelease to workaround touch screen driver limitation
    public void mouseReleased(java.awt.event.MouseEvent e) {
        super.mouseReleased(e);
        if (panelEditor!=null)
            performMouseClicked(e);
    }
    
    /** 
     * This was added in so that the layout editor can handle the mouseclicked when zoomed in
    */
    public void performMouseClicked(java.awt.event.MouseEvent e){
        if (!getControlling()) return;
        if (getForceControlOff()) return;
        if (e.isMetaDown() || e.isAltDown() ) return;
        if (mHead==null) {
            log.error("No turnout connection, can't process click");
            return;
        }
        if (layoutPanel!=null)
            layoutPanel.setAwaitingIconChange();
        switch (clickMode) {
        case 0 :
            switch (mHead.getAppearance()) {
            case jmri.SignalHead.RED:
            case jmri.SignalHead.FLASHRED:
                mHead.setAppearance(jmri.SignalHead.YELLOW);
                break;
            case jmri.SignalHead.YELLOW:
            case jmri.SignalHead.FLASHYELLOW:
                mHead.setAppearance(jmri.SignalHead.GREEN);
                break;
            case jmri.SignalHead.GREEN:
            case jmri.SignalHead.FLASHGREEN:
                mHead.setAppearance(jmri.SignalHead.RED);
                break;
            default:
                mHead.setAppearance(jmri.SignalHead.RED);
                break;
            }
            return;
        case 1 :
            mHead.setLit(!mHead.getLit());
            return;
        case 2 : 
            mHead.setHeld(!mHead.getHeld());
            return;
        default:
            log.error("Click in mode "+clickMode);
        //}
        }
    }
    
        /**
     * Change the SignalHead state when the icon is clicked.
     * Note that this change may not be permanent if there is
     * logic controlling the signal head.
     * @param e
     */
    /*public void mouseClicked(java.awt.event.MouseEvent e) {
        System.out.println("MouseClicked - SignalHead Icon");
        if (layoutPanel!=null){
        if (!getControlling()) return;
        if (getForceControlOff()) return;
        if (e.isMetaDown() || e.isAltDown() ) return;
        if (mHead==null) {
            log.error("No Signal Head connection, can't process click");
            return;
        }
        switch (clickMode) {
        case 0 :
            switch (mHead.getAppearance()) {
            case jmri.SignalHead.RED:
            case jmri.SignalHead.FLASHRED:
                mHead.setAppearance(jmri.SignalHead.YELLOW);
                break;
            case jmri.SignalHead.YELLOW:
            case jmri.SignalHead.FLASHYELLOW:
                mHead.setAppearance(jmri.SignalHead.GREEN);
                break;
            case jmri.SignalHead.GREEN:
            case jmri.SignalHead.FLASHGREEN:
                mHead.setAppearance(jmri.SignalHead.RED);
                break;
            default:
                mHead.setAppearance(jmri.SignalHead.RED);
                break;
            }
            return;
        case 1 :
            mHead.setLit(!mHead.getLit());
            return;
        case 2 : 
            mHead.setHeld(!mHead.getHeld());
            return;
        default:
            log.error("Click in mode "+clickMode);
        }
        }
    }*/
    public void mouseClicked(java.awt.event.MouseEvent e) {
        if (getLayoutPanel()!=null)
            super.layoutPanel.handleMouseClicked(e, getX(), getY());
	}

    //private static boolean warned = false;

    public void dispose() {
        mHead.removePropertyChangeListener(this);
        mHead = null;

        red = null;
        flashRed = null;
        yellow = null;
        flashYellow = null;
        green = null;
        flashGreen = null;
        dark = null;
        held = null;

        super.dispose();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignalHeadIcon.class.getName());
}

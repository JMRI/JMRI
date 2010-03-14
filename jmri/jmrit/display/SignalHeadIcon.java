// SignalHeadIcon.java

package jmri.jmrit.display;

import jmri.SignalHead;
import jmri.InstanceManager;
import jmri.jmrit.catalog.NamedIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButtonMenuItem;
import jmri.util.NamedBeanHandle;

/**
 * An icon to display a status of a SignalHead.
 * <P>
 * SignalHeads are located via the SignalHeadManager, which in turn is located
 * via the InstanceManager.
 *
 * @see jmri.SignalHeadManager
 * @see jmri.InstanceManager
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 * @version $Revision: 1.62 $
 */

public class SignalHeadIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

    public SignalHeadIcon(Editor editor){
        // super ctor call to make sure this is an icon label
        super(new NamedIcon("resources/icons/smallschematics/searchlights/left-red-short.gif",
                            "resources/icons/smallschematics/searchlights/left-red-short.gif"), editor);
        setDisplayLevel(Editor.SIGNALS);
        _control = true;
        displayState(SignalHead.RED);
        setPopupUtility(null);
    }

//    private SignalHead mHead;
    private NamedBeanHandle<SignalHead> namedHead;

    /**
     * Attached a signalhead element to this display item
     * @param sh Specific SignalHead object
     */
    public void setSignalHead(NamedBeanHandle<SignalHead> sh) {
        if (namedHead != null) {
            getSignalHead().removePropertyChangeListener(this);
        }
        namedHead = sh;
        if (namedHead != null) {
            displayState(headState());
            getSignalHead().addPropertyChangeListener(this);
        }
    }
    
     /**
     * Taken from the layout editor
     * Attached a numbered element to this display item
     * @param pName Used as a system/user name to lookup the SignalHead object
     */
    public void setSignalHead(String pName) {
        SignalHead mHead = InstanceManager.signalHeadManagerInstance().getBySystemName(pName);
        if (mHead == null) mHead = InstanceManager.signalHeadManagerInstance().getByUserName(pName);
        if (mHead == null) log.warn("did not find a SignalHead named "+pName);
        else {
            setSignalHead(new NamedBeanHandle<SignalHead>(pName, mHead));
        }
    }

    public NamedBeanHandle<SignalHead> getNamedSignalHead() {
        return namedHead;
    }

    public SignalHead getSignalHead(){
        if (namedHead==null)
            return null;
        return namedHead.getBean();
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

    public int maxHeight() {
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
    public int maxWidth() {
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
        if (getSignalHead()==null) return 0;
        else return getSignalHead().getAppearance();
    }

    // update icon as state of turnout changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) log.debug("property change: "+e.getPropertyName()
                                            +" current state: "+headState());
        displayState(headState());
		_editor.getTargetPanel().repaint(); 
    }

    public String getNameString() {
        String name;
        if (namedHead == null) name = rb.getString("NotConnected");
        else
            name = namedHead.getName();
        return name;
    }

    ButtonGroup clickButtonGroup = null;
    ButtonGroup litButtonGroup = null;

    /**
     * Pop-up just displays the name
     */
    public boolean showPopUp(JPopupMenu popup) {
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

        popup.add(new AbstractAction(rb.getString("EditLogic")) {
            public void actionPerformed(ActionEvent e) {
                jmri.jmrit.blockboss.BlockBossFrame f = new jmri.jmrit.blockboss.BlockBossFrame();
                String name;
                /*if (mHead.getUserName()==null || mHead.getUserName().equals(""))
                    name = mHead.getSystemName();
                else*/
                    name = getNameString();
                f.setTitle(java.text.MessageFormat.format(rb.getString("SignalLogic"), name));
                f.setSignal(name);
                f.setVisible(true);
            }
        });
        return true;
    }
    
    /*************** popup AbstractAction.actionPerformed method overrides ************/

    protected void rotateOrthogonal() {
        green.setRotation(green.getRotation()+1, this);
        red.setRotation(red.getRotation()+1, this);
        yellow.setRotation(yellow.getRotation()+1, this);
        lunar.setRotation(lunar.getRotation()+1, this);
        if (flashGreen !=null) flashGreen.setRotation(flashGreen.getRotation()+1, this);
        if (flashRed !=null) flashRed.setRotation(flashRed.getRotation()+1, this);
        if (flashYellow !=null) flashYellow.setRotation(flashYellow.getRotation()+1, this);
        if (flashLunar !=null) flashLunar.setRotation(flashLunar.getRotation()+1, this);
        if (dark !=null) dark.setRotation(dark.getRotation()+1, this);
        if (held !=null) held.setRotation(held.getRotation()+1, this);
        displayState(headState());
        // bug fix, must repaint icons that have same width and height
        repaint();
    
    }

    public void setScale(double s) {
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
        if (getSignalHead() == null) {
            log.debug("Display state "+state+", disconnected");
        } else {
            log.debug("Display state "+state+" for "+getNameString());
            if (getSignalHead().getHeld()) {
                if (isText()) super.setText(rb.getString("Held"));
                if (isIcon()) super.setIcon(held);
                return;
            }
            else if (getLitMode() && !getSignalHead().getLit()) {
                if (isText()) super.setText(rb.getString("Dark"));
                if (isIcon()) super.setIcon(dark);
                return;
            }
        }
        switch (state) {
        case SignalHead.RED:
            if (isText()) super.setText(rb.getString("red"));
            if (isIcon()) super.setIcon(red);
            break;
        case SignalHead.FLASHRED:
            if (isText()) super.setText(rb.getString("FlashRed"));
            if (isIcon()) super.setIcon(flashRed);
            break;
        case SignalHead.YELLOW:
            if (isText()) super.setText(rb.getString("yellow"));
            if (isIcon()) super.setIcon(yellow);
            break;
        case SignalHead.FLASHYELLOW:
            if (isText()) super.setText(rb.getString("FlashYellow"));
            if (isIcon()) super.setIcon(flashYellow);
            break;
        case SignalHead.GREEN:
            if (isText()) super.setText(rb.getString("green"));
            if (isIcon()) super.setIcon(green);
            break;
        case SignalHead.FLASHGREEN:
            if (isText()) super.setText(rb.getString("FlashGreen"));
            if (isIcon()) super.setIcon(flashGreen);
            break;
        case SignalHead.LUNAR:
            if (isText()) super.setText(rb.getString("lunar"));
            if (isIcon()) super.setIcon(lunar);
            break;
        case SignalHead.FLASHLUNAR:
            if (isText()) super.setText(rb.getString("FlashGreen"));
            if (isIcon()) super.setIcon(flashLunar);
            break;
        case SignalHead.DARK:
            if (isText()) super.setText(rb.getString("Dark"));
            if (isIcon()) super.setIcon(dark);
            break;

        default:
            log.error("unexpected state during display: "+state);
        }
        return;
    }

    protected void edit() {
        if (showIconEditorFrame(this)) {
            return;
        }
        _iconEditor = new IconAdder();
        _iconEditor.setIcon(0, "SignalHeadStateRed", red);
        _iconEditor.setIcon(1, "SignalHeadStateYellow", yellow);
        _iconEditor.setIcon(2, "SignalHeadStateGreen", green);
        _iconEditor.setIcon(3, "SignalHeadStateDark", dark);
        _iconEditor.setIcon(4, "SignalHeadStateHeld", held);
        _iconEditor.setIcon(5, "SignalHeadStateLunar", lunar);
        _iconEditor.setIcon(6, "SignalHeadStateFlashingRed", flashRed);
        _iconEditor.setIcon(7, "SignalHeadStateFlashingYellow", flashYellow);
        _iconEditor.setIcon(8, "SignalHeadStateFlashingGreen", flashGreen);
        _iconEditor.setIcon(9, "SignalHeadStateFlashingLunar", flashLunar);
        _iconEditorFrame = makeAddIconFrame("EditSignalHead", "addIconsToPanel", 
                                           "SelectSignalHead", _iconEditor, this);
        _iconEditor.makeIconPanel();
        _iconEditor.setPickList(PickListModel.signalHeadPickModelInstance());

        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                updateSignal();
            }
        };
        ActionListener changeIconAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    _iconEditor.addCatalog();
                    _iconEditorFrame.pack();
                }
        };
        _iconEditor.complete(addIconAction, changeIconAction, false, true);
        _iconEditor.setSelection(getSignalHead());
    }
    void updateSignal() {
        setRedIcon(_iconEditor.getIcon("SignalHeadStateRed"));
        setFlashRedIcon(_iconEditor.getIcon("SignalHeadStateFlashingRed"));
        setYellowIcon(_iconEditor.getIcon("SignalHeadStateYellow"));
        setFlashYellowIcon(_iconEditor.getIcon("SignalHeadStateFlashingYellow"));
        setGreenIcon(_iconEditor.getIcon("SignalHeadStateGreen"));
        setFlashGreenIcon(_iconEditor.getIcon("SignalHeadStateFlashingGreen"));
        setLunarIcon(_iconEditor.getIcon("SignalHeadStateLunar"));
        setFlashLunarIcon(_iconEditor.getIcon("SignalHeadStateFlashingLunar"));
        setDarkIcon(_iconEditor.getIcon("SignalHeadStateDark"));
        setHeldIcon(_iconEditor.getIcon("SignalHeadStateHeld"));
        setSignalHead(_iconEditor.getTableSelection().getDisplayName());
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
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
    public void doMouseClicked(java.awt.event.MouseEvent e) {
        performMouseClicked(e);
    }
    
    /** 
     * This was added in so that the layout editor can handle the mouseclicked when zoomed in
    */
    public void performMouseClicked(java.awt.event.MouseEvent e){
        if (!isControlling()) return;
        if (e.isMetaDown() || e.isAltDown() ) return;
        if (getSignalHead()==null) {
            log.error("No turnout connection, can't process click");
            return;
        }
        switch (clickMode) {
        case 0 :
            switch (getSignalHead().getAppearance()) {
            case jmri.SignalHead.RED:
            case jmri.SignalHead.FLASHRED:
                getSignalHead().setAppearance(jmri.SignalHead.YELLOW);
                break;
            case jmri.SignalHead.YELLOW:
            case jmri.SignalHead.FLASHYELLOW:
                getSignalHead().setAppearance(jmri.SignalHead.GREEN);
                break;
            case jmri.SignalHead.GREEN:
            case jmri.SignalHead.FLASHGREEN:
                getSignalHead().setAppearance(jmri.SignalHead.RED);
                break;
            default:
                getSignalHead().setAppearance(jmri.SignalHead.RED);
                break;
            }
            return;
        case 1 :
            getSignalHead().setLit(!getSignalHead().getLit());
            return;
        case 2 : 
            getSignalHead().setHeld(!getSignalHead().getHeld());
            return;
        default:
            log.error("Click in mode "+clickMode);
        //}
        }
    }

    //private static boolean warned = false;

    public void dispose() {
        getSignalHead().removePropertyChangeListener(this);
        namedHead = null;

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

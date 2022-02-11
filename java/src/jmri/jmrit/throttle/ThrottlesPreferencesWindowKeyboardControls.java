package jmri.jmrit.throttle;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to store JMRI throttles keyboard shortcuts
 * 
 * @author Lionel Jeanson - 2021
 * 
 */
public class ThrottlesPreferencesWindowKeyboardControls implements Cloneable {
    
    // speed multiplier
    private float moreSpeedMultiplier = 5f;
    
    //
    // shortcuts stored as arrays of pairs [modifier][key]
    //
    // all bellow defaults are extracted from previous existing code
    //
    // moving through throttle windows
    private int[][] nextThrottleWindowKeys = { 
        {0,KeyEvent.VK_INSERT}
    };
    private int[][] prevThrottleWindowKeys = { 
        {0,KeyEvent.VK_DELETE}
    };
    // moving through 1 throttle window frames
    private int[][] nextThrottleFrameKeys = { 
        {0,KeyEvent.VK_END}
    };
    private int[][] prevThrottleFrameKeys = { 
        {0,KeyEvent.VK_HOME}
    };
    // moving through running throttle frames
    private int[][] nextRunThrottleFrameKeys = {
        {KeyEvent.CTRL_DOWN_MASK,KeyEvent.VK_END}
    };
    private int[][] prevRunThrottleFrameKeys = {
        {KeyEvent.CTRL_DOWN_MASK,KeyEvent.VK_HOME}
    };      
    
    // moving through internal windows
    private int[][] nextThrottleInternalWindowKeys = { 
        {0,KeyEvent.VK_K},
        {0,KeyEvent.VK_TAB}
    };
    private int[][] prevThrottleInternalWindowKeys = { 
        {0,KeyEvent.VK_L},
        {KeyEvent.SHIFT_DOWN_MASK, KeyEvent.VK_TAB}          
    };
    
    // select internal window
    private int[][] moveToControlPanelKeys = { 
        {0,KeyEvent.VK_C}
    };
    private int[][] moveToFunctionPanelKeys = { 
        {0,KeyEvent.VK_F}
    };
    private int[][] moveToAddressPanelKeys=  { 
        {0,KeyEvent.VK_A}
    };
    
    
    // Speed
    private int[][] reverseKeys = { 
        {0,KeyEvent.VK_DOWN} // Down arrow
    }; 
    private int[][] forwardKeys = { 
        {0,KeyEvent.VK_UP} // Up arrow
    };
    private int[][] switchDirectionKeys = { 
    };         
    
    private int[][] idleKeys = {
        {0,KeyEvent.VK_MULTIPLY},  // numpad *
        {0,KeyEvent.VK_SPACE}
    };
    private int[][] stopKeys=  {
        {0,KeyEvent.VK_DIVIDE}, // numpad /
        {0,KeyEvent.VK_ESCAPE}
    };
    
    private int[][] accelerateKeys = {
        {0,KeyEvent.VK_ADD},  // numpad +
        {0,KeyEvent.VK_LEFT}
    };
    private int[][] decelerateKeys = {
        {0,KeyEvent.VK_SUBTRACT}, // numpad -;
        {0,KeyEvent.VK_RIGHT}
    };
    private int[][] accelerateMoreKeys = {
        {0,KeyEvent.VK_PAGE_UP},
        {KeyEvent.CTRL_DOWN_MASK,KeyEvent.VK_LEFT}
    };
    private int[][] decelerateMoreKeys = {
        {0,KeyEvent.VK_PAGE_DOWN},
        {KeyEvent.CTRL_DOWN_MASK,KeyEvent.VK_RIGHT}
    };    
    
    // function buttons
    private int[][][] functionsKeys = { 
        {{0,KeyEvent.VK_NUMPAD0}}, // F0
        {{0,KeyEvent.VK_F1},{0,KeyEvent.VK_NUMPAD1}}, // F1
        {{0,KeyEvent.VK_F2},{0,KeyEvent.VK_NUMPAD2}}, // F2
        {{0,KeyEvent.VK_F3},{0,KeyEvent.VK_NUMPAD3}}, // F3
        {{0,KeyEvent.VK_F4},{0,KeyEvent.VK_NUMPAD4}}, // F4
        {{0,KeyEvent.VK_F5},{0,KeyEvent.VK_NUMPAD5}}, // F5
        {{0,KeyEvent.VK_F6},{0,KeyEvent.VK_NUMPAD6}}, // F6
        {{0,KeyEvent.VK_F7},{0,KeyEvent.VK_NUMPAD7}}, // F7
        {{0,KeyEvent.VK_F8},{0,KeyEvent.VK_NUMPAD8}}, // F8
        {{0,KeyEvent.VK_F9},{0,KeyEvent.VK_NUMPAD9}}, // F9
        {{0,KeyEvent.VK_F10},{0,KeyEvent.VK_DECIMAL},{KeyEvent.SHIFT_DOWN_MASK,KeyEvent.VK_NUMPAD0}}, // F10
        {{0,KeyEvent.VK_F11},{KeyEvent.SHIFT_DOWN_MASK,KeyEvent.VK_F1},{KeyEvent.SHIFT_DOWN_MASK,KeyEvent.VK_NUMPAD1}}, // F11
        {{0,KeyEvent.VK_F12},{KeyEvent.SHIFT_DOWN_MASK,KeyEvent.VK_F2},{KeyEvent.SHIFT_DOWN_MASK,KeyEvent.VK_NUMPAD2}}, // F12
        {{0,KeyEvent.VK_F13},{KeyEvent.SHIFT_DOWN_MASK,KeyEvent.VK_F3},{KeyEvent.SHIFT_DOWN_MASK,KeyEvent.VK_NUMPAD3}}, // F13
        {{0,KeyEvent.VK_F14},{KeyEvent.SHIFT_DOWN_MASK,KeyEvent.VK_F4},{KeyEvent.SHIFT_DOWN_MASK,KeyEvent.VK_NUMPAD4}}, // F14
        {{0,KeyEvent.VK_F15},{KeyEvent.SHIFT_DOWN_MASK,KeyEvent.VK_F5},{KeyEvent.SHIFT_DOWN_MASK,KeyEvent.VK_NUMPAD5}}, // F15
        {{0,KeyEvent.VK_F16},{KeyEvent.SHIFT_DOWN_MASK,KeyEvent.VK_F6},{KeyEvent.SHIFT_DOWN_MASK,KeyEvent.VK_NUMPAD6}}, // F16
        {{0,KeyEvent.VK_F17},{KeyEvent.SHIFT_DOWN_MASK,KeyEvent.VK_F7},{KeyEvent.SHIFT_DOWN_MASK,KeyEvent.VK_NUMPAD7}}, // F17
        {{0,KeyEvent.VK_F18},{KeyEvent.SHIFT_DOWN_MASK,KeyEvent.VK_F8},{KeyEvent.SHIFT_DOWN_MASK,KeyEvent.VK_NUMPAD8}}, // F18
        {{0,KeyEvent.VK_F19},{KeyEvent.SHIFT_DOWN_MASK,KeyEvent.VK_F9},{KeyEvent.SHIFT_DOWN_MASK,KeyEvent.VK_NUMPAD9}}, // F19
        {{0,KeyEvent.VK_F20},{KeyEvent.CTRL_DOWN_MASK,KeyEvent.VK_F10},{KeyEvent.CTRL_DOWN_MASK,KeyEvent.VK_NUMPAD0}}, // F20
        {{0,KeyEvent.VK_F21},{KeyEvent.CTRL_DOWN_MASK,KeyEvent.VK_F1},{KeyEvent.CTRL_DOWN_MASK,KeyEvent.VK_NUMPAD1}}, // F21
        {{0,KeyEvent.VK_F22},{KeyEvent.CTRL_DOWN_MASK,KeyEvent.VK_F2},{KeyEvent.CTRL_DOWN_MASK,KeyEvent.VK_NUMPAD2}}, // F22
        {{0,KeyEvent.VK_F23},{KeyEvent.CTRL_DOWN_MASK,KeyEvent.VK_F3},{KeyEvent.CTRL_DOWN_MASK,KeyEvent.VK_NUMPAD3}}, // F23
        {{0,KeyEvent.VK_F24},{KeyEvent.CTRL_DOWN_MASK,KeyEvent.VK_F4},{KeyEvent.CTRL_DOWN_MASK,KeyEvent.VK_NUMPAD4}}, // F24
        {{KeyEvent.CTRL_DOWN_MASK,KeyEvent.VK_F5},{KeyEvent.CTRL_DOWN_MASK,KeyEvent.VK_NUMPAD5}}, // F25
        {{KeyEvent.CTRL_DOWN_MASK,KeyEvent.VK_F6},{KeyEvent.CTRL_DOWN_MASK,KeyEvent.VK_NUMPAD6}}, // F26
        {{KeyEvent.CTRL_DOWN_MASK,KeyEvent.VK_F7},{KeyEvent.CTRL_DOWN_MASK,KeyEvent.VK_NUMPAD7}}, // F27
        {{KeyEvent.CTRL_DOWN_MASK,KeyEvent.VK_F8},{KeyEvent.CTRL_DOWN_MASK,KeyEvent.VK_NUMPAD8}}  // F28 
        // simply add more lines for more functions controls...
        // {{KeyEvent.CTRL_DOWN_MASK,KeyEvent.VK_F9},{KeyEvent.CTRL_DOWN_MASK,KeyEvent.VK_NUMPAD9}}, // F29
    };
        
    /**
     * @return the nextThrottleWindowKeys
     */
    public int[][] getNextThrottleWindowKeys() {
        return nextThrottleWindowKeys;
    }

    /**
     * @return the prevThrottleWindowKeys
     */
    public int[][] getPrevThrottleWindowKeys() {
        return prevThrottleWindowKeys;
    }

    /**
     * @return the nextThrottleFrameKeys
     */
    public int[][] getNextThrottleFrameKeys() {
        return nextThrottleFrameKeys;
    }

    /**
     * @return the prevThrottleFrameKeys
     */
    public int[][] getPrevThrottleFrameKeys() {
        return prevThrottleFrameKeys;
    }

    /**
     * @return the nextRunThrottleFrameKeys
     */
    public int[][] getNextRunThrottleFrameKeys() {
        return nextRunThrottleFrameKeys;
    }

    /**
     * @return the prevRunThrottleFrameKeys
     */
    public int[][] getPrevRunThrottleFrameKeys() {
        return prevRunThrottleFrameKeys;
    }

    /**
     * @return the nextThrottleInternalWindowKeys
     */
    public int[][] getNextThrottleInternalWindowKeys() {
        return nextThrottleInternalWindowKeys;
    }

    /**
     * @return the prevThrottleInternalWindowKeys
     */
    public int[][] getPrevThrottleInternalWindowKeys() {
        return prevThrottleInternalWindowKeys;
    }

    /**
     * @return the moveToControlPanelKeys
     */
    public int[][] getMoveToControlPanelKeys() {
        return moveToControlPanelKeys;
    }

    /**
     * @return the moveToFunctionPanelKeys
     */
    public int[][] getMoveToFunctionPanelKeys() {
        return moveToFunctionPanelKeys;
    }

    /**
     * @return the moveToAddressPanelKeys
     */
    public int[][] getMoveToAddressPanelKeys() {
        return moveToAddressPanelKeys;
    }

    /**
     * @return the reverseKeys
     */
    public int[][] getReverseKeys() {
        return reverseKeys;
    }

    /**
     * @return the forwardKeys
     */
    public int[][] getForwardKeys() {
        return forwardKeys;
    }

    /**
     * @return the switchDirectionKeys
     */
    public int[][] getSwitchDirectionKeys() {
        return switchDirectionKeys;
    }

    /**
     * @return the idleKeys
     */
    public int[][] getIdleKeys() {
        return idleKeys;
    }

    /**
     * @return the stopKeys
     */
    public int[][] getStopKeys() {
        return stopKeys;
    }

    /**
     * @return the accelerateKeys
     */
    public int[][] getAccelerateKeys() {
        return accelerateKeys;
    }

    /**
     * @return the decelerateKeys
     */
    public int[][] getDecelerateKeys() {
        return decelerateKeys;
    }

    /**
     * @return the accelerateMoreKeys
     */
    public int[][] getAccelerateMoreKeys() {
        return accelerateMoreKeys;
    }

    /**
     * @return the decelerateMoreKeys
     */
    public int[][] getDecelerateMoreKeys() {
        return decelerateMoreKeys;
    }

    /**
     * @param fn function number
     * @return the functionsKeys
     */
    public int[][] getFunctionsKeys(int fn) {
        return functionsKeys[fn];
    }
    
    public KeyStroke[] getKeyStrokes(String evtString) {
        int [][] keys = null;
        boolean onKeyRelease = true;
        switch (evtString) {
            // Throttle commands
            case "accelerate"      : keys = getAccelerateKeys(); onKeyRelease = false; break;
            case "decelerate"      : keys = getDecelerateKeys(); onKeyRelease = false; break;
            case "accelerateMore"  : keys = getAccelerateMoreKeys(); onKeyRelease = false; break;
            case "decelerateMore"  : keys = getDecelerateMoreKeys(); onKeyRelease = false; break;
            case "idle"            : keys = getIdleKeys(); break;
            case "stop"            : keys = getStopKeys(); break;
            case "forward"         : keys = getForwardKeys(); break;
            case "reverse"         : keys = getReverseKeys(); break;
            case "switchDirection" : keys = getSwitchDirectionKeys(); break;
            // Throttle inner window cycling
            case "nextJInternalFrame"     : keys = getNextThrottleInternalWindowKeys(); break;
            case "previousJInternalFrame" : keys = getPrevThrottleInternalWindowKeys(); break;
            case "showControlPanel"       : keys = getMoveToControlPanelKeys(); break;
            case "showFunctionPanel"      : keys = getMoveToFunctionPanelKeys(); break;
            case "showAddressPanel"       : keys = getMoveToAddressPanelKeys(); break;
            // Throttle frames
            case "nextThrottleFrame"            : keys = getNextThrottleFrameKeys(); break;
            case "previousThrottleFrame"        : keys = getPrevThrottleFrameKeys(); break;
            case "nextRunningThrottleFrame"     : keys = getNextRunThrottleFrameKeys(); break;
            case "previousRunningThrottleFrame" : keys = getPrevRunThrottleFrameKeys(); break;       
            // Throttle windows
            case "nextThrottleWindow"     : keys = getNextThrottleWindowKeys(); break;
            case "previousThrottleWindow" : keys = getPrevThrottleWindowKeys(); break;
            default:
        }
        // function buttons
        if (evtString.matches("fn_.*_.*")) {
            String[]tokens = evtString.split("_");
            keys = functionsKeys[Integer.parseInt(tokens[1])];
            if("Pressed".equals(tokens[2])) {
                onKeyRelease = false;
            } 
        }
        if (keys == null) {       
            return new KeyStroke[0];
        }        
        KeyStroke[] ks = new KeyStroke[keys.length];
        for (int i = 0 ; i < ks.length; i++) {
            ks[i] = KeyStroke.getKeyStroke(keys[i][1], keys[i][0], onKeyRelease);
        }
        return ks;
    }    
    
    /**
     * @return the number of functions shortcuts
     */
    public int getNbFunctionsKeys() {
        return functionsKeys.length;
    }

    /**
     * @param fn the function numbers
     * @param aFunctionsKeys the functionsKeys to set
     */
    public void setFunctionsKeys(int fn, int[][] aFunctionsKeys) {
        this.functionsKeys[fn] = aFunctionsKeys;
    }
    
    /**
     * @return the moreSpeedMultiplier
     */
    public float getMoreSpeedMultiplier() {
        return moreSpeedMultiplier;
    }

    /**
     * @param moreSpeedMultiplier the moreSpeedMultiplier to set
     */
    public void setMoreSpeedMultiplier(float moreSpeedMultiplier) {
        this.moreSpeedMultiplier = moreSpeedMultiplier;
    }

    /**
     * @param nextThrottleWindowKeys the nextThrottleWindowKeys to set
     */
    public void setNextThrottleWindowKeys(int[][] nextThrottleWindowKeys) {
        this.nextThrottleWindowKeys = nextThrottleWindowKeys;
    }

    /**
     * @param prevThrottleWindowKeys the prevThrottleWindowKeys to set
     */
    public void setPrevThrottleWindowKeys(int[][] prevThrottleWindowKeys) {
        this.prevThrottleWindowKeys = prevThrottleWindowKeys;
    }

    /**
     * @param nextTrottleFrameKeys the nextThrottleFrameKeys to set
     */
    public void setNextTrottleFrameKeys(int[][] nextTrottleFrameKeys) {
        this.nextThrottleFrameKeys = nextTrottleFrameKeys;
    }

    /**
     * @param prevThrottleFrameKeys the prevThrottleFrameKeys to set
     */
    public void setPrevThrottleFrameKeys(int[][] prevThrottleFrameKeys) {
        this.prevThrottleFrameKeys = prevThrottleFrameKeys;
    }

    /**
     * @param nextRunThrottleFrameKeys the nextRunThrottleFrameKeys to set
     */
    public void setNextRunThrottleFrameKeys(int[][] nextRunThrottleFrameKeys) {
        this.nextRunThrottleFrameKeys = nextRunThrottleFrameKeys;
    }

    /**
     * @param prevRunThrottleFrameKeys the prevRunThrottleFrameKeys to set
     */
    public void setPrevRunThrottleFrameKeys(int[][] prevRunThrottleFrameKeys) {
        this.prevRunThrottleFrameKeys = prevRunThrottleFrameKeys;
    }

    /**
     * @param nextThrottleInternalWindowKeys the nextThrottleInternalWindowKeys to set
     */
    public void setNextThrottleInternalWindowKeys(int[][] nextThrottleInternalWindowKeys) {
        this.nextThrottleInternalWindowKeys = nextThrottleInternalWindowKeys;
    }

    /**
     * @param prevThrottleInternalWindowKeys the prevThrottleInternalWindowKeys to set
     */
    public void setPrevThrottleInternalWindowKeys(int[][] prevThrottleInternalWindowKeys) {
        this.prevThrottleInternalWindowKeys = prevThrottleInternalWindowKeys;
    }

    /**
     * @param moveToControlPanelKeys the moveToControlPanelKeys to set
     */
    public void setMoveToControlPanelKeys(int[][] moveToControlPanelKeys) {
        this.moveToControlPanelKeys = moveToControlPanelKeys;
    }

    /**
     * @param moveToFunctionPanelKeys the moveToFunctionPanelKeys to set
     */
    public void setMoveToFunctionPanelKeys(int[][] moveToFunctionPanelKeys) {
        this.moveToFunctionPanelKeys = moveToFunctionPanelKeys;
    }

    /**
     * @param moveToAddressPanelKeys the moveToAddressPanelKeys to set
     */
    public void setMoveToAddressPanelKeys(int[][] moveToAddressPanelKeys) {
        this.moveToAddressPanelKeys = moveToAddressPanelKeys;
    }

    /**
     * @param reverseKeys the reverseKeys to set
     */
    public void setReverseKeys(int[][] reverseKeys) {
        this.reverseKeys = reverseKeys;
    }

    /**
     * @param forwardKeys the forwardKeys to set
     */
    public void setForwardKeys(int[][] forwardKeys) {
        this.forwardKeys = forwardKeys;
    }

    /**
     * @param switchDirectionKeys the switchDirectionKeys to set
     */
    public void setSwitchDirectionKeys(int[][] switchDirectionKeys) {
        this.switchDirectionKeys = switchDirectionKeys;
    }

    /**
     * @param idleKeys the idleKeys to set
     */
    public void setIdleKeys(int[][] idleKeys) {
        this.idleKeys = idleKeys;
    }

    /**
     * @param stopKeys the stopKeys to set
     */
    public void setStopKeys(int[][] stopKeys) {
        this.stopKeys = stopKeys;
    }

    /**
     * @param accelerateKeys the accelerateKeys to set
     */
    public void setAccelerateKeys(int[][] accelerateKeys) {
        this.accelerateKeys = accelerateKeys;
    }

    /**
     * @param decelerateKeys the decelerateKeys to set
     */
    public void setDecelerateKeys(int[][] decelerateKeys) {
        this.decelerateKeys = decelerateKeys;
    }

    /**
     * @param accelerateMoreKeys the accelerateMoreKeys to set
     */
    public void setAccelerateMoreKeys(int[][] accelerateMoreKeys) {
        this.accelerateMoreKeys = accelerateMoreKeys;
    }

    /**
     * @param decelerateMoreKeys the decelerateMoreKeys to set
     */
    public void setDecelerateMoreKeys(int[][] decelerateMoreKeys) {
        this.decelerateMoreKeys = decelerateMoreKeys;
    }
    
    @Override
    public ThrottlesPreferencesWindowKeyboardControls clone() throws CloneNotSupportedException {
        ThrottlesPreferencesWindowKeyboardControls ret = (ThrottlesPreferencesWindowKeyboardControls) super.clone();    
        
        ret.moreSpeedMultiplier = this.moreSpeedMultiplier;        
        ret.nextThrottleWindowKeys = clone(this.nextThrottleWindowKeys);
        ret.prevThrottleWindowKeys = clone(this.prevThrottleWindowKeys);        
        ret.nextThrottleFrameKeys = clone(this.nextThrottleFrameKeys);    
        ret.prevThrottleFrameKeys = clone(this.prevThrottleFrameKeys);    
        ret.nextRunThrottleFrameKeys = clone(this.nextRunThrottleFrameKeys);    
        ret.prevRunThrottleFrameKeys = clone(this.prevRunThrottleFrameKeys);
        ret.nextThrottleInternalWindowKeys  = clone(this.nextThrottleInternalWindowKeys);
        ret.prevThrottleInternalWindowKeys = clone(this.prevThrottleInternalWindowKeys);
        ret.moveToControlPanelKeys = clone(this.moveToControlPanelKeys);
        ret.moveToFunctionPanelKeys = clone(this.moveToFunctionPanelKeys);
        ret.moveToAddressPanelKeys = clone(this.moveToAddressPanelKeys);
        ret.reverseKeys = clone(this.reverseKeys);
        ret.forwardKeys = clone(this.forwardKeys);
        ret.switchDirectionKeys = clone(this.switchDirectionKeys);
        ret.idleKeys = clone(this.idleKeys);
        ret.stopKeys = clone(this.stopKeys);
        ret.accelerateKeys = clone(this.accelerateKeys);
        ret.decelerateKeys = clone(this.decelerateKeys);
        ret.accelerateMoreKeys = clone(this.accelerateMoreKeys);
        ret.decelerateMoreKeys = clone(this.decelerateMoreKeys);
        ret.functionsKeys = new int[this.functionsKeys.length][0][0];
        for (int i=0; i<this.functionsKeys.length; i++) {
            ret.functionsKeys[i] = clone(this.functionsKeys[i]);
        }
        return ret;
    }

    private int[][] clone(int[][] from) {
        int[][] to = new int[from.length][2];
        for (int i=0;i<from.length;i++) {            
            to[i][0] = from[i][0];
            to[i][1] = from[i][1];
        }
        return to;
    }
    
    private Element getControlXml(String eltname, int[][] controls) {        
        java.util.ArrayList<Element> children = new java.util.ArrayList<>(controls.length);
        for (int i=0;i<controls.length;i++) {
            org.jdom2.Element e = new org.jdom2.Element("ksc-"+i);
            e.setAttribute("m",""+controls[i][0]);
            e.setAttribute("k",""+controls[i][1]);
            children.add(e);
        }
        org.jdom2.Element e = new org.jdom2.Element(eltname);
        e.setContent(children);
        return e;
    }
    
    Element store() {
        java.util.ArrayList<Element> children = new java.util.ArrayList<>(10);
        children.add( getControlXml("nextThrottleWindowKeys", nextThrottleWindowKeys));
        children.add( getControlXml("prevThrottleWindowKeys", prevThrottleWindowKeys));
        children.add( getControlXml("nextThrottleFrameKeys", nextThrottleFrameKeys));
        children.add( getControlXml("prevThrottleFrameKeys", prevThrottleFrameKeys));
        children.add( getControlXml("nextRunThrottleFrameKeys", nextRunThrottleFrameKeys));
        children.add( getControlXml("prevRunThrottleFrameKeys", prevRunThrottleFrameKeys));
        children.add( getControlXml("nextThrottleInternalWindowKeys", nextThrottleInternalWindowKeys));
        children.add( getControlXml("prevThrottleInternalWindowKeys", prevThrottleInternalWindowKeys));
        children.add( getControlXml("moveToControlPanelKeys", moveToControlPanelKeys));
        children.add( getControlXml("moveToFunctionPanelKeys", moveToFunctionPanelKeys));
        children.add( getControlXml("moveToAddressPanelKeys", moveToAddressPanelKeys));
        children.add( getControlXml("reverseKeys", reverseKeys));
        children.add( getControlXml("forwardKeys", forwardKeys));
        children.add( getControlXml("switchDirectionKeys", switchDirectionKeys));
        children.add( getControlXml("idleKeys", idleKeys));
        children.add( getControlXml("stopKeys", stopKeys));
        children.add( getControlXml("accelerateKeys", accelerateKeys));
        children.add( getControlXml("decelerateKeys", decelerateKeys));
        children.add( getControlXml("accelerateMoreKeys", accelerateMoreKeys));
        children.add( getControlXml("decelerateMoreKeys", decelerateMoreKeys));
        for (int i=0;i<functionsKeys.length;i++) {
            children.add( getControlXml("functionsKeys-"+i, functionsKeys[i]));
        }
        org.jdom2.Element e = new org.jdom2.Element("throttlesControls");
        e.setAttribute("moreSpeedMultiplier",""+moreSpeedMultiplier);
        e.setContent(children);
        return e;        
    }
    
    private int[][] createControlFromXml(Element child) {
        int[][] ret = new int[child.getChildren().size()][2];
        int i=0;
        for (Element e : child.getChildren()) {
            ret[i][0] = Integer.parseInt(e.getAttributeValue("m"));
            ret[i][1] = Integer.parseInt(e.getAttributeValue("k"));
            i++;
        }
        
        return ret;
    }

    public void load(org.jdom2.Element e) {
        if ((e == null) || (e.getChildren().isEmpty())) {
            return;
        }
        try {
            nextThrottleWindowKeys = createControlFromXml(e.getChild("nextThrottleWindowKeys"));
            prevThrottleWindowKeys = createControlFromXml(e.getChild("prevThrottleWindowKeys"));
            nextThrottleFrameKeys = createControlFromXml(e.getChild("nextThrottleFrameKeys"));
            prevThrottleFrameKeys = createControlFromXml(e.getChild("prevThrottleFrameKeys"));
            nextRunThrottleFrameKeys = createControlFromXml(e.getChild("nextRunThrottleFrameKeys"));
            prevRunThrottleFrameKeys = createControlFromXml(e.getChild("prevRunThrottleFrameKeys"));
            nextThrottleInternalWindowKeys = createControlFromXml(e.getChild("nextThrottleInternalWindowKeys"));
            prevThrottleInternalWindowKeys = createControlFromXml(e.getChild("prevThrottleInternalWindowKeys"));
            moveToControlPanelKeys = createControlFromXml(e.getChild("moveToControlPanelKeys"));
            moveToFunctionPanelKeys = createControlFromXml(e.getChild("moveToFunctionPanelKeys"));
            moveToAddressPanelKeys = createControlFromXml(e.getChild("moveToAddressPanelKeys"));
            reverseKeys = createControlFromXml(e.getChild("reverseKeys"));
            forwardKeys = createControlFromXml(e.getChild("forwardKeys"));
            switchDirectionKeys = createControlFromXml(e.getChild("switchDirectionKeys"));
            idleKeys = createControlFromXml(e.getChild("idleKeys"));
            stopKeys = createControlFromXml(e.getChild("stopKeys"));
            accelerateKeys = createControlFromXml(e.getChild("accelerateKeys"));
            decelerateKeys = createControlFromXml(e.getChild("decelerateKeys"));
            accelerateMoreKeys = createControlFromXml(e.getChild("accelerateMoreKeys"));
            decelerateMoreKeys = createControlFromXml(e.getChild("decelerateMoreKeys"));
            for (int i=0;i<functionsKeys.length;i++) {
                functionsKeys[i] = createControlFromXml(e.getChild("functionsKeys-"+i));
            }
            moreSpeedMultiplier = Float.parseFloat(e.getAttributeValue("moreSpeedMultiplier"));
        } catch (NumberFormatException exc) {
            log.error("Error while restoring thottle controls from xml : "+exc.getMessage());
        }
        
    }
    
    private final static Logger log = LoggerFactory.getLogger(ThrottlesPreferencesWindowKeyboardControls.class);
}

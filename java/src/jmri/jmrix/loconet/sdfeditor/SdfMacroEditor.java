package jmri.jmrix.loconet.sdfeditor;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import jmri.jmrix.loconet.sdf.BranchTo;
import jmri.jmrix.loconet.sdf.ChannelStart;
import jmri.jmrix.loconet.sdf.DelaySound;
import jmri.jmrix.loconet.sdf.EndSound;
import jmri.jmrix.loconet.sdf.FourByteMacro;
import jmri.jmrix.loconet.sdf.GenerateTrigger;
import jmri.jmrix.loconet.sdf.InitiateSound;
import jmri.jmrix.loconet.sdf.LoadModifier;
import jmri.jmrix.loconet.sdf.MaskCompare;
import jmri.jmrix.loconet.sdf.Play;
import jmri.jmrix.loconet.sdf.SdfMacro;
import jmri.jmrix.loconet.sdf.SdlVersion;
import jmri.jmrix.loconet.sdf.SkemeStart;
import jmri.jmrix.loconet.sdf.SkipOnTrigger;
import jmri.jmrix.loconet.sdf.TwoByteMacro;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common base for all the SDF macro editors.
 *
 * @author Bob Jacobsen Copyright (C) 2007
 */
public abstract class SdfMacroEditor extends JPanel {

    public SdfMacroEditor(SdfMacro inst) {
        this.inst = inst;

        // add a default behavior
        add(new JLabel("This instruction has no options to set."));
    }

    SdfMacro inst;

    /**
     * Update editor when it's reshown
     */
    public void update() {
    }

    SdfMacro getMacro() {
        return inst;
    }

    /**
     * Notify that something has changed
     */
    public void updated() {
        if (treenode != null) {
            treenode.setUserObject(this);
        }
        if (editor != null) {
            editor.updateSummary();
        }
    }

    @Override
    public String toString() {
        return inst.toString();
    }

    public String oneInstructionString() {
        return inst.oneInstructionString();
    }

    public String allInstructionString(String indent) {
        return inst.allInstructionString(indent);
    }

    DefaultMutableTreeNode treenode = null;
    EditorPane editor = null;

    public void setNotify(DefaultMutableTreeNode node, EditorPane pane) {
        treenode = node;
        editor = pane;
    }

    /**
     * Return an editor object for a SdfMacro type.
     */
    static public SdfMacroEditor attachEditor(SdfMacro inst) {

        // full 1st byte decoder
        if (inst instanceof ChannelStart) {
            return new ChannelStartEditor(inst);
        } else if (inst instanceof SdlVersion) {
            return new SdlVersionEditor(inst);
        } else if (inst instanceof SkemeStart) {
            return new SkemeStartEditor(inst);
        } else if (inst instanceof GenerateTrigger) {
            return new GenerateTriggerEditor(inst);
        } else if (inst instanceof EndSound) {
            return new EndSoundEditor(inst);
        } else // 7 bit decode
        if (inst instanceof DelaySound) {
            return new DelaySoundEditor(inst);
        } else // 6 bit decode
        if (inst instanceof SkipOnTrigger) {
            return new SkipOnTriggerEditor(inst);
        } else // 5 bit decode
        if (inst instanceof InitiateSound) {
            return new InitiateSoundEditor(inst);
        } else if (inst instanceof MaskCompare) {
            return new MaskCompareEditor(inst);
        } else // 4 bit decode
        if (inst instanceof LoadModifier) {
            return new LoadModifierEditor(inst);
        } else if (inst instanceof BranchTo) {
            return new BranchToEditor(inst);
        } else // 2 bit decode
        if (inst instanceof Play) {
            return new PlayEditor(inst);
        } else // generics
        if (inst instanceof FourByteMacro) {
            return new FourByteMacroEditor(inst);
        } else if (inst instanceof TwoByteMacro) {
            return new TwoByteMacroEditor(inst);
        }

        log.error("PANIC");
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(SdfMacroEditor.class);

}

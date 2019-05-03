package jmri.jmrix.loconet.sdf;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common base for all the SDF macros defined by Digitrax for their sound
 * definition language.
 * <p>
 * Each macro has a number of descriptive forms:
 * <dl>
 * <dt>name()<dd>Just the name, in MPASM form.
 * <dt>toString()<dd>A brief description, with a terminating newline
 * <dt>oneInstructionString()<dd>The entire single instruction in MPASM from,
 * with a terminating newline
 * <dt>allInstructionString()<dd>The instruction and all those logically grouped
 * within it.
 * <dt>name()<dd>
 * </dl>
 *
 * SdfMacro and its subclasses don't do the notification needed to be Models in
 * an MVC edit paradyme. This is because there are a lot of SdfMacros in
 * realistic sound file, and the per-object overhead needed would be too large.
 * Hence (or perhaps because of no need), there is no support for simultaneous
 * editing of a single macro instruction updating multiple windows. You can have
 * multiple editors open on a single SdfBuffer, but these are not interlocked
 * against each other. (We could fix this by having a shared pool of "objects to
 * be notified of changes in the SdfBuffer, acccessed by reference during
 * editing (to avoid another dependency), but that's a project for another day)
 *
 * @author Bob Jacobsen Copyright (C) 2007
 */
public abstract class SdfMacro implements SdfConstants {

    /**
     * Name used by the macro in the SDF definition
     *
     * @return Fixed name associated with this type of instructio
     */
    abstract public String name();

    /**
     * Provide number of bytes defined by this macro
     *
     * @return Fixed numher of bytes defined (a constant for the instruction
     *         type)
     */
    abstract public int length();

    /**
     * Provide a single-line simplified representation, including the trailing
     * newline. This is used e.g. in the tree format section of the
     * {@link jmri.jmrix.loconet.sdfeditor.EditorFrame}.
     *
     * @return newline-terminated string; never null
     */
    @Override
    abstract public String toString();

    /**
     * Provide single instruction in MPASM format, including the trailing
     * newline.
     *
     * @return Newline terminated string, never null
     */
    abstract public String oneInstructionString();

    /**
     * Provide instructions in MPASM format, including the trailing newline and
     * all nested instructions.
     *
     * @return Newline terminated string, never null
     * @param indent String inserted at the start of each output line, typically
     *               some number of spaces.
     */
    abstract public String allInstructionString(String indent);

    /**
     * Access child (nested) instructions.
     *
     * @return List of children, which will be null except in case of nesting.
     */
    public List<SdfMacro> getChildren() {
        return children;
    }
    /**
     * Local member hold list of child (contained) instructions
     */
    ArrayList<SdfMacro> children = null;  // not changed unless there are some!

    /**
     * Total length, including contained instructions
     *
     * @return length of all parts
     */
    public int totalLength() {
        int result = length();
        List<SdfMacro> l = getChildren();
        if (l == null) {
            return result;
        }
        for (int i = 0; i < l.size(); i++) {
            result += l.get(i).totalLength();
        }
        return result;
    }

    /**
     * Store into a buffer.
     * <p>
     * This provides a default implementation for children, but each subclass
     * needs to store its own data with setAtIndexAndInc().
     *
     * @param buffer load with all children
     */
    public void loadByteArray(SdfBuffer buffer) {
        List<SdfMacro> l = getChildren();
        if (l == null) {
            return;
        }
        for (int i = 0; i < l.size(); i++) {
            l.get(i).loadByteArray(buffer);
        }
    }

    /**
     * Get the next instruction macro in a buffer.
     * <p>
     * Note this uses the index contained in the SdfBuffer implementation, and
     * has the side-effect of bumping that forward.
     *
     * @param buff The SdfBuffer being scanned for instruction macros.
     * @return Object of SdfMacro subtype for specific next instruction
     */
    static public SdfMacro decodeInstruction(SdfBuffer buff) {
        SdfMacro m;

        // full 1st byte decoder
        if ((m = ChannelStart.match(buff)) != null) {
            return m;
        } else if ((m = SdlVersion.match(buff)) != null) {
            return m;
        } else if ((m = SkemeStart.match(buff)) != null) {
            return m;
        } else if ((m = GenerateTrigger.match(buff)) != null) {
            return m;
        } else if ((m = EndSound.match(buff)) != null) {
            return m;
        } else // 7 bit decode
        if ((m = DelaySound.match(buff)) != null) {
            return m;
        } else // 6 bit decode
        if ((m = SkipOnTrigger.match(buff)) != null) {
            return m;
        } else // 5 bit decode
        if ((m = InitiateSound.match(buff)) != null) {
            return m;
        } else if ((m = MaskCompare.match(buff)) != null) {
            return m;
        } else // 4 bit decode
        if ((m = LoadModifier.match(buff)) != null) {
            return m;
        } else if ((m = BranchTo.match(buff)) != null) {
            return m;
        } else // 2 bit decode
        if ((m = Play.match(buff)) != null) {
            return m;
        } else // generics
        if ((m = FourByteMacro.match(buff)) != null) {
            return m;
        } else  { // only case left is TwoByteMacro, which has to match
            return TwoByteMacro.match(buff);
        }
    }

    /**
     * Service method to unpack various bit-coded values for display, using a
     * mask array.
     * <p>
     * Note that multiple values can be returned, e.g. this can be used to scan
     * for individual bits set in a variable.
     *
     * @param input  Single value to be matched
     * @param values Array of possible values which the input might match
     * @param masks  Array of masks to be applied when comparing against the
     *               corresponding items in the values array. This is separate
     *               for each possible value to e.g. allow the encoding of a set
     *               of independent bits.
     * @param labels Should there be a match-under-mask of a value, the
     *               corresponding label is returned
     * @return "+" separated list of labels, or "&lt;ERROR&gt;" if none matched
     */
    String decodeFlags(int input, int[] values, int[] masks, String[] labels) {
        String[] names = jmri.util.StringUtil.getNamesFromStateMasked(input, values, masks, labels);
        if (names.length == 0) {
            return labels[labels.length - 1];  // last name is non-of-above special case
        } else if (names.length == 1) {
            return names[0];
        }
        StringBuilder output = new StringBuilder(names[0]);
        for (int i = 1; i < names.length; i++) {
            output.append("+").append(names[i]);
        }
        return output.toString();
    }

    String decodeState(int input, int[] values, String[] labels) {
        String val = jmri.util.StringUtil.getNameFromState(input, values, labels);
        if (val == null) {
            return labels[labels.length - 1];
        }
        return val;
    }

    // private final static Logger log = LoggerFactory.getLogger(SdfMacro.class);

}

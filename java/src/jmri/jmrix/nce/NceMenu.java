package jmri.jmrix.nce;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Create a "Systems" menu containing the Jmri NCE-specific tools.
 * <p>
 * Some of the tools used here are also used directly by the Wangrow support in
 * {@link jmri.jmrix.wangrow}.
 *
 * @author Bob Jacobsen Copyright 2003 moved to swing class
 * @author kcameron 2010
 */
@Deprecated
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public class NceMenu extends jmri.jmrix.nce.swing.NceMenu {

    public NceMenu(NceSystemConnectionMemo memo) {
        super(memo);
    }
}

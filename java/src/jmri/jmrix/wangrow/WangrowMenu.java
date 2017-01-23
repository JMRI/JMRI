package jmri.jmrix.wangrow;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.jmrix.nce.NceSystemConnectionMemo;

/**
 * Create a "Systems" menu containing the Jmri NCE-specific tools.
 * <P>
 * Note that this is still using specific tools from the {@link jmri.jmrix.nce}
 * package.
 *
 * @author	Bob Jacobsen Copyright 2003
 */
@Deprecated
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public class WangrowMenu extends jmri.jmrix.nce.swing.NceMenu {

    public WangrowMenu(NceSystemConnectionMemo memo) {
        super(memo);
    }
}

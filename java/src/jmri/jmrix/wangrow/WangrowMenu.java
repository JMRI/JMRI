// WangrowMenu.java
package jmri.jmrix.wangrow;

import jmri.jmrix.nce.NceSystemConnectionMemo;

/**
 * Create a "Systems" menu containing the Jmri NCE-specific tools.
 * <P>
 * Note that this is still using specific tools from the {@link jmri.jmrix.nce}
 * package.
 *
 * @author	Bob Jacobsen Copyright 2003
 * @version $Revision$
 */
@Deprecated
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public class WangrowMenu extends jmri.jmrix.nce.swing.NceMenu {

    /**
     *
     */
    private static final long serialVersionUID = 3673055801676833967L;

    public WangrowMenu(NceSystemConnectionMemo memo) {
        super(memo);
    }
}

package jmri.jmrix.wangrow;

import jmri.jmrix.nce.NceSystemConnectionMemo;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Create a "Systems" menu containing the Jmri Wangrow-specific tools.
 * <p>
 * Note that this is still using specific tools from the {@link jmri.jmrix.nce}
 * package.
 *
 * @author Bob Jacobsen Copyright 2003
 */
@Deprecated
@API(status = EXPERIMENTAL)
public class WangrowMenu extends jmri.jmrix.nce.swing.NceMenu {

    public WangrowMenu(NceSystemConnectionMemo memo) {
        super(memo);
    }

}

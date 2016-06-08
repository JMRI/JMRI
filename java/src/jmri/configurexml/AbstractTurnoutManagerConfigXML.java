package jmri.configurexml;

/**
 * Dummy class, just present so files that refer to this class (e.g. pre JMRI
 * 2.5.4 files) can still be read by deferring to the present class.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 * @deprecated 2.5.4  - left so old files can be read
 */
@Deprecated
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "duplicate name OK while deprecated, for XML config migration")
public abstract class AbstractTurnoutManagerConfigXML
        extends jmri.managers.configurexml.AbstractTurnoutManagerConfigXML {
}

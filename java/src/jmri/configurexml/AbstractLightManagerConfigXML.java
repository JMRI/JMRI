package jmri.configurexml;

/**
 * Dummy class, just present so files that refer to this class (e.g. pre JMRI
 * 2.5.4 files) can still be read by deferring to the present class.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 * @version $Revision$
 * @deprecated 2.5.4
 */
@Deprecated
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public abstract class AbstractLightManagerConfigXML
        extends jmri.managers.configurexml.AbstractLightManagerConfigXML {
}

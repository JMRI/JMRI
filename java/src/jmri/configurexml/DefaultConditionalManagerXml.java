package jmri.configurexml;

/**
 * Dummy class, just present so files that refer to this class (e.g. pre JMRI
 * 2.8 files) can still be read by deferring to the present class.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 * @version $Revision$
 * @deprecated 2.7.8
 */
@Deprecated
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
// This is a configurexml migration class, same name is OK
public class DefaultConditionalManagerXml
        extends jmri.managers.configurexml.DefaultConditionalManagerXml {
}

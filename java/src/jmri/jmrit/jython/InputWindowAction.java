package jmri.jmrit.jython;

/**
 * This class is left in place so that existing preferences will still work until they
 * are migrated. Do not use.
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2022
 * @deprecated For removal after JMRI 5.2
 */
@Deprecated(forRemoval=true)
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value="NM_SAME_SIMPLE_NAME_AS_SUPERCLASS",
            justification="Deliberate shadowing to migrate preferences files")
public class InputWindowAction extends jmri.script.swing.InputWindowAction {
    // everything is deferred to jmri.script.swing.InputWindowAction
}

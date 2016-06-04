package jmri;

/**
 * Migration stand-in for {@link apps.GuiLafConfigPane} so old config files can be read
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @deprecated 2.9.5 - left so old files can be read
 */
@Deprecated
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")  // duplicate name OK while deprecated, for XML config migration
public class GuiLafConfigPane extends apps.GuiLafConfigPane {

}

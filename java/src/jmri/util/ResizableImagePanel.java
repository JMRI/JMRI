package jmri.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * A class extending JPanels to have a image display in a panel, supports: +
 * drag'n drop of image file + can resize container + can scale content to size
 * + respect aspect ratio by default (when resizing content).
 *
 * @author Lionel Jeanson - Copyright 2009
 * @deprecated since 4.9.4; not used in JMRI; use
 * {@link jmri.util.swing.ResizableImagePanel} instead
 */
@Deprecated
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "Deprecated package for same class in different package.")
public class ResizableImagePanel extends jmri.util.swing.ResizableImagePanel {
}

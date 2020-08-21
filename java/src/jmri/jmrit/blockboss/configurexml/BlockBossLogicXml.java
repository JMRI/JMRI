package jmri.jmrit.blockboss.configurexml;

/**
 * Handle XML persistance of Simple Signal Logic objects.
 *
 * <p>
 * In JMRI 2.1.5, the XML written by this package was changed.
 * <p>
 * Previously, it wrote a single "blocks" element, which contained multiple
 * "block" elements to represent each individual BlockBoss (Simple Signal Logic)
 * object.
 * <p>
 * These names were too generic, and conflicted with storing true Block objects.
 * <p>
 * Starting in JMRI 2.1.5 (May 2008), these were changed to "signalelements" and
 * "signalelement" respectively.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2005
 *
 * Revisions to add facing point sensors, approach lighting, and limited speed.
 * Dick Bronson (RJB) 2006
 */
public class BlockBossLogicXml extends BlockBossLogicProviderXml {
}

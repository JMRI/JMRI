package jmri.jmrit.ussctc;

import jmri.*;

/**
 * A Station represents the bits in the code message and the 
 * hardware at both ends that is controlled
 * <ul>
 * <li>Two bits for Turnouts
 * <li>Three bits for Signals
 * <li>One bit for maintainer call, track circuits, etc
 * </ul>
 * The basic structure is to mate two objects that interact via a 
 * shared enum.  E.g. a TurnoutSection (on CTC machine) and TurnoutController (in field)
 * <ul>
 * <li>The field object listens to the status of the layout and sends indications on changes
 * <p>The CTC object responds to those indications
 * <li>The CTC machine object sends when Code is pressed.
 * <p>The field object responds to those when received
 * </ul>
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
public class Station {

}

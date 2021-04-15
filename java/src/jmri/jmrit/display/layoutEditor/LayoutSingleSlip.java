package jmri.jmrit.display.layoutEditor;

/**
 * A LayoutSlip is a crossing of two straight tracks designed in such a way as
 * to allow trains to change from one straight track to the other, as well as
 * going straight across.
 * <p>
 * A LayoutSlip has four connection points, designated A, B, C, and D. A train
 * may proceed between A and D, A and C, B and D and in the case of
 * double-slips, B and C.
 * <br>
 * <pre>
 * \\      //
 *   A==-==D
 *    \\ //
 *      X
 *    // \\
 *   B==-==C
 *  //      \\
 * </pre>
 * <br>
 * For drawing purposes, each LayoutSlip carries a center point and
 * displacements for A and B. The displacements for C = - the displacement for
 * A, and the displacement for D = - the displacement for B. The center point
 * and these displacements may be adjusted by the user when in edit mode.
 * <p>
 * When LayoutSlips are first created, there are no connections. Block
 * information and connections are added when available.
 * <p>
 * SignalHead names are saved here to keep track of where signals are.
 * LayoutSlip only serves as a storage place for SignalHead names. The names are
 * placed here by Set Signals at Level Crossing in Tools menu.
 *
 * @author Dave Duchamp Copyright (c) 2004-2007
 * @author George Warner Copyright (c) 2017-2019
 */
public class LayoutSingleSlip extends LayoutSlip {

    /**
     * Constructor method.
     * @param id slip ID.
     * @param layoutEditor main layout editor.
     */
    public LayoutSingleSlip(String id, LayoutEditor layoutEditor) {
        super(id, layoutEditor, TurnoutType.SINGLE_SLIP);
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutSingleSlip.class);
}

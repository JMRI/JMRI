package jmri.jmrit.display;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * IndicatorTurnoutIconTest.java
 *
 * @author Bob Jacobsen
 */
public class IndicatorTurnoutIconTest extends PositionableIconTest {

    @Test
    @DisabledIfHeadless
    public void testEquals() {

        IndicatorTurnoutIcon to = new IndicatorTurnoutIcon(editor);
        jmri.Turnout turnout = jmri.InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        to.setTurnout(new jmri.NamedBeanHandle<>("IT1", turnout));

        IndicatorTurnoutIcon to2 = new IndicatorTurnoutIcon(editor);
        turnout = jmri.InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        to2.setTurnout(new jmri.NamedBeanHandle<>("IT1", turnout));

        assertTrue( to.equals(to), "identity");
        assertFalse( to2.equals(to), "object (not content) equality");
        assertFalse( to.equals(to2), "object (not content) equality commutes");
    }

    @Test
    @Override
    @DisabledIfHeadless
    public void testClone() {

        IndicatorTurnoutIcon to = (IndicatorTurnoutIcon)p; 

        IndicatorTurnoutIcon to2 = (IndicatorTurnoutIcon) to.deepClone();

        assertFalse( to2.equals(to), "clone object (not content) equality");

        assertTrue( to2.getClass().equals(to.getClass()), "class type equality");

    }

    @Test
    @Disabled("unreliable on CI servers")
    @Override
    @DisabledIfHeadless
    public void testGetAndSetPositionable() {
        assertTrue( p.isPositionable(), "Defalt Positionable");
        p.setPositionable(false);
        assertFalse( p.isPositionable(), "Positionable after set false");
        p.setPositionable(true);
        assertTrue( p.isPositionable(), "Positionable after set true");
    }

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp(); // creates editor
        IndicatorTurnoutIcon to = new IndicatorTurnoutIcon(editor);
        jmri.Turnout turnout = jmri.InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        to.setTurnout(new jmri.NamedBeanHandle<>("IT1", turnout));
        p = to;
    }

    // private final static Logger log = LoggerFactory.getLogger(IndicatorTurnoutIconTest.class);

}

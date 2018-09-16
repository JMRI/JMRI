package jmri;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test the Conditional interface
 */
public class ConditionalTest {

    @Test
    public void testConstants() {
        // It might be a good idea to change constants into enums.
        // These tests ensures that the values of the constants stay the same
        // if that change is done.
        
        Assert.assertTrue(NamedBean.UNKNOWN == 0x01);
        Assert.assertTrue(Conditional.UNKNOWN == 0x01);
        Assert.assertTrue(Conditional.FALSE == 0x02);
        Assert.assertTrue(Conditional.TRUE == 0x04);

        // logic operators used in antecedent
        Assert.assertTrue(Conditional.ALL_AND == 0x01);
        Assert.assertTrue(Conditional.ALL_OR == 0x02);
        Assert.assertTrue(Conditional.MIXED == 0x03);

        // state variable definitions
        Assert.assertTrue(Conditional.OPERATOR_AND == 1);
        Assert.assertTrue(Conditional.OPERATOR_NOT == 2);
        Assert.assertTrue(Conditional.OPERATOR_AND_NOT == 3);
        Assert.assertTrue(Conditional.OPERATOR_NONE == 4);
        Assert.assertTrue(Conditional.OPERATOR_OR == 5);
        Assert.assertTrue(Conditional.OPERATOR_OR_NOT == 6);
        // state variable types
        Assert.assertTrue(Conditional.TYPE_NONE == 0);
        Assert.assertTrue(Conditional.TYPE_SENSOR_ACTIVE == 1);
        Assert.assertTrue(Conditional.TYPE_SENSOR_INACTIVE == 2);
        Assert.assertTrue(Conditional.TYPE_TURNOUT_THROWN == 3);
        Assert.assertTrue(Conditional.TYPE_TURNOUT_CLOSED == 4);
        Assert.assertTrue(Conditional.TYPE_CONDITIONAL_TRUE == 5);
        Assert.assertTrue(Conditional.TYPE_CONDITIONAL_FALSE == 6);
        Assert.assertTrue(Conditional.TYPE_LIGHT_ON == 7);
        Assert.assertTrue(Conditional.TYPE_LIGHT_OFF == 8);
        Assert.assertTrue(Conditional.TYPE_MEMORY_EQUALS == 9);
        Assert.assertTrue(Conditional.TYPE_FAST_CLOCK_RANGE == 10);
        // Note - within the TYPE_SIGNAL_HEAD definitions, all must be together,
        //  RED must be first, and HELD must be last
        Assert.assertTrue(Conditional.TYPE_SIGNAL_HEAD_RED == 11);
        Assert.assertTrue(Conditional.TYPE_SIGNAL_HEAD_YELLOW == 12);
        Assert.assertTrue(Conditional.TYPE_SIGNAL_HEAD_GREEN == 13);
        Assert.assertTrue(Conditional.TYPE_SIGNAL_HEAD_DARK == 14);
        Assert.assertTrue(Conditional.TYPE_SIGNAL_HEAD_FLASHRED == 15);
        Assert.assertTrue(Conditional.TYPE_SIGNAL_HEAD_FLASHYELLOW == 16);
        Assert.assertTrue(Conditional.TYPE_SIGNAL_HEAD_FLASHGREEN == 17);
        Assert.assertTrue(Conditional.TYPE_SIGNAL_HEAD_LIT == 18);
        Assert.assertTrue(Conditional.TYPE_SIGNAL_HEAD_HELD == 19);
        Assert.assertTrue(Conditional.TYPE_MEMORY_COMPARE == 20);
        Assert.assertTrue(Conditional.TYPE_SIGNAL_HEAD_LUNAR == 21);
        Assert.assertTrue(Conditional.TYPE_SIGNAL_HEAD_FLASHLUNAR == 22);
        Assert.assertTrue(Conditional.TYPE_MEMORY_EQUALS_INSENSITIVE == 23);
        Assert.assertTrue(Conditional.TYPE_MEMORY_COMPARE_INSENSITIVE == 24);
        // Warrant variables
        Assert.assertTrue(Conditional.TYPE_ROUTE_FREE == 25);
        Assert.assertTrue(Conditional.TYPE_ROUTE_OCCUPIED == 26);
        Assert.assertTrue(Conditional.TYPE_ROUTE_ALLOCATED == 27);
        Assert.assertTrue(Conditional.TYPE_ROUTE_SET == 28);
        Assert.assertTrue(Conditional.TYPE_TRAIN_RUNNING == 29);
        Assert.assertTrue(Conditional.TYPE_SIGNAL_MAST_ASPECT_EQUALS == 30);
        Assert.assertTrue(Conditional.TYPE_SIGNAL_MAST_LIT == 31);
        Assert.assertTrue(Conditional.TYPE_SIGNAL_MAST_HELD == 32);
        Assert.assertTrue(Conditional.TYPE_SIGNAL_HEAD_APPEARANCE_EQUALS == 33);
        Assert.assertTrue(Conditional.TYPE_BLOCK_STATUS_EQUALS == 34);

        //Entry Exit Rules
        Assert.assertTrue(Conditional.TYPE_ENTRYEXIT_ACTIVE == 35);
        Assert.assertTrue(Conditional.TYPE_ENTRYEXIT_INACTIVE == 36);

        // action definitions
        Assert.assertTrue(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE == 1);
        Assert.assertTrue(Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE == 2);
        Assert.assertTrue(Conditional.ACTION_OPTION_ON_CHANGE == 3);
        Assert.assertTrue(Conditional.NUM_ACTION_OPTIONS == 3);

        // action types
        Assert.assertTrue(Conditional.ACTION_NONE == 1);
        Assert.assertTrue(Conditional.ACTION_SET_TURNOUT == 2);
        // allowed settings for turnout are Thrown and Closed (in data)
        Assert.assertTrue(Conditional.ACTION_SET_SIGNAL_APPEARANCE == 3);
        // allowed settings for signal head are the seven Appearances (in data)
        Assert.assertTrue(Conditional.ACTION_SET_SIGNAL_HELD == 4);
        Assert.assertTrue(Conditional.ACTION_CLEAR_SIGNAL_HELD == 5);
        Assert.assertTrue(Conditional.ACTION_SET_SIGNAL_DARK == 6);
        Assert.assertTrue(Conditional.ACTION_SET_SIGNAL_LIT == 7);
        Assert.assertTrue(Conditional.ACTION_TRIGGER_ROUTE == 8);
        Assert.assertTrue(Conditional.ACTION_SET_SENSOR == 9);
        // allowed settings for sensor are active and inactive (in data)
        Assert.assertTrue(Conditional.ACTION_DELAYED_SENSOR == 10);
        // allowed settings for timed sensor are active and inactive (in data)
        //   time in seconds before setting sensor should be in delay
        Assert.assertTrue(Conditional.ACTION_SET_LIGHT == 11);
        // allowed settings for light are ON and OFF (in data)
        Assert.assertTrue(Conditional.ACTION_SET_MEMORY == 12);
        // text to set into the memory variable should be in string
        Assert.assertTrue(Conditional.ACTION_ENABLE_LOGIX == 13);
        Assert.assertTrue(Conditional.ACTION_DISABLE_LOGIX == 14);
        Assert.assertTrue(Conditional.ACTION_PLAY_SOUND == 15);
        // reference to sound should be in string
        Assert.assertTrue(Conditional.ACTION_RUN_SCRIPT == 16);
        // reference to script should be in string
        Assert.assertTrue(Conditional.ACTION_DELAYED_TURNOUT == 17);
        // allowed settings for timed turnout are Thrown and Closed (in data)
        //   time in seconds before setting turnout should be in delay
        Assert.assertTrue(Conditional.ACTION_LOCK_TURNOUT == 18);
        Assert.assertTrue(Conditional.ACTION_RESET_DELAYED_SENSOR == 19);
        // allowed settings for timed sensor are active and inactive (in data)
        //   time in seconds before setting sensor should be in delay
        Assert.assertTrue(Conditional.ACTION_CANCEL_SENSOR_TIMERS == 20);
        // cancels all timers delaying setting of specified sensor
        Assert.assertTrue(Conditional.ACTION_RESET_DELAYED_TURNOUT == 21);
        // allowed settings for timed sensor are active and inactive (in data)
        //   time in seconds before setting sensor should be in delay
        Assert.assertTrue(Conditional.ACTION_CANCEL_TURNOUT_TIMERS == 22);
        // cancels all timers delaying setting of specified sensor
        Assert.assertTrue(Conditional.ACTION_SET_FAST_CLOCK_TIME == 23);
        // sets the fast clock time to the time specified
        Assert.assertTrue(Conditional.ACTION_START_FAST_CLOCK == 24);
        // starts the fast clock
        Assert.assertTrue(Conditional.ACTION_STOP_FAST_CLOCK == 25);
        // stops the fast clock
        Assert.assertTrue(Conditional.ACTION_COPY_MEMORY == 26);
        // copies value from memory variable (in name) to memory variable (in string)
        Assert.assertTrue(Conditional.ACTION_SET_LIGHT_INTENSITY == 27);
        Assert.assertTrue(Conditional.ACTION_SET_LIGHT_TRANSITION_TIME == 28);
        // control the specified audio object
        Assert.assertTrue(Conditional.ACTION_CONTROL_AUDIO == 29);
        // execute a jython command
        Assert.assertTrue(Conditional.ACTION_JYTHON_COMMAND == 30);
        // Warrant actions
        Assert.assertTrue(Conditional.ACTION_ALLOCATE_WARRANT_ROUTE == 31);
        Assert.assertTrue(Conditional.ACTION_DEALLOCATE_WARRANT_ROUTE == 32);
        Assert.assertTrue(Conditional.ACTION_SET_ROUTE_TURNOUTS == 33);
        Assert.assertTrue(Conditional.ACTION_AUTO_RUN_WARRANT == 34);
        Assert.assertTrue(Conditional.ACTION_CONTROL_TRAIN == 35);
        Assert.assertTrue(Conditional.ACTION_SET_TRAIN_ID == 36);
        Assert.assertTrue(Conditional.ACTION_SET_SIGNALMAST_ASPECT == 37);
        Assert.assertTrue(Conditional.ACTION_THROTTLE_FACTOR == 38);
        Assert.assertTrue(Conditional.ACTION_SET_SIGNALMAST_HELD == 39);
        Assert.assertTrue(Conditional.ACTION_CLEAR_SIGNALMAST_HELD == 40);
        Assert.assertTrue(Conditional.ACTION_SET_SIGNALMAST_DARK == 41);
        Assert.assertTrue(Conditional.ACTION_SET_SIGNALMAST_LIT == 42);
        Assert.assertTrue(Conditional.ACTION_SET_BLOCK_ERROR == 43);
        Assert.assertTrue(Conditional.ACTION_CLEAR_BLOCK_ERROR == 44);
        Assert.assertTrue(Conditional.ACTION_DEALLOCATE_BLOCK == 45);
        Assert.assertTrue(Conditional.ACTION_SET_BLOCK_OUT_OF_SERVICE == 46);
        Assert.assertTrue(Conditional.ACTION_SET_BLOCK_IN_SERVICE == 47);
        Assert.assertTrue(Conditional.ACTION_MANUAL_RUN_WARRANT == 48);
        Assert.assertTrue(Conditional.ACTION_SET_TRAIN_NAME == 49);
        Assert.assertTrue(Conditional.ACTION_SET_BLOCK_VALUE == 50);
        // EntryExit Actions
        Assert.assertTrue(Conditional.ACTION_SET_NXPAIR_ENABLED == 51);
        Assert.assertTrue(Conditional.ACTION_SET_NXPAIR_DISABLED == 52);
        Assert.assertTrue(Conditional.ACTION_SET_NXPAIR_SEGMENT == 53);
        Assert.assertTrue(Conditional.NUM_ACTION_TYPES == 53);

        /**
         * ***********************************************************************************
         */
        /* New Variable and Action type scheme for Logix UI
         * State Variables and actions are grouped according to type.  Variable and action
         * types share the following group categories:
         */
        // state variable and action items used by logix.
        // When a new type is added, insert at proper location and update 'LAST' numbers
        Assert.assertTrue(Conditional.ITEM_TYPE_SENSOR == 1);
        Assert.assertTrue(Conditional.ITEM_TYPE_TURNOUT == 2);
        Assert.assertTrue(Conditional.ITEM_TYPE_LIGHT == 3);
        Assert.assertTrue(Conditional.ITEM_TYPE_SIGNALHEAD == 4);
        Assert.assertTrue(Conditional.ITEM_TYPE_SIGNALMAST == 5);
        Assert.assertTrue(Conditional.ITEM_TYPE_MEMORY == 6);
        Assert.assertTrue(Conditional.ITEM_TYPE_CONDITIONAL == 7);  // used only by ConditionalVariable
        Assert.assertTrue(Conditional.ITEM_TYPE_LOGIX == 7);        // used only by ConditionalAction
        Assert.assertTrue(Conditional.ITEM_TYPE_WARRANT == 8);
        Assert.assertTrue(Conditional.ITEM_TYPE_CLOCK == 9);
        Assert.assertTrue(Conditional.ITEM_TYPE_OBLOCK == 10);
        Assert.assertTrue(Conditional.ITEM_TYPE_ENTRYEXIT == 11);
        Assert.assertTrue(Conditional.ITEM_TYPE_LAST_STATE_VAR == 11);

        Assert.assertTrue(Conditional.ITEM_TYPE_AUDIO == 12);
        Assert.assertTrue(Conditional.ITEM_TYPE_SCRIPT == 13);
        Assert.assertTrue(Conditional.ITEM_TYPE_OTHER == 14);
        Assert.assertTrue(Conditional.ITEM_TYPE_LAST_ACTION == 14);
    }
    
    @Test
    public void testArrays() {
        // These arrays has @SuppressFBWarnings so it might be good to refactor the code,
        // for example by using enums.
        
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_NONE] == Conditional.TYPE_NONE);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_SENSOR_ACTIVE] == Conditional.ITEM_TYPE_SENSOR);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_SENSOR_INACTIVE] == Conditional.ITEM_TYPE_SENSOR);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_TURNOUT_THROWN] == Conditional.ITEM_TYPE_TURNOUT);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_TURNOUT_CLOSED] == Conditional.ITEM_TYPE_TURNOUT);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_CONDITIONAL_TRUE] == Conditional.ITEM_TYPE_CONDITIONAL);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_CONDITIONAL_FALSE] == Conditional.ITEM_TYPE_CONDITIONAL);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_LIGHT_ON] == Conditional.ITEM_TYPE_LIGHT);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_LIGHT_OFF] == Conditional.ITEM_TYPE_LIGHT);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_MEMORY_EQUALS] == Conditional.ITEM_TYPE_MEMORY);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_FAST_CLOCK_RANGE] == Conditional.ITEM_TYPE_CLOCK);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_SIGNAL_HEAD_RED] == Conditional.ITEM_TYPE_SIGNALHEAD);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_SIGNAL_HEAD_YELLOW] == Conditional.ITEM_TYPE_SIGNALHEAD);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_SIGNAL_HEAD_GREEN] == Conditional.ITEM_TYPE_SIGNALHEAD);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_SIGNAL_HEAD_DARK] == Conditional.ITEM_TYPE_SIGNALHEAD);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_SIGNAL_HEAD_FLASHRED] == Conditional.ITEM_TYPE_SIGNALHEAD);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_SIGNAL_HEAD_FLASHYELLOW] == Conditional.ITEM_TYPE_SIGNALHEAD);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_SIGNAL_HEAD_FLASHGREEN] == Conditional.ITEM_TYPE_SIGNALHEAD);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_SIGNAL_HEAD_LIT] == Conditional.ITEM_TYPE_SIGNALHEAD);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_SIGNAL_HEAD_HELD] == Conditional.ITEM_TYPE_SIGNALHEAD);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_MEMORY_COMPARE] == Conditional.ITEM_TYPE_MEMORY);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_SIGNAL_HEAD_LUNAR] == Conditional.ITEM_TYPE_SIGNALHEAD);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_SIGNAL_HEAD_FLASHLUNAR] == Conditional.ITEM_TYPE_SIGNALHEAD);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_MEMORY_EQUALS_INSENSITIVE] == Conditional.ITEM_TYPE_MEMORY);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_MEMORY_COMPARE_INSENSITIVE] == Conditional.ITEM_TYPE_MEMORY);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_ROUTE_FREE] == Conditional.ITEM_TYPE_WARRANT);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_ROUTE_OCCUPIED] == Conditional.ITEM_TYPE_WARRANT);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_ROUTE_ALLOCATED] == Conditional.ITEM_TYPE_WARRANT);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_ROUTE_SET] == Conditional.ITEM_TYPE_WARRANT);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_TRAIN_RUNNING] == Conditional.ITEM_TYPE_WARRANT);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_SIGNAL_MAST_ASPECT_EQUALS] == Conditional.ITEM_TYPE_SIGNALMAST);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_SIGNAL_MAST_LIT] == Conditional.ITEM_TYPE_SIGNALMAST);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_SIGNAL_MAST_HELD] == Conditional.ITEM_TYPE_SIGNALMAST);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_SIGNAL_HEAD_APPEARANCE_EQUALS] == Conditional.ITEM_TYPE_SIGNALHEAD);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_BLOCK_STATUS_EQUALS] == Conditional.ITEM_TYPE_OBLOCK);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_ENTRYEXIT_ACTIVE] == Conditional.ITEM_TYPE_ENTRYEXIT);
        Assert.assertTrue(Conditional.TEST_TO_ITEM[Conditional.TYPE_ENTRYEXIT_INACTIVE] == Conditional.ITEM_TYPE_ENTRYEXIT);
    }
    
    // from here down is testing infrastructure

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initIdTagManager();
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }
    
}

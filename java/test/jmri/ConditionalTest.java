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
        
        Assert.assertEquals(NamedBean.UNKNOWN, 0x01);
        Assert.assertEquals(Conditional.UNKNOWN, 0x01);
        Assert.assertEquals(Conditional.FALSE, 0x02);
        Assert.assertEquals(Conditional.TRUE, 0x04);

        // logic operators used in antecedent
        Assert.assertEquals(Conditional.ALL_AND, 0x01);
        Assert.assertEquals(Conditional.ALL_OR, 0x02);
        Assert.assertEquals(Conditional.MIXED, 0x03);

        // state variable definitions
        Assert.assertEquals(Conditional.OPERATOR_AND, 1);
        Assert.assertEquals(Conditional.OPERATOR_NOT, 2);
        Assert.assertEquals(Conditional.OPERATOR_AND_NOT, 3);
        Assert.assertEquals(Conditional.OPERATOR_NONE, 4);
        Assert.assertEquals(Conditional.OPERATOR_OR, 5);
        Assert.assertEquals(Conditional.OPERATOR_OR_NOT, 6);
        // state variable types
        Assert.assertEquals(Conditional.TYPE_NONE, 0);
        Assert.assertEquals(Conditional.TYPE_SENSOR_ACTIVE, 1);
        Assert.assertEquals(Conditional.TYPE_SENSOR_INACTIVE, 2);
        Assert.assertEquals(Conditional.TYPE_TURNOUT_THROWN, 3);
        Assert.assertEquals(Conditional.TYPE_TURNOUT_CLOSED, 4);
        Assert.assertEquals(Conditional.TYPE_CONDITIONAL_TRUE, 5);
        Assert.assertEquals(Conditional.TYPE_CONDITIONAL_FALSE, 6);
        Assert.assertEquals(Conditional.TYPE_LIGHT_ON, 7);
        Assert.assertEquals(Conditional.TYPE_LIGHT_OFF, 8);
        Assert.assertEquals(Conditional.TYPE_MEMORY_EQUALS, 9);
        Assert.assertEquals(Conditional.TYPE_FAST_CLOCK_RANGE, 10);
        // Note - within the TYPE_SIGNAL_HEAD definitions, all must be together,
        //  RED must be first, and HELD must be last
        Assert.assertEquals(Conditional.TYPE_SIGNAL_HEAD_RED, 11);
        Assert.assertEquals(Conditional.TYPE_SIGNAL_HEAD_YELLOW, 12);
        Assert.assertEquals(Conditional.TYPE_SIGNAL_HEAD_GREEN, 13);
        Assert.assertEquals(Conditional.TYPE_SIGNAL_HEAD_DARK, 14);
        Assert.assertEquals(Conditional.TYPE_SIGNAL_HEAD_FLASHRED, 15);
        Assert.assertEquals(Conditional.TYPE_SIGNAL_HEAD_FLASHYELLOW, 16);
        Assert.assertEquals(Conditional.TYPE_SIGNAL_HEAD_FLASHGREEN, 17);
        Assert.assertEquals(Conditional.TYPE_SIGNAL_HEAD_LIT, 18);
        Assert.assertEquals(Conditional.TYPE_SIGNAL_HEAD_HELD, 19);
        Assert.assertEquals(Conditional.TYPE_MEMORY_COMPARE, 20);
        Assert.assertEquals(Conditional.TYPE_SIGNAL_HEAD_LUNAR, 21);
        Assert.assertEquals(Conditional.TYPE_SIGNAL_HEAD_FLASHLUNAR, 22);
        Assert.assertEquals(Conditional.TYPE_MEMORY_EQUALS_INSENSITIVE, 23);
        Assert.assertEquals(Conditional.TYPE_MEMORY_COMPARE_INSENSITIVE, 24);
        // Warrant variables
        Assert.assertEquals(Conditional.TYPE_ROUTE_FREE, 25);
        Assert.assertEquals(Conditional.TYPE_ROUTE_OCCUPIED, 26);
        Assert.assertEquals(Conditional.TYPE_ROUTE_ALLOCATED, 27);
        Assert.assertEquals(Conditional.TYPE_ROUTE_SET, 28);
        Assert.assertEquals(Conditional.TYPE_TRAIN_RUNNING, 29);
        Assert.assertEquals(Conditional.TYPE_SIGNAL_MAST_ASPECT_EQUALS, 30);
        Assert.assertEquals(Conditional.TYPE_SIGNAL_MAST_LIT, 31);
        Assert.assertEquals(Conditional.TYPE_SIGNAL_MAST_HELD, 32);
        Assert.assertEquals(Conditional.TYPE_SIGNAL_HEAD_APPEARANCE_EQUALS, 33);
        Assert.assertEquals(Conditional.TYPE_BLOCK_STATUS_EQUALS, 34);

        //Entry Exit Rules
        Assert.assertEquals(Conditional.TYPE_ENTRYEXIT_ACTIVE, 35);
        Assert.assertEquals(Conditional.TYPE_ENTRYEXIT_INACTIVE, 36);

        // action definitions
        Assert.assertEquals(Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE, 1);
        Assert.assertEquals(Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE, 2);
        Assert.assertEquals(Conditional.ACTION_OPTION_ON_CHANGE, 3);
        Assert.assertEquals(Conditional.NUM_ACTION_OPTIONS, 3);

        // action types
        Assert.assertEquals(Conditional.ACTION_NONE, 1);
        Assert.assertEquals(Conditional.ACTION_SET_TURNOUT, 2);
        // allowed settings for turnout are Thrown and Closed (in data)
        Assert.assertEquals(Conditional.ACTION_SET_SIGNAL_APPEARANCE, 3);
        // allowed settings for signal head are the seven Appearances (in data)
        Assert.assertEquals(Conditional.ACTION_SET_SIGNAL_HELD, 4);
        Assert.assertEquals(Conditional.ACTION_CLEAR_SIGNAL_HELD, 5);
        Assert.assertEquals(Conditional.ACTION_SET_SIGNAL_DARK, 6);
        Assert.assertEquals(Conditional.ACTION_SET_SIGNAL_LIT, 7);
        Assert.assertEquals(Conditional.ACTION_TRIGGER_ROUTE, 8);
        Assert.assertEquals(Conditional.ACTION_SET_SENSOR, 9);
        // allowed settings for sensor are active and inactive (in data)
        Assert.assertEquals(Conditional.ACTION_DELAYED_SENSOR, 10);
        // allowed settings for timed sensor are active and inactive (in data)
        //   time in seconds before setting sensor should be in delay
        Assert.assertEquals(Conditional.ACTION_SET_LIGHT, 11);
        // allowed settings for light are ON and OFF (in data)
        Assert.assertEquals(Conditional.ACTION_SET_MEMORY, 12);
        // text to set into the memory variable should be in string
        Assert.assertEquals(Conditional.ACTION_ENABLE_LOGIX, 13);
        Assert.assertEquals(Conditional.ACTION_DISABLE_LOGIX, 14);
        Assert.assertEquals(Conditional.ACTION_PLAY_SOUND, 15);
        // reference to sound should be in string
        Assert.assertEquals(Conditional.ACTION_RUN_SCRIPT, 16);
        // reference to script should be in string
        Assert.assertEquals(Conditional.ACTION_DELAYED_TURNOUT, 17);
        // allowed settings for timed turnout are Thrown and Closed (in data)
        //   time in seconds before setting turnout should be in delay
        Assert.assertEquals(Conditional.ACTION_LOCK_TURNOUT, 18);
        Assert.assertEquals(Conditional.ACTION_RESET_DELAYED_SENSOR, 19);
        // allowed settings for timed sensor are active and inactive (in data)
        //   time in seconds before setting sensor should be in delay
        Assert.assertEquals(Conditional.ACTION_CANCEL_SENSOR_TIMERS, 20);
        // cancels all timers delaying setting of specified sensor
        Assert.assertEquals(Conditional.ACTION_RESET_DELAYED_TURNOUT, 21);
        // allowed settings for timed sensor are active and inactive (in data)
        //   time in seconds before setting sensor should be in delay
        Assert.assertEquals(Conditional.ACTION_CANCEL_TURNOUT_TIMERS, 22);
        // cancels all timers delaying setting of specified sensor
        Assert.assertEquals(Conditional.ACTION_SET_FAST_CLOCK_TIME, 23);
        // sets the fast clock time to the time specified
        Assert.assertEquals(Conditional.ACTION_START_FAST_CLOCK, 24);
        // starts the fast clock
        Assert.assertEquals(Conditional.ACTION_STOP_FAST_CLOCK, 25);
        // stops the fast clock
        Assert.assertEquals(Conditional.ACTION_COPY_MEMORY, 26);
        // copies value from memory variable (in name) to memory variable (in string)
        Assert.assertEquals(Conditional.ACTION_SET_LIGHT_INTENSITY, 27);
        Assert.assertEquals(Conditional.ACTION_SET_LIGHT_TRANSITION_TIME, 28);
        // control the specified audio object
        Assert.assertEquals(Conditional.ACTION_CONTROL_AUDIO, 29);
        // execute a jython command
        Assert.assertEquals(Conditional.ACTION_JYTHON_COMMAND, 30);
        // Warrant actions
        Assert.assertEquals(Conditional.ACTION_ALLOCATE_WARRANT_ROUTE, 31);
        Assert.assertEquals(Conditional.ACTION_DEALLOCATE_WARRANT_ROUTE, 32);
        Assert.assertEquals(Conditional.ACTION_SET_ROUTE_TURNOUTS, 33);
        Assert.assertEquals(Conditional.ACTION_AUTO_RUN_WARRANT, 34);
        Assert.assertEquals(Conditional.ACTION_CONTROL_TRAIN, 35);
        Assert.assertEquals(Conditional.ACTION_SET_TRAIN_ID, 36);
        Assert.assertEquals(Conditional.ACTION_SET_SIGNALMAST_ASPECT, 37);
        Assert.assertEquals(Conditional.ACTION_THROTTLE_FACTOR, 38);
        Assert.assertEquals(Conditional.ACTION_SET_SIGNALMAST_HELD, 39);
        Assert.assertEquals(Conditional.ACTION_CLEAR_SIGNALMAST_HELD, 40);
        Assert.assertEquals(Conditional.ACTION_SET_SIGNALMAST_DARK, 41);
        Assert.assertEquals(Conditional.ACTION_SET_SIGNALMAST_LIT, 42);
        Assert.assertEquals(Conditional.ACTION_SET_BLOCK_ERROR, 43);
        Assert.assertEquals(Conditional.ACTION_CLEAR_BLOCK_ERROR, 44);
        Assert.assertEquals(Conditional.ACTION_DEALLOCATE_BLOCK, 45);
        Assert.assertEquals(Conditional.ACTION_SET_BLOCK_OUT_OF_SERVICE, 46);
        Assert.assertEquals(Conditional.ACTION_SET_BLOCK_IN_SERVICE, 47);
        Assert.assertEquals(Conditional.ACTION_MANUAL_RUN_WARRANT, 48);
        Assert.assertEquals(Conditional.ACTION_SET_TRAIN_NAME, 49);
        Assert.assertEquals(Conditional.ACTION_SET_BLOCK_VALUE, 50);
        // EntryExit Actions
        Assert.assertEquals(Conditional.ACTION_SET_NXPAIR_ENABLED, 51);
        Assert.assertEquals(Conditional.ACTION_SET_NXPAIR_DISABLED, 52);
        Assert.assertEquals(Conditional.ACTION_SET_NXPAIR_SEGMENT, 53);
        Assert.assertEquals(Conditional.NUM_ACTION_TYPES, 53);

        /**
         * ***********************************************************************************
         */
        /* New Variable and Action type scheme for Logix UI
         * State Variables and actions are grouped according to type.  Variable and action
         * types share the following group categories:
         */
        // state variable and action items used by logix.
        // When a new type is added, insert at proper location and update 'LAST' numbers
        Assert.assertEquals(Conditional.ITEM_TYPE_SENSOR, 1);
        Assert.assertEquals(Conditional.ITEM_TYPE_TURNOUT, 2);
        Assert.assertEquals(Conditional.ITEM_TYPE_LIGHT, 3);
        Assert.assertEquals(Conditional.ITEM_TYPE_SIGNALHEAD, 4);
        Assert.assertEquals(Conditional.ITEM_TYPE_SIGNALMAST, 5);
        Assert.assertEquals(Conditional.ITEM_TYPE_MEMORY, 6);
        Assert.assertEquals(Conditional.ITEM_TYPE_CONDITIONAL, 7);  // used only by ConditionalVariable
        Assert.assertEquals(Conditional.ITEM_TYPE_LOGIX, 7);        // used only by ConditionalAction
        Assert.assertEquals(Conditional.ITEM_TYPE_WARRANT, 8);
        Assert.assertEquals(Conditional.ITEM_TYPE_CLOCK, 9);
        Assert.assertEquals(Conditional.ITEM_TYPE_OBLOCK, 10);
        Assert.assertEquals(Conditional.ITEM_TYPE_ENTRYEXIT, 11);
        Assert.assertEquals(Conditional.ITEM_TYPE_LAST_STATE_VAR, 11);

        Assert.assertEquals(Conditional.ITEM_TYPE_AUDIO, 12);
        Assert.assertEquals(Conditional.ITEM_TYPE_SCRIPT, 13);
        Assert.assertEquals(Conditional.ITEM_TYPE_OTHER, 14);
        Assert.assertEquals(Conditional.ITEM_TYPE_LAST_ACTION, 14);
    }
    
    @Test
    public void testArrays() {
        // These arrays has @SuppressFBWarnings so it might be good to refactor the code,
        // for example by using enums.
        
        Assert.assertEquals(Conditional.TYPE_NONE, Conditional.TEST_TO_ITEM[Conditional.TYPE_NONE]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SENSOR, Conditional.TEST_TO_ITEM[Conditional.TYPE_SENSOR_ACTIVE]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SENSOR, Conditional.TEST_TO_ITEM[Conditional.TYPE_SENSOR_INACTIVE]);
        Assert.assertEquals(Conditional.ITEM_TYPE_TURNOUT, Conditional.TEST_TO_ITEM[Conditional.TYPE_TURNOUT_THROWN]);
        Assert.assertEquals(Conditional.ITEM_TYPE_TURNOUT, Conditional.TEST_TO_ITEM[Conditional.TYPE_TURNOUT_CLOSED]);
        Assert.assertEquals(Conditional.ITEM_TYPE_CONDITIONAL, Conditional.TEST_TO_ITEM[Conditional.TYPE_CONDITIONAL_TRUE]);
        Assert.assertEquals(Conditional.ITEM_TYPE_CONDITIONAL, Conditional.TEST_TO_ITEM[Conditional.TYPE_CONDITIONAL_FALSE]);
        Assert.assertEquals(Conditional.ITEM_TYPE_LIGHT, Conditional.TEST_TO_ITEM[Conditional.TYPE_LIGHT_ON]);
        Assert.assertEquals(Conditional.ITEM_TYPE_LIGHT, Conditional.TEST_TO_ITEM[Conditional.TYPE_LIGHT_OFF]);
        Assert.assertEquals(Conditional.ITEM_TYPE_MEMORY, Conditional.TEST_TO_ITEM[Conditional.TYPE_MEMORY_EQUALS]);
        Assert.assertEquals(Conditional.ITEM_TYPE_CLOCK, Conditional.TEST_TO_ITEM[Conditional.TYPE_FAST_CLOCK_RANGE]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SIGNALHEAD, Conditional.TEST_TO_ITEM[Conditional.TYPE_SIGNAL_HEAD_RED]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SIGNALHEAD, Conditional.TEST_TO_ITEM[Conditional.TYPE_SIGNAL_HEAD_YELLOW]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SIGNALHEAD, Conditional.TEST_TO_ITEM[Conditional.TYPE_SIGNAL_HEAD_GREEN]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SIGNALHEAD, Conditional.TEST_TO_ITEM[Conditional.TYPE_SIGNAL_HEAD_DARK]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SIGNALHEAD, Conditional.TEST_TO_ITEM[Conditional.TYPE_SIGNAL_HEAD_FLASHRED]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SIGNALHEAD, Conditional.TEST_TO_ITEM[Conditional.TYPE_SIGNAL_HEAD_FLASHYELLOW]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SIGNALHEAD, Conditional.TEST_TO_ITEM[Conditional.TYPE_SIGNAL_HEAD_FLASHGREEN]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SIGNALHEAD, Conditional.TEST_TO_ITEM[Conditional.TYPE_SIGNAL_HEAD_LIT]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SIGNALHEAD, Conditional.TEST_TO_ITEM[Conditional.TYPE_SIGNAL_HEAD_HELD]);
        Assert.assertEquals(Conditional.ITEM_TYPE_MEMORY, Conditional.TEST_TO_ITEM[Conditional.TYPE_MEMORY_COMPARE]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SIGNALHEAD, Conditional.TEST_TO_ITEM[Conditional.TYPE_SIGNAL_HEAD_LUNAR]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SIGNALHEAD, Conditional.TEST_TO_ITEM[Conditional.TYPE_SIGNAL_HEAD_FLASHLUNAR]);
        Assert.assertEquals(Conditional.ITEM_TYPE_MEMORY, Conditional.TEST_TO_ITEM[Conditional.TYPE_MEMORY_EQUALS_INSENSITIVE]);
        Assert.assertEquals(Conditional.ITEM_TYPE_MEMORY, Conditional.TEST_TO_ITEM[Conditional.TYPE_MEMORY_COMPARE_INSENSITIVE]);
        Assert.assertEquals(Conditional.ITEM_TYPE_WARRANT, Conditional.TEST_TO_ITEM[Conditional.TYPE_ROUTE_FREE]);
        Assert.assertEquals(Conditional.ITEM_TYPE_WARRANT, Conditional.TEST_TO_ITEM[Conditional.TYPE_ROUTE_OCCUPIED]);
        Assert.assertEquals(Conditional.ITEM_TYPE_WARRANT, Conditional.TEST_TO_ITEM[Conditional.TYPE_ROUTE_ALLOCATED]);
        Assert.assertEquals(Conditional.ITEM_TYPE_WARRANT, Conditional.TEST_TO_ITEM[Conditional.TYPE_ROUTE_SET]);
        Assert.assertEquals(Conditional.ITEM_TYPE_WARRANT, Conditional.TEST_TO_ITEM[Conditional.TYPE_TRAIN_RUNNING]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SIGNALMAST, Conditional.TEST_TO_ITEM[Conditional.TYPE_SIGNAL_MAST_ASPECT_EQUALS]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SIGNALMAST, Conditional.TEST_TO_ITEM[Conditional.TYPE_SIGNAL_MAST_LIT]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SIGNALMAST, Conditional.TEST_TO_ITEM[Conditional.TYPE_SIGNAL_MAST_HELD]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SIGNALHEAD, Conditional.TEST_TO_ITEM[Conditional.TYPE_SIGNAL_HEAD_APPEARANCE_EQUALS]);
        Assert.assertEquals(Conditional.ITEM_TYPE_OBLOCK, Conditional.TEST_TO_ITEM[Conditional.TYPE_BLOCK_STATUS_EQUALS]);
        Assert.assertEquals(Conditional.ITEM_TYPE_ENTRYEXIT, Conditional.TEST_TO_ITEM[Conditional.TYPE_ENTRYEXIT_ACTIVE]);
        Assert.assertEquals(Conditional.ITEM_TYPE_ENTRYEXIT, Conditional.TEST_TO_ITEM[Conditional.TYPE_ENTRYEXIT_INACTIVE]);
        
        // Map SignalHead comboBox items to SignalHead Conditional variable types
        Assert.assertEquals(Conditional.TYPE_NONE, Conditional.ITEM_TO_SIGNAL_HEAD_TEST[0]);
        Assert.assertEquals(Conditional.TYPE_SIGNAL_HEAD_APPEARANCE_EQUALS, Conditional.ITEM_TO_SIGNAL_HEAD_TEST[1]);
        Assert.assertEquals(Conditional.TYPE_SIGNAL_HEAD_LIT, Conditional.ITEM_TO_SIGNAL_HEAD_TEST[2]);
        Assert.assertEquals(Conditional.TYPE_SIGNAL_HEAD_HELD, Conditional.ITEM_TO_SIGNAL_HEAD_TEST[3]);

        // Map SignalMAst comboBox items to SignalMast Conditional variable types
        Assert.assertEquals(Conditional.TYPE_NONE, Conditional.ITEM_TO_SIGNAL_MAST_TEST[0]);
        Assert.assertEquals(Conditional.TYPE_SIGNAL_MAST_ASPECT_EQUALS, Conditional.ITEM_TO_SIGNAL_MAST_TEST[1]);
        Assert.assertEquals(Conditional.TYPE_SIGNAL_MAST_LIT, Conditional.ITEM_TO_SIGNAL_MAST_TEST[2]);
        Assert.assertEquals(Conditional.TYPE_SIGNAL_MAST_HELD, Conditional.ITEM_TO_SIGNAL_MAST_TEST[3]);
        
        // Map Sensor state comboBox items to Sensor Conditional variable types
        Assert.assertEquals(Conditional.TYPE_SENSOR_ACTIVE, Conditional.ITEM_TO_SENSOR_TEST[0]);
        Assert.assertEquals(Conditional.TYPE_SENSOR_INACTIVE, Conditional.ITEM_TO_SENSOR_TEST[1]);

        // Map Turnout state comboBox items to Turnout Conditional variable types
        Assert.assertEquals(Conditional.TYPE_TURNOUT_THROWN, Conditional.ITEM_TO_TURNOUT_TEST[0]);
        Assert.assertEquals(Conditional.TYPE_TURNOUT_CLOSED, Conditional.ITEM_TO_TURNOUT_TEST[1]);

        // Map Conditional state comboBox items to  Condition ConditionalVvariable types
        Assert.assertEquals(Conditional.TYPE_CONDITIONAL_TRUE, Conditional.ITEM_TO_CONDITIONAL_TEST[0]);
        Assert.assertEquals(Conditional.TYPE_CONDITIONAL_FALSE, Conditional.ITEM_TO_CONDITIONAL_TEST[1]);

        // Map Memory state comboBox items to Light ConditionalVariable types
        Assert.assertEquals(Conditional.TYPE_LIGHT_ON, Conditional.ITEM_TO_LIGHT_TEST[0]);
        Assert.assertEquals(Conditional.TYPE_LIGHT_OFF, Conditional.ITEM_TO_LIGHT_TEST[1]);

        // Map Warrant state comboBox items to Warrant ConditionalVariable types
        Assert.assertEquals(Conditional.TYPE_ROUTE_FREE, Conditional.ITEM_TO_WARRANT_TEST[0]);
        Assert.assertEquals(Conditional.TYPE_ROUTE_SET, Conditional.ITEM_TO_WARRANT_TEST[1]);
        Assert.assertEquals(Conditional.TYPE_ROUTE_ALLOCATED, Conditional.ITEM_TO_WARRANT_TEST[2]);
        Assert.assertEquals(Conditional.TYPE_ROUTE_OCCUPIED, Conditional.ITEM_TO_WARRANT_TEST[3]);
        Assert.assertEquals(Conditional.TYPE_TRAIN_RUNNING, Conditional.ITEM_TO_WARRANT_TEST[4]);

        // Map Memory Compare Type comboBox items to Memory ConditionalVariable types
        Assert.assertEquals(Conditional.TYPE_MEMORY_EQUALS, Conditional.ITEM_TO_MEMORY_TEST[0]);
        Assert.assertEquals(Conditional.TYPE_MEMORY_EQUALS_INSENSITIVE, Conditional.ITEM_TO_MEMORY_TEST[1]);
        Assert.assertEquals(Conditional.TYPE_MEMORY_COMPARE, Conditional.ITEM_TO_MEMORY_TEST[2]);
        Assert.assertEquals(Conditional.TYPE_MEMORY_COMPARE_INSENSITIVE, Conditional.ITEM_TO_MEMORY_TEST[3]);

        Assert.assertEquals(Conditional.TYPE_BLOCK_STATUS_EQUALS, Conditional.ITEM_TO_OBLOCK_TEST[0]);

        Assert.assertEquals(Conditional.TYPE_ENTRYEXIT_ACTIVE, Conditional.ITEM_TO_ENTRYEXIT_TEST[0]);
        Assert.assertEquals(Conditional.TYPE_ENTRYEXIT_INACTIVE, Conditional.ITEM_TO_ENTRYEXIT_TEST[1]);

        /**
         * *************** ConditionalAction Maps *******************************
         */
        // Map action type to the item type
        Assert.assertEquals(Conditional.TYPE_NONE, Conditional.ACTION_TO_ITEM[Conditional.ACTION_NONE]);
        Assert.assertEquals(Conditional.ITEM_TYPE_TURNOUT, Conditional.ACTION_TO_ITEM[Conditional.ACTION_SET_TURNOUT]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SIGNALHEAD, Conditional.ACTION_TO_ITEM[Conditional.ACTION_SET_SIGNAL_APPEARANCE]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SIGNALHEAD, Conditional.ACTION_TO_ITEM[Conditional.ACTION_SET_SIGNAL_HELD]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SIGNALHEAD, Conditional.ACTION_TO_ITEM[Conditional.ACTION_CLEAR_SIGNAL_HELD]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SIGNALHEAD, Conditional.ACTION_TO_ITEM[Conditional.ACTION_SET_SIGNAL_DARK]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SIGNALHEAD, Conditional.ACTION_TO_ITEM[Conditional.ACTION_SET_SIGNAL_LIT]);
        Assert.assertEquals(Conditional.ITEM_TYPE_OTHER, Conditional.ACTION_TO_ITEM[Conditional.ACTION_TRIGGER_ROUTE]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SENSOR, Conditional.ACTION_TO_ITEM[Conditional.ACTION_SET_SENSOR]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SENSOR, Conditional.ACTION_TO_ITEM[Conditional.ACTION_DELAYED_SENSOR]);
        Assert.assertEquals(Conditional.ITEM_TYPE_LIGHT, Conditional.ACTION_TO_ITEM[Conditional.ACTION_SET_LIGHT]);
        Assert.assertEquals(Conditional.ITEM_TYPE_MEMORY, Conditional.ACTION_TO_ITEM[Conditional.ACTION_SET_MEMORY]);
        Assert.assertEquals(Conditional.ITEM_TYPE_LOGIX, Conditional.ACTION_TO_ITEM[Conditional.ACTION_ENABLE_LOGIX]);
        Assert.assertEquals(Conditional.ITEM_TYPE_LOGIX, Conditional.ACTION_TO_ITEM[Conditional.ACTION_DISABLE_LOGIX]);
        Assert.assertEquals(Conditional.ITEM_TYPE_AUDIO, Conditional.ACTION_TO_ITEM[Conditional.ACTION_PLAY_SOUND]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SCRIPT, Conditional.ACTION_TO_ITEM[Conditional.ACTION_RUN_SCRIPT]);
        Assert.assertEquals(Conditional.ITEM_TYPE_TURNOUT, Conditional.ACTION_TO_ITEM[Conditional.ACTION_DELAYED_TURNOUT]);
        Assert.assertEquals(Conditional.ITEM_TYPE_TURNOUT, Conditional.ACTION_TO_ITEM[Conditional.ACTION_LOCK_TURNOUT]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SENSOR, Conditional.ACTION_TO_ITEM[Conditional.ACTION_RESET_DELAYED_SENSOR]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SENSOR, Conditional.ACTION_TO_ITEM[Conditional.ACTION_CANCEL_SENSOR_TIMERS]);
        Assert.assertEquals(Conditional.ITEM_TYPE_TURNOUT, Conditional.ACTION_TO_ITEM[Conditional.ACTION_RESET_DELAYED_TURNOUT]);
        Assert.assertEquals(Conditional.ITEM_TYPE_TURNOUT, Conditional.ACTION_TO_ITEM[Conditional.ACTION_CANCEL_TURNOUT_TIMERS]);
        Assert.assertEquals(Conditional.ITEM_TYPE_CLOCK, Conditional.ACTION_TO_ITEM[Conditional.ACTION_SET_FAST_CLOCK_TIME]);
        Assert.assertEquals(Conditional.ITEM_TYPE_CLOCK, Conditional.ACTION_TO_ITEM[Conditional.ACTION_START_FAST_CLOCK]);
        Assert.assertEquals(Conditional.ITEM_TYPE_CLOCK, Conditional.ACTION_TO_ITEM[Conditional.ACTION_STOP_FAST_CLOCK]);
        Assert.assertEquals(Conditional.ITEM_TYPE_MEMORY, Conditional.ACTION_TO_ITEM[Conditional.ACTION_COPY_MEMORY]);
        Assert.assertEquals(Conditional.ITEM_TYPE_LIGHT, Conditional.ACTION_TO_ITEM[Conditional.ACTION_SET_LIGHT_INTENSITY]);
        Assert.assertEquals(Conditional.ITEM_TYPE_LIGHT, Conditional.ACTION_TO_ITEM[Conditional.ACTION_SET_LIGHT_TRANSITION_TIME]);
        Assert.assertEquals(Conditional.ITEM_TYPE_AUDIO, Conditional.ACTION_TO_ITEM[Conditional.ACTION_CONTROL_AUDIO]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SCRIPT, Conditional.ACTION_TO_ITEM[Conditional.ACTION_JYTHON_COMMAND]);
        Assert.assertEquals(Conditional.ITEM_TYPE_WARRANT, Conditional.ACTION_TO_ITEM[Conditional.ACTION_ALLOCATE_WARRANT_ROUTE]);
        Assert.assertEquals(Conditional.ITEM_TYPE_WARRANT, Conditional.ACTION_TO_ITEM[Conditional.ACTION_DEALLOCATE_WARRANT_ROUTE]);
        Assert.assertEquals(Conditional.ITEM_TYPE_WARRANT, Conditional.ACTION_TO_ITEM[Conditional.ACTION_SET_ROUTE_TURNOUTS]);
        Assert.assertEquals(Conditional.ITEM_TYPE_WARRANT, Conditional.ACTION_TO_ITEM[Conditional.ACTION_AUTO_RUN_WARRANT]);
        Assert.assertEquals(Conditional.ITEM_TYPE_WARRANT, Conditional.ACTION_TO_ITEM[Conditional.ACTION_CONTROL_TRAIN]);
        Assert.assertEquals(Conditional.ITEM_TYPE_WARRANT, Conditional.ACTION_TO_ITEM[Conditional.ACTION_SET_TRAIN_ID]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SIGNALMAST, Conditional.ACTION_TO_ITEM[Conditional.ACTION_SET_SIGNALMAST_ASPECT]);
        Assert.assertEquals(Conditional.ITEM_TYPE_WARRANT, Conditional.ACTION_TO_ITEM[Conditional.ACTION_THROTTLE_FACTOR]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SIGNALMAST, Conditional.ACTION_TO_ITEM[Conditional.ACTION_SET_SIGNALMAST_HELD]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SIGNALMAST, Conditional.ACTION_TO_ITEM[Conditional.ACTION_CLEAR_SIGNALMAST_HELD]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SIGNALMAST, Conditional.ACTION_TO_ITEM[Conditional.ACTION_SET_SIGNALMAST_DARK]);
        Assert.assertEquals(Conditional.ITEM_TYPE_SIGNALMAST, Conditional.ACTION_TO_ITEM[Conditional.ACTION_SET_SIGNALMAST_LIT]);
        Assert.assertEquals(Conditional.ITEM_TYPE_OBLOCK, Conditional.ACTION_TO_ITEM[Conditional.ACTION_SET_BLOCK_ERROR]);
        Assert.assertEquals(Conditional.ITEM_TYPE_OBLOCK, Conditional.ACTION_TO_ITEM[Conditional.ACTION_CLEAR_BLOCK_ERROR]);
        Assert.assertEquals(Conditional.ITEM_TYPE_OBLOCK, Conditional.ACTION_TO_ITEM[Conditional.ACTION_DEALLOCATE_BLOCK]);
        Assert.assertEquals(Conditional.ITEM_TYPE_OBLOCK, Conditional.ACTION_TO_ITEM[Conditional.ACTION_SET_BLOCK_OUT_OF_SERVICE]);
        Assert.assertEquals(Conditional.ITEM_TYPE_OBLOCK, Conditional.ACTION_TO_ITEM[Conditional.ACTION_SET_BLOCK_IN_SERVICE]);
        Assert.assertEquals(Conditional.ITEM_TYPE_WARRANT, Conditional.ACTION_TO_ITEM[Conditional.ACTION_MANUAL_RUN_WARRANT]);
        Assert.assertEquals(Conditional.ITEM_TYPE_WARRANT, Conditional.ACTION_TO_ITEM[Conditional.ACTION_SET_TRAIN_NAME]);
        Assert.assertEquals(Conditional.ITEM_TYPE_OBLOCK, Conditional.ACTION_TO_ITEM[Conditional.ACTION_SET_BLOCK_VALUE]);
        Assert.assertEquals(Conditional.ITEM_TYPE_ENTRYEXIT, Conditional.ACTION_TO_ITEM[Conditional.ACTION_SET_NXPAIR_ENABLED]);
        Assert.assertEquals(Conditional.ITEM_TYPE_ENTRYEXIT, Conditional.ACTION_TO_ITEM[Conditional.ACTION_SET_NXPAIR_DISABLED]);
        Assert.assertEquals(Conditional.ITEM_TYPE_ENTRYEXIT, Conditional.ACTION_TO_ITEM[Conditional.ACTION_SET_NXPAIR_SEGMENT]);

        // Map Sensor Type comboBox items to Sensor action types
        Assert.assertEquals(Conditional.ACTION_SET_SENSOR, Conditional.ITEM_TO_SENSOR_ACTION[0]);
        Assert.assertEquals(Conditional.ACTION_DELAYED_SENSOR, Conditional.ITEM_TO_SENSOR_ACTION[1]);
        Assert.assertEquals(Conditional.ACTION_RESET_DELAYED_SENSOR, Conditional.ITEM_TO_SENSOR_ACTION[2]);
        Assert.assertEquals(Conditional.ACTION_CANCEL_SENSOR_TIMERS, Conditional.ITEM_TO_SENSOR_ACTION[3]);

        // Map Turnout Type comboBox items to Turnout action types
        Assert.assertEquals(Conditional.ACTION_SET_TURNOUT, Conditional.ITEM_TO_TURNOUT_ACTION[0]);
        Assert.assertEquals(Conditional.ACTION_DELAYED_TURNOUT, Conditional.ITEM_TO_TURNOUT_ACTION[1]);
        Assert.assertEquals(Conditional.ACTION_LOCK_TURNOUT, Conditional.ITEM_TO_TURNOUT_ACTION[2]);
        Assert.assertEquals(Conditional.ACTION_CANCEL_TURNOUT_TIMERS, Conditional.ITEM_TO_TURNOUT_ACTION[3]);
        Assert.assertEquals(Conditional.ACTION_RESET_DELAYED_TURNOUT, Conditional.ITEM_TO_TURNOUT_ACTION[4]);

        // Map Memory Type comboBox items to Memory action types
        Assert.assertEquals(12, Conditional.ITEM_TO_MEMORY_ACTION[0]);
        Assert.assertEquals(Conditional.ACTION_SET_MEMORY, Conditional.ITEM_TO_MEMORY_ACTION[0]);
        Assert.assertEquals(26, Conditional.ITEM_TO_MEMORY_ACTION[1]);
        Assert.assertEquals(Conditional.ACTION_COPY_MEMORY, Conditional.ITEM_TO_MEMORY_ACTION[1]);

        // Map Light Type comboBox items to Light action types
        Assert.assertEquals(Conditional.ACTION_SET_LIGHT, Conditional.ITEM_TO_LIGHT_ACTION[0]);
        Assert.assertEquals(Conditional.ACTION_SET_LIGHT_INTENSITY, Conditional.ITEM_TO_LIGHT_ACTION[1]);
        Assert.assertEquals(Conditional.ACTION_SET_LIGHT_TRANSITION_TIME, Conditional.ITEM_TO_LIGHT_ACTION[2]);

        // Map FastClock Type comboBox items to FastClock action types
        Assert.assertEquals(Conditional.ACTION_SET_FAST_CLOCK_TIME, Conditional.ITEM_TO_CLOCK_ACTION[0]);
        Assert.assertEquals(Conditional.ACTION_START_FAST_CLOCK, Conditional.ITEM_TO_CLOCK_ACTION[1]);
        Assert.assertEquals(Conditional.ACTION_STOP_FAST_CLOCK, Conditional.ITEM_TO_CLOCK_ACTION[2]);

        // Map Logix Type comboBox items to Logix action types
        Assert.assertEquals(Conditional.ACTION_ENABLE_LOGIX, Conditional.ITEM_TO_LOGIX_ACTION[0]);
        Assert.assertEquals(Conditional.ACTION_DISABLE_LOGIX, Conditional.ITEM_TO_LOGIX_ACTION[1]);

        // Map Warrant Type comboBox items to Warrant action types
        Assert.assertEquals(Conditional.ACTION_ALLOCATE_WARRANT_ROUTE, Conditional.ITEM_TO_WARRANT_ACTION[0]);
        Assert.assertEquals(Conditional.ACTION_DEALLOCATE_WARRANT_ROUTE, Conditional.ITEM_TO_WARRANT_ACTION[1]);
        Assert.assertEquals(Conditional.ACTION_SET_ROUTE_TURNOUTS, Conditional.ITEM_TO_WARRANT_ACTION[2]);
        Assert.assertEquals(Conditional.ACTION_AUTO_RUN_WARRANT, Conditional.ITEM_TO_WARRANT_ACTION[3]);
        Assert.assertEquals(Conditional.ACTION_MANUAL_RUN_WARRANT, Conditional.ITEM_TO_WARRANT_ACTION[4]);
        Assert.assertEquals(Conditional.ACTION_CONTROL_TRAIN, Conditional.ITEM_TO_WARRANT_ACTION[5]);
        Assert.assertEquals(Conditional.ACTION_SET_TRAIN_ID, Conditional.ITEM_TO_WARRANT_ACTION[6]);
        Assert.assertEquals(Conditional.ACTION_SET_TRAIN_NAME, Conditional.ITEM_TO_WARRANT_ACTION[7]);
        Assert.assertEquals(Conditional.ACTION_THROTTLE_FACTOR, Conditional.ITEM_TO_WARRANT_ACTION[8]);

        Assert.assertEquals(Conditional.ACTION_DEALLOCATE_BLOCK, Conditional.ITEM_TO_OBLOCK_ACTION[0]);
        Assert.assertEquals(Conditional.ACTION_SET_BLOCK_VALUE, Conditional.ITEM_TO_OBLOCK_ACTION[1]);
        Assert.assertEquals(Conditional.ACTION_SET_BLOCK_ERROR, Conditional.ITEM_TO_OBLOCK_ACTION[2]);
        Assert.assertEquals(Conditional.ACTION_CLEAR_BLOCK_ERROR, Conditional.ITEM_TO_OBLOCK_ACTION[3]);
        Assert.assertEquals(Conditional.ACTION_SET_BLOCK_OUT_OF_SERVICE, Conditional.ITEM_TO_OBLOCK_ACTION[4]);
        Assert.assertEquals(Conditional.ACTION_SET_BLOCK_IN_SERVICE, Conditional.ITEM_TO_OBLOCK_ACTION[5]);

        // Map Signal Head Type comboBox items to Signal Head action types
        Assert.assertEquals(Conditional.ACTION_SET_SIGNAL_APPEARANCE, Conditional.ITEM_TO_SIGNAL_HEAD_ACTION[0]);
        Assert.assertEquals(Conditional.ACTION_SET_SIGNAL_HELD, Conditional.ITEM_TO_SIGNAL_HEAD_ACTION[1]);
        Assert.assertEquals(Conditional.ACTION_CLEAR_SIGNAL_HELD, Conditional.ITEM_TO_SIGNAL_HEAD_ACTION[2]);
        Assert.assertEquals(Conditional.ACTION_SET_SIGNAL_DARK, Conditional.ITEM_TO_SIGNAL_HEAD_ACTION[3]);
        Assert.assertEquals(Conditional.ACTION_SET_SIGNAL_LIT, Conditional.ITEM_TO_SIGNAL_HEAD_ACTION[4]);

        // Map Signal Mast Type comboBox items to Signal Mast action types
        Assert.assertEquals(Conditional.ACTION_SET_SIGNALMAST_ASPECT, Conditional.ITEM_TO_SIGNAL_MAST_ACTION[0]);
        Assert.assertEquals(Conditional.ACTION_SET_SIGNALMAST_HELD, Conditional.ITEM_TO_SIGNAL_MAST_ACTION[1]);
        Assert.assertEquals(Conditional.ACTION_CLEAR_SIGNALMAST_HELD, Conditional.ITEM_TO_SIGNAL_MAST_ACTION[2]);
        Assert.assertEquals(Conditional.ACTION_SET_SIGNALMAST_DARK, Conditional.ITEM_TO_SIGNAL_MAST_ACTION[3]);
        Assert.assertEquals(Conditional.ACTION_SET_SIGNALMAST_LIT, Conditional.ITEM_TO_SIGNAL_MAST_ACTION[4]);

        // Map Audio Type comboBox items to Audio action types
        Assert.assertEquals(Conditional.ACTION_PLAY_SOUND, Conditional.ITEM_TO_AUDIO_ACTION[0]);
        Assert.assertEquals(Conditional.ACTION_CONTROL_AUDIO, Conditional.ITEM_TO_AUDIO_ACTION[1]);

        // Map Script Type comboBox items to Script action types
        Assert.assertEquals(Conditional.ACTION_RUN_SCRIPT, Conditional.ITEM_TO_SCRIPT_ACTION[0]);
        Assert.assertEquals(Conditional.ACTION_JYTHON_COMMAND, Conditional.ITEM_TO_SCRIPT_ACTION[1]);

        // Map EntryExit Type comboBox items to EntryExit action types
        Assert.assertEquals(Conditional.ACTION_SET_NXPAIR_ENABLED, Conditional.ITEM_TO_ENTRYEXIT_ACTION[0]);
        Assert.assertEquals(Conditional.ACTION_SET_NXPAIR_DISABLED, Conditional.ITEM_TO_ENTRYEXIT_ACTION[1]);
        Assert.assertEquals(Conditional.ACTION_SET_NXPAIR_SEGMENT, Conditional.ITEM_TO_ENTRYEXIT_ACTION[2]);

        // Map Misc Type comboBox items to Misc action types
        Assert.assertEquals(Conditional.ACTION_TRIGGER_ROUTE, Conditional.ITEM_TO_OTHER_ACTION[0]);
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

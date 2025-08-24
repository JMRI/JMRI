package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test the Conditional interface
 */
public class ConditionalTest {

    @Test
    public void testConstants() {
        // It might be a good idea to change constants into enums.
        // These tests ensures that the values of the constants stay the same
        // if that change is done.
        
        assertEquals( 0x01, NamedBean.UNKNOWN);
        assertEquals( 0x01, Conditional.UNKNOWN);
        assertEquals( 0x02, Conditional.FALSE);
        assertEquals( 0x04, Conditional.TRUE);

        // logic operators used in antecedent
        assertEquals( 0x01, Conditional.ALL_AND);
        assertEquals( 0x02, Conditional.ALL_OR);
        assertEquals( 0x03, Conditional.MIXED);

        // state variable definitions
        assertEquals( 1, Conditional.OPERATOR_AND);
        assertEquals( 4, Conditional.OPERATOR_NONE);
        assertEquals( 5, Conditional.OPERATOR_OR);
        // state variable types
        assertEquals( 0, Conditional.TYPE_NONE);
        assertEquals( 1, Conditional.TYPE_SENSOR_ACTIVE);
        assertEquals( 2, Conditional.TYPE_SENSOR_INACTIVE);
        assertEquals( 3, Conditional.TYPE_TURNOUT_THROWN);
        assertEquals( 4, Conditional.TYPE_TURNOUT_CLOSED);
        assertEquals( 5, Conditional.TYPE_CONDITIONAL_TRUE);
        assertEquals( 6, Conditional.TYPE_CONDITIONAL_FALSE);
        assertEquals( 7, Conditional.TYPE_LIGHT_ON);
        assertEquals( 8, Conditional.TYPE_LIGHT_OFF);
        assertEquals( 9, Conditional.TYPE_MEMORY_EQUALS);
        assertEquals( 10, Conditional.TYPE_FAST_CLOCK_RANGE);
        // Note - within the TYPE_SIGNAL_HEAD definitions, all must be together,
        //  RED must be first, and HELD must be last
        assertEquals( 11, Conditional.TYPE_SIGNAL_HEAD_RED);
        assertEquals( 12, Conditional.TYPE_SIGNAL_HEAD_YELLOW);
        assertEquals( 13, Conditional.TYPE_SIGNAL_HEAD_GREEN);
        assertEquals( 14, Conditional.TYPE_SIGNAL_HEAD_DARK);
        assertEquals( 15, Conditional.TYPE_SIGNAL_HEAD_FLASHRED);
        assertEquals( 16, Conditional.TYPE_SIGNAL_HEAD_FLASHYELLOW);
        assertEquals( 17, Conditional.TYPE_SIGNAL_HEAD_FLASHGREEN);
        assertEquals( 18, Conditional.TYPE_SIGNAL_HEAD_LIT);
        assertEquals( 19, Conditional.TYPE_SIGNAL_HEAD_HELD);
        assertEquals( 20, Conditional.TYPE_MEMORY_COMPARE);
        assertEquals( 21, Conditional.TYPE_SIGNAL_HEAD_LUNAR);
        assertEquals( 22, Conditional.TYPE_SIGNAL_HEAD_FLASHLUNAR);
        assertEquals( 23, Conditional.TYPE_MEMORY_EQUALS_INSENSITIVE);
        assertEquals( 24, Conditional.TYPE_MEMORY_COMPARE_INSENSITIVE);
        // Warrant variables
        assertEquals( 25, Conditional.TYPE_ROUTE_FREE);
        assertEquals( 26, Conditional.TYPE_ROUTE_OCCUPIED);
        assertEquals( 27, Conditional.TYPE_ROUTE_ALLOCATED);
        assertEquals( 28, Conditional.TYPE_ROUTE_SET);
        assertEquals( 29, Conditional.TYPE_TRAIN_RUNNING);
        assertEquals( 30, Conditional.TYPE_SIGNAL_MAST_ASPECT_EQUALS);
        assertEquals( 31, Conditional.TYPE_SIGNAL_MAST_LIT);
        assertEquals( 32, Conditional.TYPE_SIGNAL_MAST_HELD);
        assertEquals( 33, Conditional.TYPE_SIGNAL_HEAD_APPEARANCE_EQUALS);
        // This item is never used
        assertEquals( 34, Conditional.TYPE_BLOCK_STATUS_EQUALS);

        //Entry Exit Rules
        assertEquals( 35, Conditional.TYPE_ENTRYEXIT_ACTIVE);
        assertEquals( 36, Conditional.TYPE_ENTRYEXIT_INACTIVE);

        // action definitions
        assertEquals( 1, Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE);
        assertEquals( 2, Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE);
        assertEquals( 3, Conditional.ACTION_OPTION_ON_CHANGE);
        assertEquals( 3, Conditional.NUM_ACTION_OPTIONS);

        // action types
        assertEquals( 1, Conditional.ACTION_NONE);
        assertEquals( 2, Conditional.ACTION_SET_TURNOUT);
        // allowed settings for turnout are Thrown and Closed (in data)
        assertEquals( 3, Conditional.ACTION_SET_SIGNAL_APPEARANCE);
        // allowed settings for signal head are the seven Appearances (in data)
        assertEquals( 4, Conditional.ACTION_SET_SIGNAL_HELD);
        assertEquals( 5, Conditional.ACTION_CLEAR_SIGNAL_HELD);
        assertEquals( 6, Conditional.ACTION_SET_SIGNAL_DARK);
        assertEquals( 7, Conditional.ACTION_SET_SIGNAL_LIT);
        assertEquals( 8, Conditional.ACTION_TRIGGER_ROUTE);
        assertEquals( 9, Conditional.ACTION_SET_SENSOR);
        // allowed settings for sensor are active and inactive (in data)
        assertEquals( 10, Conditional.ACTION_DELAYED_SENSOR);
        // allowed settings for timed sensor are active and inactive (in data)
        //   time in seconds before setting sensor should be in delay
        assertEquals( 11, Conditional.ACTION_SET_LIGHT);
        // allowed settings for light are ON and OFF (in data)
        assertEquals( 12, Conditional.ACTION_SET_MEMORY);
        // text to set into the memory variable should be in string
        assertEquals( 13, Conditional.ACTION_ENABLE_LOGIX);
        assertEquals( 14, Conditional.ACTION_DISABLE_LOGIX);
        assertEquals( 15, Conditional.ACTION_PLAY_SOUND);
        // reference to sound should be in string
        assertEquals( 16, Conditional.ACTION_RUN_SCRIPT);
        // reference to script should be in string
        assertEquals( 17, Conditional.ACTION_DELAYED_TURNOUT);
        // allowed settings for timed turnout are Thrown and Closed (in data)
        //   time in seconds before setting turnout should be in delay
        assertEquals( 18, Conditional.ACTION_LOCK_TURNOUT);
        assertEquals( 19, Conditional.ACTION_RESET_DELAYED_SENSOR);
        // allowed settings for timed sensor are active and inactive (in data)
        //   time in seconds before setting sensor should be in delay
        assertEquals( 20, Conditional.ACTION_CANCEL_SENSOR_TIMERS);
        // cancels all timers delaying setting of specified sensor
        assertEquals( 21, Conditional.ACTION_RESET_DELAYED_TURNOUT);
        // allowed settings for timed sensor are active and inactive (in data)
        //   time in seconds before setting sensor should be in delay
        assertEquals( 22, Conditional.ACTION_CANCEL_TURNOUT_TIMERS);
        // cancels all timers delaying setting of specified sensor
        assertEquals( 23, Conditional.ACTION_SET_FAST_CLOCK_TIME);
        // sets the fast clock time to the time specified
        assertEquals( 24, Conditional.ACTION_START_FAST_CLOCK);
        // starts the fast clock
        assertEquals( 25, Conditional.ACTION_STOP_FAST_CLOCK);
        // stops the fast clock
        assertEquals( 26, Conditional.ACTION_COPY_MEMORY);
        // copies value from memory variable (in name) to memory variable (in string)
        assertEquals( 27, Conditional.ACTION_SET_LIGHT_INTENSITY);
        assertEquals( 28, Conditional.ACTION_SET_LIGHT_TRANSITION_TIME);
        // control the specified audio object
        assertEquals( 29, Conditional.ACTION_CONTROL_AUDIO);
        // execute a jython command
        assertEquals( 30, Conditional.ACTION_JYTHON_COMMAND);
        // Warrant actions
        assertEquals( 31, Conditional.ACTION_ALLOCATE_WARRANT_ROUTE);
        assertEquals( 32, Conditional.ACTION_DEALLOCATE_WARRANT_ROUTE);
        assertEquals( 33, Conditional.ACTION_SET_ROUTE_TURNOUTS);
        assertEquals( 34, Conditional.ACTION_AUTO_RUN_WARRANT);
        assertEquals( 35, Conditional.ACTION_CONTROL_TRAIN);
        assertEquals( 36, Conditional.ACTION_SET_TRAIN_ID);
        assertEquals( 37, Conditional.ACTION_SET_SIGNALMAST_ASPECT);
        assertEquals( 38, Conditional.ACTION_GET_TRAIN_LOCATION);
        assertEquals( 39, Conditional.ACTION_SET_SIGNALMAST_HELD);
        assertEquals( 40, Conditional.ACTION_CLEAR_SIGNALMAST_HELD);
        assertEquals( 41, Conditional.ACTION_SET_SIGNALMAST_DARK);
        assertEquals( 42, Conditional.ACTION_SET_SIGNALMAST_LIT);
        assertEquals( 43, Conditional.ACTION_SET_BLOCK_ERROR);
        assertEquals( 44, Conditional.ACTION_CLEAR_BLOCK_ERROR);
        assertEquals( 45, Conditional.ACTION_DEALLOCATE_BLOCK);
        assertEquals( 46, Conditional.ACTION_SET_BLOCK_OUT_OF_SERVICE);
        assertEquals( 47, Conditional.ACTION_SET_BLOCK_IN_SERVICE);
        assertEquals( 48, Conditional.ACTION_MANUAL_RUN_WARRANT);
        assertEquals( 49, Conditional.ACTION_SET_TRAIN_NAME);
        assertEquals( 50, Conditional.ACTION_SET_BLOCK_VALUE);
        assertEquals( 55, Conditional.ACTION_GET_BLOCK_TRAIN_NAME);
        assertEquals( 54, Conditional.ACTION_GET_BLOCK_WARRANT);
        // EntryExit Actions
        assertEquals( 51, Conditional.ACTION_SET_NXPAIR_ENABLED);
        assertEquals( 52, Conditional.ACTION_SET_NXPAIR_DISABLED);
        assertEquals( 53, Conditional.ACTION_SET_NXPAIR_SEGMENT);
//        Assert.assertEquals(Conditional.NUM_ACTION_TYPES, 53);

        /**
         * ***********************************************************************************
         */
        /* New Variable and Action type scheme for Logix UI
         * State Variables and actions are grouped according to type.  Variable and action
         * types share the following group categories:
         */
        // state variable and action items used by logix.
        // When a new type is added, insert at proper location and update 'LAST' numbers
        assertEquals( 1, Conditional.ITEM_TYPE_SENSOR);
        assertEquals( 2, Conditional.ITEM_TYPE_TURNOUT);
        assertEquals( 3, Conditional.ITEM_TYPE_LIGHT);
        assertEquals( 4, Conditional.ITEM_TYPE_SIGNALHEAD);
        assertEquals( 5, Conditional.ITEM_TYPE_SIGNALMAST);
        assertEquals( 6, Conditional.ITEM_TYPE_MEMORY);
        assertEquals( 7, Conditional.ITEM_TYPE_CONDITIONAL);  // used only by ConditionalVariable
        assertEquals( 7, Conditional.ITEM_TYPE_LOGIX);        // used only by ConditionalAction
        assertEquals( 8, Conditional.ITEM_TYPE_WARRANT);
        assertEquals( 9, Conditional.ITEM_TYPE_CLOCK);
        assertEquals( 10, Conditional.ITEM_TYPE_OBLOCK);
        assertEquals( 11, Conditional.ITEM_TYPE_ENTRYEXIT);
//        Assert.assertEquals(Conditional.ITEM_TYPE_LAST_STATE_VAR, 11);

        assertEquals( 12, Conditional.ITEM_TYPE_AUDIO, 12);
        assertEquals( 13, Conditional.ITEM_TYPE_SCRIPT, 13);
        assertEquals( 14, Conditional.ITEM_TYPE_OTHER, 14);
//        Assert.assertEquals(Conditional.ITEM_TYPE_LAST_ACTION, 14);
    }

    @Test
    public void testArrays() {
        // Map SignalHead comboBox items to SignalHead Conditional variable types
        assertEquals(Conditional.Type.NONE, Conditional.Type.getSignalHeadStateMachineItems().get(0));
        assertEquals(Conditional.Type.SIGNAL_HEAD_APPEARANCE_EQUALS, Conditional.Type.getSignalHeadStateMachineItems().get(1));
        assertEquals(Conditional.Type.SIGNAL_HEAD_LIT, Conditional.Type.getSignalHeadStateMachineItems().get(2));
        assertEquals(Conditional.Type.SIGNAL_HEAD_HELD, Conditional.Type.getSignalHeadStateMachineItems().get(3));

        // Map SignalMAst comboBox items to SignalMast Conditional variable types
        assertEquals(Conditional.Type.NONE, Conditional.Type.getSignalMastItems().get(0));
        assertEquals(Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS, Conditional.Type.getSignalMastItems().get(1));
        assertEquals(Conditional.Type.SIGNAL_MAST_LIT, Conditional.Type.getSignalMastItems().get(2));
        assertEquals(Conditional.Type.SIGNAL_MAST_HELD, Conditional.Type.getSignalMastItems().get(3));
        
        // Map Sensor state comboBox items to Sensor Conditional variable types
        assertEquals(Conditional.Type.SENSOR_ACTIVE, Conditional.Type.getSensorItems().get(0));
        assertEquals(Conditional.Type.SENSOR_INACTIVE, Conditional.Type.getSensorItems().get(1));

        // Map Turnout state comboBox items to Turnout Conditional variable types
        assertEquals(Conditional.Type.TURNOUT_THROWN, Conditional.Type.getTurnoutItems().get(0));
        assertEquals(Conditional.Type.TURNOUT_CLOSED, Conditional.Type.getTurnoutItems().get(1));

        // Map Conditional state comboBox items to  Condition ConditionalVvariable types
        assertEquals(Conditional.Type.CONDITIONAL_TRUE, Conditional.Type.getConditionalItems().get(0));
        assertEquals(Conditional.Type.CONDITIONAL_FALSE, Conditional.Type.getConditionalItems().get(1));

        // Map Memory state comboBox items to Light ConditionalVariable types
        assertEquals(Conditional.Type.LIGHT_ON, Conditional.Type.getLightItems().get(0));
        assertEquals(Conditional.Type.LIGHT_OFF, Conditional.Type.getLightItems().get(1));

        // Map Warrant state comboBox items to Warrant ConditionalVariable types
        assertEquals(Conditional.Type.ROUTE_FREE, Conditional.Type.getWarrantItems().get(0));
        assertEquals(Conditional.Type.ROUTE_SET, Conditional.Type.getWarrantItems().get(1));
        assertEquals(Conditional.Type.ROUTE_ALLOCATED, Conditional.Type.getWarrantItems().get(2));
        assertEquals(Conditional.Type.ROUTE_OCCUPIED, Conditional.Type.getWarrantItems().get(3));
        assertEquals(Conditional.Type.TRAIN_RUNNING, Conditional.Type.getWarrantItems().get(4));

        // Map Memory Compare Type comboBox items to Memory ConditionalVariable types
        assertEquals(Conditional.Type.MEMORY_EQUALS, Conditional.Type.getMemoryItems().get(0));
        assertEquals(Conditional.Type.MEMORY_EQUALS_INSENSITIVE, Conditional.Type.getMemoryItems().get(1));
        assertEquals(Conditional.Type.MEMORY_COMPARE, Conditional.Type.getMemoryItems().get(2));
        assertEquals(Conditional.Type.MEMORY_COMPARE_INSENSITIVE, Conditional.Type.getMemoryItems().get(3));

//        Assert.assertEquals(Conditional.TYPE_BLOCK_STATUS_EQUALS, Conditional.Type.getOBlockItems().get(0));

        assertEquals(Conditional.Type.ENTRYEXIT_ACTIVE, Conditional.Type.getEntryExitItems().get(0));
        assertEquals(Conditional.Type.ENTRYEXIT_INACTIVE, Conditional.Type.getEntryExitItems().get(1));

        /**
         * *************** ConditionalAction Maps *******************************
         */
        // Map action type to the item type
        assertEquals(Conditional.ItemType.NONE, Conditional.Action.NONE.getItemType());
        assertEquals(Conditional.ItemType.TURNOUT, Conditional.Action.SET_TURNOUT.getItemType());
        assertEquals(Conditional.ItemType.SIGNALHEAD, Conditional.Action.SET_SIGNAL_APPEARANCE.getItemType());
        assertEquals(Conditional.ItemType.SIGNALHEAD, Conditional.Action.SET_SIGNAL_HELD.getItemType());
        assertEquals(Conditional.ItemType.SIGNALHEAD, Conditional.Action.CLEAR_SIGNAL_HELD.getItemType());
        assertEquals(Conditional.ItemType.SIGNALHEAD, Conditional.Action.SET_SIGNAL_DARK.getItemType());
        assertEquals(Conditional.ItemType.SIGNALHEAD, Conditional.Action.SET_SIGNAL_LIT.getItemType());
        assertEquals(Conditional.ItemType.OTHER, Conditional.Action.TRIGGER_ROUTE.getItemType());
        assertEquals(Conditional.ItemType.SENSOR, Conditional.Action.SET_SENSOR.getItemType());
        assertEquals(Conditional.ItemType.SENSOR, Conditional.Action.DELAYED_SENSOR.getItemType());
        assertEquals(Conditional.ItemType.LIGHT, Conditional.Action.SET_LIGHT.getItemType());
        assertEquals(Conditional.ItemType.MEMORY, Conditional.Action.SET_MEMORY.getItemType());
        assertEquals(Conditional.ItemType.LOGIX, Conditional.Action.ENABLE_LOGIX.getItemType());
        assertEquals(Conditional.ItemType.LOGIX, Conditional.Action.DISABLE_LOGIX.getItemType());
        assertEquals(Conditional.ItemType.AUDIO, Conditional.Action.PLAY_SOUND.getItemType());
        assertEquals(Conditional.ItemType.SCRIPT, Conditional.Action.RUN_SCRIPT.getItemType());
        assertEquals(Conditional.ItemType.TURNOUT, Conditional.Action.DELAYED_TURNOUT.getItemType());
        assertEquals(Conditional.ItemType.TURNOUT, Conditional.Action.LOCK_TURNOUT.getItemType());
        assertEquals(Conditional.ItemType.SENSOR, Conditional.Action.RESET_DELAYED_SENSOR.getItemType());
        assertEquals(Conditional.ItemType.SENSOR, Conditional.Action.CANCEL_SENSOR_TIMERS.getItemType());
        assertEquals(Conditional.ItemType.TURNOUT, Conditional.Action.RESET_DELAYED_TURNOUT.getItemType());
        assertEquals(Conditional.ItemType.TURNOUT, Conditional.Action.CANCEL_TURNOUT_TIMERS.getItemType());
        assertEquals(Conditional.ItemType.CLOCK, Conditional.Action.SET_FAST_CLOCK_TIME.getItemType());
        assertEquals(Conditional.ItemType.CLOCK, Conditional.Action.START_FAST_CLOCK.getItemType());
        assertEquals(Conditional.ItemType.CLOCK, Conditional.Action.STOP_FAST_CLOCK.getItemType());
        assertEquals(Conditional.ItemType.MEMORY, Conditional.Action.COPY_MEMORY.getItemType());
        assertEquals(Conditional.ItemType.LIGHT, Conditional.Action.SET_LIGHT_INTENSITY.getItemType());
        assertEquals(Conditional.ItemType.LIGHT, Conditional.Action.SET_LIGHT_TRANSITION_TIME.getItemType());
        assertEquals(Conditional.ItemType.AUDIO, Conditional.Action.CONTROL_AUDIO.getItemType());
        assertEquals(Conditional.ItemType.SCRIPT, Conditional.Action.JYTHON_COMMAND.getItemType());
        assertEquals(Conditional.ItemType.WARRANT, Conditional.Action.ALLOCATE_WARRANT_ROUTE.getItemType());
        assertEquals(Conditional.ItemType.WARRANT, Conditional.Action.DEALLOCATE_WARRANT_ROUTE.getItemType());
        assertEquals(Conditional.ItemType.WARRANT, Conditional.Action.SET_ROUTE_TURNOUTS.getItemType());
        assertEquals(Conditional.ItemType.WARRANT, Conditional.Action.AUTO_RUN_WARRANT.getItemType());
        assertEquals(Conditional.ItemType.WARRANT, Conditional.Action.CONTROL_TRAIN.getItemType());
        assertEquals(Conditional.ItemType.WARRANT, Conditional.Action.SET_TRAIN_ID.getItemType());
        assertEquals(Conditional.ItemType.SIGNALMAST, Conditional.Action.SET_SIGNALMAST_ASPECT.getItemType());
        assertEquals(Conditional.ItemType.WARRANT, Conditional.Action.GET_TRAIN_LOCATION.getItemType());
        assertEquals(Conditional.ItemType.SIGNALMAST, Conditional.Action.SET_SIGNALMAST_HELD.getItemType());
        assertEquals(Conditional.ItemType.SIGNALMAST, Conditional.Action.CLEAR_SIGNALMAST_HELD.getItemType());
        assertEquals(Conditional.ItemType.SIGNALMAST, Conditional.Action.SET_SIGNALMAST_DARK.getItemType());
        assertEquals(Conditional.ItemType.SIGNALMAST, Conditional.Action.SET_SIGNALMAST_LIT.getItemType());
        assertEquals(Conditional.ItemType.OBLOCK, Conditional.Action.SET_BLOCK_ERROR.getItemType());
        assertEquals(Conditional.ItemType.OBLOCK, Conditional.Action.CLEAR_BLOCK_ERROR.getItemType());
        assertEquals(Conditional.ItemType.OBLOCK, Conditional.Action.DEALLOCATE_BLOCK.getItemType());
        assertEquals(Conditional.ItemType.OBLOCK, Conditional.Action.SET_BLOCK_OUT_OF_SERVICE.getItemType());
        assertEquals(Conditional.ItemType.OBLOCK, Conditional.Action.SET_BLOCK_IN_SERVICE.getItemType());
        assertEquals(Conditional.ItemType.OBLOCK, Conditional.Action.GET_BLOCK_TRAIN_NAME.getItemType());
        assertEquals(Conditional.ItemType.OBLOCK, Conditional.Action.GET_BLOCK_WARRANT.getItemType());
        assertEquals(Conditional.ItemType.WARRANT, Conditional.Action.MANUAL_RUN_WARRANT.getItemType());
        assertEquals(Conditional.ItemType.WARRANT, Conditional.Action.SET_TRAIN_NAME.getItemType());
        assertEquals(Conditional.ItemType.OBLOCK, Conditional.Action.SET_BLOCK_VALUE.getItemType());
        assertEquals(Conditional.ItemType.ENTRYEXIT, Conditional.Action.SET_NXPAIR_ENABLED.getItemType());
        assertEquals(Conditional.ItemType.ENTRYEXIT, Conditional.Action.SET_NXPAIR_DISABLED.getItemType());
        assertEquals(Conditional.ItemType.ENTRYEXIT, Conditional.Action.SET_NXPAIR_SEGMENT.getItemType());
/*
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
*/
        // Map Sensor Type comboBox items to Sensor action types
        assertEquals(Conditional.Action.SET_SENSOR, Conditional.Action.getSensorItems().get(0));
        assertEquals(Conditional.Action.DELAYED_SENSOR, Conditional.Action.getSensorItems().get(1));
        assertEquals(Conditional.Action.RESET_DELAYED_SENSOR, Conditional.Action.getSensorItems().get(2));
        assertEquals(Conditional.Action.CANCEL_SENSOR_TIMERS, Conditional.Action.getSensorItems().get(3));

        // Map Sensor Type comboBox items to Sensor action types
//        Assert.assertEquals(Conditional.ACTION_SET_SENSOR, Conditional.ITEM_TO_SENSOR_ACTION[0]);
//        Assert.assertEquals(Conditional.ACTION_DELAYED_SENSOR, Conditional.ITEM_TO_SENSOR_ACTION[1]);
//        Assert.assertEquals(Conditional.ACTION_RESET_DELAYED_SENSOR, Conditional.ITEM_TO_SENSOR_ACTION[2]);
//        Assert.assertEquals(Conditional.ACTION_CANCEL_SENSOR_TIMERS, Conditional.ITEM_TO_SENSOR_ACTION[3]);

        // Map Turnout Type comboBox items to Turnout action types
        assertEquals(Conditional.Action.SET_TURNOUT, Conditional.Action.getTurnoutItems().get(0));
        assertEquals(Conditional.Action.DELAYED_TURNOUT, Conditional.Action.getTurnoutItems().get(1));
        assertEquals(Conditional.Action.LOCK_TURNOUT, Conditional.Action.getTurnoutItems().get(2));
        assertEquals(Conditional.Action.CANCEL_TURNOUT_TIMERS, Conditional.Action.getTurnoutItems().get(3));
        assertEquals(Conditional.Action.RESET_DELAYED_TURNOUT, Conditional.Action.getTurnoutItems().get(4));

        // Map Turnout Type comboBox items to Turnout action types
//        Assert.assertEquals(Conditional.ACTION_SET_TURNOUT, Conditional.ITEM_TO_TURNOUT_ACTION[0]);
//        Assert.assertEquals(Conditional.ACTION_DELAYED_TURNOUT, Conditional.ITEM_TO_TURNOUT_ACTION[1]);
//        Assert.assertEquals(Conditional.ACTION_LOCK_TURNOUT, Conditional.ITEM_TO_TURNOUT_ACTION[2]);
//        Assert.assertEquals(Conditional.ACTION_CANCEL_TURNOUT_TIMERS, Conditional.ITEM_TO_TURNOUT_ACTION[3]);
//        Assert.assertEquals(Conditional.ACTION_RESET_DELAYED_TURNOUT, Conditional.ITEM_TO_TURNOUT_ACTION[4]);

        // Map Memory Type comboBox items to Memory action types
        assertEquals(Conditional.Action.SET_MEMORY, Conditional.Action.getMemoryItems().get(0));
        assertEquals(Conditional.Action.COPY_MEMORY, Conditional.Action.getMemoryItems().get(1));

        // Map Memory Type comboBox items to Memory action types
//        Assert.assertEquals(12, Conditional.ITEM_TO_MEMORY_ACTION[0]);
//        Assert.assertEquals(Conditional.ACTION_SET_MEMORY, Conditional.ITEM_TO_MEMORY_ACTION[0]);
//        Assert.assertEquals(26, Conditional.ITEM_TO_MEMORY_ACTION[1]);
//        Assert.assertEquals(Conditional.ACTION_COPY_MEMORY, Conditional.ITEM_TO_MEMORY_ACTION[1]);

        // Map Light Type comboBox items to Light action types
        assertEquals(Conditional.Action.SET_LIGHT, Conditional.Action.getLightItems().get(0));
        assertEquals(Conditional.Action.SET_LIGHT_INTENSITY, Conditional.Action.getLightItems().get(1));
        assertEquals(Conditional.Action.SET_LIGHT_TRANSITION_TIME, Conditional.Action.getLightItems().get(2));

        // Map Light Type comboBox items to Light action types
//        Assert.assertEquals(Conditional.ACTION_SET_LIGHT, Conditional.ITEM_TO_LIGHT_ACTION[0]);
//        Assert.assertEquals(Conditional.ACTION_SET_LIGHT_INTENSITY, Conditional.ITEM_TO_LIGHT_ACTION[1]);
//        Assert.assertEquals(Conditional.ACTION_SET_LIGHT_TRANSITION_TIME, Conditional.ITEM_TO_LIGHT_ACTION[2]);

        // Map FastClock Type comboBox items to FastClock action types
        assertEquals(Conditional.Action.SET_FAST_CLOCK_TIME, Conditional.Action.getClockItems().get(0));
        assertEquals(Conditional.Action.START_FAST_CLOCK, Conditional.Action.getClockItems().get(1));
        assertEquals(Conditional.Action.STOP_FAST_CLOCK, Conditional.Action.getClockItems().get(2));

        // Map FastClock Type comboBox items to FastClock action types
//        Assert.assertEquals(Conditional.ACTION_SET_FAST_CLOCK_TIME, Conditional.ITEM_TO_CLOCK_ACTION[0]);
//        Assert.assertEquals(Conditional.ACTION_START_FAST_CLOCK, Conditional.ITEM_TO_CLOCK_ACTION[1]);
//        Assert.assertEquals(Conditional.ACTION_STOP_FAST_CLOCK, Conditional.ITEM_TO_CLOCK_ACTION[2]);

        // Map Logix Type comboBox items to Logix action types
        assertEquals(Conditional.Action.ENABLE_LOGIX, Conditional.Action.getLogixItems().get(0));
        assertEquals(Conditional.Action.DISABLE_LOGIX, Conditional.Action.getLogixItems().get(1));

        // Map Logix Type comboBox items to Logix action types
//        Assert.assertEquals(Conditional.ACTION_ENABLE_LOGIX, Conditional.ITEM_TO_LOGIX_ACTION[0]);
//        Assert.assertEquals(Conditional.ACTION_DISABLE_LOGIX, Conditional.ITEM_TO_LOGIX_ACTION[1]);

        // Map Warrant Type comboBox items to Warrant action types
        assertEquals(Conditional.Action.ALLOCATE_WARRANT_ROUTE, Conditional.Action.getWarrantItems().get(0));
        assertEquals(Conditional.Action.DEALLOCATE_WARRANT_ROUTE, Conditional.Action.getWarrantItems().get(1));
        assertEquals(Conditional.Action.SET_ROUTE_TURNOUTS, Conditional.Action.getWarrantItems().get(2));
        assertEquals(Conditional.Action.AUTO_RUN_WARRANT, Conditional.Action.getWarrantItems().get(3));
        assertEquals(Conditional.Action.MANUAL_RUN_WARRANT, Conditional.Action.getWarrantItems().get(4));
        assertEquals(Conditional.Action.CONTROL_TRAIN, Conditional.Action.getWarrantItems().get(5));
        assertEquals(Conditional.Action.SET_TRAIN_ID, Conditional.Action.getWarrantItems().get(6));
        assertEquals(Conditional.Action.SET_TRAIN_NAME, Conditional.Action.getWarrantItems().get(7));
        assertEquals(Conditional.Action.GET_TRAIN_LOCATION, Conditional.Action.getWarrantItems().get(8));

        // Map Warrant Type comboBox items to Warrant action types
//        Assert.assertEquals(Conditional.ACTION_ALLOCATE_WARRANT_ROUTE, Conditional.ITEM_TO_WARRANT_ACTION[0]);
//        Assert.assertEquals(Conditional.ACTION_DEALLOCATE_WARRANT_ROUTE, Conditional.ITEM_TO_WARRANT_ACTION[1]);
//        Assert.assertEquals(Conditional.ACTION_SET_ROUTE_TURNOUTS, Conditional.ITEM_TO_WARRANT_ACTION[2]);
//        Assert.assertEquals(Conditional.ACTION_AUTO_RUN_WARRANT, Conditional.ITEM_TO_WARRANT_ACTION[3]);
//        Assert.assertEquals(Conditional.ACTION_MANUAL_RUN_WARRANT, Conditional.ITEM_TO_WARRANT_ACTION[4]);
//        Assert.assertEquals(Conditional.ACTION_CONTROL_TRAIN, Conditional.ITEM_TO_WARRANT_ACTION[5]);
//        Assert.assertEquals(Conditional.ACTION_SET_TRAIN_ID, Conditional.ITEM_TO_WARRANT_ACTION[6]);
//        Assert.assertEquals(Conditional.ACTION_SET_TRAIN_NAME, Conditional.ITEM_TO_WARRANT_ACTION[7]);
//        Assert.assertEquals(Conditional.ACTION_THROTTLE_FACTOR, Conditional.ITEM_TO_WARRANT_ACTION[8]);

        assertEquals(Conditional.Action.DEALLOCATE_BLOCK, Conditional.Action.getOBlockItems().get(0));
        assertEquals(Conditional.Action.SET_BLOCK_VALUE, Conditional.Action.getOBlockItems().get(1));
        assertEquals(Conditional.Action.SET_BLOCK_ERROR, Conditional.Action.getOBlockItems().get(2));
        assertEquals(Conditional.Action.CLEAR_BLOCK_ERROR, Conditional.Action.getOBlockItems().get(3));
        assertEquals(Conditional.Action.SET_BLOCK_OUT_OF_SERVICE, Conditional.Action.getOBlockItems().get(4));
        assertEquals(Conditional.Action.SET_BLOCK_IN_SERVICE, Conditional.Action.getOBlockItems().get(5));
        assertEquals(Conditional.Action.GET_BLOCK_TRAIN_NAME, Conditional.Action.getOBlockItems().get(6));
        assertEquals(Conditional.Action.GET_BLOCK_WARRANT, Conditional.Action.getOBlockItems().get(7));

//        Assert.assertEquals(Conditional.ACTION_DEALLOCATE_BLOCK, Conditional.ITEM_TO_OBLOCK_ACTION[0]);
//        Assert.assertEquals(Conditional.ACTION_SET_BLOCK_VALUE, Conditional.ITEM_TO_OBLOCK_ACTION[1]);
//        Assert.assertEquals(Conditional.ACTION_SET_BLOCK_ERROR, Conditional.ITEM_TO_OBLOCK_ACTION[2]);
//        Assert.assertEquals(Conditional.ACTION_CLEAR_BLOCK_ERROR, Conditional.ITEM_TO_OBLOCK_ACTION[3]);
//        Assert.assertEquals(Conditional.ACTION_SET_BLOCK_OUT_OF_SERVICE, Conditional.ITEM_TO_OBLOCK_ACTION[4]);
//        Assert.assertEquals(Conditional.ACTION_SET_BLOCK_IN_SERVICE, Conditional.ITEM_TO_OBLOCK_ACTION[5]);

        // Map Signal Head Type comboBox items to Signal Head action types
        assertEquals(Conditional.Action.SET_SIGNAL_APPEARANCE, Conditional.Action.getSignalHeadItems().get(0));
        assertEquals(Conditional.Action.SET_SIGNAL_HELD, Conditional.Action.getSignalHeadItems().get(1));
        assertEquals(Conditional.Action.CLEAR_SIGNAL_HELD, Conditional.Action.getSignalHeadItems().get(2));
        assertEquals(Conditional.Action.SET_SIGNAL_DARK, Conditional.Action.getSignalHeadItems().get(3));
        assertEquals(Conditional.Action.SET_SIGNAL_LIT, Conditional.Action.getSignalHeadItems().get(4));

        // Map Signal Head Type comboBox items to Signal Head action types
//        Assert.assertEquals(Conditional.ACTION_SET_SIGNAL_APPEARANCE, Conditional.ITEM_TO_SIGNAL_HEAD_ACTION[0]);
//        Assert.assertEquals(Conditional.ACTION_SET_SIGNAL_HELD, Conditional.ITEM_TO_SIGNAL_HEAD_ACTION[1]);
//        Assert.assertEquals(Conditional.ACTION_CLEAR_SIGNAL_HELD, Conditional.ITEM_TO_SIGNAL_HEAD_ACTION[2]);
//        Assert.assertEquals(Conditional.ACTION_SET_SIGNAL_DARK, Conditional.ITEM_TO_SIGNAL_HEAD_ACTION[3]);
//        Assert.assertEquals(Conditional.ACTION_SET_SIGNAL_LIT, Conditional.ITEM_TO_SIGNAL_HEAD_ACTION[4]);

        // Map Signal Mast Type comboBox items to Signal Mast action types
        assertEquals(Conditional.Action.SET_SIGNALMAST_ASPECT, Conditional.Action.getSignalMastItems().get(0));
        assertEquals(Conditional.Action.SET_SIGNALMAST_HELD, Conditional.Action.getSignalMastItems().get(1));
        assertEquals(Conditional.Action.CLEAR_SIGNALMAST_HELD, Conditional.Action.getSignalMastItems().get(2));
        assertEquals(Conditional.Action.SET_SIGNALMAST_DARK, Conditional.Action.getSignalMastItems().get(3));
        assertEquals(Conditional.Action.SET_SIGNALMAST_LIT, Conditional.Action.getSignalMastItems().get(4));

        // Map Signal Mast Type comboBox items to Signal Mast action types
//        Assert.assertEquals(Conditional.ACTION_SET_SIGNALMAST_ASPECT, Conditional.ITEM_TO_SIGNAL_MAST_ACTION[0]);
//        Assert.assertEquals(Conditional.ACTION_SET_SIGNALMAST_HELD, Conditional.ITEM_TO_SIGNAL_MAST_ACTION[1]);
//        Assert.assertEquals(Conditional.ACTION_CLEAR_SIGNALMAST_HELD, Conditional.ITEM_TO_SIGNAL_MAST_ACTION[2]);
//        Assert.assertEquals(Conditional.ACTION_SET_SIGNALMAST_DARK, Conditional.ITEM_TO_SIGNAL_MAST_ACTION[3]);
//        Assert.assertEquals(Conditional.ACTION_SET_SIGNALMAST_LIT, Conditional.ITEM_TO_SIGNAL_MAST_ACTION[4]);

        // Map Audio Type comboBox items to Audio action types
        assertEquals(Conditional.Action.PLAY_SOUND, Conditional.Action.getAudioItems().get(0));
        assertEquals(Conditional.Action.CONTROL_AUDIO, Conditional.Action.getAudioItems().get(1));

        // Map Audio Type comboBox items to Audio action types
//        Assert.assertEquals(Conditional.ACTION_PLAY_SOUND, Conditional.ITEM_TO_AUDIO_ACTION[0]);
//        Assert.assertEquals(Conditional.ACTION_CONTROL_AUDIO, Conditional.ITEM_TO_AUDIO_ACTION[1]);

        // Map Script Type comboBox items to Script action types
        assertEquals(Conditional.Action.RUN_SCRIPT, Conditional.Action.getScriptItems().get(0));
        assertEquals(Conditional.Action.JYTHON_COMMAND, Conditional.Action.getScriptItems().get(1));

        // Map Script Type comboBox items to Script action types
//        Assert.assertEquals(Conditional.ACTION_RUN_SCRIPT, Conditional.ITEM_TO_SCRIPT_ACTION[0]);
//        Assert.assertEquals(Conditional.ACTION_JYTHON_COMMAND, Conditional.ITEM_TO_SCRIPT_ACTION[1]);

        // Map EntryExit Type comboBox items to EntryExit action types
        assertEquals(Conditional.Action.SET_NXPAIR_ENABLED, Conditional.Action.getEntryExitItems().get(0));
        assertEquals(Conditional.Action.SET_NXPAIR_DISABLED, Conditional.Action.getEntryExitItems().get(1));
        assertEquals(Conditional.Action.SET_NXPAIR_SEGMENT, Conditional.Action.getEntryExitItems().get(2));

        // Map EntryExit Type comboBox items to EntryExit action types
//        Assert.assertEquals(Conditional.ACTION_SET_NXPAIR_ENABLED, Conditional.ITEM_TO_ENTRYEXIT_ACTION[0]);
//        Assert.assertEquals(Conditional.ACTION_SET_NXPAIR_DISABLED, Conditional.ITEM_TO_ENTRYEXIT_ACTION[1]);
//        Assert.assertEquals(Conditional.ACTION_SET_NXPAIR_SEGMENT, Conditional.ITEM_TO_ENTRYEXIT_ACTION[2]);

        // Map Misc Type comboBox items to Misc action types
        assertEquals(Conditional.Action.TRIGGER_ROUTE, Conditional.Action.getOtherItems().get(0));

        // Map Misc Type comboBox items to Misc action types
//        Assert.assertEquals(Conditional.ACTION_TRIGGER_ROUTE, Conditional.ITEM_TO_OTHER_ACTION[0]);
    }
/*    
    @Test
    public void testEnums() {
        
        // This is a temporary test to check the new enums
        for (Conditional.Type type : Conditional.Type.values()) {
            Conditional.ItemType itemType = type.getItemType();
            
            int typeInt = type.getIntValue();
            int itemTypeInt = type.getItemType().getIntValue();
            
            String message = String.format("type %s has correct itemType %s", type.name(), itemType.name());
            
            if (typeInt != -1) {
                Assert.assertTrue(message, Conditional.TEST_TO_ITEM[typeInt] == itemTypeInt);
            }
        }
    }
*/    
    
    // from here down is testing infrastructure

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initIdTagManager();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
    
}

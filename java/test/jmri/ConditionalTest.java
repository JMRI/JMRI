package jmri;

import org.junit.*;

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
        Assert.assertEquals(Conditional.OPERATOR_NONE, 4);
        Assert.assertEquals(Conditional.OPERATOR_OR, 5);
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
        // This item is never used
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
//        Assert.assertEquals(Conditional.ITEM_TYPE_LAST_STATE_VAR, 11);

        Assert.assertEquals(Conditional.ITEM_TYPE_AUDIO, 12);
        Assert.assertEquals(Conditional.ITEM_TYPE_SCRIPT, 13);
        Assert.assertEquals(Conditional.ITEM_TYPE_OTHER, 14);
//        Assert.assertEquals(Conditional.ITEM_TYPE_LAST_ACTION, 14);
    }
    
    @Test
    public void testArrays() {
        // Map SignalHead comboBox items to SignalHead Conditional variable types
        Assert.assertEquals(Conditional.Type.NONE, Conditional.Type.getSignalHeadStateMachineItems().get(0));
        Assert.assertEquals(Conditional.Type.SIGNAL_HEAD_APPEARANCE_EQUALS, Conditional.Type.getSignalHeadStateMachineItems().get(1));
        Assert.assertEquals(Conditional.Type.SIGNAL_HEAD_LIT, Conditional.Type.getSignalHeadStateMachineItems().get(2));
        Assert.assertEquals(Conditional.Type.SIGNAL_HEAD_HELD, Conditional.Type.getSignalHeadStateMachineItems().get(3));

        // Map SignalMAst comboBox items to SignalMast Conditional variable types
        Assert.assertEquals(Conditional.Type.NONE, Conditional.Type.getSignalMastItems().get(0));
        Assert.assertEquals(Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS, Conditional.Type.getSignalMastItems().get(1));
        Assert.assertEquals(Conditional.Type.SIGNAL_MAST_LIT, Conditional.Type.getSignalMastItems().get(2));
        Assert.assertEquals(Conditional.Type.SIGNAL_MAST_HELD, Conditional.Type.getSignalMastItems().get(3));
        
        // Map Sensor state comboBox items to Sensor Conditional variable types
        Assert.assertEquals(Conditional.Type.SENSOR_ACTIVE, Conditional.Type.getSensorItems().get(0));
        Assert.assertEquals(Conditional.Type.SENSOR_INACTIVE, Conditional.Type.getSensorItems().get(1));

        // Map Turnout state comboBox items to Turnout Conditional variable types
        Assert.assertEquals(Conditional.Type.TURNOUT_THROWN, Conditional.Type.getTurnoutItems().get(0));
        Assert.assertEquals(Conditional.Type.TURNOUT_CLOSED, Conditional.Type.getTurnoutItems().get(1));

        // Map Conditional state comboBox items to  Condition ConditionalVvariable types
        Assert.assertEquals(Conditional.Type.CONDITIONAL_TRUE, Conditional.Type.getConditionalItems().get(0));
        Assert.assertEquals(Conditional.Type.CONDITIONAL_FALSE, Conditional.Type.getConditionalItems().get(1));

        // Map Memory state comboBox items to Light ConditionalVariable types
        Assert.assertEquals(Conditional.Type.LIGHT_ON, Conditional.Type.getLightItems().get(0));
        Assert.assertEquals(Conditional.Type.LIGHT_OFF, Conditional.Type.getLightItems().get(1));

        // Map Warrant state comboBox items to Warrant ConditionalVariable types
        Assert.assertEquals(Conditional.Type.ROUTE_FREE, Conditional.Type.getWarrantItems().get(0));
        Assert.assertEquals(Conditional.Type.ROUTE_SET, Conditional.Type.getWarrantItems().get(1));
        Assert.assertEquals(Conditional.Type.ROUTE_ALLOCATED, Conditional.Type.getWarrantItems().get(2));
        Assert.assertEquals(Conditional.Type.ROUTE_OCCUPIED, Conditional.Type.getWarrantItems().get(3));
        Assert.assertEquals(Conditional.Type.TRAIN_RUNNING, Conditional.Type.getWarrantItems().get(4));

        // Map Memory Compare Type comboBox items to Memory ConditionalVariable types
        Assert.assertEquals(Conditional.Type.MEMORY_EQUALS, Conditional.Type.getMemoryItems().get(0));
        Assert.assertEquals(Conditional.Type.MEMORY_EQUALS_INSENSITIVE, Conditional.Type.getMemoryItems().get(1));
        Assert.assertEquals(Conditional.Type.MEMORY_COMPARE, Conditional.Type.getMemoryItems().get(2));
        Assert.assertEquals(Conditional.Type.MEMORY_COMPARE_INSENSITIVE, Conditional.Type.getMemoryItems().get(3));

//        Assert.assertEquals(Conditional.TYPE_BLOCK_STATUS_EQUALS, Conditional.Type.getOBlockItems().get(0));

        Assert.assertEquals(Conditional.Type.ENTRYEXIT_ACTIVE, Conditional.Type.getEntryExitItems().get(0));
        Assert.assertEquals(Conditional.Type.ENTRYEXIT_INACTIVE, Conditional.Type.getEntryExitItems().get(1));

        /**
         * *************** ConditionalAction Maps *******************************
         */
        // Map action type to the item type
        Assert.assertEquals(Conditional.ItemType.NONE, Conditional.Action.NONE.getItemType());
        Assert.assertEquals(Conditional.ItemType.TURNOUT, Conditional.Action.SET_TURNOUT.getItemType());
        Assert.assertEquals(Conditional.ItemType.SIGNALHEAD, Conditional.Action.SET_SIGNAL_APPEARANCE.getItemType());
        Assert.assertEquals(Conditional.ItemType.SIGNALHEAD, Conditional.Action.SET_SIGNAL_HELD.getItemType());
        Assert.assertEquals(Conditional.ItemType.SIGNALHEAD, Conditional.Action.CLEAR_SIGNAL_HELD.getItemType());
        Assert.assertEquals(Conditional.ItemType.SIGNALHEAD, Conditional.Action.SET_SIGNAL_DARK.getItemType());
        Assert.assertEquals(Conditional.ItemType.SIGNALHEAD, Conditional.Action.SET_SIGNAL_LIT.getItemType());
        Assert.assertEquals(Conditional.ItemType.OTHER, Conditional.Action.TRIGGER_ROUTE.getItemType());
        Assert.assertEquals(Conditional.ItemType.SENSOR, Conditional.Action.SET_SENSOR.getItemType());
        Assert.assertEquals(Conditional.ItemType.SENSOR, Conditional.Action.DELAYED_SENSOR.getItemType());
        Assert.assertEquals(Conditional.ItemType.LIGHT, Conditional.Action.SET_LIGHT.getItemType());
        Assert.assertEquals(Conditional.ItemType.MEMORY, Conditional.Action.SET_MEMORY.getItemType());
        Assert.assertEquals(Conditional.ItemType.LOGIX, Conditional.Action.ENABLE_LOGIX.getItemType());
        Assert.assertEquals(Conditional.ItemType.LOGIX, Conditional.Action.DISABLE_LOGIX.getItemType());
        Assert.assertEquals(Conditional.ItemType.AUDIO, Conditional.Action.PLAY_SOUND.getItemType());
        Assert.assertEquals(Conditional.ItemType.SCRIPT, Conditional.Action.RUN_SCRIPT.getItemType());
        Assert.assertEquals(Conditional.ItemType.TURNOUT, Conditional.Action.DELAYED_TURNOUT.getItemType());
        Assert.assertEquals(Conditional.ItemType.TURNOUT, Conditional.Action.LOCK_TURNOUT.getItemType());
        Assert.assertEquals(Conditional.ItemType.SENSOR, Conditional.Action.RESET_DELAYED_SENSOR.getItemType());
        Assert.assertEquals(Conditional.ItemType.SENSOR, Conditional.Action.CANCEL_SENSOR_TIMERS.getItemType());
        Assert.assertEquals(Conditional.ItemType.TURNOUT, Conditional.Action.RESET_DELAYED_TURNOUT.getItemType());
        Assert.assertEquals(Conditional.ItemType.TURNOUT, Conditional.Action.CANCEL_TURNOUT_TIMERS.getItemType());
        Assert.assertEquals(Conditional.ItemType.CLOCK, Conditional.Action.SET_FAST_CLOCK_TIME.getItemType());
        Assert.assertEquals(Conditional.ItemType.CLOCK, Conditional.Action.START_FAST_CLOCK.getItemType());
        Assert.assertEquals(Conditional.ItemType.CLOCK, Conditional.Action.STOP_FAST_CLOCK.getItemType());
        Assert.assertEquals(Conditional.ItemType.MEMORY, Conditional.Action.COPY_MEMORY.getItemType());
        Assert.assertEquals(Conditional.ItemType.LIGHT, Conditional.Action.SET_LIGHT_INTENSITY.getItemType());
        Assert.assertEquals(Conditional.ItemType.LIGHT, Conditional.Action.SET_LIGHT_TRANSITION_TIME.getItemType());
        Assert.assertEquals(Conditional.ItemType.AUDIO, Conditional.Action.CONTROL_AUDIO.getItemType());
        Assert.assertEquals(Conditional.ItemType.SCRIPT, Conditional.Action.JYTHON_COMMAND.getItemType());
        Assert.assertEquals(Conditional.ItemType.WARRANT, Conditional.Action.ALLOCATE_WARRANT_ROUTE.getItemType());
        Assert.assertEquals(Conditional.ItemType.WARRANT, Conditional.Action.DEALLOCATE_WARRANT_ROUTE.getItemType());
        Assert.assertEquals(Conditional.ItemType.WARRANT, Conditional.Action.SET_ROUTE_TURNOUTS.getItemType());
        Assert.assertEquals(Conditional.ItemType.WARRANT, Conditional.Action.AUTO_RUN_WARRANT.getItemType());
        Assert.assertEquals(Conditional.ItemType.WARRANT, Conditional.Action.CONTROL_TRAIN.getItemType());
        Assert.assertEquals(Conditional.ItemType.WARRANT, Conditional.Action.SET_TRAIN_ID.getItemType());
        Assert.assertEquals(Conditional.ItemType.SIGNALMAST, Conditional.Action.SET_SIGNALMAST_ASPECT.getItemType());
        Assert.assertEquals(Conditional.ItemType.WARRANT, Conditional.Action.THROTTLE_FACTOR.getItemType());
        Assert.assertEquals(Conditional.ItemType.SIGNALMAST, Conditional.Action.SET_SIGNALMAST_HELD.getItemType());
        Assert.assertEquals(Conditional.ItemType.SIGNALMAST, Conditional.Action.CLEAR_SIGNALMAST_HELD.getItemType());
        Assert.assertEquals(Conditional.ItemType.SIGNALMAST, Conditional.Action.SET_SIGNALMAST_DARK.getItemType());
        Assert.assertEquals(Conditional.ItemType.SIGNALMAST, Conditional.Action.SET_SIGNALMAST_LIT.getItemType());
        Assert.assertEquals(Conditional.ItemType.OBLOCK, Conditional.Action.SET_BLOCK_ERROR.getItemType());
        Assert.assertEquals(Conditional.ItemType.OBLOCK, Conditional.Action.CLEAR_BLOCK_ERROR.getItemType());
        Assert.assertEquals(Conditional.ItemType.OBLOCK, Conditional.Action.DEALLOCATE_BLOCK.getItemType());
        Assert.assertEquals(Conditional.ItemType.OBLOCK, Conditional.Action.SET_BLOCK_OUT_OF_SERVICE.getItemType());
        Assert.assertEquals(Conditional.ItemType.OBLOCK, Conditional.Action.SET_BLOCK_IN_SERVICE.getItemType());
        Assert.assertEquals(Conditional.ItemType.WARRANT, Conditional.Action.MANUAL_RUN_WARRANT.getItemType());
        Assert.assertEquals(Conditional.ItemType.WARRANT, Conditional.Action.SET_TRAIN_NAME.getItemType());
        Assert.assertEquals(Conditional.ItemType.OBLOCK, Conditional.Action.SET_BLOCK_VALUE.getItemType());
        Assert.assertEquals(Conditional.ItemType.ENTRYEXIT, Conditional.Action.SET_NXPAIR_ENABLED.getItemType());
        Assert.assertEquals(Conditional.ItemType.ENTRYEXIT, Conditional.Action.SET_NXPAIR_DISABLED.getItemType());
        Assert.assertEquals(Conditional.ItemType.ENTRYEXIT, Conditional.Action.SET_NXPAIR_SEGMENT.getItemType());
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
        Assert.assertEquals(Conditional.Action.SET_SENSOR, Conditional.Action.getSensorItems().get(0));
        Assert.assertEquals(Conditional.Action.DELAYED_SENSOR, Conditional.Action.getSensorItems().get(1));
        Assert.assertEquals(Conditional.Action.RESET_DELAYED_SENSOR, Conditional.Action.getSensorItems().get(2));
        Assert.assertEquals(Conditional.Action.CANCEL_SENSOR_TIMERS, Conditional.Action.getSensorItems().get(3));

        // Map Sensor Type comboBox items to Sensor action types
//        Assert.assertEquals(Conditional.ACTION_SET_SENSOR, Conditional.ITEM_TO_SENSOR_ACTION[0]);
//        Assert.assertEquals(Conditional.ACTION_DELAYED_SENSOR, Conditional.ITEM_TO_SENSOR_ACTION[1]);
//        Assert.assertEquals(Conditional.ACTION_RESET_DELAYED_SENSOR, Conditional.ITEM_TO_SENSOR_ACTION[2]);
//        Assert.assertEquals(Conditional.ACTION_CANCEL_SENSOR_TIMERS, Conditional.ITEM_TO_SENSOR_ACTION[3]);

        // Map Turnout Type comboBox items to Turnout action types
        Assert.assertEquals(Conditional.Action.SET_TURNOUT, Conditional.Action.getTurnoutItems().get(0));
        Assert.assertEquals(Conditional.Action.DELAYED_TURNOUT, Conditional.Action.getTurnoutItems().get(1));
        Assert.assertEquals(Conditional.Action.LOCK_TURNOUT, Conditional.Action.getTurnoutItems().get(2));
        Assert.assertEquals(Conditional.Action.CANCEL_TURNOUT_TIMERS, Conditional.Action.getTurnoutItems().get(3));
        Assert.assertEquals(Conditional.Action.RESET_DELAYED_TURNOUT, Conditional.Action.getTurnoutItems().get(4));

        // Map Turnout Type comboBox items to Turnout action types
//        Assert.assertEquals(Conditional.ACTION_SET_TURNOUT, Conditional.ITEM_TO_TURNOUT_ACTION[0]);
//        Assert.assertEquals(Conditional.ACTION_DELAYED_TURNOUT, Conditional.ITEM_TO_TURNOUT_ACTION[1]);
//        Assert.assertEquals(Conditional.ACTION_LOCK_TURNOUT, Conditional.ITEM_TO_TURNOUT_ACTION[2]);
//        Assert.assertEquals(Conditional.ACTION_CANCEL_TURNOUT_TIMERS, Conditional.ITEM_TO_TURNOUT_ACTION[3]);
//        Assert.assertEquals(Conditional.ACTION_RESET_DELAYED_TURNOUT, Conditional.ITEM_TO_TURNOUT_ACTION[4]);

        // Map Memory Type comboBox items to Memory action types
        Assert.assertEquals(Conditional.Action.SET_MEMORY, Conditional.Action.getMemoryItems().get(0));
        Assert.assertEquals(Conditional.Action.COPY_MEMORY, Conditional.Action.getMemoryItems().get(1));

        // Map Memory Type comboBox items to Memory action types
//        Assert.assertEquals(12, Conditional.ITEM_TO_MEMORY_ACTION[0]);
//        Assert.assertEquals(Conditional.ACTION_SET_MEMORY, Conditional.ITEM_TO_MEMORY_ACTION[0]);
//        Assert.assertEquals(26, Conditional.ITEM_TO_MEMORY_ACTION[1]);
//        Assert.assertEquals(Conditional.ACTION_COPY_MEMORY, Conditional.ITEM_TO_MEMORY_ACTION[1]);

        // Map Light Type comboBox items to Light action types
        Assert.assertEquals(Conditional.Action.SET_LIGHT, Conditional.Action.getLightItems().get(0));
        Assert.assertEquals(Conditional.Action.SET_LIGHT_INTENSITY, Conditional.Action.getLightItems().get(1));
        Assert.assertEquals(Conditional.Action.SET_LIGHT_TRANSITION_TIME, Conditional.Action.getLightItems().get(2));

        // Map Light Type comboBox items to Light action types
//        Assert.assertEquals(Conditional.ACTION_SET_LIGHT, Conditional.ITEM_TO_LIGHT_ACTION[0]);
//        Assert.assertEquals(Conditional.ACTION_SET_LIGHT_INTENSITY, Conditional.ITEM_TO_LIGHT_ACTION[1]);
//        Assert.assertEquals(Conditional.ACTION_SET_LIGHT_TRANSITION_TIME, Conditional.ITEM_TO_LIGHT_ACTION[2]);

        // Map FastClock Type comboBox items to FastClock action types
        Assert.assertEquals(Conditional.Action.SET_FAST_CLOCK_TIME, Conditional.Action.getClockItems().get(0));
        Assert.assertEquals(Conditional.Action.START_FAST_CLOCK, Conditional.Action.getClockItems().get(1));
        Assert.assertEquals(Conditional.Action.STOP_FAST_CLOCK, Conditional.Action.getClockItems().get(2));

        // Map FastClock Type comboBox items to FastClock action types
//        Assert.assertEquals(Conditional.ACTION_SET_FAST_CLOCK_TIME, Conditional.ITEM_TO_CLOCK_ACTION[0]);
//        Assert.assertEquals(Conditional.ACTION_START_FAST_CLOCK, Conditional.ITEM_TO_CLOCK_ACTION[1]);
//        Assert.assertEquals(Conditional.ACTION_STOP_FAST_CLOCK, Conditional.ITEM_TO_CLOCK_ACTION[2]);

        // Map Logix Type comboBox items to Logix action types
        Assert.assertEquals(Conditional.Action.ENABLE_LOGIX, Conditional.Action.getLogixItems().get(0));
        Assert.assertEquals(Conditional.Action.DISABLE_LOGIX, Conditional.Action.getLogixItems().get(1));

        // Map Logix Type comboBox items to Logix action types
//        Assert.assertEquals(Conditional.ACTION_ENABLE_LOGIX, Conditional.ITEM_TO_LOGIX_ACTION[0]);
//        Assert.assertEquals(Conditional.ACTION_DISABLE_LOGIX, Conditional.ITEM_TO_LOGIX_ACTION[1]);

        // Map Warrant Type comboBox items to Warrant action types
        Assert.assertEquals(Conditional.Action.ALLOCATE_WARRANT_ROUTE, Conditional.Action.getWarrantItems().get(0));
        Assert.assertEquals(Conditional.Action.DEALLOCATE_WARRANT_ROUTE, Conditional.Action.getWarrantItems().get(1));
        Assert.assertEquals(Conditional.Action.SET_ROUTE_TURNOUTS, Conditional.Action.getWarrantItems().get(2));
        Assert.assertEquals(Conditional.Action.AUTO_RUN_WARRANT, Conditional.Action.getWarrantItems().get(3));
        Assert.assertEquals(Conditional.Action.MANUAL_RUN_WARRANT, Conditional.Action.getWarrantItems().get(4));
        Assert.assertEquals(Conditional.Action.CONTROL_TRAIN, Conditional.Action.getWarrantItems().get(5));
        Assert.assertEquals(Conditional.Action.SET_TRAIN_ID, Conditional.Action.getWarrantItems().get(6));
        Assert.assertEquals(Conditional.Action.SET_TRAIN_NAME, Conditional.Action.getWarrantItems().get(7));
        Assert.assertEquals(Conditional.Action.THROTTLE_FACTOR, Conditional.Action.getWarrantItems().get(8));

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

        Assert.assertEquals(Conditional.Action.DEALLOCATE_BLOCK, Conditional.Action.getOBlockItems().get(0));
        Assert.assertEquals(Conditional.Action.SET_BLOCK_VALUE, Conditional.Action.getOBlockItems().get(1));
        Assert.assertEquals(Conditional.Action.SET_BLOCK_ERROR, Conditional.Action.getOBlockItems().get(2));
        Assert.assertEquals(Conditional.Action.CLEAR_BLOCK_ERROR, Conditional.Action.getOBlockItems().get(3));
        Assert.assertEquals(Conditional.Action.SET_BLOCK_OUT_OF_SERVICE, Conditional.Action.getOBlockItems().get(4));
        Assert.assertEquals(Conditional.Action.SET_BLOCK_IN_SERVICE, Conditional.Action.getOBlockItems().get(5));

//        Assert.assertEquals(Conditional.ACTION_DEALLOCATE_BLOCK, Conditional.ITEM_TO_OBLOCK_ACTION[0]);
//        Assert.assertEquals(Conditional.ACTION_SET_BLOCK_VALUE, Conditional.ITEM_TO_OBLOCK_ACTION[1]);
//        Assert.assertEquals(Conditional.ACTION_SET_BLOCK_ERROR, Conditional.ITEM_TO_OBLOCK_ACTION[2]);
//        Assert.assertEquals(Conditional.ACTION_CLEAR_BLOCK_ERROR, Conditional.ITEM_TO_OBLOCK_ACTION[3]);
//        Assert.assertEquals(Conditional.ACTION_SET_BLOCK_OUT_OF_SERVICE, Conditional.ITEM_TO_OBLOCK_ACTION[4]);
//        Assert.assertEquals(Conditional.ACTION_SET_BLOCK_IN_SERVICE, Conditional.ITEM_TO_OBLOCK_ACTION[5]);

        // Map Signal Head Type comboBox items to Signal Head action types
        Assert.assertEquals(Conditional.Action.SET_SIGNAL_APPEARANCE, Conditional.Action.getSignalHeadItems().get(0));
        Assert.assertEquals(Conditional.Action.SET_SIGNAL_HELD, Conditional.Action.getSignalHeadItems().get(1));
        Assert.assertEquals(Conditional.Action.CLEAR_SIGNAL_HELD, Conditional.Action.getSignalHeadItems().get(2));
        Assert.assertEquals(Conditional.Action.SET_SIGNAL_DARK, Conditional.Action.getSignalHeadItems().get(3));
        Assert.assertEquals(Conditional.Action.SET_SIGNAL_LIT, Conditional.Action.getSignalHeadItems().get(4));

        // Map Signal Head Type comboBox items to Signal Head action types
//        Assert.assertEquals(Conditional.ACTION_SET_SIGNAL_APPEARANCE, Conditional.ITEM_TO_SIGNAL_HEAD_ACTION[0]);
//        Assert.assertEquals(Conditional.ACTION_SET_SIGNAL_HELD, Conditional.ITEM_TO_SIGNAL_HEAD_ACTION[1]);
//        Assert.assertEquals(Conditional.ACTION_CLEAR_SIGNAL_HELD, Conditional.ITEM_TO_SIGNAL_HEAD_ACTION[2]);
//        Assert.assertEquals(Conditional.ACTION_SET_SIGNAL_DARK, Conditional.ITEM_TO_SIGNAL_HEAD_ACTION[3]);
//        Assert.assertEquals(Conditional.ACTION_SET_SIGNAL_LIT, Conditional.ITEM_TO_SIGNAL_HEAD_ACTION[4]);

        // Map Signal Mast Type comboBox items to Signal Mast action types
        Assert.assertEquals(Conditional.Action.SET_SIGNALMAST_ASPECT, Conditional.Action.getSignalMastItems().get(0));
        Assert.assertEquals(Conditional.Action.SET_SIGNALMAST_HELD, Conditional.Action.getSignalMastItems().get(1));
        Assert.assertEquals(Conditional.Action.CLEAR_SIGNALMAST_HELD, Conditional.Action.getSignalMastItems().get(2));
        Assert.assertEquals(Conditional.Action.SET_SIGNALMAST_DARK, Conditional.Action.getSignalMastItems().get(3));
        Assert.assertEquals(Conditional.Action.SET_SIGNALMAST_LIT, Conditional.Action.getSignalMastItems().get(4));

        // Map Signal Mast Type comboBox items to Signal Mast action types
//        Assert.assertEquals(Conditional.ACTION_SET_SIGNALMAST_ASPECT, Conditional.ITEM_TO_SIGNAL_MAST_ACTION[0]);
//        Assert.assertEquals(Conditional.ACTION_SET_SIGNALMAST_HELD, Conditional.ITEM_TO_SIGNAL_MAST_ACTION[1]);
//        Assert.assertEquals(Conditional.ACTION_CLEAR_SIGNALMAST_HELD, Conditional.ITEM_TO_SIGNAL_MAST_ACTION[2]);
//        Assert.assertEquals(Conditional.ACTION_SET_SIGNALMAST_DARK, Conditional.ITEM_TO_SIGNAL_MAST_ACTION[3]);
//        Assert.assertEquals(Conditional.ACTION_SET_SIGNALMAST_LIT, Conditional.ITEM_TO_SIGNAL_MAST_ACTION[4]);

        // Map Audio Type comboBox items to Audio action types
        Assert.assertEquals(Conditional.Action.PLAY_SOUND, Conditional.Action.getAudioItems().get(0));
        Assert.assertEquals(Conditional.Action.CONTROL_AUDIO, Conditional.Action.getAudioItems().get(1));

        // Map Audio Type comboBox items to Audio action types
//        Assert.assertEquals(Conditional.ACTION_PLAY_SOUND, Conditional.ITEM_TO_AUDIO_ACTION[0]);
//        Assert.assertEquals(Conditional.ACTION_CONTROL_AUDIO, Conditional.ITEM_TO_AUDIO_ACTION[1]);

        // Map Script Type comboBox items to Script action types
        Assert.assertEquals(Conditional.Action.RUN_SCRIPT, Conditional.Action.getScriptItems().get(0));
        Assert.assertEquals(Conditional.Action.JYTHON_COMMAND, Conditional.Action.getScriptItems().get(1));

        // Map Script Type comboBox items to Script action types
//        Assert.assertEquals(Conditional.ACTION_RUN_SCRIPT, Conditional.ITEM_TO_SCRIPT_ACTION[0]);
//        Assert.assertEquals(Conditional.ACTION_JYTHON_COMMAND, Conditional.ITEM_TO_SCRIPT_ACTION[1]);

        // Map EntryExit Type comboBox items to EntryExit action types
        Assert.assertEquals(Conditional.Action.SET_NXPAIR_ENABLED, Conditional.Action.getEntryExitItems().get(0));
        Assert.assertEquals(Conditional.Action.SET_NXPAIR_DISABLED, Conditional.Action.getEntryExitItems().get(1));
        Assert.assertEquals(Conditional.Action.SET_NXPAIR_SEGMENT, Conditional.Action.getEntryExitItems().get(2));

        // Map EntryExit Type comboBox items to EntryExit action types
//        Assert.assertEquals(Conditional.ACTION_SET_NXPAIR_ENABLED, Conditional.ITEM_TO_ENTRYEXIT_ACTION[0]);
//        Assert.assertEquals(Conditional.ACTION_SET_NXPAIR_DISABLED, Conditional.ITEM_TO_ENTRYEXIT_ACTION[1]);
//        Assert.assertEquals(Conditional.ACTION_SET_NXPAIR_SEGMENT, Conditional.ITEM_TO_ENTRYEXIT_ACTION[2]);

        // Map Misc Type comboBox items to Misc action types
        Assert.assertEquals(Conditional.Action.TRIGGER_ROUTE, Conditional.Action.getOtherItems().get(0));

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

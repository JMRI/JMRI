package jmri;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.ResourceBundle;

/**
 * A Conditional is layout control logic, consisting of a logical expression and
 * an action.
 * <p>
 * A Conditional does not exist on its own, but is part of a Logix. The system
 * name of each Conditional is set automatically when the conditional is
 * created. It begins with the system name of its parent Logix. There is no
 * Conditional Table. Conditionals are created, editted, and deleted via the
 * Logix Table.
 * <p>
 * A Conditional has a "state", which changes depending on whether its logical
 * expression calculates to TRUE or FALSE. The "state" may not be changed by the
 * user. It only changes in response to changes in the "state variables" used in
 * its logical expression.
 * <p>
 * Listeners may be set to monitor a change in the state of a conditional.
 *
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * @author Dave Duchamp Copyright (C) 2007, 2008
 * @author Pete Cressman Copyright (C) 2009, 2010, 2011
 * @author Matthew Harris copyright (c) 2009
 */
public interface Conditional extends NamedBean {

    static final ResourceBundle rbx = ResourceBundle.getBundle("jmri.jmrit.conditional.ConditionalBundle");
    
    // states
    enum State {
        UNKNOWN(NamedBean.UNKNOWN, "StateUnknown"),
        FALSE(Conditional.FALSE, "StateFalse"),
        TRUE(Conditional.TRUE, "StateTrue");
        
        private final int _state;
        private final String _bundleKey;
        
        private State(int state, String bundleKey) {
            _state = state;
            _bundleKey = bundleKey;
        }
        
        public int getIntValue() {
            return _state;
        }
        
        public static State getOperatorFromIntValue(int stateInt) {
            for (State state : State.values()) {
                if (state.getIntValue() == stateInt) {
                    return state;
                }
            }
            
            throw new IllegalArgumentException("State is unknown");
        }

        @Override
        public String toString() {
            return Bundle.getMessage(_bundleKey);
        }
    }
    
    /**
     * @deprecated since 4.7.1; use {@link jmri.NamedBean#UNKNOWN} instead
     */
    @Deprecated
    static final int UNKNOWN = NamedBean.UNKNOWN;
    static final int FALSE = 0x02;
    static final int TRUE = 0x04;

    // logic operators used in antecedent
    static final int ALL_AND = 0x01;
    static final int ALL_OR = 0x02;
    static final int MIXED = 0x03;

    public enum Operator {
        NONE,
        AND,
        OR;
        
        // This method is used by DefaultConditionalManagerXml.store() for backward compatibility
        public int getIntValue() {
            switch (this) {
                case NONE: return OPERATOR_NONE;
                case AND: return OPERATOR_AND;
                case OR: return OPERATOR_OR;
                default: throw new IllegalArgumentException(String.format("operator %s is unknown", this.name()));
            }
        }
        
        // This method is used by DefaultConditionalManagerXml.loadConditionals() for backward compatibility
        public static Operator getOperatorFromIntValue(int opern) {
            switch (opern) {
                case OPERATOR_AND: return Operator.AND;
                case OPERATOR_NOT: return Operator.NONE;
                case OPERATOR_AND_NOT: return Operator.AND;
                case OPERATOR_NONE: return Operator.NONE;
                case OPERATOR_OR: return Operator.OR;
                case OPERATOR_OR_NOT: return Operator.OR;
                default: throw new IllegalArgumentException(String.format("operator %d is unknown", opern));
            }
        }
    }
    
    // state variable definitions. Keep these since they are needed
    // for backward compatibility in DefaultConditionalManagerXml.
    // But they are not used elsewhere.
    static final int OPERATOR_AND = 1;
    static final int OPERATOR_NONE = 4;
    static final int OPERATOR_OR = 5;
    /**
     * @deprecated since 4.13.4; It is not stored in the XML file since 4.13.4.
     */
    @Deprecated
    static final int OPERATOR_NOT = 2;
    /**
     * @deprecated since 4.13.4; It is not stored in the XML file since 4.13.4.
     */
    @Deprecated
    static final int OPERATOR_AND_NOT = 3;
    /**
     * @deprecated since 4.13.4; It is not stored in the XML file since 4.13.4.
     */
    @Deprecated
    static final int OPERATOR_OR_NOT = 6;
    
    // state variable and action items used by logix.
    enum ItemType {
        NONE(TYPE_NONE, IsStateVar.IS_STATE_VAR, "ItemTypeNone"),        // There is no ITEM_TYPE_NONE so use TYPE_NONE instead
        SENSOR(ITEM_TYPE_SENSOR, IsStateVar.IS_STATE_VAR, "ItemTypeSensor"),
        TURNOUT(ITEM_TYPE_TURNOUT, IsStateVar.IS_STATE_VAR, "ItemTypeTurnout"),
        LIGHT(ITEM_TYPE_LIGHT, IsStateVar.IS_STATE_VAR, "ItemTypeLight"),
        SIGNALHEAD(ITEM_TYPE_SIGNALHEAD, IsStateVar.IS_STATE_VAR, "ItemTypeSignalHead"),
        SIGNALMAST(ITEM_TYPE_SIGNALMAST, IsStateVar.IS_STATE_VAR, "ItemTypeSignalMast"),
        MEMORY(ITEM_TYPE_MEMORY, IsStateVar.IS_STATE_VAR, "ItemTypeMemory"),
        CONDITIONAL(ITEM_TYPE_CONDITIONAL, IsStateVar.IS_STATE_VAR, "ItemTypeConditional"),  // used only by ConditionalVariable
        LOGIX(ITEM_TYPE_LOGIX, IsStateVar.IS_STATE_VAR, "ItemTypeLogix"),                    // used only by ConditionalAction
        WARRANT(ITEM_TYPE_WARRANT, IsStateVar.IS_STATE_VAR, "ItemTypeWarrant"),
        CLOCK(ITEM_TYPE_CLOCK, IsStateVar.IS_STATE_VAR, "ItemTypeClock"),
        OBLOCK(ITEM_TYPE_OBLOCK, IsStateVar.IS_STATE_VAR, "ItemTypeOBlock"),
        ENTRYEXIT(ITEM_TYPE_ENTRYEXIT, IsStateVar.IS_STATE_VAR, "ItemTypeEntryExit"),
//        LAST_STATE_VAR(ITEM_TYPE_LAST_STATE_VAR, IsStateVar.IS_STATE_VAR, "ItemTypeStateVar"),

        AUDIO(ITEM_TYPE_AUDIO, IsStateVar.IS_NOT_STATE_VAR, "ItemTypeAudio"),
        SCRIPT(ITEM_TYPE_SCRIPT, IsStateVar.IS_NOT_STATE_VAR, "ItemTypeScript"),
        OTHER(ITEM_TYPE_OTHER, IsStateVar.IS_NOT_STATE_VAR, "ItemTypeOther");
//        LAST_ACTION(ITEM_TYPE_LAST_ACTION, "ItemTypeLastAction");
        
        private final int _type;
        private IsStateVar _isStateVar;
        private final String _bundleKey;
        
        private static final List<ItemType> stateVarList;
        
        static
        {
            stateVarList = new ArrayList<>();
            
            for (ItemType itemType : ItemType.values()) {
                if (itemType._isStateVar == IsStateVar.IS_STATE_VAR) {
                    stateVarList.add(itemType);
                }
            }
        }
        
        private ItemType(int type, IsStateVar isStateVar, String bundleKey) {
            _type = type;
            _isStateVar = isStateVar;
            _bundleKey = bundleKey;
        }
        
        public static List<ItemType> getStateVarList() {
            return stateVarList;
        }
        
        public int getIntValue() {
            return _type;
        }
        
        public static ItemType getOperatorFromIntValue(int itemTypeInt) {
            for (ItemType itemType : ItemType.values()) {
                if (itemType.getIntValue() == itemTypeInt) {
                    return itemType;
                }
            }
            
            throw new IllegalArgumentException("ItemType is unknown");
        }

        @Override
        public String toString() {
            return Bundle.getMessage(_bundleKey);
        }
        
        // This enum is only used within the outer enum ItemType.
        private enum IsStateVar {
            IS_STATE_VAR,
            IS_NOT_STATE_VAR
        }
    }
    
    // items
    enum Type {
        ERROR(TYPE_ERROR, ItemType.NONE, "", ""), // NOI18N
        NONE(TYPE_NONE, ItemType.NONE, "", ""), // NOI18N
        SENSOR_ACTIVE(TYPE_SENSOR_ACTIVE, ItemType.SENSOR, Bundle.getMessage("SensorStateActive"), rbx.getString("TypeSensorActive")), // NOI18N
        SENSOR_INACTIVE(TYPE_SENSOR_INACTIVE, ItemType.SENSOR, Bundle.getMessage("SensorStateInactive"), rbx.getString("TypeSensorInactive")), // NOI18N
        TURNOUT_THROWN(TYPE_TURNOUT_THROWN, ItemType.TURNOUT, Bundle.getMessage("TurnoutStateThrown"), rbx.getString("TypeTurnoutThrown")), // NOI18N
        TURNOUT_CLOSED(TYPE_TURNOUT_CLOSED, ItemType.TURNOUT, Bundle.getMessage("TurnoutStateClosed"), rbx.getString("TypeTurnoutClosed")), // NOI18N
        CONDITIONAL_TRUE(TYPE_CONDITIONAL_TRUE, ItemType.CONDITIONAL, Bundle.getMessage("True"), rbx.getString("TypeConditionalTrue")), // NOI18N
        CONDITIONAL_FALSE(TYPE_CONDITIONAL_FALSE, ItemType.CONDITIONAL, Bundle.getMessage("False"), rbx.getString("TypeConditionalFalse")), // NOI18N
        LIGHT_ON(TYPE_LIGHT_ON, ItemType.LIGHT, rbx.getString("LightOn"), rbx.getString("TypeLightOn")), // NOI18N
        LIGHT_OFF(TYPE_LIGHT_OFF, ItemType.LIGHT, rbx.getString("LightOff"), rbx.getString("TypeLightOff")), // NOI18N
        MEMORY_EQUALS(TYPE_MEMORY_EQUALS, ItemType.MEMORY, rbx.getString("StateMemoryEquals"), rbx.getString("TypeMemoryEquals")), // NOI18N
        MEMORY_COMPARE(TYPE_MEMORY_COMPARE, ItemType.MEMORY, rbx.getString("StateMemoryCompare"), rbx.getString("TypeMemoryCompare")), // NOI18N
        MEMORY_EQUALS_INSENSITIVE(TYPE_MEMORY_EQUALS_INSENSITIVE, ItemType.MEMORY, rbx.getString("StateMemoryEqualsInsensitive"), rbx.getString("TypeMemoryEqualsInsensitive")), // NOI18N
        MEMORY_COMPARE_INSENSITIVE(TYPE_MEMORY_COMPARE_INSENSITIVE, ItemType.MEMORY, rbx.getString("StateMemoryCompareInsensitive"), rbx.getString("TypeMemoryCompareInsensitive")), // NOI18N
        FAST_CLOCK_RANGE(TYPE_FAST_CLOCK_RANGE, ItemType.CLOCK, rbx.getString("TypeFastClockRange"), rbx.getString("TypeFastClockRange")), // NOI18N
        
        // Note the set signalHeadAppearanceSet below which holds those SignalHead types that are appearances.
        SIGNAL_HEAD_RED(TYPE_SIGNAL_HEAD_RED, ItemType.SIGNALHEAD, Bundle.getMessage("SignalHeadStateRed"), Bundle.getMessage("SignalHeadStateRed")), // NOI18N
        SIGNAL_HEAD_YELLOW(TYPE_SIGNAL_HEAD_YELLOW, ItemType.SIGNALHEAD, Bundle.getMessage("SignalHeadStateYellow"), Bundle.getMessage("SignalHeadStateYellow")), // NOI18N
        SIGNAL_HEAD_GREEN(TYPE_SIGNAL_HEAD_GREEN, ItemType.SIGNALHEAD, Bundle.getMessage("SignalHeadStateGreen"), Bundle.getMessage("SignalHeadStateGreen")), // NOI18N
        SIGNAL_HEAD_DARK(TYPE_SIGNAL_HEAD_DARK, ItemType.SIGNALHEAD, Bundle.getMessage("SignalHeadStateDark"), Bundle.getMessage("SignalHeadStateDark")), // NOI18N
        SIGNAL_HEAD_FLASHRED(TYPE_SIGNAL_HEAD_FLASHRED, ItemType.SIGNALHEAD, Bundle.getMessage("SignalHeadStateFlashingRed"), Bundle.getMessage("SignalHeadStateFlashingRed")), // NOI18N
        SIGNAL_HEAD_FLASHYELLOW(TYPE_SIGNAL_HEAD_FLASHYELLOW, ItemType.SIGNALHEAD, Bundle.getMessage("SignalHeadStateFlashingYellow"), Bundle.getMessage("SignalHeadStateFlashingYellow")), // NOI18N
        SIGNAL_HEAD_FLASHGREEN(TYPE_SIGNAL_HEAD_FLASHGREEN, ItemType.SIGNALHEAD, Bundle.getMessage("SignalHeadStateFlashingGreen"), Bundle.getMessage("SignalHeadStateFlashingGreen")), // NOI18N
        SIGNAL_HEAD_LIT(TYPE_SIGNAL_HEAD_LIT, ItemType.SIGNALHEAD, Bundle.getMessage("SignalHeadStateLit"), Bundle.getMessage("SignalHeadStateLit")), // NOI18N
        SIGNAL_HEAD_HELD(TYPE_SIGNAL_HEAD_HELD, ItemType.SIGNALHEAD, Bundle.getMessage("SignalHeadStateHeld"), Bundle.getMessage("SignalHeadStateHeld")), // NOI18N
        SIGNAL_HEAD_LUNAR(TYPE_SIGNAL_HEAD_LUNAR, ItemType.SIGNALHEAD, Bundle.getMessage("SignalHeadStateLunar"), Bundle.getMessage("SignalHeadStateLunar")), // NOI18N
        SIGNAL_HEAD_FLASHLUNAR(TYPE_SIGNAL_HEAD_FLASHLUNAR, ItemType.SIGNALHEAD, Bundle.getMessage("SignalHeadStateFlashingLunar"), Bundle.getMessage("SignalHeadStateFlashingLunar")), // NOI18N
        // Warrant variables
        ROUTE_FREE(TYPE_ROUTE_FREE, ItemType.WARRANT, rbx.getString("StateRouteFree"), rbx.getString("TypeWarrantRouteFree")), // NOI18N
        ROUTE_OCCUPIED(TYPE_ROUTE_OCCUPIED, ItemType.WARRANT, rbx.getString("stateRouteOccupied"), rbx.getString("TypeWarrantRouteOccupied")), // NOI18N
        ROUTE_ALLOCATED(TYPE_ROUTE_ALLOCATED, ItemType.WARRANT, rbx.getString("StateRouteReserved"), rbx.getString("TypeWarrantRouteAllocated")), // NOI18N
        ROUTE_SET(TYPE_ROUTE_SET, ItemType.WARRANT, rbx.getString("StateRouteIsSet"), rbx.getString("TypeRouteIsSet")), // NOI18N
        TRAIN_RUNNING(TYPE_TRAIN_RUNNING, ItemType.WARRANT, rbx.getString("StateTrainRunning"), rbx.getString("TypeTrainRunning")), // NOI18N
        SIGNAL_MAST_ASPECT_EQUALS(TYPE_SIGNAL_MAST_ASPECT_EQUALS, ItemType.SIGNALMAST, rbx.getString("TypeSignalMastAspectEquals"), rbx.getString("TypeSignalMastAspectEquals")), // NOI18N
        SIGNAL_MAST_LIT(TYPE_SIGNAL_MAST_LIT, ItemType.SIGNALMAST, Bundle.getMessage("SignalMastStateLit"), Bundle.getMessage("SignalMastStateLit")), // NOI18N
        SIGNAL_MAST_HELD(TYPE_SIGNAL_MAST_HELD, ItemType.SIGNALMAST, Bundle.getMessage("SignalMastStateHeld"), Bundle.getMessage("SignalMastStateHeld")), // NOI18N
        SIGNAL_HEAD_APPEARANCE_EQUALS(TYPE_SIGNAL_HEAD_APPEARANCE_EQUALS, ItemType.SIGNALHEAD, rbx.getString("TypeSignalHeadAspectEquals"), rbx.getString("TypeSignalHeadAspectEquals")), // NOI18N
        BLOCK_STATUS_EQUALS(TYPE_BLOCK_STATUS_EQUALS, ItemType.OBLOCK, "", ""), // NOI18N
        //Entry Exit Rules
        ENTRYEXIT_ACTIVE(TYPE_ENTRYEXIT_ACTIVE, ItemType.ENTRYEXIT, Bundle.getMessage("SensorStateActive"), rbx.getString("TypeEntryExitActive")), // NOI18N
        ENTRYEXIT_INACTIVE(TYPE_ENTRYEXIT_INACTIVE, ItemType.ENTRYEXIT, Bundle.getMessage("SensorStateInactive"), rbx.getString("TypeEntryExitInactive")); // NOI18N

        private final int _item;
        private final ItemType _itemType;
        private final String _string;
        private final String _testTypeString;
        
        private static final List<Type> sensorItemsList;
        private static final List<Type> turnoutItemsList;
        private static final List<Type> conditionalItemsList;
        private static final List<Type> lightItemsList;
        private static final List<Type> warrantItemsList;
        private static final List<Type> memoryItemsList;
//        private static final List<Type> oblockItemsList;
        private static final List<Type> entryExitItemsList;
        private static final List<Type> signalHeadItemsList;
        private static final List<Type> signalMastItemsList;
        
        private static final Set<Type> signalHeadAppearanceSet;
        
        
        static
        {
            Type[] typeArray1 = {SENSOR_ACTIVE, SENSOR_INACTIVE};
            sensorItemsList = Collections.unmodifiableList(Arrays.asList(typeArray1));
            
            Type[] typeArray2 = {TURNOUT_THROWN, TURNOUT_CLOSED};
            turnoutItemsList = Collections.unmodifiableList(Arrays.asList(typeArray2));
            
            Type[] typeArray3 = {CONDITIONAL_TRUE, CONDITIONAL_FALSE};
            conditionalItemsList = Collections.unmodifiableList(Arrays.asList(typeArray3));
            
            Type[] typeArray4 = {LIGHT_ON, LIGHT_OFF};
            lightItemsList = Collections.unmodifiableList(Arrays.asList(typeArray4));
            
            Type[] typeArray5 = {ROUTE_FREE, ROUTE_SET, ROUTE_ALLOCATED, ROUTE_OCCUPIED, TRAIN_RUNNING};
            warrantItemsList = Collections.unmodifiableList(Arrays.asList(typeArray5));
            
            Type[] typeArray6 = {MEMORY_EQUALS, MEMORY_EQUALS_INSENSITIVE,
                MEMORY_COMPARE, MEMORY_COMPARE_INSENSITIVE};
            memoryItemsList = Collections.unmodifiableList(Arrays.asList(typeArray6));
            
            Type[] typeArray7 = {ENTRYEXIT_ACTIVE, ENTRYEXIT_INACTIVE};
            entryExitItemsList = Collections.unmodifiableList(Arrays.asList(typeArray7));
            
            Type[] typeArray8 = {NONE, SIGNAL_HEAD_APPEARANCE_EQUALS, SIGNAL_HEAD_LIT, SIGNAL_HEAD_HELD};
            signalHeadItemsList = Collections.unmodifiableList(Arrays.asList(typeArray8));
            
            Type[] typeArray9 = {SIGNAL_HEAD_RED, SIGNAL_HEAD_YELLOW, SIGNAL_HEAD_GREEN,
                SIGNAL_HEAD_DARK, SIGNAL_HEAD_FLASHRED, SIGNAL_HEAD_FLASHYELLOW,
                SIGNAL_HEAD_FLASHGREEN, SIGNAL_HEAD_LUNAR, SIGNAL_HEAD_FLASHLUNAR,
            };
            signalHeadAppearanceSet = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(typeArray9)));
            
            Type[] typeArray10 = {NONE, SIGNAL_MAST_ASPECT_EQUALS, SIGNAL_MAST_LIT, SIGNAL_MAST_HELD};
            signalMastItemsList = Collections.unmodifiableList(Arrays.asList(typeArray10));
        }
        
        private Type(int state, ItemType itemType, String string, String testTypeString) {
            _item = state;
            _itemType = itemType;
            _string = string;
            _testTypeString = testTypeString;
        }
        
        public ItemType getItemType() {
            return _itemType;
        }
        
        public int getIntValue() {
            return _item;
        }
        
        public static List<Type> getSensorItems() {
            return sensorItemsList;
        }
        
        public static List<Type> getTurnoutItems() {
            return turnoutItemsList;
        }
        
        public static List<Type> getConditionalItems() {
            return conditionalItemsList;
        }
        
        public static List<Type> getLightItems() {
            return lightItemsList;
        }
        
        public static List<Type> getWarrantItems() {
            return warrantItemsList;
        }
        
        public static List<Type> getMemoryItems() {
            return memoryItemsList;
        }
        
//        public static List<Type> getOBlockItems() {
//            return oblockItemsList;
//        }
        
        public static List<Type> getEntryExitItems() {
            return entryExitItemsList;
        }
        
        public static List<Type> getSignalHeadItems() {
            return signalHeadItemsList;
        }
        
        public static boolean isSignalHeadApperance(Type type) {
            return signalHeadAppearanceSet.contains(type);
        }
        
        public static List<Type> getSignalMastItems() {
            return signalMastItemsList;
        }
        
        public static int getIndexInList(List<Type> table, Type entry) {
            for (int i = 0; i < table.size(); i++) {
                if (entry == table.get(i)) {
                    return i;
                }
            }
            return -1;
        }
        
        public static Type getOperatorFromIntValue(int typeInt) {
            for (Type type : Type.values()) {
                if (type.getIntValue() == typeInt) {
                    return type;
                }
            }
            
            throw new IllegalArgumentException("Type is unknown");
        }

        // Some items uses Bundle.getString() and some items uses rbx.getString()
        // and therefore the items must call getString() in the call to the constructor.
        @Override
        public String toString() {
            return _string;
        }
        
        public String getTestTypeString() {
            return _testTypeString;
        }
    }
    
    // items
    enum ActionType {
        NONE(ACTION_NONE, ItemType.NONE, "aaa"), // NOI18N
        SET_TURNOUT(ACTION_SET_TURNOUT, ItemType.TURNOUT, "aaa"), // NOI18N
        // allowed settings for turnout are Thrown and Closed (in data)
        SET_SIGNAL_APPEARANCE(ACTION_SET_SIGNAL_APPEARANCE, ItemType.SIGNALHEAD, "aaa"), // NOI18N
        // allowed settings for signal head are the seven Appearances (in data)
        SET_SIGNAL_HELD(ACTION_SET_SIGNAL_HELD, ItemType.SIGNALHEAD, "aaa"), // NOI18N
        CLEAR_SIGNAL_HELD(ACTION_CLEAR_SIGNAL_HELD, ItemType.SIGNALHEAD, "aaa"), // NOI18N
        SET_SIGNAL_DARK(ACTION_SET_SIGNAL_DARK, ItemType.SIGNALHEAD, "aaa"), // NOI18N
        SET_SIGNAL_LIT(ACTION_SET_SIGNAL_LIT, ItemType.SIGNALHEAD, "aaa"), // NOI18N
        TRIGGER_ROUTE(ACTION_TRIGGER_ROUTE, ItemType.OTHER, "aaa"), // NOI18N
        SET_SENSOR(ACTION_SET_SENSOR, ItemType.SENSOR, "aaa"), // NOI18N
        // allowed settings for sensor are active and inactive (in data)
        DELAYED_SENSOR(ACTION_DELAYED_SENSOR, ItemType.SENSOR, "aaa"), // NOI18N
        // allowed settings for timed sensor are active and inactive (in data)
        //   time in seconds before setting sensor should be in delay
        SET_LIGHT(ACTION_SET_LIGHT, ItemType.LIGHT, "aaa"), // NOI18N
        // allowed settings for light are ON and OFF (in data)
        SET_MEMORY(ACTION_SET_MEMORY, ItemType.MEMORY, "aaa"), // NOI18N
        // text to set into the memory variable should be in string
        ENABLE_LOGIX(ACTION_ENABLE_LOGIX, ItemType.LOGIX, "aaa"), // NOI18N
        DISABLE_LOGIX(ACTION_DISABLE_LOGIX, ItemType.LOGIX, "aaa"), // NOI18N
        PLAY_SOUND(ACTION_PLAY_SOUND, ItemType.AUDIO, "aaa"), // NOI18N
        // reference to sound should be in string
        RUN_SCRIPT(ACTION_RUN_SCRIPT, ItemType.SCRIPT, "aaa"), // NOI18N
        // reference to script should be in string
        DELAYED_TURNOUT(ACTION_DELAYED_TURNOUT, ItemType.TURNOUT, "aaa"), // NOI18N
        // allowed settings for timed turnout are Thrown and Closed (in data)
        //   time in seconds before setting turnout should be in delay
        LOCK_TURNOUT(ACTION_LOCK_TURNOUT, ItemType.TURNOUT, "aaa"), // NOI18N
        RESET_DELAYED_SENSOR(ACTION_RESET_DELAYED_SENSOR, ItemType.SENSOR, "aaa"), // NOI18N
        // allowed settings for timed sensor are active and inactive (in data)
        //   time in seconds before setting sensor should be in delay
        CANCEL_SENSOR_TIMERS(ACTION_CANCEL_SENSOR_TIMERS, ItemType.SENSOR, "aaa"), // NOI18N
        // cancels all timers delaying setting of specified sensor
        RESET_DELAYED_TURNOUT(ACTION_RESET_DELAYED_TURNOUT, ItemType.TURNOUT, "aaa"), // NOI18N
        // allowed settings for timed sensor are active and inactive (in data)
        //   time in seconds before setting sensor should be in delay
        CANCEL_TURNOUT_TIMERS(ACTION_CANCEL_TURNOUT_TIMERS, ItemType.TURNOUT, "aaa"), // NOI18N
        // cancels all timers delaying setting of specified sensor
        SET_FAST_CLOCK_TIME(ACTION_SET_FAST_CLOCK_TIME, ItemType.CLOCK, "aaa"), // NOI18N
        // sets the fast clock time to the time specified
        START_FAST_CLOCK(ACTION_START_FAST_CLOCK, ItemType.CLOCK, "aaa"), // NOI18N
        // starts the fast clock
        STOP_FAST_CLOCK(ACTION_STOP_FAST_CLOCK, ItemType.CLOCK, "aaa"), // NOI18N
        // stops the fast clock
        COPY_MEMORY(ACTION_COPY_MEMORY, ItemType.MEMORY, "aaa"), // NOI18N
        // copies value from memory variable (in name) to memory variable (in string)
        SET_LIGHT_INTENSITY(ACTION_SET_LIGHT_INTENSITY, ItemType.LIGHT, "aaa"), // NOI18N
        SET_LIGHT_TRANSITION_TIME(ACTION_SET_LIGHT_TRANSITION_TIME, ItemType.LIGHT, "aaa"), // NOI18N
        // control the specified audio object
        CONTROL_AUDIO(ACTION_CONTROL_AUDIO, ItemType.AUDIO, "aaa"), // NOI18N
        // execute a jython command
        JYTHON_COMMAND(ACTION_JYTHON_COMMAND, ItemType.SCRIPT, "aaa"), // NOI18N
        // Warrant actions
        ALLOCATE_WARRANT_ROUTE(ACTION_ALLOCATE_WARRANT_ROUTE, ItemType.WARRANT, "aaa"), // NOI18N
        DEALLOCATE_WARRANT_ROUTE(ACTION_DEALLOCATE_WARRANT_ROUTE, ItemType.WARRANT, "aaa"), // NOI18N
        SET_ROUTE_TURNOUTS(ACTION_SET_ROUTE_TURNOUTS, ItemType.WARRANT, "aaa"), // NOI18N
        AUTO_RUN_WARRANT(ACTION_AUTO_RUN_WARRANT, ItemType.WARRANT, "aaa"), // NOI18N
        CONTROL_TRAIN(ACTION_CONTROL_TRAIN, ItemType.WARRANT, "aaa"), // NOI18N
        SET_TRAIN_ID(ACTION_SET_TRAIN_ID, ItemType.WARRANT, "aaa"), // NOI18N
        SET_SIGNALMAST_ASPECT(ACTION_SET_SIGNALMAST_ASPECT, ItemType.SIGNALMAST, "aaa"), // NOI18N
        THROTTLE_FACTOR(ACTION_THROTTLE_FACTOR, ItemType.WARRANT, "aaa"), // NOI18N
        SET_SIGNALMAST_HELD(ACTION_SET_SIGNALMAST_HELD, ItemType.SIGNALMAST, "aaa"), // NOI18N
        CLEAR_SIGNALMAST_HELD(ACTION_CLEAR_SIGNALMAST_HELD, ItemType.SIGNALMAST, "aaa"), // NOI18N
        SET_SIGNALMAST_DARK(ACTION_SET_SIGNALMAST_DARK, ItemType.SIGNALMAST, "aaa"), // NOI18N
        SET_SIGNALMAST_LIT(ACTION_SET_SIGNALMAST_LIT, ItemType.SIGNALMAST, "aaa"), // NOI18N
        SET_BLOCK_ERROR(ACTION_SET_BLOCK_ERROR, ItemType.OBLOCK, "aaa"), // NOI18N
        CLEAR_BLOCK_ERROR(ACTION_CLEAR_BLOCK_ERROR, ItemType.OBLOCK, "aaa"), // NOI18N
        DEALLOCATE_BLOCK(ACTION_DEALLOCATE_BLOCK, ItemType.OBLOCK, "aaa"), // NOI18N
        SET_BLOCK_OUT_OF_SERVICE(ACTION_SET_BLOCK_OUT_OF_SERVICE, ItemType.OBLOCK, "aaa"), // NOI18N
        SET_BLOCK_IN_SERVICE(ACTION_SET_BLOCK_IN_SERVICE, ItemType.OBLOCK, "aaa"), // NOI18N
        MANUAL_RUN_WARRANT(ACTION_MANUAL_RUN_WARRANT, ItemType.WARRANT, "aaa"), // NOI18N
        SET_TRAIN_NAME(ACTION_SET_TRAIN_NAME, ItemType.WARRANT, "aaa"), // NOI18N
        SET_BLOCK_VALUE(ACTION_SET_BLOCK_VALUE, ItemType.OBLOCK, "aaa"), // NOI18N
        // EntryExit Actions
        SET_NXPAIR_ENABLED(ACTION_SET_NXPAIR_ENABLED, ItemType.ENTRYEXIT, "aaa"), // NOI18N
        SET_NXPAIR_DISABLED(ACTION_SET_NXPAIR_DISABLED, ItemType.ENTRYEXIT, "aaa"), // NOI18N
        SET_NXPAIR_SEGMENT(ACTION_SET_NXPAIR_SEGMENT, ItemType.ENTRYEXIT, "aaa"); // NOI18N
//        static final int NUM_ACTION_TYPES(NUM_ACTION_TYPES, ItemType.NONE, "aaa"); // NOI18N
        
        private final int _item;
        private final ItemType _itemType;
        private final String _bundleKey;
/*        
        private static final List<Type> sensorItemsList;
        private static final List<Type> turnoutItemsList;
        private static final List<Type> conditionalItemsList;
        private static final List<Type> lightItemsList;
        private static final List<Type> warrantItemsList;
        private static final List<Type> memoryItemsList;
//        private static final List<Type> oblockItemsList;
        private static final List<Type> entryExitItemsList;
        private static final List<Type> signalHeadItemsList;
        private static final List<Type> signalMastItemsList;
        
        private static final Set<Type> signalHeadAppearanceSet;
        
        
        static
        {
            Type[] typeArray1 = {SENSOR_ACTIVE, SENSOR_INACTIVE};
            sensorItemsList = Collections.unmodifiableList(Arrays.asList(typeArray1));
            
            Type[] typeArray2 = {TURNOUT_THROWN, TURNOUT_CLOSED};
            turnoutItemsList = Collections.unmodifiableList(Arrays.asList(typeArray2));
            
            Type[] typeArray3 = {CONDITIONAL_TRUE, CONDITIONAL_FALSE};
            conditionalItemsList = Collections.unmodifiableList(Arrays.asList(typeArray3));
            
            Type[] typeArray4 = {LIGHT_ON, LIGHT_OFF};
            lightItemsList = Collections.unmodifiableList(Arrays.asList(typeArray4));
            
            Type[] typeArray5 = {ROUTE_FREE, ROUTE_SET, ROUTE_ALLOCATED, ROUTE_OCCUPIED, TRAIN_RUNNING};
            warrantItemsList = Collections.unmodifiableList(Arrays.asList(typeArray5));
            
            Type[] typeArray6 = {MEMORY_EQUALS, MEMORY_EQUALS_INSENSITIVE,
                MEMORY_COMPARE, MEMORY_COMPARE_INSENSITIVE};
            memoryItemsList = Collections.unmodifiableList(Arrays.asList(typeArray6));
            
            Type[] typeArray7 = {ENTRYEXIT_ACTIVE, ENTRYEXIT_INACTIVE};
            entryExitItemsList = Collections.unmodifiableList(Arrays.asList(typeArray7));
            
            Type[] typeArray8 = {NONE, SIGNAL_HEAD_APPEARANCE_EQUALS, SIGNAL_HEAD_LIT, SIGNAL_HEAD_HELD};
            signalHeadItemsList = Collections.unmodifiableList(Arrays.asList(typeArray8));
            
            Type[] typeArray9 = {SIGNAL_HEAD_RED, SIGNAL_HEAD_YELLOW, SIGNAL_HEAD_GREEN,
                SIGNAL_HEAD_DARK, SIGNAL_HEAD_FLASHRED, SIGNAL_HEAD_FLASHYELLOW,
                SIGNAL_HEAD_FLASHGREEN, SIGNAL_HEAD_LUNAR, SIGNAL_HEAD_FLASHLUNAR,
            };
            signalHeadAppearanceSet = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(typeArray9)));
            
            Type[] typeArray10 = {NONE, SIGNAL_MAST_ASPECT_EQUALS, SIGNAL_MAST_LIT, SIGNAL_MAST_HELD};
            signalMastItemsList = Collections.unmodifiableList(Arrays.asList(typeArray10));
        }
*/        
        private ActionType(int state, ItemType itemType, String bundleKey) {
            _item = state;
            _itemType = itemType;
            _bundleKey = bundleKey;
        }
        
        public ItemType getItemType() {
            return _itemType;
        }
        
        public int getIntValue() {
            return _item;
        }
/*        
        public static List<Type> getSensorItems() {
            return sensorItemsList;
        }
        
        public static List<Type> getTurnoutItems() {
            return turnoutItemsList;
        }
        
        public static List<Type> getConditionalItems() {
            return conditionalItemsList;
        }
        
        public static List<Type> getLightItems() {
            return lightItemsList;
        }
        
        public static List<Type> getWarrantItems() {
            return warrantItemsList;
        }
        
        public static List<Type> getMemoryItems() {
            return memoryItemsList;
        }
        
//        public static List<Type> getOBlockItems() {
//            return oblockItemsList;
//        }
        
        public static List<Type> getEntryExitItems() {
            return entryExitItemsList;
        }
        
        public static List<Type> getSignalHeadItems() {
            return signalHeadItemsList;
        }
        
        public static boolean isSignalHeadApperance(Type type) {
            return signalHeadAppearanceSet.contains(type);
        }
        
        public static List<Type> getSignalMastItems() {
            return signalMastItemsList;
        }
*/        
        public static int getIndexInList(List<ActionType> table, ActionType entry) {
            for (int i = 0; i < table.size(); i++) {
                if (entry == table.get(i)) {
                    return i;
                }
            }
            return -1;
        }
        
        public static ActionType getOperatorFromIntValue(int actionInt) {
            for (ActionType action : ActionType.values()) {
                if (action.getIntValue() == actionInt) {
                    return action;
                }
            }
            
            throw new IllegalArgumentException("Action is unknown");
        }

        // Some items uses Bundle.getString() and some items uses rbx.getString()
        // and therefore the items must call getString() in the call to the constructor.
        @Override
        public String toString() {
            return Bundle.getMessage(_bundleKey);
        }
    }

    // state variable types
    static final int TYPE_ERROR = -1;
    static final int TYPE_NONE = 0;
    static final int TYPE_SENSOR_ACTIVE = 1;
    static final int TYPE_SENSOR_INACTIVE = 2;
    static final int TYPE_TURNOUT_THROWN = 3;
    static final int TYPE_TURNOUT_CLOSED = 4;
    static final int TYPE_CONDITIONAL_TRUE = 5;
    static final int TYPE_CONDITIONAL_FALSE = 6;
    static final int TYPE_LIGHT_ON = 7;
    static final int TYPE_LIGHT_OFF = 8;
    static final int TYPE_MEMORY_EQUALS = 9;
    static final int TYPE_FAST_CLOCK_RANGE = 10;
// Note - within the TYPE_SIGNAL_HEAD definitions, all must be together,
//  RED must be first, and HELD must be last
    static final int TYPE_SIGNAL_HEAD_RED = 11;
    static final int TYPE_SIGNAL_HEAD_YELLOW = 12;
    static final int TYPE_SIGNAL_HEAD_GREEN = 13;
    static final int TYPE_SIGNAL_HEAD_DARK = 14;
    static final int TYPE_SIGNAL_HEAD_FLASHRED = 15;
    static final int TYPE_SIGNAL_HEAD_FLASHYELLOW = 16;
    static final int TYPE_SIGNAL_HEAD_FLASHGREEN = 17;
    static final int TYPE_SIGNAL_HEAD_LIT = 18;
    static final int TYPE_SIGNAL_HEAD_HELD = 19;
    static final int TYPE_MEMORY_COMPARE = 20;
    static final int TYPE_SIGNAL_HEAD_LUNAR = 21;
    static final int TYPE_SIGNAL_HEAD_FLASHLUNAR = 22;
    static final int TYPE_MEMORY_EQUALS_INSENSITIVE = 23;
    static final int TYPE_MEMORY_COMPARE_INSENSITIVE = 24;
    // Warrant variables
    static final int TYPE_ROUTE_FREE = 25;
    static final int TYPE_ROUTE_OCCUPIED = 26;
    static final int TYPE_ROUTE_ALLOCATED = 27;
    static final int TYPE_ROUTE_SET = 28;
    static final int TYPE_TRAIN_RUNNING = 29;
    static final int TYPE_SIGNAL_MAST_ASPECT_EQUALS = 30;
    static final int TYPE_SIGNAL_MAST_LIT = 31;
    static final int TYPE_SIGNAL_MAST_HELD = 32;
    static final int TYPE_SIGNAL_HEAD_APPEARANCE_EQUALS = 33;
    
    static final int TYPE_BLOCK_STATUS_EQUALS = 34;

    //Entry Exit Rules
    static final int TYPE_ENTRYEXIT_ACTIVE = 35;
    static final int TYPE_ENTRYEXIT_INACTIVE = 36;

    // action definitions
    static final int ACTION_OPTION_ON_CHANGE_TO_TRUE = 1;
    static final int ACTION_OPTION_ON_CHANGE_TO_FALSE = 2;
    static final int ACTION_OPTION_ON_CHANGE = 3;
    static final int NUM_ACTION_OPTIONS = 3;

    // action types
    static final int ACTION_NONE = 1;
    static final int ACTION_SET_TURNOUT = 2;
    // allowed settings for turnout are Thrown and Closed (in data)
    static final int ACTION_SET_SIGNAL_APPEARANCE = 3;
    // allowed settings for signal head are the seven Appearances (in data)
    static final int ACTION_SET_SIGNAL_HELD = 4;
    static final int ACTION_CLEAR_SIGNAL_HELD = 5;
    static final int ACTION_SET_SIGNAL_DARK = 6;
    static final int ACTION_SET_SIGNAL_LIT = 7;
    static final int ACTION_TRIGGER_ROUTE = 8;
    static final int ACTION_SET_SENSOR = 9;
    // allowed settings for sensor are active and inactive (in data)
    static final int ACTION_DELAYED_SENSOR = 10;
    // allowed settings for timed sensor are active and inactive (in data)
    //   time in seconds before setting sensor should be in delay
    static final int ACTION_SET_LIGHT = 11;
    // allowed settings for light are ON and OFF (in data)
    static final int ACTION_SET_MEMORY = 12;
    // text to set into the memory variable should be in string
    static final int ACTION_ENABLE_LOGIX = 13;
    static final int ACTION_DISABLE_LOGIX = 14;
    static final int ACTION_PLAY_SOUND = 15;
    // reference to sound should be in string
    static final int ACTION_RUN_SCRIPT = 16;
    // reference to script should be in string
    static final int ACTION_DELAYED_TURNOUT = 17;
    // allowed settings for timed turnout are Thrown and Closed (in data)
    //   time in seconds before setting turnout should be in delay
    static final int ACTION_LOCK_TURNOUT = 18;
    static final int ACTION_RESET_DELAYED_SENSOR = 19;
    // allowed settings for timed sensor are active and inactive (in data)
    //   time in seconds before setting sensor should be in delay
    static final int ACTION_CANCEL_SENSOR_TIMERS = 20;
    // cancels all timers delaying setting of specified sensor
    static final int ACTION_RESET_DELAYED_TURNOUT = 21;
    // allowed settings for timed sensor are active and inactive (in data)
    //   time in seconds before setting sensor should be in delay
    static final int ACTION_CANCEL_TURNOUT_TIMERS = 22;
    // cancels all timers delaying setting of specified sensor
    static final int ACTION_SET_FAST_CLOCK_TIME = 23;
    // sets the fast clock time to the time specified
    static final int ACTION_START_FAST_CLOCK = 24;
    // starts the fast clock
    static final int ACTION_STOP_FAST_CLOCK = 25;
    // stops the fast clock
    static final int ACTION_COPY_MEMORY = 26;
    // copies value from memory variable (in name) to memory variable (in string)
    static final int ACTION_SET_LIGHT_INTENSITY = 27;
    static final int ACTION_SET_LIGHT_TRANSITION_TIME = 28;
    // control the specified audio object
    static final int ACTION_CONTROL_AUDIO = 29;
    // execute a jython command
    static final int ACTION_JYTHON_COMMAND = 30;
    // Warrant actions
    static final int ACTION_ALLOCATE_WARRANT_ROUTE = 31;
    static final int ACTION_DEALLOCATE_WARRANT_ROUTE = 32;
    static final int ACTION_SET_ROUTE_TURNOUTS = 33;
    static final int ACTION_AUTO_RUN_WARRANT = 34;
    static final int ACTION_CONTROL_TRAIN = 35;
    static final int ACTION_SET_TRAIN_ID = 36;
    static final int ACTION_SET_SIGNALMAST_ASPECT = 37;
    static final int ACTION_THROTTLE_FACTOR = 38;
    static final int ACTION_SET_SIGNALMAST_HELD = 39;
    static final int ACTION_CLEAR_SIGNALMAST_HELD = 40;
    static final int ACTION_SET_SIGNALMAST_DARK = 41;
    static final int ACTION_SET_SIGNALMAST_LIT = 42;
    static final int ACTION_SET_BLOCK_ERROR = 43;
    static final int ACTION_CLEAR_BLOCK_ERROR = 44;
    static final int ACTION_DEALLOCATE_BLOCK = 45;
    static final int ACTION_SET_BLOCK_OUT_OF_SERVICE = 46;
    static final int ACTION_SET_BLOCK_IN_SERVICE = 47;
    static final int ACTION_MANUAL_RUN_WARRANT = 48;
    static final int ACTION_SET_TRAIN_NAME = 49;
    static final int ACTION_SET_BLOCK_VALUE = 50;
    // EntryExit Actions
    static final int ACTION_SET_NXPAIR_ENABLED = 51;
    static final int ACTION_SET_NXPAIR_DISABLED = 52;
    static final int ACTION_SET_NXPAIR_SEGMENT = 53;
    static final int NUM_ACTION_TYPES = 53;

    /**
     * ***********************************************************************************
     */
    /* New Variable and ActionType type scheme for Logix UI
     * State Variables and actions are grouped according to type.  Variable and action
     * types share the following group categories:
     */
    // state variable and action items used by logix.
    static final int ITEM_TYPE_SENSOR = 1;
    static final int ITEM_TYPE_TURNOUT = 2;
    static final int ITEM_TYPE_LIGHT = 3;
    static final int ITEM_TYPE_SIGNALHEAD = 4;
    static final int ITEM_TYPE_SIGNALMAST = 5;
    static final int ITEM_TYPE_MEMORY = 6;
    static final int ITEM_TYPE_CONDITIONAL = 7;  // used only by ConditionalVariable
    static final int ITEM_TYPE_LOGIX = 7;        // used only by ConditionalAction
    static final int ITEM_TYPE_WARRANT = 8;
    static final int ITEM_TYPE_CLOCK = 9;
    static final int ITEM_TYPE_OBLOCK = 10;
    static final int ITEM_TYPE_ENTRYEXIT = 11;
//    static final int ITEM_TYPE_LAST_STATE_VAR = 11;

    static final int ITEM_TYPE_AUDIO = 12;
    static final int ITEM_TYPE_SCRIPT = 13;
    static final int ITEM_TYPE_OTHER = 14;
//    static final int ITEM_TYPE_LAST_ACTION = 14;

    /**
     * *************** ConditionalAction Maps *******************************
     */
    // Map action type to the item type
    @SuppressFBWarnings(value = "MS_MUTABLE_ARRAY") // with existing code structure, 
    // just have to accept these exposed
    // arrays. Someday...
    // WHAT IS EXPOSED IN A STATIC FINAL ARRAY 
    // OF STATIC FINAL ELEMENTS??
    public static final int[] ACTION_TO_ITEM = {TYPE_NONE,
        TYPE_NONE, // ACTION_NONE              1
        ITEM_TYPE_TURNOUT, // ACTION_SET_TURNOUT       2
        ITEM_TYPE_SIGNALHEAD, // ACTION_SET_SIGNAL_APPEARANCE 
        ITEM_TYPE_SIGNALHEAD, // ACTION_SET_SIGNAL_HELD   4
        ITEM_TYPE_SIGNALHEAD, // ACTION_CLEAR_SIGNAL_HELD 5
        ITEM_TYPE_SIGNALHEAD, // ACTION_SET_SIGNAL_DARK   6
        ITEM_TYPE_SIGNALHEAD, // ACTION_SET_SIGNAL_LIT    7
        ITEM_TYPE_OTHER, // ACTION_TRIGGER_ROUTE     8
        ITEM_TYPE_SENSOR, // ACTION_SET_SENSOR        9
        ITEM_TYPE_SENSOR, // ACTION_DELAYED_SENSOR    10
        ITEM_TYPE_LIGHT, // ACTION_SET_LIGHT         11
        ITEM_TYPE_MEMORY, // ACTION_SET_MEMORY        12
        ITEM_TYPE_LOGIX, // ACTION_ENABLE_LOGIX      13
        ITEM_TYPE_LOGIX, // ACTION_DISABLE_LOGIX     14
        ITEM_TYPE_AUDIO, // ACTION_PLAY_SOUND        15
        ITEM_TYPE_SCRIPT, // ACTION_RUN_SCRIPT        16
        ITEM_TYPE_TURNOUT, // ACTION_DELAYED_TURNOUT   17
        ITEM_TYPE_TURNOUT, // ACTION_LOCK_TURNOUT      18
        ITEM_TYPE_SENSOR, // ACTION_RESET_DELAYED_SENSOR
        ITEM_TYPE_SENSOR, // ACTION_CANCEL_SENSOR_TIMERS 20
        ITEM_TYPE_TURNOUT, // ACTION_RESET_DELAYED_TURNOUT
        ITEM_TYPE_TURNOUT, // ACTION_CANCEL_TURNOUT_TIMERS
        ITEM_TYPE_CLOCK, // ACTION_SET_FAST_CLOCK_TIME 23
        ITEM_TYPE_CLOCK, // ACTION_START_FAST_CLOCK  24
        ITEM_TYPE_CLOCK, // ACTION_STOP_FAST_CLOCK   25
        ITEM_TYPE_MEMORY, // ACTION_COPY_MEMORY       26
        ITEM_TYPE_LIGHT, // ACTION_SET_LIGHT_INTENSITY 27
        ITEM_TYPE_LIGHT, // ACTION_SET_LIGHT_TRANSITION_TIME
        ITEM_TYPE_AUDIO, // ACTION_CONTROL_AUDIO     29
        ITEM_TYPE_SCRIPT, // ACTION_JYTHON_COMMAND    30
        ITEM_TYPE_WARRANT, // ACTION_ALLOCATE_WARRANT_ROUTE 31
        ITEM_TYPE_WARRANT, // ACTION_DEALLOCATE_WARRANT_ROUTE
        ITEM_TYPE_WARRANT, // ACTION_SET_ROUTE_TURNOUTS 33
        ITEM_TYPE_WARRANT, // ACTION_AUTO_RUN_WARRANT       34
        ITEM_TYPE_WARRANT, // ACTION_CONTROL_TRAIN     35
        ITEM_TYPE_WARRANT, // ACTION_SET_TRAIN_ID      36
        ITEM_TYPE_SIGNALMAST, // ACTION_SET_SIGNALMAST_ASPECT 37 
        ITEM_TYPE_WARRANT, // ACTION_THROTTLE_FACTOR   38
        ITEM_TYPE_SIGNALMAST, // ACTION_SET_SIGNALMAST_HELD = 39;
        ITEM_TYPE_SIGNALMAST, // ACTION_CLEAR_SIGNALMAST_HELD = 40 
        ITEM_TYPE_SIGNALMAST, // ACTION_SET_SIGNALMAST_DARK = 41
        ITEM_TYPE_SIGNALMAST, // ACTION_SET_SIGNALMAST_LIT = 42
        ITEM_TYPE_OBLOCK, // ACTION_SET_BLOCK_ERROR = 43;
        ITEM_TYPE_OBLOCK, //  ACTION_CLEAR_BLOCK_ERROR = 44;
        ITEM_TYPE_OBLOCK, //  ACTION_DEALLOCATE_BLOCK = 45;
        ITEM_TYPE_OBLOCK, //  ACTION_SET_BLOCK_OUT_OF_SERVICE = 46;
        ITEM_TYPE_OBLOCK, //  ACTION_SET_BLOCK_IN_SERVICE = 47;
        ITEM_TYPE_WARRANT, // ACTION_MANUAL_RUN_WARRANT 48
        ITEM_TYPE_WARRANT, // ACTION_SET_TRAIN_NAME 49
        ITEM_TYPE_OBLOCK, //ACTION_SET_BLOCK_VALUE 50
        ITEM_TYPE_ENTRYEXIT, //ACTION_SET_NXPAIR_ENABLED 51
        ITEM_TYPE_ENTRYEXIT, //ACTION_SET_NXPAIR_DISABLED 52
        ITEM_TYPE_ENTRYEXIT //ACTION_SET_NXPAIR_SEGMENT 53
};

    // Map Sensor Type comboBox items to Sensor action types
    @SuppressFBWarnings(value = "MS_MUTABLE_ARRAY") // with existing code structure, 
    // just have to accept these exposed
    // arrays. Someday...
    // WHAT IS EXPOSED IN A STATIC FINAL ARRAY 
    // OF STATIC FINAL ELEMENTS??
    public static final int[] ITEM_TO_SENSOR_ACTION = {ACTION_SET_SENSOR, ACTION_DELAYED_SENSOR,
        ACTION_RESET_DELAYED_SENSOR, ACTION_CANCEL_SENSOR_TIMERS};

    // Map Turnout Type comboBox items to Turnout action types
    @SuppressFBWarnings(value = "MS_MUTABLE_ARRAY") // with existing code structure, 
    // just have to accept these exposed
    // arrays. Someday...
    // WHAT IS EXPOSED IN A STATIC FINAL ARRAY 
    // OF STATIC FINAL ELEMENTS??
    public static final int[] ITEM_TO_TURNOUT_ACTION = {ACTION_SET_TURNOUT, ACTION_DELAYED_TURNOUT,
        ACTION_LOCK_TURNOUT, ACTION_CANCEL_TURNOUT_TIMERS, ACTION_RESET_DELAYED_TURNOUT};

    // Map Memory Type comboBox items to Memory action types
    @SuppressFBWarnings(value = "MS_MUTABLE_ARRAY") // with existing code structure, 
    // just have to accept these exposed
    // arrays. Someday...
    // WHAT IS EXPOSED IN A STATIC FINAL ARRAY 
    // OF STATIC FINAL ELEMENTS??
    public static final int[] ITEM_TO_MEMORY_ACTION = {12, 26};

    // Map Light Type comboBox items to Light action types
    @SuppressFBWarnings(value = "MS_MUTABLE_ARRAY") // with existing code structure, 
    // just have to accept these exposed
    // arrays. Someday...
    // WHAT IS EXPOSED IN A STATIC FINAL ARRAY 
    // OF STATIC FINAL ELEMENTS??
    public static final int[] ITEM_TO_LIGHT_ACTION = {ACTION_SET_LIGHT, ACTION_SET_LIGHT_INTENSITY,
        ACTION_SET_LIGHT_TRANSITION_TIME};

    // Map FastClock Type comboBox items to FastClock action types
    @SuppressFBWarnings(value = "MS_MUTABLE_ARRAY") // with existing code structure, 
    // just have to accept these exposed
    // arrays. Someday...
    // WHAT IS EXPOSED IN A STATIC FINAL ARRAY 
    // OF STATIC FINAL ELEMENTS??
    public static final int[] ITEM_TO_CLOCK_ACTION = {ACTION_SET_FAST_CLOCK_TIME,
        ACTION_START_FAST_CLOCK, ACTION_STOP_FAST_CLOCK};

    // Map Logix Type comboBox items to Logix action types
    @SuppressFBWarnings(value = "MS_MUTABLE_ARRAY") // with existing code structure, 
    // just have to accept these exposed
    // arrays. Someday...
    // WHAT IS EXPOSED IN A STATIC FINAL ARRAY 
    // OF STATIC FINAL ELEMENTS??
    public static final int[] ITEM_TO_LOGIX_ACTION = {ACTION_ENABLE_LOGIX, ACTION_DISABLE_LOGIX};

    // Map Warrant Type comboBox items to Warrant action types
    @SuppressFBWarnings(value = "MS_MUTABLE_ARRAY") // with existing code structure, 
    // just have to accept these exposed
    // arrays. Someday...
    // WHAT IS EXPOSED IN A STATIC FINAL ARRAY 
    // OF STATIC FINAL ELEMENTS??
    public final static int[] ITEM_TO_WARRANT_ACTION = {ACTION_ALLOCATE_WARRANT_ROUTE,
        ACTION_DEALLOCATE_WARRANT_ROUTE, ACTION_SET_ROUTE_TURNOUTS, ACTION_AUTO_RUN_WARRANT,
        ACTION_MANUAL_RUN_WARRANT, ACTION_CONTROL_TRAIN, ACTION_SET_TRAIN_ID,
        ACTION_SET_TRAIN_NAME, ACTION_THROTTLE_FACTOR};

    @SuppressFBWarnings(value = "MS_MUTABLE_ARRAY")
    public final static int[] ITEM_TO_OBLOCK_ACTION = {ACTION_DEALLOCATE_BLOCK,
        ACTION_SET_BLOCK_VALUE, ACTION_SET_BLOCK_ERROR, ACTION_CLEAR_BLOCK_ERROR,
        ACTION_SET_BLOCK_OUT_OF_SERVICE, ACTION_SET_BLOCK_IN_SERVICE};

    // Map Signal Head Type comboBox items to Signal Head action types
    @SuppressFBWarnings(value = "MS_MUTABLE_ARRAY") // with existing code structure, 
    // just have to accept these exposed
    // arrays. Someday...
    // WHAT IS EXPOSED IN A STATIC FINAL ARRAY 
    // OF STATIC FINAL ELEMENTS??
    public static final int[] ITEM_TO_SIGNAL_HEAD_ACTION = {ACTION_SET_SIGNAL_APPEARANCE,
        ACTION_SET_SIGNAL_HELD, ACTION_CLEAR_SIGNAL_HELD,
        ACTION_SET_SIGNAL_DARK, ACTION_SET_SIGNAL_LIT};

    // Map Signal Mast Type comboBox items to Signal Mast action types
    @SuppressFBWarnings(value = "MS_MUTABLE_ARRAY") // with existing code structure, 
    // just have to accept these exposed
    // arrays. Someday...
    // WHAT IS EXPOSED IN A STATIC FINAL ARRAY 
    // OF STATIC FINAL ELEMENTS??
    public static final int[] ITEM_TO_SIGNAL_MAST_ACTION = {ACTION_SET_SIGNALMAST_ASPECT,
        ACTION_SET_SIGNALMAST_HELD, ACTION_CLEAR_SIGNALMAST_HELD,
        ACTION_SET_SIGNALMAST_DARK, ACTION_SET_SIGNALMAST_LIT};

    // Map Audio Type comboBox items to Audio action types
    @SuppressFBWarnings(value = "MS_MUTABLE_ARRAY") // with existing code structure, 
    // just have to accept these exposed
    // arrays. Someday...
    // WHAT IS EXPOSED IN A STATIC FINAL ARRAY 
    // OF STATIC FINAL ELEMENTS??
    public static final int[] ITEM_TO_AUDIO_ACTION = {ACTION_PLAY_SOUND, ACTION_CONTROL_AUDIO};

    // Map Script Type comboBox items to Script action types
    @SuppressFBWarnings(value = "MS_MUTABLE_ARRAY") // with existing code structure, 
    // just have to accept these exposed
    // arrays. Someday...
    // WHAT IS EXPOSED IN A STATIC FINAL ARRAY 
    // OF STATIC FINAL ELEMENTS??
    public static final int[] ITEM_TO_SCRIPT_ACTION = {ACTION_RUN_SCRIPT, ACTION_JYTHON_COMMAND};

    // Map EntryExit Type comboBox items to EntryExit action types
    @SuppressFBWarnings(value = "MS_MUTABLE_ARRAY") // with existing code structure,
    // just have to accept these exposed
    // arrays. Someday...
    // WHAT IS EXPOSED IN A STATIC FINAL ARRAY
    // OF STATIC FINAL ELEMENTS??
    public static final int[] ITEM_TO_ENTRYEXIT_ACTION = {ACTION_SET_NXPAIR_ENABLED,
        ACTION_SET_NXPAIR_DISABLED, ACTION_SET_NXPAIR_SEGMENT};

    // Map Misc Type comboBox items to Misc action types
    @SuppressFBWarnings(value = "MS_MUTABLE_ARRAY") // with existing code structure, 
    // just have to accept these exposed
    // arrays. Someday...
    public static final int[] ITEM_TO_OTHER_ACTION = {ACTION_TRIGGER_ROUTE};

    /**
     * set the logic type (all AND's all OR's or mixed AND's and OR's set the
     * antecedent expression - should be a well formed boolean statement with
     * parenthesis indicating the order of evaluation
     *
     * @param type       the type
     * @param antecedent the expression
     */
    public void setLogicType(int type, String antecedent);

    /**
     * Get antecedent (boolean expression) of Conditional
     *
     * @return the expression
     */
    public String getAntecedentExpression();

    /**
     * Get type of operators in the antecedent statement
     *
     * @return the type
     */
    public int getLogicType();

    /**
     * @return true if action list is executed only when state changes, false if
     *         action list is executed on every calculation of state
     */
    public boolean getTriggerOnChange();

    /**
     * Set policy for execution of action list
     *
     * @param trigger true execute only on change of state
     */
    public void setTriggerOnChange(boolean trigger);

    /**
     * Set list of actions
     *
     * @param arrayList the actions
     */
    public void setAction(List<ConditionalAction> arrayList);

    /**
     * Make deep clone of actions
     *
     * @return a list of copies of actionss
     */
    public List<ConditionalAction> getCopyOfActions();

    /**
     * Set State Variables for this Conditional. Each state variable will
     * evaluate either True or False when this Conditional is calculated.
     * <p>
     * This method assumes that all information has been validated.
     *
     * @param arrayList the list of variables
     */
    public void setStateVariables(List<ConditionalVariable> arrayList);

    /**
     * Make deep clone of variables
     *
     * @return a list containing copies of variables
     */
    public List<ConditionalVariable> getCopyOfStateVariables();

    /**
     * Calculate this Conditional, triggering either or both actions if the user
     * specified conditions are met, and the Logix is enabled. Sets the state of
     * the conditional. Returns the calculated state of this Conditional.
     *
     * @param enabled true if Logix should be enabled; false otherwise
     * @param evt     event to trigger if true
     * @return the new state
     */
    public int calculate(boolean enabled, PropertyChangeEvent evt);

    /**
     * Check that an antecedent is well formed. If not, returns an error
     * message. Otherwise returns null.
     *
     * @param ant          the expression
     * @param variableList list of variables
     * @return true if well formed; false otherwise
     */
    public String validateAntecedent(String ant, List<ConditionalVariable> variableList);

    /**
     * Stop a sensor timer if one is actively delaying setting of the specified
     * sensor
     *
     * @param sname the name of the timer
     */
    public void cancelSensorTimer(String sname);

    /**
     * Stop a turnout timer if one is actively delaying setting of the specified
     * turnout
     *
     * @param sname the name of the timer
     */
    public void cancelTurnoutTimer(String sname);

    /**
     * State of the Conditional is returned.
     *
     * @return state value
     */
    @Override
    public int getState();

    /**
     * Request a call-back when the bound KnownState property changes.
     *
     * @param l the listener
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l);

    /**
     * Remove a request for a call-back when a bound property changes.
     *
     * @param l the listener
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener l);

    /**
     * Remove references to and from this object, so that it can eventually be
     * garbage-collected.
     */
    @Override
    public void dispose();  // remove _all_ connections!

}

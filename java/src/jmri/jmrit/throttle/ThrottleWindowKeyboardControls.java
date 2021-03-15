package jmri.jmrit.throttle;

import java.awt.event.KeyEvent;

/**
 *
 * @author Lionel Jeanson - 2021
 */
public class ThrottleWindowKeyboardControls {
    
    // moving through throttle windows
    static final int[] NEXT_THROTTLE_WINDOW_KEYS = { 
        KeyEvent.VK_INSERT 
    };
    static final int[] PREV_THROTTLE_WINDOW_KEYS = { 
        KeyEvent.VK_DELETE 
    };
    // moving through 1 throttle window frames
    static final int[] NEXT_THROTTLE_FRAME_KEYS = { 
        KeyEvent.VK_END 
    };
    static final int[] PREV_THROTTLE_FRAME_KEYS = { 
        KeyEvent.VK_HOME 
    };   
    
    // moving through internal windows
    static final int[] NEXT_THROTTLE_INTW_KEYS = { 
        KeyEvent.VK_K };
    static final int[] PREV_THROTTLE_INTW_KEYS = { 
        KeyEvent.VK_L };
    
    // select internal window
    static final int[] MOVE_TO_CONTROL_KEYS = { 
        KeyEvent.VK_C 
    };
    static final int[] MOVE_TO_FUNCTIONS_KEYS = { 
        KeyEvent.VK_F 
    };
    static final int[] MOVE_TO_ADDRESS_KEYS = { 
        KeyEvent.VK_A 
    };
    
    
    // Speed
    static final int[] REVERSE_KEYS = { 
        KeyEvent.VK_DOWN // Down arrow
    }; 
    static final int[] FORWARD_KEY = { 
        KeyEvent.VK_UP // Up arrow
    }; 
    
    static final int[] IDLE_KEYS = {
        KeyEvent.VK_MULTIPLY  // numpad *
    };
    static final int[] STOP_KEYS = {
        KeyEvent.VK_DIVIDE // numpad /
    };
    
    static final int[] ACCELERATE_KEYS = {
        KeyEvent.VK_ADD,  // numpad +
        KeyEvent.VK_LEFT
    };
    static final int[] DECELERATE_KEYS = {
        KeyEvent.VK_SUBTRACT, // numpad -;
        KeyEvent.VK_RIGHT
    };
    static final int[] ACCELERATEMORE_KEYS = {
        KeyEvent.VK_PAGE_UP 
    };
    static final int[] DECELERATEMORE_KEYS = {
        KeyEvent.VK_PAGE_DOWN
    };    
    
    // function buttons
    static final int FUNCTIONS_KEY[] = { 
        KeyEvent.VK_NUMPAD0, // F0
        KeyEvent.VK_NUMPAD1, // F1
        KeyEvent.VK_NUMPAD2, // F2
        KeyEvent.VK_NUMPAD3, // F3
        KeyEvent.VK_NUMPAD4, // F4
        KeyEvent.VK_NUMPAD5, // F5
        KeyEvent.VK_NUMPAD6, // F6
        KeyEvent.VK_NUMPAD7, // F7
        KeyEvent.VK_NUMPAD8, // F8
        KeyEvent.VK_NUMPAD9, // F9
        KeyEvent.VK_DECIMAL, // F10
        KeyEvent.VK_F11, // F11
        KeyEvent.VK_F12, // F12
        KeyEvent.VK_F13, // F13
        KeyEvent.VK_F14, // F14
        KeyEvent.VK_F15, // F15
        KeyEvent.VK_F16, // F16
        KeyEvent.VK_F17, // F17
        KeyEvent.VK_F18, // F18
        KeyEvent.VK_F19, // F19
        KeyEvent.VK_F20, // F20
        KeyEvent.VK_F21, // F21
        KeyEvent.VK_F22, // F22
        KeyEvent.VK_F23, // F23
        KeyEvent.VK_F24, // F24
        0xF00C, // F25
        0xF00D, // F26
        0xF00E, // F27
        0xF00F  // F28
    };

}

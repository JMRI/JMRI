/** 
 * ErrLog.java
 *
 * Description:		<describe the ErrLog class here>
 * @author			Bob Jacobsen
 * @version			
 */

package ErrLoggerJ;

import ErrLoggerJ.ErrLogStdImpl;

final public class ErrLog {
    // severity levels are defined; use only these values
    public static int debugging = -1;
    public static int trace 	 = 0;
    public static int routine	 = 1;
    public static int warning 	 = 2;
    public static int error	 = 3;
    public static int fatal	 = 4;
    
    //  fatal:          The message is related to a condition preventing
    //                  further execution of the program.  ErrLogger will
    //                  terminate the program.  Programmers should not call
    //                  abort or exit themselves.

    //  error:          A condition exists such that requested result
    //                  or action can not be produced.  This is a serious

    //  warning:        The result is produced, but may not be
    //                  what's desired due to an unexpected condition

    //  routine:        Nothing known to be wrong with the result;
    //                  messages that are always produced in normal
    //                  operation

    //  trace:          Messages about the flow of program control
    //                  and which optional operations took place.
    //                  (This is the default if nothing is defined)

    //  debugging:      Information in addition to the above
    

	// usual entry points to ErrLogger. In the C++ implementation, these are
	// cpp macros to get file information, etc.  In the current first Java
	// implementation, that information is not available.
	
	public static void msg(int severity, String text) {
		msg(severity, "<unknown>", "<unknown>", text);
	}
	
	public static boolean logging(int severity) {
		return logging(severity, "<unknown>", "<unknown>");
	}
	
	// static members for use of the logger. Not that, unlike the 
	// C++ implementation, the entire message is passed via the 
	// arguments instead of returning a stream to put data into.
	
	public static void msg(int severity, 
                String facility, 
                String code,
                String text) {
                	implementation.msg(severity, facility, code, text);
                }

    public static void msg(int severity, 
                String facility, 
                int code,
                String text) {
                	implementation.msg(severity, facility, code, text);
   				}

    public static boolean logging(int severity, 
                String facility, 
                String code) {
                	return implementation.logging(severity, facility, code);
    			} 

    public static boolean logging(int severity, 
                String facility, 
                int code) {
                	return implementation.logging(severity, facility, code);
        		}

    // utility to turn off previous error message warnings
    public static void turnOffWarnings() {  msg(error, "ErrLogger", "", "turnOffWarnings not implemented yet!");
    }  // !!

	// infrastructure for hooking up the real logger(s)
	public static void installHandler(ErrLogBase impl) {
		if (implementation != null) {
			// existing implementation being closed out
			msg(routine, "ErrLogger", "", "Existing ErrLogger implementation being closed out\n");
			}
		implementation = impl;
		msg(routine, "ErrLogger", "", "New ErrLogger implementation installed\n");

	}	

	private static ErrLogBase implementation = new ErrLogStdImpl();

}


/* @(#)ErrLog.java */

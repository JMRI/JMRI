/** 
 * ErrLogBase.java
 *
 * Description:		Base class for ErrLoggerJ implementations. Note that, unlike the
 					C++ package, this is an interface, not a subclass of the ErrLogger class.
 * @author			Bob Jacobsen
 * @version			
 */

package ErrLoggerJ;


interface ErrLogBase {
	abstract void msg(int severity, 
                String facility, 
                String code,
                String text);

    abstract void msg(int severity, 
                String facility, 
                int code,
                String text);

    abstract boolean logging(int severity, 
                String facility, 
                String code);

    abstract boolean logging(int severity, 
                String facility, 
                int code);
                
    // static data to improve logging speed
    static final String labels[] = {"debugging", "trace", "routine", "warning", "error", "fatal"};

}


/* @(#)ErrLogBase.java */

/** 
 * ErrLogStdImpl.java
 *
 * Description:		Implement ErrLogger, sending all messages to standard output
 * @author			Bob Jacobsen
 * @version			
 */

package ErrLoggerJ;


class ErrLogStdImpl extends ErrLogBase {
	void msg(int severity, 
                String facility, 
                String code,
                String text) {
          System.out.println("ErrLogger:"+labels[severity-ErrLog.debugging]+":"+facility+":"+code+":"+text);
          }

    void msg(int severity, 
                String facility, 
                int code,
                String text) {
          this.msg(severity, facility, String.valueOf(code), text);
          }

    boolean logging(int severity, 
                String facility, 
                String code) {
          return true;   // !!
          }

    boolean logging(int severity, 
                String facility, 
                int code){
          return true;   // !!
          }
         
}


/* @(#)ErrLogStdImpl.java */

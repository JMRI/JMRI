/** 
 * ErrLogStdImpl.java
 *
 * Description:		Implement ErrLogger, sending all messages to standard output
 * @author			Bob Jacobsen
 * @version			
 */

package ErrLoggerJ;


class ErrLogStdImpl implements ErrLogBase {
	public void msg(int severity, 
                String facility, 
                String code,
                String text) {
          System.out.println("ErrLogger:"+labels[severity-ErrLog.debugging]+":"+facility+":"+code+":"+text);
          }

    public void msg(int severity, 
                String facility, 
                int code,
                String text) {
          this.msg(severity, facility, String.valueOf(code), text);
          }

    public boolean logging(int severity, 
                String facility, 
                String code) {
          return true;   // !!
          }

    public boolean logging(int severity, 
                String facility, 
                int code){
          return true;   // !!
          }
         
}


/* @(#)ErrLogStdImpl.java */

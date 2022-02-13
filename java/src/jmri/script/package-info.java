/**
 * Provides JMRI's built in scripting support.
 * <p>
 * Note that this package is in flux.
 * <ul>
 *   <li>Pre-JMRI 5, this used Java's
 *      {@link javax.script} support, specifically
 *      {@link javax.script.ScriptEngineManager} and
 *      {@link javax.script.ScriptEngine} et al to provide
 *      <ul>
 *          <li>python support via Jython, and
 *          <li>Javascript support via the Nashorn engine.
 *      </ul>.
 *    <li>JMRI 5 brings the beginning of support for the next
 *        generation of scripting support, specifically through
 *        <a href="https://www.graalvm.org/">GraalVM</a>.
 *        This is being done as a first phase by wrapping
 *        the GraalVM Python 3 interpreter so that it's usable by the
 *        {@link jmri.script.JmriScriptEngineManager} class
 *        and the jmri.script.jsr223graalpython package.
 *        For more information on this, see the associated
 *        <a href="README-WIP.txt">README-WIP.md</a> file.
 * </ul>
 *
 */
// include empty DefaultAnnotation to avoid excessive recompilation
@edu.umd.cs.findbugs.annotations.DefaultAnnotation(value={})
package jmri.script;

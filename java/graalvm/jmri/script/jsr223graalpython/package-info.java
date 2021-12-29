/**
 * Provides a ScriptEngine JSR-223 implementation for GraalVM's Python support.
 * <p>
 * This was made in large part from the GraalJS implementation for JavaScript
 * <p>
 * It implements a (JSR-223) ScriptEnginer in terms of a (GraalVM) polyglot engine. This
 * includes mapping a ScriptContext to a Context.
 * <p>
 * Original source<ul>
 * <li>https://github.com/oracle/graaljs/blob/master/graal-js/src/com.oracle.truffle.js.scriptengine/src/com/oracle/truffle/js/scriptengine/GraalJSEngineFactory.java 30fcd78
 * <li>https://github.com/oracle/graaljs/blob/master/graal-js/src/com.oracle.truffle.js.scriptengine/src/com/oracle/truffle/js/scriptengine/GraalJSScriptEngine.java 9632cd5
 * <li>https://github.com/oracle/graaljs/blob/master/graal-js/src/com.oracle.truffle.js.scriptengine/src/com/oracle/truffle/js/scriptengine/GraalJSBindings.java 8c98f7b
 *</ul>As of 12/24/2021
 */
// include empty DefaultAnnotation to avoid excessive recompilation
@edu.umd.cs.findbugs.annotations.DefaultAnnotation(value={})
package jmri.script.jsr223graalpython;

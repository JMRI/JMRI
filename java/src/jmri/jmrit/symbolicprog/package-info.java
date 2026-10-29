/**
 * Basic support for advanced programming, primarily used by DecoderPro.
 * <p>
 * Variables and CVs track their states, as shown below.<br>
 * <a href="doc-files/VariableStates.png"><img src="doc-files/VariableStates.png" alt="States for variables and their colors" height="33%" width="33%"></a>
 *
 * @since 1.7.3
 */
// include empty DefaultAnnotation to avoid excessive recompilation
@edu.umd.cs.findbugs.annotations.DefaultAnnotation(value={})
package jmri.jmrit.symbolicprog;

/*
@startuml jmri/jmrit/symbolicprog/doc-files/VariableStates.png

state UNKNOWN #FF0000

state EDITED #ED8B00

state READ #FFFFFF

state STORED #FFFFFF

state FROMFILE #FFFF00

state FROMFILEUNKNOWN #FFFF00

state SAME #FFFFFF

state DIFFERENT #FF0000

state FROMDEFAULT #FFFF00

[*] --> FROMFILE : Previously\nRead/Written/Edited
[*] --> FROMFILEUNKNOWN : Previous State\nNot Known
[*] --> FROMDEFAULT : Previously at\nDEFAULT

state "Any State" as anyEdit #D0D0D0
anyEdit --> EDITED : User\nmodification

state "Any State" as anyReadOp #D0D0D0
anyReadOp --> READ : Successful\nRead
anyReadOp --> UNKNOWN : Unsuccessful\nRead

state "Any State" as anyWriteOp #D0D0D0
anyWriteOp --> STORED : Successful\nStore
anyWriteOp --> UNKNOWN : Unsuccessful\nStore

state "Any State" as anyConfirmOp #D0D0D0
anyConfirmOp --> SAME : Successful\nConfirm
anyConfirmOp --> DIFFERENT : Unsuccessful\nConfirm

@end
 */

package com.luma.lexer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GraphvizExporter {

    private static class NfaTraverser {

        private void writeDotTransitions(BufferedWriter writer, NfaState state) throws IOException {
        
            // Use a set to avoid printing the same transition multiple times
            Set<NfaState> visitedStates = new HashSet<>();
        
            // Recursive function to traverse the Nfa and write its transitions to the .dot file
            traverseState(writer, state, visitedStates);
        }
        
        private void traverseState(BufferedWriter writer, NfaState state, Set<NfaState> visitedStates) throws IOException {
            if (visitedStates.contains(state)) return;
            visitedStates.add(state);
        
            // Write transitions for each state
            for (Map.Entry<Character, List<NfaState>> entry : state.transitions.entrySet()) {
                char transitionChar = entry.getKey();
                for (NfaState nextState : entry.getValue()) {
                    writer.write("    " + state.id + " -> " + nextState.id + " [label=\"" + transitionChar + "\", color=black];\n");
                    traverseState(writer, nextState, visitedStates);
                }
            }
        
            // Write epsilon transitions
            for (NfaState nextState : state.epsilonTransitions) {
                writer.write("    " + state.id + " -> " + nextState.id + " [label=\"ε\", color=red];\n");
                traverseState(writer, nextState, visitedStates);
            }
        }
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public static void generateNfaDotFile(Nfa nfa, String tokenName) {
        String file_path = "/home/arqqm/luma/src/main/java/com/luma/lexer/out/Nfa" + tokenName + ".dot";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file_path))) {
            writer.write("digraph Nfa {\n");
            writer.write("    node [shape=circle, style=filled, fillcolor=lightgrey];\n");
        
            NfaTraverser traverser = new NfaTraverser();
            
            traverser.writeDotTransitions(writer, nfa.start);
        
            for (NfaState accept : nfa.acceptStates) {
                writer.write("    " + accept.id + " [shape=doublecircle, fillcolor=lightblue];\n");
            }
        
            writer.write("}\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

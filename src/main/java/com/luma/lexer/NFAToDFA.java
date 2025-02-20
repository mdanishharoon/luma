package com.luma.lexer;

import java.util.*;
import java.io.PrintWriter;
import java.util.Arrays;

public class NFAToDFA {

    // A counter for generating unique DFA state IDs.
    private static int dfaStateCount = 0;

    /**
     * Represents a state in the DFA.
     * Each DFA state corresponds to a set of NFA states.
     */
    public static class DFAState {
        int id;
        // The set of NFA states this DFA state represents.
        Set<RegexToNFA.State> nfaStates;
        Map<Character, DFAState> transitions = new HashMap<>();
        boolean isAccepting;
        // Instead of a single token type, we now collect all token types from the NFA accept states.
        Set<String> tokenTypes;

        public DFAState(Set<RegexToNFA.State> nfaStates) {
            // Create a new canonical copy of the set.
            this.nfaStates = new HashSet<>(nfaStates);
            this.id = dfaStateCount++;
            updateAcceptingStatus();
        }

        /**
         * Iterates over all NFA states in this DFA state and collects all token types.
         * Marks this DFA state as accepting if at least one token type is present.
         */
        private void updateAcceptingStatus() {
            tokenTypes = new HashSet<>();
            for (RegexToNFA.State s : nfaStates) {
                if (s.tokenType != null) {
                    tokenTypes.add(s.tokenType);
                }
            }
            this.isAccepting = !tokenTypes.isEmpty();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DFAState)) return false;
            DFAState other = (DFAState) o;
            // Equality is solely based on the set of NFA states.
            return this.nfaStates.equals(other.nfaStates);
        }

        @Override
        public int hashCode() {
            return nfaStates.hashCode();
        }
    }

    /**
     * Represents the DFA as a whole.
     */
    public static class DFA {
        DFAState start;
        Set<DFAState> states;

        public DFA(DFAState start, Set<DFAState> states) {
            this.start = start;
            this.states = states;
        }

        /**
         * Exports the DFA as a Graphviz DOT file.
         */
        public void toGraphviz(String filename) {
            try (PrintWriter out = new PrintWriter(filename)) {
                out.println("digraph DFA {");
                out.println("    rankdir=LR;");
                out.println("    size=\"8,5\";");

                // Accepting states are drawn as double circles, and we list all token types.
                out.println("    node [shape = doublecircle, style=filled, fillcolor=lightblue];");
                for (DFAState state : states) {
                    if (state.isAccepting) {
                        String label = "q" + state.id + "\\n" + String.join(",", state.tokenTypes);
                        out.println("    q" + state.id + " [label=\"" + label + "\"];");
                    }
                }

                // Non-accepting states.
                out.println("    node [shape = circle, style=filled, fillcolor=lightgrey];");
                for (DFAState state : states) {
                    if (!state.isAccepting) {
                        out.println("    q" + state.id + " [label=\"q" + state.id + "\"];");
                    }
                }

                // Transitions.
                for (DFAState state : states) {
                    for (Map.Entry<Character, DFAState> entry : state.transitions.entrySet()) {
                        out.println("    q" + state.id + " -> q" + entry.getValue().id 
                                    + " [label=\"" + entry.getKey() + "\"];");
                    }
                }

                // Start state.
                out.println("    start [shape=point];");
                out.println("    start -> q" + start.id + ";");

                out.println("}");
            } catch (Exception e) {
                System.out.println("Error writing to file: " + filename);
                e.printStackTrace();
            }
        }
    }

    /**
     * Computes the epsilon closure of a set of NFA states.
     */
    public static Set<RegexToNFA.State> epsilonClosure(Set<RegexToNFA.State> states) {
        Set<RegexToNFA.State> closure = new HashSet<>(states);
        Stack<RegexToNFA.State> stack = new Stack<>();
        stack.addAll(states);
        while (!stack.isEmpty()) {
            RegexToNFA.State state = stack.pop();
            for (RegexToNFA.State next : state.epsilonTransitions) {
                if (closure.add(next)) { // add returns true if next was not already present
                    stack.push(next);
                }
            }
        }
        return closure;
    }

    /**
     * Computes the set of NFA states reachable from the given set on the input symbol.
     */
    public static Set<RegexToNFA.State> move(Set<RegexToNFA.State> states, char symbol) {
        Set<RegexToNFA.State> result = new HashSet<>();
        for (RegexToNFA.State state : states) {
            List<RegexToNFA.State> list = state.transitions.get(symbol);
            if (list != null) {
                result.addAll(list);
            }
        }
        return result;
    }

    /**
     * Helper method to extract the alphabet (i.e. the set of all characters)
     * from the NFA by traversing reachable states starting from nfa.start.
     */
    public static Set<Character> getAlphabet(RegexToNFA.NFA nfa) {
        Set<Character> alphabet = new HashSet<>();
        Set<RegexToNFA.State> visited = new HashSet<>();
        Stack<RegexToNFA.State> stack = new Stack<>();
        stack.push(nfa.start);
        while (!stack.isEmpty()) {
            RegexToNFA.State state = stack.pop();
            if (!visited.add(state)) continue;
            for (Map.Entry<Character, List<RegexToNFA.State>> entry : state.transitions.entrySet()) {
                alphabet.add(entry.getKey());
                for (RegexToNFA.State next : entry.getValue()) {
                    if (!visited.contains(next)) {
                        stack.push(next);
                    }
                }
            }
            for (RegexToNFA.State next : state.epsilonTransitions) {
                if (!visited.contains(next)) {
                    stack.push(next);
                }
            }
        }
        return alphabet;
    }

    /**
     * Converts the given NFA to a DFA using the subset construction algorithm.
     * Each DFA state represents a set of NFA states and may be accepting for multiple token types.
     */
    public static DFA convert(RegexToNFA.NFA nfa) {
        Set<Character> alphabet = getAlphabet(nfa);
        // Use a canonical key based on the set of NFA states.
        Map<Set<RegexToNFA.State>, DFAState> dfaStatesMap = new HashMap<>();
        Queue<DFAState> worklist = new LinkedList<>();
        Set<DFAState> allDFAStates = new HashSet<>();

        // Start with the epsilon closure of the NFA's start state.
        Set<RegexToNFA.State> startClosure = epsilonClosure(new HashSet<>(Arrays.asList(nfa.start)));
        DFAState dfaStart = new DFAState(startClosure);
        dfaStatesMap.put(dfaStart.nfaStates, dfaStart);
        worklist.add(dfaStart);
        allDFAStates.add(dfaStart);

        // Process each DFA state.
        while (!worklist.isEmpty()) {
            DFAState currentDFA = worklist.poll();
            for (char symbol : alphabet) {
                Set<RegexToNFA.State> moveResult = move(currentDFA.nfaStates, symbol);
                if (moveResult.isEmpty()) continue;
                Set<RegexToNFA.State> nextStates = epsilonClosure(moveResult);
                if (nextStates.isEmpty()) continue;
                DFAState nextDFA = dfaStatesMap.get(nextStates);
                if (nextDFA == null) {
                    nextDFA = new DFAState(nextStates);
                    dfaStatesMap.put(nextDFA.nfaStates, nextDFA);
                    worklist.add(nextDFA);
                    allDFAStates.add(nextDFA);
                }
                currentDFA.transitions.put(symbol, nextDFA);
            }
        }
        return new DFA(dfaStart, allDFAStates);
    }

    // For testing purposes.
    public static void main(String[] args) {
        // For example, build an NFA using RegexToNFA (which sets token types on its accept states)
        // and convert it to a DFA. The DFA will now report all token types in each accept state.
        RegexToNFA.NFA nfa = RegexToNFA.generateNFAMachines(args); // example postfix regex
        DFA dfa = convert(nfa);
        dfa.toGraphviz("dfa.dot");
    }
}

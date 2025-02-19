package com.luma.lexer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class RegexToNFA {

    // A counter to assign unique IDs to states.
    private static int stateCount = 0;

    // Represents a state in the NFA.
    static class State {
        int id;
        // Transitions on a character (non-epsilon).
        Map<Character, List<State>> transitions = new HashMap<>();
        // Epsilon transitions (transitions that do not consume a character).
        List<State> epsilonTransitions = new ArrayList<>();

        State() {
            this.id = stateCount++;
        }

        // Add a transition for a specific character.
        void addTransition(char c, State state) {
            transitions.computeIfAbsent(c, k -> new ArrayList<>()).add(state);
        }

        // Add an epsilon transition.
        void addEpsilonTransition(State state) {
            epsilonTransitions.add(state);
        }
    }

    // Represents an NFA with a start state and an accept state.
    static class NFA {
        State start;
        State accept;

        NFA(State start, State accept) {
            this.start = start;
            this.accept = accept;
        }

        @SuppressWarnings("CallToPrintStackTrace")
        public void generateDotFile(String tokenName) {
            String file_path = "/home/arqqm/luma/src/main/java/com/luma/lexer/nfa" + tokenName + ".dot";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file_path))) {
            writer.write("digraph NFA {\n");
            writer.write("    node [shape=circle, style=filled, fillcolor=lightgrey];\n");
        
            // Write states and their transitions
            writeDotTransitions(writer, this.start);
        
            // Mark the accept state with a double circle and a different color
            writer.write("    " + this.accept.id + " [shape=doublecircle, fillcolor=lightblue];\n");
        
            writer.write("}\n");
            } catch (IOException e) {
            e.printStackTrace();
            }
        }
        
        private static void writeDotTransitions(BufferedWriter writer, State state) throws IOException {
            // Use a set to avoid printing the same transition multiple times
            Set<State> visitedStates = new HashSet<>();
        
            // Recursive function to traverse the NFA and write its transitions to the .dot file
            traverseState(writer, state, visitedStates);
        }
        
        private static void traverseState(BufferedWriter writer, State state, Set<State> visitedStates) throws IOException {
            if (visitedStates.contains(state)) return;
            visitedStates.add(state);
        
            // Write transitions for each state
            for (Map.Entry<Character, List<State>> entry : state.transitions.entrySet()) {
            char transitionChar = entry.getKey();
            for (State nextState : entry.getValue()) {
                writer.write("    " + state.id + " -> " + nextState.id + " [label=\"" + transitionChar + "\", color=black];\n");
                traverseState(writer, nextState, visitedStates);
            }
            }
        
            // Write epsilon transitions
            for (State nextState : state.epsilonTransitions) {
            writer.write("    " + state.id + " -> " + nextState.id + " [label=\"ε\", color=red];\n");
            traverseState(writer, nextState, visitedStates);
            }
        }
        }

    /**
     * Constructs an NFA from a regex in postfix notation using Thompson's construction.
     * Supported operators:
     * - '*' for Kleene star
     * - '.' for concatenation
     * - '|' for alternation
     * All other characters are treated as literals.
     */
    static NFA regexToNFA(String regex) {
        Stack<NFA> stack = new Stack<>();

        for (int i = 0; i < regex.length(); i++) {
            char token = regex.charAt(i);
            if (Character.isWhitespace(token)) {
                continue;
            }
            // Check for an escape character.
            if (token == '\\') {
                if (i + 1 < regex.length()) {
                    // Increment to get the escaped character.
                    token = regex.charAt(++i);
                    // Treat the escaped character as a literal.
                    State start = new State();
                    State accept = new State();
                    start.addTransition(token == ' ' ? ' ' : token, accept);
                    stack.push(new NFA(start, accept));
                    continue;
                } else {
                    throw new IllegalArgumentException("Escape character at end of regex");
                }
            }

            switch (token) {
                case '*': {
                    // Kleene star: Pop one NFA and create a new NFA that loops.
                    NFA nfa = stack.pop();
                    State start = new State();
                    State accept = new State();
                    start.addEpsilonTransition(nfa.start);
                    start.addEpsilonTransition(accept);
                    nfa.accept.addEpsilonTransition(nfa.start);
                    nfa.accept.addEpsilonTransition(accept);
                    stack.push(new NFA(start, accept));
                    break;
                }
                case '.': {
                    // Concatenation: Pop two NFAs and connect them.
                    NFA nfa2 = stack.pop();
                    NFA nfa1 = stack.pop();
                    nfa1.accept.addEpsilonTransition(nfa2.start);
                    stack.push(new NFA(nfa1.start, nfa2.accept));
                    break;
                }
                case '|': {
                    // Alternation: Pop two NFAs and create a new branching structure.
                    NFA nfa2 = stack.pop();
                    NFA nfa1 = stack.pop();
                    State start = new State();
                    State accept = new State();
                    start.addEpsilonTransition(nfa1.start);
                    start.addEpsilonTransition(nfa2.start);
                    nfa1.accept.addEpsilonTransition(accept);
                    nfa2.accept.addEpsilonTransition(accept);
                    stack.push(new NFA(start, accept));
                    break;
                }
                default: {
                    // For literal characters, create a simple NFA.
                    State start = new State();
                    State accept = new State();
                    start.addTransition(token, accept);
                    stack.push(new NFA(start, accept));
                    break;
                }
            }
        }

        // The final NFA on the stack represents the entire regex.
        return stack.pop();
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public static void generateNFAMachines(String[] args) {
        // Read the regex rules from a file named "regex_rules".
        // Each line is assumed to have the format:
        // TOKEN_NAME REGEX_IN_POSTFIX
        
        // TODO: Implement a dynamic way to get the regex_rules file.
        // The file should be placed at src/main/resources/regex_rules.txt
        // try (BufferedReader br = new BufferedReader(new InputStreamReader(RegexToNFA.class.getResourceAsStream("/regex_rules.txt")))) {

        String file_path = "/home/arqqm/luma/src/main/resources/regex_rules.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(file_path))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Skip empty lines or lines starting with '#' (comments).
                if (line.trim().isEmpty() || line.trim().startsWith("#")) continue;
                String[] parts = line.split("\\s+", 2);
                if (parts.length != 2) {
                    System.err.println("Invalid rule format: " + line);
                    continue;
                }
                String tokenName = parts[0];
                String regex = parts[1].trim();
                // Generate the NFA for this regex.
                NFA nfa = regexToNFA(regex);
                System.out.println("Generated NFA for token: " + tokenName);
                System.out.println("  Start state: " + nfa.start.id);
                System.out.println("  Accept state: " + nfa.accept.id);
                nfa.generateDotFile(tokenName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        generateNFAMachines(args);
    }
}

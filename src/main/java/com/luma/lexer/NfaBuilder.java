package com.luma.lexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * NfaBuilder is responsible for constructing Non-deterministic Finite Automata (NFA) from regular expressions.
 * It supports basic regex operators such as Kleene star (*), concatenation (.), and alternation (|).
 */
public class NfaBuilder {

    /**
     * Constructs an Nfa from a regex in postfix notation using Thompson's construction.
     * Supported operators:
     * - '*' for Kleene star
     * - '.' for concatenation
     * - '|' for alternation
     * All other characters are treated as literals.
     *
     * @param regex the regular expression in postfix notation
     * @return the constructed Nfa
     */
    private static Nfa convertRegexToNfa(String regex) {
        Stack<Nfa> stack = new Stack<>();
        for (int i = 0; i < regex.length(); i++) {
            char token = regex.charAt(i);
            if (Character.isWhitespace(token)) continue;
            switch (token) {
                case '*': stack.push(handleKleeneStar(stack.pop())); break;
                case '.': stack.push(handleConcatenation(stack.pop(), stack.pop())); break;
                case '|': stack.push(handleAlternation(stack.pop(), stack.pop())); break;
                default: stack.push(handleLiteralCharacter(token)); break;
            }
        }
        return stack.pop();
    }

    /**
     * Handles the Kleene star operation for an Nfa.
     *
     * @param nfa the Nfa to apply the Kleene star to
     * @return the new Nfa after applying the Kleene star
     */
    private static Nfa handleKleeneStar(Nfa nfa) {
        NfaState start = new NfaState(StateIdGenerator.generateId("NFA"));
        NfaState accept = new NfaState(StateIdGenerator.generateId("NFA"));

        start.addEpsilonTransition(nfa.start);
        start.addEpsilonTransition(accept);
        for (NfaState as : nfa.acceptStates) {
            as.addEpsilonTransition(nfa.start);
            as.addEpsilonTransition(accept);
        }
        return new Nfa(start, accept);
    }

    /**
     * Handles the concatenation operation for two Nfas.
     *
     * @param nfa2 the second Nfa
     * @param nfa1 the first Nfa
     * @return the new Nfa after concatenation
     */
    private static Nfa handleConcatenation(Nfa nfa2, Nfa nfa1) {
        for (NfaState as : nfa1.acceptStates) {
            as.addEpsilonTransition(nfa2.start);
        }
        return new Nfa(nfa1.start, nfa2.acceptStates);
    }

    /**
     * Handles the alternation operation for two Nfas.
     *
     * @param nfa2 the second Nfa
     * @param nfa1 the first Nfa
     * @return the new Nfa after alternation
     */
    private static Nfa handleAlternation(Nfa nfa2, Nfa nfa1) {
        NfaState start = new NfaState(StateIdGenerator.generateId("NFA"));
        NfaState accept = new NfaState(StateIdGenerator.generateId("NFA"));

        start.addEpsilonTransition(nfa1.start);
        start.addEpsilonTransition(nfa2.start);
        for (NfaState as : nfa1.acceptStates) {
            as.addEpsilonTransition(accept);
        }
        for (NfaState as : nfa2.acceptStates) {
            as.addEpsilonTransition(accept);
        }
        return new Nfa(start, accept);
    }

    /**
     * Handles a literal character by creating an Nfa that recognizes the character.
     *
     * @param token the literal character
     * @return the new Nfa for the literal character
     */
    private static Nfa handleLiteralCharacter(char token) {
        NfaState start = new NfaState(StateIdGenerator.generateId("NFA"));
        NfaState accept = new NfaState(StateIdGenerator.generateId("NFA"));

        start.addTransition(token, accept);
        return new Nfa(start, accept);
    }

    /**
     * Merges multiple Nfas into a single Nfa.
     * The merged Nfa has a new start state with epsilon transitions to each individual Nfa's start state.
     *
     * @param nfaList the list of Nfas to merge
     * @return the merged Nfa
     */
    private static Nfa mergeNfas(List<Nfa> nfaList) {
        NfaState newStart = new NfaState(StateIdGenerator.generateId("NFA"));

        Set<NfaState> mergedAccepts = new HashSet<>();
        for (Nfa nfa : nfaList) {
            newStart.addEpsilonTransition(nfa.start);
            mergedAccepts.addAll(nfa.acceptStates);
        }
        return new Nfa(newStart, mergedAccepts);
    }

    /**
     * Generates a list of NFAs (Non-deterministic Finite Automata) from a given file.
     * Each line in the file should contain a token name and a regular expression, separated by whitespace.
     * Lines that are empty or start with a '#' character are ignored.
     *
     * @param filePath the path to the file containing the token names and regular expressions
     * @return a list of NFAs generated from the regular expressions in the file
     */
    @SuppressWarnings("CallToPrintStackTrace")
    private static List<Nfa> generateNfas(InputStream inputStream) {
        List<Nfa> nfaList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.trim().startsWith("#")) continue;
                String[] parts = line.split("\\s+", 2);
                if (parts.length != 2) {
                    System.err.println("Invalid rule format: " + line);
                    continue;
                }
                String tokenName = parts[0];
                String regex = parts[1].trim();
                Nfa nfa = convertRegexToNfa(regex);
                setTokenTypeForAcceptStates(nfa, tokenName);
                GraphvizExporter.generateNfaDotFile(nfa, tokenName);
                nfaList.add(nfa);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nfaList;
    }

    /**
     * Sets the token type for the accept states of an Nfa.
     *
     * @param nfa the Nfa whose accept states will be updated
     * @param tokenName the token type to set
     */
    private static void setTokenTypeForAcceptStates(Nfa nfa, String tokenName) {
        for (NfaState accept : nfa.acceptStates) {
            accept.tokenType = tokenName;
        }
    }

    /**
     * Generates the Nfa by reading the regex rules from a file.
     * The file should be located in the resources folder and named "regex_rules.txt".
     *
     * @return the generated Nfa
     */
    public static Nfa generateNfa() {
        InputStream fileInputStream = NfaBuilder.class.getClassLoader().getResourceAsStream("com/luma/lexer/resources/regex_rules.txt");

        if (fileInputStream == null) {
            throw new IllegalArgumentException("File not found in resources: regex_rules.txt");
        }
        
        List<Nfa> nfaList = generateNfas(fileInputStream);
        Nfa mergedNfa = mergeNfas(nfaList);
        GraphvizExporter.generateNfaDotFile(mergedNfa, "_merged");
        return mergedNfa;
    }

    /**
     * The main method to generate the Nfa.
     * This method allows the class to be run independently to generate and check the NFA.
     */
    public static void main(String[] args) {
        generateNfa();
    }
}

package com.luma.lexer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Lexer {

    private final NFAToDFA.DFA dfa;

    /**
     * Constructs a Lexer given a complete, minimized DFA.
     */
    public Lexer(NFAToDFA.DFA dfa) {
        this.dfa = dfa;
    }

    /**
     * Tokenizes the input string using the DFA.
     * Implements longest-match scanning.
     */
    public List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        int index = 0;
        while (index < input.length()) {
            // Skip whitespace
            if (Character.isWhitespace(input.charAt(index))) {
                index++;
                continue;
            }
            int lastAccept = -1;
            Set<String> lastTokenTypes = null;
            int i = index;
            NFAToDFA.DFAState current = dfa.start;
            // Record the last accepting position and its token types.
            while (i < input.length()) {
                char c = input.charAt(i);
                current = current.transitions.get(c);
                if (current == null) {
                    break;
                }
                if (current.isAccepting) {
                    lastAccept = i;
                    lastTokenTypes = new HashSet<>(current.tokenTypes);
                }
                i++;
            }
            if (lastAccept >= index) {
                String lexeme = input.substring(index, lastAccept + 1);
                tokens.add(new Token(lexeme, lastTokenTypes));
                index = lastAccept + 1;
            } else {
                // No token recognized: report an error and skip the character.
                System.err.println("Lexer error at index " + index + ": unexpected character '" + input.charAt(index) + "'");
                index++;
            }
        }
        return tokens;
    }

    /**
     * Represents a token with its lexeme and set of token types.
     */
    public static class Token {
        public final String lexeme;
        public final Set<String> tokenTypes;

        public Token(String lexeme, Set<String> tokenTypes) {
            this.lexeme = lexeme;
            this.tokenTypes = tokenTypes;
        }

        @Override
        public String toString() {
            return "Token [lexeme=" + lexeme + ", tokenTypes=" + tokenTypes + "]";
        }
    }

    /**
     * Main method for testing.
     * It reads regex rules from "regex_rules.txt", builds a combined NFA,
     * converts and minimizes it to a DFA, then tokenizes the contents of "code.lm".
     */
    public static void main(String[] args) {
        try {
            // === STEP 1: Build the merged NFA from the regex rules ===
            RegexToNFA.NFA nfa = RegexToNFA.generateNFAMachines(args);

            // === STEP 2: Convert the merged NFA to a DFA and minimize it ===
            NFAToDFA.DFA dfa;
            dfa = NFAToDFA.convert(nfa);

            // Minimize and complete the DFA (sink state added)
            NFAToDFA.DFA minimizedDFA = DFAMinimizer.minimize(dfa);

            // === STEP 3: Use the DFA in the lexer to tokenize an input file ===
            // Read the input file "code.lm"
            Lexer lexer = new Lexer(minimizedDFA);
        
            // List the input files to tokenize.
            String[] inputFiles = {"/home/arqqm/luma/src/main/java/code1.lm", "/home/arqqm/luma/src/main/java/code2.lm", "/home/arqqm/luma/src/main/java/code3.lm"};
            for (String filename : inputFiles) {
                // Read the contents of the file.
                String input = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filename)));
                System.out.println("Lexing file: " + filename);
                // Tokenize the input.
                List<Lexer.Token> tokens = lexer.tokenize(input);
                // Print each token.
                for (Lexer.Token token : tokens) {
                    System.out.println(token);
                }
                System.out.println("-----------");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

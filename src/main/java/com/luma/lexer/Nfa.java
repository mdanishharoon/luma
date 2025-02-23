package com.luma.lexer;

import java.util.HashSet;
import java.util.Set;

public class Nfa {
    NfaState start;
    Set<NfaState> acceptStates;

    // Constructor for a single accept state.
    public Nfa(NfaState start, NfaState accept) {
        this.start = start;
        this.acceptStates = new HashSet<>();
        this.acceptStates.add(accept);
    }

    // Constructor for multiple accept states.
    public Nfa(NfaState start, Set<NfaState> acceptStates) {
        this.start = start;
        this.acceptStates = acceptStates;
    }
}

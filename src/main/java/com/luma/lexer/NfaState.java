package com.luma.lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NfaState {
    int id;
    Map<Character, List<NfaState>> transitions = new HashMap<>();
    List<NfaState> epsilonTransitions = new ArrayList<>();

    String tokenType = null;     // This field is used to mark accept states with the token type (easier for identification later)

    NfaState(int id) {
        this.id = id;
    }

    // Add a transition for a specific character.
    void addTransition(char c, NfaState state) {
        transitions.computeIfAbsent(c, k -> new ArrayList<>()).add(state);
    }

    // Add an epsilon transition.
    void addEpsilonTransition(NfaState state) {
        epsilonTransitions.add(state);
    }
}
package com.luma.lexer;

import java.util.HashMap;
import java.util.Map;

public class StateIdGenerator {
    // Map to store counters for different contexts (e.g., NFA, DFA)
    private static final Map<String, Integer> counters = new HashMap<>();

    // Get the next unique ID for the given context (e.g., "NFA", "DFA")
    public static int generateId(String context) {
        counters.putIfAbsent(context, 1); // Initialize the counter if not already set
        int id = counters.get(context);
        counters.put(context, id + 1);  // Increment the counter after returning the current ID
        return id;
    }
}

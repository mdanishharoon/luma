package com.luma.lexer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DFAMinimizer minimizes a given DFA using a three‐step approach:
 *
 * 1. Remove unreachable states.
 * 2. Remove dead states (states that cannot reach an accepting state).
 * 3. Partition the remaining states into equivalence classes (using a variant of Hopcroft’s algorithm)
 *    and build a new minimized DFA.
 *
 * Finally, for the complete DFA, a sink state is added so that every transition is defined.
 */
public class DFAMinimizer {

    /**
     * Minimizes the given DFA and returns a complete DFA with a sink state.
     */
    public static NFAToDFA.DFA minimize(NFAToDFA.DFA dfa) {
        // Step 1: Remove unreachable states.
        Set<NFAToDFA.DFAState> reachable = getReachableStates(dfa);

        // Step 2: Remove dead states (states that cannot reach an accepting state).
        Set<NFAToDFA.DFAState> live = getLiveStates(reachable);

        // Step 3: Partition live states into equivalence classes and build the minimized DFA.
        Set<Character> alphabet = getAlphabet(live);
        Set<Set<NFAToDFA.DFAState>> partitions = initializePartitions(live);
        partitions = refinePartitions(partitions, alphabet);

        // Build a mapping from each partition block to a new DFA state.
        Map<Set<NFAToDFA.DFAState>, NFAToDFA.DFAState> blockToState = new HashMap<>();
        for (Set<NFAToDFA.DFAState> block : partitions) {
            NFAToDFA.DFAState newState = createNewStateFromBlock(block);
            blockToState.put(block, newState);
        }
        // Set up transitions for the new DFA.
        for (Set<NFAToDFA.DFAState> block : partitions) {
            NFAToDFA.DFAState newState = blockToState.get(block);
            // Use an arbitrary representative from the block.
            NFAToDFA.DFAState rep = block.iterator().next();
            for (char c : alphabet) {
                NFAToDFA.DFAState target = rep.transitions.get(c);
                if (target != null && live.contains(target)) {
                    Set<NFAToDFA.DFAState> targetBlock = findBlockContaining(partitions, target);
                    NFAToDFA.DFAState newTarget = blockToState.get(targetBlock);
                    newState.transitions.put(c, newTarget);
                }
            }
        }
        // The new start state is the block containing the old start.
        Set<NFAToDFA.DFAState> startBlock = findBlockContaining(partitions, dfa.start);
        NFAToDFA.DFAState newStart = blockToState.get(startBlock);
        Set<NFAToDFA.DFAState> minimizedStates = new HashSet<>(blockToState.values());
        NFAToDFA.DFA minimizedDFA = new NFAToDFA.DFA(newStart, minimizedStates);

        // Add a sink state to complete the DFA.
        return completeDFA(minimizedDFA);
    }

    /**
     * Performs a breadth-first search from the DFA start state to find all reachable states.
     */
    private static Set<NFAToDFA.DFAState> getReachableStates(NFAToDFA.DFA dfa) {
        Set<NFAToDFA.DFAState> reachable = new HashSet<>();
        Queue<NFAToDFA.DFAState> queue = new LinkedList<>();
        queue.add(dfa.start);
        reachable.add(dfa.start);
        while (!queue.isEmpty()) {
            NFAToDFA.DFAState s = queue.poll();
            for (NFAToDFA.DFAState t : s.transitions.values()) {
                if (!reachable.contains(t)) {
                    reachable.add(t);
                    queue.add(t);
                }
            }
        }
        return reachable;
    }

    /**
     * Computes live states: states that can eventually reach an accepting state.
     * Builds a reverse transition map and starts from every accepting state.
     */
    private static Set<NFAToDFA.DFAState> getLiveStates(Set<NFAToDFA.DFAState> states) {
        Set<NFAToDFA.DFAState> live = new HashSet<>();
        Map<NFAToDFA.DFAState, Set<NFAToDFA.DFAState>> reverseMap = new HashMap<>();
        for (NFAToDFA.DFAState s : states) {
            reverseMap.put(s, new HashSet<>());
        }
        for (NFAToDFA.DFAState s : states) {
            for (NFAToDFA.DFAState t : s.transitions.values()) {
                if (states.contains(t)) {
                    reverseMap.get(t).add(s);
                }
            }
        }
        Queue<NFAToDFA.DFAState> queue = new LinkedList<>();
        for (NFAToDFA.DFAState s : states) {
            if (s.isAccepting) {
                live.add(s);
                queue.add(s);
            }
        }
        while (!queue.isEmpty()) {
            NFAToDFA.DFAState s = queue.poll();
            for (NFAToDFA.DFAState pred : reverseMap.get(s)) {
                if (!live.contains(pred)) {
                    live.add(pred);
                    queue.add(pred);
                }
            }
        }
        return live;
    }

    /**
     * Extracts the input alphabet from the given set of DFA states.
     */
    private static Set<Character> getAlphabet(Set<NFAToDFA.DFAState> states) {
        Set<Character> alphabet = new HashSet<>();
        for (NFAToDFA.DFAState s : states) {
            alphabet.addAll(s.transitions.keySet());
        }
        return alphabet;
    }

    /**
     * Initializes the partition of states into equivalence classes.
     * Final states are grouped by their set of token types; non-final states form one block.
     */
    private static Set<Set<NFAToDFA.DFAState>> initializePartitions(Set<NFAToDFA.DFAState> states) {
        Map<Set<String>, Set<NFAToDFA.DFAState>> finalPartitions = new HashMap<>();
        Set<NFAToDFA.DFAState> nonFinal = new HashSet<>();
        for (NFAToDFA.DFAState s : states) {
            if (s.isAccepting) {
                Set<String> tokens = s.tokenTypes;
                finalPartitions.computeIfAbsent(tokens, k -> new HashSet<>()).add(s);
            } else {
                nonFinal.add(s);
            }
        }
        Set<Set<NFAToDFA.DFAState>> partitions = new HashSet<>();
        partitions.addAll(finalPartitions.values());
        if (!nonFinal.isEmpty()) {
            partitions.add(nonFinal);
        }
        return partitions;
    }

    /**
     * Refines the partitions using a Hopcroft-style algorithm.
     */
    private static Set<Set<NFAToDFA.DFAState>> refinePartitions(Set<Set<NFAToDFA.DFAState>> partitions, Set<Character> alphabet) {
        Queue<Set<NFAToDFA.DFAState>> worklist = new LinkedList<>(partitions);
        while (!worklist.isEmpty()) {
            Set<NFAToDFA.DFAState> A = worklist.poll();
            for (char c : alphabet) {
                // X = states with a transition on symbol c that lands in A.
                Set<NFAToDFA.DFAState> X = partitions.stream()
                        .flatMap(block -> block.stream())
                        .filter(s -> {
                            NFAToDFA.DFAState t = s.transitions.get(c);
                            return t != null && A.contains(t);
                        })
                        .collect(Collectors.toSet());
                if (X.isEmpty()) continue;
                Set<Set<NFAToDFA.DFAState>> newPartitions = new HashSet<>();
                Iterator<Set<NFAToDFA.DFAState>> it = partitions.iterator();
                while (it.hasNext()) {
                    Set<NFAToDFA.DFAState> Y = it.next();
                    Set<NFAToDFA.DFAState> intersection = new HashSet<>(Y);
                    intersection.retainAll(X);
                    Set<NFAToDFA.DFAState> difference = new HashSet<>(Y);
                    difference.removeAll(X);
                    if (!intersection.isEmpty() && !difference.isEmpty()) {
                        it.remove();
                        newPartitions.add(intersection);
                        newPartitions.add(difference);
                        // Add the smaller set for further refinement.
                        if (intersection.size() <= difference.size()) {
                            worklist.add(intersection);
                        } else {
                            worklist.add(difference);
                        }
                    }
                }
                partitions.addAll(newPartitions);
            }
        }
        return partitions;
    }

    /**
     * Finds and returns the partition block that contains the given state.
     */
    private static Set<NFAToDFA.DFAState> findBlockContaining(Set<Set<NFAToDFA.DFAState>> partitions, NFAToDFA.DFAState s) {
        for (Set<NFAToDFA.DFAState> block : partitions) {
            if (block.contains(s)) {
                return block;
            }
        }
        return null;
    }

    /**
     * Creates a new DFA state representing a block by combining the NFA states of all states in the block.
     */
    private static NFAToDFA.DFAState createNewStateFromBlock(Set<NFAToDFA.DFAState> block) {
        Set<RegexToNFA.State> combinedNFAStates = new HashSet<>();
        for (NFAToDFA.DFAState s : block) {
            combinedNFAStates.addAll(s.nfaStates);
        }
        return new NFAToDFA.DFAState(combinedNFAStates);
    }

    /**
     * Completes the DFA by adding a sink (dead) state so that every state has transitions for every symbol.
     * The sink state loops to itself on every symbol.
     */
    private static NFAToDFA.DFA completeDFA(NFAToDFA.DFA dfa) {
        // Compute the complete alphabet for the DFA.
        Set<Character> alphabet = getAlphabet(dfa.states);
        // Create the sink state. We use an empty set for NFA states.
        NFAToDFA.DFAState sink = new NFAToDFA.DFAState(new HashSet<>());
        sink.isAccepting = false;
        sink.tokenTypes = new HashSet<>();
        // For every symbol in the alphabet, the sink state transitions to itself.
        for (char c : alphabet) {
            sink.transitions.put(c, sink);
        }
        // For each state in the DFA, if a transition is missing for any symbol, add one to the sink state.
        for (NFAToDFA.DFAState state : dfa.states) {
            for (char c : alphabet) {
                if (!state.transitions.containsKey(c)) {
                    state.transitions.put(c, sink);
                }
            }
        }
        // Add the sink state to the set of DFA states.
        dfa.states.add(sink);
        return dfa;
    }

    public static void main(String[] args) {
        RegexToNFA.NFA nfa = RegexToNFA.generateNFAMachines(args);
        NFAToDFA.DFA dfa = NFAToDFA.convert(nfa);
        dfa.toGraphviz("dfa.dot");
        NFAToDFA.DFA minimized = minimize(dfa);
        minimized.toGraphviz("minimized_dfa.dot");
    }
}

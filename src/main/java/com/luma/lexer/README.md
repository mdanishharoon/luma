# Lexer

This package will implement the Lexer for Luma, rules defined in `regex_rules.txt` will be used to generate the state machines.
Each machine also comes with the `.dot` files to render a graphical representation.

Here is an example NFA machine generated from a few basic regex rules

![Generated NFA](out/nfa_merged.svg)

Here is an example DFA machine generated from the same regex rules:

![Generated DFA](out/dfa.svg)

Here is an example DFA machine fully minimized WITH a sink state formed from the same regex rules:

![Minimized DFA](out/minimized_dfa.svg)

Current Todos:

- [x] Postfix Regex to NFA
- [x] Concat all NFAs
- [x] NFA to DFA
- [x] DFA Minimization
- [ ] Clean up and refactoring classes
- [ ] Infix to Postfix Regex

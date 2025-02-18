# Compiler Construction

# **Programming Language Syntax Overview**

## **Introduction**
This document serves as a reference guide for the syntax and semantics of the language. The language is statically typed, functional with strong support for immutability, traits, function pointers, and heap allocation. It prioritizes readability and performance while providing a clear model for memory management.

---

## **1. Keywords**

### **Variable Declaration**
- `var` - Declares a mutable variable.
- `<TYPE>` - Declares an immutable variable (default).

### **Functions**
- `func` - Declares a normal stack-allocated function.
- `hfunc` - Declares a heap-allocated function.

### **Control Flow**
- `if`, `else`
- `match`, `case`
- `while`, `do while`
- `for`, `for each`

### **Data Structures**
- `struct` - Defines a structure.
- `trait` - Defines an interface-like trait.

### **Miscellaneous**
- `return` - Returns a value from a function.
- `import` - Imports external modules.
- `export` - Exports a module.

---

## **2. Operators**

### **Arithmetic Operators**
| Operator | Description |
|----------|------------|
| `+` | Addition |
| `-` | Subtraction |
| `*` | Multiplication |
| `/` | Division |
| `%` | Modulo |

### **Comparison Operators**
| Operator | Description |
|----------|------------|
| `==` | Equal to |
| `!=` | Not equal to |
| `>` | Greater than |
| `<` | Less than |
| `>=` | Greater than or equal to |
| `<=` | Less than or equal to |

### **Logical Operators**
| Operator | Description |
|----------|------------|
| `&&` | Logical AND |
| `||` | Logical OR |
| `!` | Logical NOT |

### **Pipe Operator**
| Operator | Description |
|----------|------------|
| `|>` | Passes the result of one function into another |

---

## **3. Variables and Data Types**

### **Immutable Variables (Default)**
```plaintext
int x = 10;
str name = "Alice";
```

### **Mutable Variables**
```plaintext
var int y = 20;
var str message = "Hello";
```

---

## **4. Functions**

### **Basic Function Syntax**
```plaintext
func add (int x, int y) : int {
    return x + y;
}
```


### **Lambda Functions**
```plaintext
var function double = (int x) : int => x * 2;
print(double(5)); // Outputs: 10
```

### **Returning Functions**
```plaintext
hfunc make_multiplier (int factor) : function {
    hfunc multiplier (int x) : int {
        return x * factor;
    }
    return &multiplier;
}
```

---

## **5. Function Pointers**
```plaintext
hfunc square (int x) : int {
    return x * x;
}

func apply (function f, int x) : int {
    return f(x);
}

func main () {
    function ptr = &square;
    print(apply(ptr, 4)); // Outputs: 16
}
```

---

## **6. Control Flow**

### **If-Else Statements**
```plaintext
if (x > 10) {
    print("Greater than 10");
} else {
    print("Less than or equal to 10");
}
```

### **Pattern Matching**
```plaintext
match x {
    1 => print("One"),
    2 => print("Two"),
    _ => print("Other")
}
```

---

## **7. Loops**

### **For Loop**
```plaintext
for (int i in 0..10) {
    print(i);
}
```

### **While Loop**
```plaintext
while (x < 10) {
    x = x + 1;
}
```

### **Do-While Loop**
```plaintext
do {
    x = x + 1;
} while (x < 10);
```

### **For Each Loop**
```plaintext
for each item in array {
    print(item);
}
```

---

## **8. Structures and Traits**

### **Defining a Struct**
```plaintext
struct Person {
    str name;
    int age;
}
```

### **Using a Struct**
```plaintext
var Person p = Person("Alice", 30);
print(p.name);
```

### **Traits (Interfaces with Default Behavior)**
```plaintext
trait Greeter {
    func greet() : str {
        return "Hello!";
    }
}
```

### **Implementing a Trait in a Struct**
```plaintext
struct Person : Greeter {
    str name;
    int age;
}
```

### **Overriding Trait Methods**
```plaintext
struct FriendlyPerson : Greeter {
    func greet() : str {
        return "Hey there!";
    }
}
```

---

## **9. Memory Management (Stack vs Heap)**

### **Heap-Allocated Functions**
```plaintext
hfunc create_adder (int y) : function {
    hfunc adder (int x) : int {
        return x + y;
    }
    return &adder;
}
```

### **Garbage Collection**
- Stack variables are automatically deallocated.
- Heap-allocated functions must be explicitly managed.
- Functions go out of scope when not referenced.

---

## **10. Example Program**
```plaintext
hfunc make_multiplier (int factor) : function {
    hfunc multiplier (int x) : int {
        return x * factor;
    }
    return &multiplier;
}

func main () {
    function times3 = make_multiplier(3);
    print(times3(5)); // Outputs: 15
}
```
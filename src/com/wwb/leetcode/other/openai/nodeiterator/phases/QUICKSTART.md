# 🚀 Toy Language Type System - QUICKSTART

**30-minute quick reference for interview preparation**

---

## 📋 Table of Contents

1. [Core Concepts](#core-concepts)
2. [Phase 1: Data Representation](#phase-1-data-representation-12-min)
3. [Phase 2: Type Inference](#phase-2-type-inference-15-min)
4. [Phase 3: Function Composition](#phase-3-function-composition-18-min)
5. [Phase 4: Type Environment](#phase-4-type-environment-12-15-min)
6. [Common Pitfalls](#common-pitfalls)
7. [Interview Tips](#interview-tips)

---

## Core Concepts

### The Type System

```
Primitives:  int, float, char
Generics:    T, T1, T2, S (uppercase letters)
Tuples:      [int, char], [[T1, float], T2]
Functions:   (params) -> returnType
```

### Node Structure

```java
class Node {
    String baseGeneric;      // "int", "T1", or null
    List<Node> children;     // For tuples, or empty
}
```

**Key insight:** A Node is either:
- A **primitive/generic** (baseGeneric != null, children empty)
- A **tuple** (baseGeneric == null, children non-empty)

### Function Structure

```java
class Function {
    List<Node> parameters;
    Node outputType;
}
```

---

## Phase 1: Data Representation (12 min)

### Goal
Implement `toString()` for Node and Function.

### Node toString()
```java
// Primitive/generic: return baseGeneric
"int" → "int"
"T1"  → "T1"

// Tuple: return [child1, child2, ...]
new Node(List.of(int, char)) → "[int,char]"
```

### Function toString()
```java
// Format: (param1, param2, ...) -> returnType
(int, char) -> float → "(int,char) -> float"
```

### Example Code
```java
// Primitive
Node intNode = new Node("int");
System.out.println(intNode);  // "int"

// Tuple
Node tuple = new Node(List.of(
    new Node("int"),
    new Node("char")
));
System.out.println(tuple);  // "[int,char]"

// Function
Function func = new Function(
    List.of(new Node("int"), new Node("char")),
    new Node("float")
);
System.out.println(func);  // "(int,char) -> float"
```

### Interview Questions to Ask
- "Should I use brackets or parentheses for tuples?"
- "Are generic names case-sensitive?"
- "Should I handle empty tuples?"

---

## Phase 2: Type Inference (15 min)

### Goal
Given a generic function and concrete arguments, infer the concrete return type.

### Algorithm

```
Step 1: Build binding map (params → args)
  Function: (T1, T2, T1) -> [T1, T2]
  Args:     (int, char, int)
  
  Traverse in parallel:
    T1 → int  (from arg 0)
    T2 → char (from arg 1)
    T1 → int  (from arg 2, consistent ✅)
  
  Map: {T1: int, T2: char}

Step 2: Apply bindings to return type
  [T1, T2] → [int, char]
```

### Key Checks

```java
// 1. Arity check
if (params.length != args.length) {
    throw TypeException.arityMismatch();
}

// 2. Conflict detection
if (bindingMap.containsKey("T1")) {
    Node existing = bindingMap.get("T1");
    if (!existing.equals(newBinding)) {
        throw TypeException.typeConflict("T1", existing, newBinding);
    }
}

// 3. Type mismatch
if (!paramType.equals(argType) && !paramType.isGeneric()) {
    throw TypeException.typeMismatch(paramType, argType);
}
```

### Example

```java
// Function: (T1, T2) -> [T1, T2]
Node t1 = new Node("T1");
Node t2 = new Node("T2");
Function func = new Function(
    List.of(t1, t2),
    new Node(List.of(t1, t2))
);

// Args: (int, char)
List<Node> args = List.of(
    new Node("int"),
    new Node("char")
);

// Infer: [int, char]
Node result = func.getReturnType(args);
System.out.println(result);  // "[int,char]"
```

### Interview Questions to Ask
- "Should I throw exception or return null on error?"
- "What if a generic appears in return but not params?" (free variable)
- "Should I handle nested tuple binding?"

---

## Phase 3: Function Composition (18 min)

### Goal
Compose two functions: `compose(f, g) = h` where `h(x) = g(f(x))`

### Composition Rules

```
Simple:  f: A → B,  g: B → C  ⟹  h: A → C
Generic: f: T1 → T2, g: T2 → T3 ⟹ h: T1 → T3
Partial: f: A → B,  g: (B, C) → D ⟹ h: (A, C) → D
```

### Algorithm

```java
public static boolean canCompose(Function f, Function g) {
    // g must have at least one parameter
    if (g.getParameters().isEmpty()) return false;
    
    // f's return must match g's first param
    Node fReturn = f.getOutputType();
    Node gFirstParam = g.getParameters().get(0);
    
    return areTypesCompatible(fReturn, gFirstParam);
}

public static ComposedFunction compose(Function f, Function g) {
    // Params: all of f's params + g's extra params
    List<Node> params = new ArrayList<>(f.getParameters());
    for (int i = 1; i < g.getParameters().size(); i++) {
        params.add(g.getParameters().get(i));
    }
    
    // Return: g's return type
    Node returnType = g.getOutputType();
    
    return new ComposedFunction(f, g, params, returnType);
}
```

### Type Compatibility

```java
private static boolean areTypesCompatible(Node t1, Node t2) {
    // Exact match
    if (t1.toString().equals(t2.toString())) return true;
    
    // Either is generic (can unify)
    if (t1.isGenericType() || t2.isGenericType()) return true;
    
    // Structural match (both tuples, check recursively)
    if (bothAreTuples(t1, t2)) {
        return checkChildrenCompatible(t1, t2);
    }
    
    return false;
}
```

### Examples

```java
// Example 1: Simple
f: (int) -> char
g: (char) -> float
h = compose(f, g): (int) -> float ✅

// Example 2: Generic
f: (T1) -> [T1, int]
g: ([T1, int]) -> T1
h = compose(f, g): (T1) -> T1 ✅

// Example 3: Partial application
f: (int) -> char
g: (char, float) -> str
h = compose(f, g): (int, float) -> str ✅
//  Note: float param from g

// Example 4: Incompatible (fails)
f: (int) -> char
g: (float) -> int
compose(f, g): ERROR ❌ (char ≠ float)
```

### Interview Questions to Ask
- "Should compose be g∘f or f∘g?" (standard is g∘f)
- "How should I handle partial application?"
- "Should I perform strict or lenient type checking?"

---

## Phase 4: Type Environment (12-15 min)

### Goal
Support let-bindings with scoping and type checking.

### TypeEnvironment Structure

```java
class TypeEnvironment {
    Map<String, Node> variables;
    Map<String, Function> functions;
    TypeEnvironment parent;  // For nested scopes
}
```

### Scoping Model

```
Global environment
    └─ let x = 10 in ...      (child env: x → int)
        └─ let y = 'a' in ... (child env: y → char, x → int from parent)
            └─ x + y          (lookup: x in parent, y in current)
```

### Type Checking Algorithm

```java
public static Node typeCheck(Expression expr, TypeEnvironment env) {
    switch (expr.getType()) {
        case LITERAL:
            return expr.getLiteralType();
        
        case VARIABLE:
            return env.lookupVariable(expr.getVariableName());
        
        case FUNCTION_CALL:
            Function func = env.lookupFunction(expr.getFunctionName());
            List<Node> argTypes = typeCheckArgs(expr.getArguments(), env);
            return func.getReturnType(argTypes);
        
        case LET_BINDING:
            // let x = v in body
            Node valueType = typeCheck(expr.getLetValue(), env);
            TypeEnvironment childEnv = env.createChild();
            childEnv.bindVariable(expr.getLetVarName(), valueType);
            return typeCheck(expr.getLetBody(), childEnv);
        
        case TUPLE:
            List<Node> elemTypes = typeCheckElements(expr.getArguments(), env);
            return new Node(elemTypes);
    }
}
```

### Examples

```java
// Example 1: Simple let
let x = 10 in x
→ Type: int

// Example 2: Nested let
let x = 10 in let y = 'a' in [x, y]
→ Type: [int, char]

// Example 3: Shadowing
let x = 10 in let x = 'a' in x
→ Type: char (inner x shadows outer x)

// Example 4: Function call
let x = 10 in id(x)
→ Type: int (assuming id: T1 -> T1)

// Example 5: Unbound variable (error)
let x = 10 in y
→ ERROR: Unbound variable y
```

### Key Operations

```java
// Bind variable
env.bindVariable("x", new Node("int"));

// Lookup variable (searches parent chain)
Node type = env.lookupVariable("x");

// Create child scope
TypeEnvironment childEnv = env.createChild();
childEnv.bindVariable("y", new Node("char"));

// Child can see parent bindings
childEnv.lookupVariable("x");  // int (from parent)
childEnv.lookupVariable("y");  // char (from current)
```

### Interview Questions to Ask
- "Should I handle recursive let-bindings?"
- "What about mutual recursion?"
- "Error on redefinition or allow shadowing?"

---

## Common Pitfalls

### ❌ Pitfall 1: Confusing baseGeneric vs children
```java
// WRONG: Setting both
Node node = new Node("int");
node.getChildren().add(something);  // Inconsistent!

// RIGHT: Either baseGeneric OR children
Node primitive = new Node("int");          // baseGeneric set
Node tuple = new Node(List.of(child1, child2));  // children set
```

### ❌ Pitfall 2: Not handling nested generics
```java
// Function: ([T1, int]) -> T1
// Args: ([char, int])

// WRONG: Compare "[T1,int]" == "[char,int]" string-wise
// RIGHT: Recurse into children, bind T1 → char
```

### ❌ Pitfall 3: Ignoring arity
```java
// WRONG: Assume params and args have same length
for (int i = 0; i < params.size(); i++) {
    bind(params.get(i), args.get(i));  // IndexOutOfBounds!
}

// RIGHT: Check arity first
if (params.size() != args.size()) {
    throw TypeException.arityMismatch(params.size(), args.size());
}
```

### ❌ Pitfall 4: Not propagating bindings
```java
// WRONG: Create new binding map for return type
Map<String, Node> returnBindings = new HashMap<>();

// RIGHT: Use the same binding map from params
applyBindings(returnType, bindingsFromParams);
```

### ❌ Pitfall 5: Breaking scoping
```java
// WRONG: Bind in parent environment
env.bindVariable("x", type);

// RIGHT: Bind in child environment for let
TypeEnvironment childEnv = env.createChild();
childEnv.bindVariable("x", type);
return typeCheck(body, childEnv);
```

---

## Interview Tips

### Before Coding
1. ✅ **Clarify toString() format** - brackets vs parentheses for tuples
2. ✅ **Understand the type grammar** - primitives vs generics vs tuples
3. ✅ **Ask about error handling** - throw exception or return null?
4. ✅ **Confirm phase scope** - which features to implement?

### During Phase 1
1. ✅ Start with simple cases (primitives)
2. ✅ Test with nested tuples early
3. ✅ Handle empty children list

### During Phase 2
1. ✅ Draw out the binding process on whiteboard
2. ✅ Check for **conflicts** before adding to map
3. ✅ Test: simple binding, nested tuples, conflicts, arity
4. ✅ Don't forget to **recurse** into nested structures

### During Phase 3
1. ✅ **Ask clarification**: g∘f or f∘g?
2. ✅ Discuss **partial application** before implementing
3. ✅ Start with `canCompose()` before `compose()`
4. ✅ Test: simple, generic, partial, incompatible

### During Phase 4
1. ✅ Draw the **environment chain** on whiteboard
2. ✅ Explain **shadowing** behavior
3. ✅ Keep environment **immutable** (create child, don't mutate parent)
4. ✅ Test: simple let, nested, shadowing, unbound

### General
1. ✅ **Think out loud** - explain your approach
2. ✅ **Test as you go** - don't wait until the end
3. ✅ **Ask for feedback** - "Does this approach make sense?"
4. ✅ **Relate to real systems** - TypeScript, Rust, Haskell
5. ✅ **Discuss trade-offs** - strict vs lenient type checking

---

## Quick Reference Card

### Time Budget (50 min interview)

```
Phase 1:  12 min (toString, basic structure)
Phase 2:  15 min (type inference, binding)
Phase 3:  15 min (composition, compatibility)
Phase 4:  8-10 min (environment, or discussion)
```

### Must-Know Algorithms

```
Type Inference:
  1. Check arity
  2. Traverse params & args in parallel
  3. Build binding map (detect conflicts)
  4. Apply bindings to return type

Composition:
  1. Check compatibility (f.return ~ g.params[0])
  2. Combine parameters (f.params + g.params[1:])
  3. Use g's return type

Type Checking:
  1. Literal → return its type
  2. Variable → lookup in environment
  3. Function call → lookup func, infer with args
  4. Let-binding → type check value, bind in child, type check body
```

### Complexity

```
Phase 1:
  toString: O(n) where n = tree nodes

Phase 2:
  getReturnType: O(p + r) where p = params size, r = return size

Phase 3:
  canCompose: O(n + m) where n = f's size, m = g's size
  compose: O(1) structure building

Phase 4:
  typeCheck: O(e * d) where e = expr size, d = scope depth
  lookup: O(d) where d = environment chain depth
```

---

## Real-World Connections

**Mention these in interview to show depth:**

### TypeScript
```typescript
// Similar type inference
function identity<T>(x: T): T { return x; }
identity(42);  // T inferred as number
```

### Rust
```rust
// Generic constraints (like Phase 3's partial application)
fn compose<F, G, A, B, C>(f: F, g: G) -> impl Fn(A) -> C
where
    F: Fn(A) -> B,
    G: Fn(B) -> C,
```

### Haskell
```haskell
-- Hindley-Milner type inference
-- let polymorphism (like Phase 4)
let id = \x -> x in (id 42, id 'a')
```

### Python mypy
```python
# Type checking with generics
from typing import TypeVar, List

T = TypeVar('T')
def first(xs: List[T]) -> T:
    return xs[0]
```

---

## 🎯 You're Ready!

**Before interview:**
- [ ] Run all 4 phases and verify tests pass
- [ ] Review this QUICKSTART.md
- [ ] Practice explaining type inference algorithm
- [ ] Draw environment chain on paper

**During interview:**
- [ ] Ask clarifying questions first
- [ ] Start simple, build up
- [ ] Think out loud
- [ ] Test edge cases
- [ ] Relate to real systems

**Good luck!** 🚀


---
trigger: always_on
---

# Coding Convention Rule

## 1. General Principles

- Code must optimize for:
  1. Readability
  2. Maintainability
  3. Predictability
  4. Consistency
- Prefer explicit code over clever code.
- Minimize hidden behavior and side effects.
- One responsibility per function/class/module.
- Write code for humans first, compiler/runtime second.
- Replace qualifier name with import instead
---

# 2. Naming Convention

## 2.1 General Rules

- Names must describe intent, not implementation.
- Use full meaningful words.
- Avoid vague names:
  - `data`
  - `temp`
  - `obj`
  - `value`
  - `handler`
  - `manager`
  - `util`
- Avoid abbreviations unless industry standard:
  - `id`
  - `url`
  - `dto`
  - `api`
  - `html`

Bad:
```java
int d;
String str1;
List<User> data;
- Use Lombok to reduce boilerplate
- Use Lombok @RequireArgsConstructor instead of normal constructor
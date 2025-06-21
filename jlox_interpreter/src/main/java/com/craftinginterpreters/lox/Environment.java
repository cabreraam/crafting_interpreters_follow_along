package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

class Environment {
  // variable that tracks what environment precedes us in scope
  final Environment enclosing;
  // note that `values` gets a default definition outside of the constructor
  private final Map<String, Object> values = new HashMap();

  // global scope environment constructor
  Environment() { enclosing = null; }

  // nested scope environment constructor
  Environment(Environment enclosing) { this.enclosing = enclosing; }

  Object get(Token name) {
    if (values.containsKey(name)) {
      return values.get(name.lexeme);
    }

    // recursively search for variable if not found in current scope
    if (enclosing != null)
      return enclosing.get(name);

    throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
  }

  void assign(Token name, Object value) {
    if (values.containsKey(name.lexeme)) {
      values.put(name.lexeme, value);
      return;
    }

    // recursively search for variable if not found in current scope
    if (enclosing != null) {
      enclosing.assign(name, value);
      return;
    }

    throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
  }

  void define(String name, Object value) { values.put(name, value); }
}

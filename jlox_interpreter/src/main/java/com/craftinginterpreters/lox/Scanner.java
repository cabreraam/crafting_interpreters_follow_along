package com.craftinginterpreters.lox;

import static com.craftinginterpreters.lox.TokenType.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Scanner {

  private static final Map<String, TokenType> keywords;

  static {
    keywords = new HashMap<>();
    keywords.put("and", AND);
    keywords.put("class", CLASS);
    keywords.put("else", ELSE);
    keywords.put("false", FALSE);
    keywords.put("for", FOR);
    keywords.put("fun", FUN);
    keywords.put("if", IF);
    keywords.put("nil", NIL);
    keywords.put("or", OR);
    keywords.put("print", PRINT);
    keywords.put("return", RETURN);
    keywords.put("super", SUPER);
    keywords.put("this", THIS);
    keywords.put("true", TRUE);
    keywords.put("var", VAR);
    keywords.put("while", WHILE);
  }

  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0;
  private int current = 0;
  private int line = 0;

  Scanner(String source) { this.source = source; }

  List<Token> scanTokens() {
    while (!isAtEnd()) {
      // We are at the beginning of the next lexeme.
      start = current;
      scanToken();
    }

    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }

  private void scanToken() {
    char c = advance();
    switch (c) {
    case '(':
      addToken(LEFT_PAREN);
      break;
    case ')':
      addToken(RIGHT_PAREN);
      break;
    case '{':
      addToken(LEFT_BRACE);
      break;
    case '}':
      addToken(RIGHT_BRACE);
      break;
    case ',':
      addToken(COMMA);
      break;
    case '.':
      addToken(DOT);
      break;
    case '-':
      addToken(MINUS);
      break;
    case '+':
      addToken(PLUS);
      break;
    case ';':
      addToken(SEMICOLON);
      break;
    case '*':
      addToken(STAR);
      break;

    case '!':
      addToken(match('=') ? BANG_EQUAL : BANG);
      break;

    case '=':
      addToken(match('=') ? EQUAL_EQUAL : EQUAL);
      break;

    case '<':
      addToken(match('=') ? LESS_EQUAL : LESS);
      break;

    case '>':
      addToken(match('=') ? GREATER_EQUAL : GREATER);
      break;

    case '/':
      if (match('/')) {
        // A comment goes until the end of the line.
        // note that we don't addToken at the end of the comment
        while (peek() != '\n' && !isAtEnd())
          advance();
      } else {
        addToken(SLASH);
      }
      break;

    // the next few cases ignore whitespace and then newline
    case ' ':
    case '\r':
    case '\t':
      break;

    case '\n':
      line++;
      break;

    // string literals
    case '"':
      string();
      break;

    default:
      if (isDigit(c)) {
        number();
      } else if (isAlpha(c)) {
        // if any lexeme starts with a letter or underscore, assume its an
        // identifier
        identifier();
      } else {
        Lox.error(line, "Unexpected character.");
        break;
      }
    }
  }

  private void identifier() {
    while (isAlphaNumeric(peek()))
      advance();

    String text = source.substring(start, current);
    TokenType type = keywords.get(text);
    if (type == null)
      type = IDENTIFIER;
    addToken(type);
  }

  private void number() {
    while (isDigit(peek()))
      advance();

    // Look for a fractional part.
    if (peek() == '.' && isDigit(peekNext())) {
      // consume the "."
      advance();

      while (isDigit(peek()))
        advance();
    }

    addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
  }

  private void string() {
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n')
        line++;
      advance();
    }

    if (isAtEnd()) {
      Lox.error(line, "Unterminated string.");
      return;
    }

    // The closing ".
    advance();

    String value = source.substring(start + 1, current - 1);
    addToken(STRING, value);
  }

  private boolean match(char expected) {
    if (isAtEnd())
      return false;
    // note:
    // because advance() uses post-increment operator, `current` is currently
    // indexing the character _after_ the one we're currently looking at
    if (source.charAt(current) != expected)
      return false;

    current++;
    return true;
  }

  // implements functionality for lookahead
  private char peek() {
    if (isAtEnd())
      return '\0';
    return source.charAt(current);
  }

  private char peekNext() {
    if (current + 1 >= source.length())
      return '\0';
    return source.charAt(current + 1);
  }

  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
  }

  private boolean isAlphaNumeric(char c) { return isAlpha(c) || isDigit(c); }

  private boolean isDigit(char c) { return c >= '0' && c <= '9'; }

  private boolean isAtEnd() { return current >= source.length(); }

  // note that this function uses post-increment
  private char advance() { return source.charAt(current++); }

  private void addToken(TokenType type) { addToken(type, null); }

  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }
}

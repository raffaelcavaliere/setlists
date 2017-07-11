package com.raffaelcavaliere.setlists.chordpro;

import java.util.Arrays;

public class Token {

  private Integer position;
  private Integer length;
  private TokenType type;

  public Token (Integer position, Integer length, TokenType type) {
    this.position = position;
    this.length = length;
    this.type = type;
  }

  public Token (Integer position, TokenType type) {
    this.position = position;
    this.type = type;
    this.length = 1;
  }

  public Integer getPosition() {
    return position;
  }

  public Integer getLength() {
    return length;
  }

  public TokenType getType() {
    return type;
  }

  public String getString(char[] charArray) {
    return new String(Arrays.copyOfRange(charArray, position, position + length));
  }
}

package com.raffaelcavaliere.setlists.chordpro;

public class DirectiveElement extends Element {

  private String content;
  private DirectiveElementType type;

  public DirectiveElement(DirectiveElementType type, String content) {
    this.content = content;
    this.type = type;
  }

  public String getContent() {
    return content;
  }

  public DirectiveElementType getType() {
    return type;
  }
}

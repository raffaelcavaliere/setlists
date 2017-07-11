package com.raffaelcavaliere.setlists.chordpro;

public class InlineElement extends Element {

  private String content;
  private InlineElementType type;

  public InlineElement (InlineElementType type, String content) {
    this.content = content;
    this.type = type;
  }

  public String getContent() {
    return content;
  }

  public InlineElementType getType() {
    return type;
  }
}

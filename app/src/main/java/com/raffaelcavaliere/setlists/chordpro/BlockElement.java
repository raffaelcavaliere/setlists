package com.raffaelcavaliere.setlists.chordpro;

import java.util.ArrayList;
import java.util.List;

public class BlockElement extends Element {

  private List<InlineElement> children;
  private String attribute;
  private BlockElementType type;

  public BlockElement(BlockElementType type) {
    this.type = type;
    this.attribute = null;
    children = new ArrayList<>();
  }

  public BlockElement(BlockElementType type, String attribute) {
    this.type = type;
    this.attribute = attribute;
    children = new ArrayList<>();
  }

  public String getAttribute() {
    return attribute;
  }

  public List<InlineElement> getChildren() {
    return children;
  }

  public BlockElementType getType() {
    return type;
  }
}

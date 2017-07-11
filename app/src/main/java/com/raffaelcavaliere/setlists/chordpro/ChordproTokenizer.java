package com.raffaelcavaliere.setlists.chordpro;

import java.util.ArrayList;
import java.util.List;

import static com.raffaelcavaliere.setlists.chordpro.TokenType.*;

public class ChordproTokenizer {

    private List<Token> tokenList = new ArrayList<>();
    private char[] dataBuffer = null;
    private int index = 0;
    private TokenType lastTokenType = null;

    public ChordproTokenizer (char[] data) {
        this.dataBuffer = data;
    }

    public void reinit () {
        this.tokenList.clear();
        this.index = 0;
    }

    public List<Token> getTokenList() {
    return tokenList;
  }

    public char[] getDataBuffer () {
    return dataBuffer;
  }

    public Token parseToken() {
        int i = index, last = tokenList.size() - 1;
        char nextChar = dataBuffer[i];
        switch (nextChar) {
            case '\n':
                if(last >= 0 && lastTokenType != CHORDPRO_CURLY_BRACKET_RIGHT) {
                    parseNewLine(i);
                } else {
                  lastTokenType = null;
                  index++;
                  return null;
                }
                index++;
                break;
            case '{' :
                parseLeftCurlyBracket(i);
                index++;
                break;
            case '}' :
                parseRightCurlyBracket(i);
                index++;
                break;
            case '[' :
                parseLeftSquareBracket(i);
                index++;
                break;
            case ']' :
                if(last >= 0 && lastTokenType == CHORDPRO_CHORD_TOKEN) {
                    parseRightSquareBracket(i);
                    index++;
                    break;
                }
            default:
                // Handle chord
                if (last >= 0 && lastTokenType == CHORDPRO_SQUARE_BRACKET_LEFT) {
                  index += parseChord(i) ;
                }
                // Handle tag key
                else if (last >= 0 && lastTokenType == CHORDPRO_CURLY_BRACKET_LEFT) {
                  index += parseTagKey(i);
                }
                // Handle delimiter
                else if(last >= 0 && lastTokenType == CHORDPRO_TAG_KEY_TOKEN && isValidDelimiter(dataBuffer[i])) {
                  parseDelimiter(i);
                  index++;
                }
                // Handle tag value
                else if(last >= 0 && lastTokenType == CHORDPRO_TAG_DELIMITER) {
                  index += parseTagValue(i);
                }
                // Handle text
                else {
                  index += parseText(i);
                }
        }
        lastTokenType = tokenList.get(tokenList.size()-1).getType();
        return tokenList.get(tokenList.size()-1);
    }

    private void parseNewLine(int position) {
        tokenList.add(new Token(position, CHORDPRO_NEWLINE_TOKEN));
    }

    private boolean isValidDelimiter(char c) {
        return (c == ':' || c == ' ');
    }

    private void parseLeftCurlyBracket (int position) {
        tokenList.add(new Token(position, CHORDPRO_CURLY_BRACKET_LEFT));
    }

    private void parseRightCurlyBracket (int position) {
        tokenList.add(new Token(position, CHORDPRO_CURLY_BRACKET_RIGHT));
    }

    private void parseLeftSquareBracket(int position) {
        tokenList.add(new Token(position, CHORDPRO_SQUARE_BRACKET_LEFT));
    }

    private void parseRightSquareBracket(int position) {
        tokenList.add(new Token(position, CHORDPRO_SQUARE_BRACKET_RIGHT));
    }

    private void parseDelimiter(int position) {
        tokenList.add(new Token(position, CHORDPRO_TAG_DELIMITER));
    }

    private int parseChord(int position) {
        int start = position, length;
        char current;
        do {
            current = dataBuffer[position];
            position++;
        } while (position < dataBuffer.length && !isChordEnd(current));
        if(isChordEnd(current)) {
            length = position - start -1;
        }
        else {
            length = position - start;
        }
        tokenList.add(new Token(start, length, CHORDPRO_CHORD_TOKEN));
        return length;
    }

    private boolean isChordEnd(char c) {
        return c == ']' || Character.isWhitespace(c);
    }

    private int parseTagKey(int position) {
        int start = position, length;
        char current;
        do {
            current = dataBuffer[position];
            position++;
        } while (position < dataBuffer.length && !isTagKeyEnd(current));
        if(isTagKeyEnd(current)) {
            length = position - start -1;
        }
        else {
            length = position - start;
        }
        tokenList.add(new Token(start, length, CHORDPRO_TAG_KEY_TOKEN));
        return length;
    }

    private boolean isTagKeyEnd(char c) {
        return c == ':' || c == '}' || Character.isWhitespace(c);
    }

    private int parseTagValue(int position) {
        int start = position, length;
        char current;
        do {
            current = dataBuffer[position];
            position++;
        } while (position < dataBuffer.length && !isTagValueEnd(current));
        if(isTagValueEnd(current)) {
            length = position - start -1;
        }
        else {
            length = position - start;
        }
        tokenList.add(new Token(start, length, CHORDPRO_TAG_VALUE_TOKEN));
        return length;
    }

    private boolean isTagValueEnd(char c) {
        return c == '\n' || c == '}';
    }

    private int parseText(int position) {
        int start = position, length;
        char current;
        do {
            current = dataBuffer[position];
            position++;
        } while (position < dataBuffer.length && !isTextEnd(current));
        if(isTextEnd(current)) {
            length = position - start -1;
        }
        else {
            length = position - start;
        }
        tokenList.add(new Token(start, length, CHORDPRO_TEXT_TOKEN));
        return length;
    }

    private boolean isTextEnd(char c) {
        return c == '\n' || c == '{' || c == '[';
    }

  public boolean hasNextToken () {
    return index < dataBuffer.length;
  }

    public List<Token> tokenize () {
        if(dataBuffer.length == 0) {
            return tokenList;
        }
        do {
            parseToken();
        } while(hasNextToken());
        return tokenList;
    }
}

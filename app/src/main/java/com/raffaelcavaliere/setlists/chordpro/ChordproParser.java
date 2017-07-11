package com.raffaelcavaliere.setlists.chordpro;

import android.util.Log;

import static com.raffaelcavaliere.setlists.chordpro.TokenType.*;
import static com.raffaelcavaliere.setlists.chordpro.BlockElementType.*;
import static com.raffaelcavaliere.setlists.chordpro.InlineElementType.*;
import static com.raffaelcavaliere.setlists.chordpro.DirectiveElementType.*;

public class ChordproParser {

    private ChordproTokenizer tokenizer;
    private ChordproRoot chordproRoot;

    public ChordproParser(ChordproTokenizer tokenizer) {
        this.tokenizer = tokenizer;
        chordproRoot = new ChordproRoot();
    }

    public ChordproRoot parseTokenizer() {
        BlockElement currentBlock = null;
        while (tokenizer.hasNextToken()) {
            Token token = tokenizer.parseToken();
            if (token != null) {
                if (token.getType() == CHORDPRO_CURLY_BRACKET_LEFT) { // left curly bracket
                    Element currentElement = parseTag();
                    if (currentElement instanceof DirectiveElement && ((DirectiveElement) currentElement).getType() == CHORDPRO_DEFINE) {
                        chordproRoot.getDefinitions().add(currentElement);
                    } else if (currentBlock == null && currentElement != null) {
                        if (currentElement instanceof BlockElement) {
                            currentBlock = (BlockElement) currentElement;
                        }
                        chordproRoot.getElements().add(currentElement);
                    } else {
                        if (currentElement != null) {
                            currentBlock.getChildren().add((InlineElement) currentElement);
                        } else {
                            currentBlock = null;
                        }
                    }
                    tokenizer.parseToken(); // right curly bracket
                } else if (token.getType() == CHORDPRO_SQUARE_BRACKET_LEFT) { // left square bracket
                    Element element = parseChord();
                    if (currentBlock != null) {
                        currentBlock.getChildren().add((InlineElement) element);
                    } else {
                        chordproRoot.getElements().add(element);
                    }
                    tokenizer.parseToken(); // right square bracket
                } else {
                    Element element;
                    if (token.getType() == CHORDPRO_NEWLINE_TOKEN)
                        element = parseBreak();
                    else
                        element = parseLyric(token);
                    if (currentBlock != null) {
                        currentBlock.getChildren().add((InlineElement) element);
                    } else {
                        chordproRoot.getElements().add(element);
                    }
                }
            }
        }
        return chordproRoot;
    }

    private Element parseTag() {
        Token token = tokenizer.parseToken();
        String tagKey = token.getString(tokenizer.getDataBuffer());
        if (tagKey.equals("t") || tagKey.equals("title")) {
            chordproRoot.setTitle(parseDirectiveElement(CHORDPRO_TITLE, tokenizer).getContent());
            return null;
        } else if (tagKey.equals("st") || tagKey.equals("subtitle")) {
            chordproRoot.setSubtitle(parseDirectiveElement(CHORDPRO_SUBTITLE, tokenizer).getContent());
            return null;
        } else if (tagKey.equals("k") || tagKey.equals("key")) {
            chordproRoot.setKey(parseDirectiveElement(CHORDPRO_KEY, tokenizer).getContent());
            return null;
        } else if (tagKey.equals("composer")) {
            chordproRoot.setComposer(parseDirectiveElement(CHORDPRO_COMPOSER, tokenizer).getContent());
            return null;
        } else if (tagKey.equals("arranger")) {
            chordproRoot.setArranger(parseDirectiveElement(CHORDPRO_ARRANGER, tokenizer).getContent());
            return null;
        } else if (tagKey.equals("lyricist")) {
            chordproRoot.setLyricist(parseDirectiveElement(CHORDPRO_LYRICIST, tokenizer).getContent());
            return null;
        } else if (tagKey.equals("album")) {
            chordproRoot.setAlbum(parseDirectiveElement(CHORDPRO_ALBUM, tokenizer).getContent());
            return null;
        } else if (tagKey.equals("copyright")) {
            chordproRoot.setCopyright(parseDirectiveElement(CHORDPRO_COPYRIGHT, tokenizer).getContent());
            return null;
        } else if (tagKey.equals("year")) {
            chordproRoot.setYear(parseDirectiveElement(CHORDPRO_YEAR, tokenizer).getContent());
            return null;
        } else if (tagKey.equals("tempo")) {
            chordproRoot.setTempo(parseDirectiveElement(CHORDPRO_TEMPO, tokenizer).getContent());
            return null;
        } else if (tagKey.equals("time")) {
            chordproRoot.setTime(parseDirectiveElement(CHORDPRO_TIME, tokenizer).getContent());
            return null;
        } else if (tagKey.equals("duration")) {
            chordproRoot.setDuration(parseDirectiveElement(CHORDPRO_DURATION, tokenizer).getContent());
            return null;
        } else {
            switch (tagKey) {
                case "c":
                case "comment":
                    return parseDirectiveElement(CHORDPRO_COMMENT, tokenizer);
                case "comment_italic":
                    return parseDirectiveElement(CHORDPRO_COMMENT_ITALIC, tokenizer);
                case "comment_box":
                    return parseDirectiveElement(CHORDPRO_COMMENT_BOX, tokenizer);
                case "d":
                case "define":
                    return parseDirectiveElement(CHORDPRO_DEFINE, tokenizer);
                case "soc":
                case "start_of_chorus":
                    return parseBlockElement(CHORDPRO_CHORUS, tokenizer);
                case "eoc":
                case "end_of_chorus":
                    return null;
                case "sov":
                case "start_of_verse":
                    return parseBlockElement(CHORDPRO_VERSE, tokenizer);
                case "eov":
                case "end_of_verse":
                    return null;
                case "soh":
                case "start_of_highlight":
                    return parseBlockElement(CHORDPRO_HIGHLIGHT, tokenizer);
                case "eoh":
                case "end_of_highlight":
                    return null;
                case "sot":
                case "start_of_tab":
                    return parseBlockElement(CHORDPRO_TAB, tokenizer);
                case "eot":
                case "end_of_tab":
                    return null;
                default:
                    Log.d("UNKNOWN TAG", tagKey);
                    return null;
            }
        }
    }

    private Element parseChord() {
        Token token = tokenizer.parseToken();
        InlineElement e = new InlineElement(CHORDPRO_CHORD, token.getString(tokenizer.getDataBuffer()));
        return e;
    }

    private Element parseLyric(Token token) {
        InlineElement e = new InlineElement(CHORDPRO_LYRIC, token.getString(tokenizer.getDataBuffer()));
        return e;
    }

    private Element parseBreak() {
        return new InlineElement(CHORDPRO_BREAK, null);
    }

    private DirectiveElement parseDirectiveElement(DirectiveElementType elementType, ChordproTokenizer tokenizer) {
        tokenizer.parseToken();
        String value = tokenizer.parseToken().getString(tokenizer.getDataBuffer()); // tag value
        return new DirectiveElement(elementType, value);
    }

    private BlockElement parseBlockElement(BlockElementType elementType, ChordproTokenizer tokenizer) {
        BlockElement element;
        Token token = tokenizer.parseToken(); // delimiter?
        if (token.getType() == CHORDPRO_TAG_DELIMITER) {
            String value = tokenizer.parseToken().getString(tokenizer.getDataBuffer()); // tag value
            element = new BlockElement(elementType, value);
        } else {
            element = new BlockElement(elementType);
        }
        return element;
    }
}

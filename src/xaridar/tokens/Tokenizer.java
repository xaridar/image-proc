package xaridar.tokens;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {

    private int counter;
    private final String str;

    public Tokenizer(String argsStr) {
        counter = 0;
        str = argsStr;
    }

    public void advance() {
        counter++;
    }

    public void advance(int n)  {
        counter += n;
    }

    public List<Token> tokenize() {
        List<Token> out = new ArrayList<>();
        while (counter < str.length()) {
            char curr = str.charAt(counter);
            switch (curr) {
                case ' ':
                    break;
                case '$':
                    out.add(new Token(Token.TokenType.SERIES));
                    break;
                case ';':
                    out.add(new Token(Token.TokenType.PARALLEL));
                    break;
                case '~':
                    out.add(new Token(Token.TokenType.AND));
                    break;
                case '+':
                    out.add(new Token(Token.TokenType.SPLIT));
                    break;
                case '(':
                    out.add(new Token(Token.TokenType.OPEN_PAREN));
                    break;
                case ')':
                    out.add(new Token(Token.TokenType.CLOSE_PAREN));
                    break;
                default:
                    out.add(getString());
                    continue;
            }
            advance();
        }
        return out;
    }

    public Token getString() {
        String strToMatch = str.substring(counter);
        Matcher m = Pattern.compile("^([^\\s()$~+;]|(?<=,)\\s)*").matcher(strToMatch);
        if (m.find()) {
            String ret = m.group();
            advance(ret.length());
            return new Token(ret);
        }
        return null;
    }
}

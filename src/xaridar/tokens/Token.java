package xaridar.tokens;

public class Token {
    public enum TokenType { OPEN_PAREN, CLOSE_PAREN, STRING, PARALLEL, AND, SERIES, SPLIT }

    public TokenType type;
    public String val;

    public Token(TokenType type) {
        this.type = type;
        this.val = null;
    }
    public Token(String val) {
        this.type = TokenType.STRING;
        this.val = val;
    }
}

package at.doml.fpgasolver.parsing.exceptions;

import at.doml.fpgasolver.parsing.lexical.LexicalUnit;
import java.util.List;

public class LexicalException extends RuntimeException {
    
    private static final long serialVersionUID = 8638160199591178954L;
    
    private final List<LexicalUnit> lexicalUnits;
    
    public LexicalException(List<LexicalUnit> lexicalUnits) {
        this.lexicalUnits = lexicalUnits;
    }
    
    public List<LexicalUnit> getLexicalUnits() {
        return lexicalUnits;
    }
}

package at.doml.fpgasolver.args.exceptions;

public class ParameterException extends RuntimeException {
    
    private static final long serialVersionUID = 5505581386017241340L;
    
    public ParameterException(String cause) {
        super(cause);
    }
}

package at.dom_l.fpga_solver.args;

import at.dom_l.fpga_solver.args.abstracts.AbstractArgument;
import java.util.function.BiConsumer;

public class ShortArgument extends AbstractArgument {
    
    private final char argChar;
    
    public ShortArgument(char argChar, int numOfParams,
            BiConsumer<String[], ArgumentParser.PropertySetter> parameterParser) {
        super(numOfParams, parameterParser);
        this.argChar = argChar;
    }
    
    @Override
    public String getArgKey() {
        return Character.toString(argChar);
    }
}
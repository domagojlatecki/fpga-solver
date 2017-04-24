package at.dom_l.fpga_solver.args;

import at.dom_l.fpga_solver.args.abstracts.AbstractArgument;
import java.util.function.BiConsumer;

public class LongArgument extends AbstractArgument {
    
    private final String argString;
    
    public LongArgument(String argString, int numOfParams,
            BiConsumer<String[], ArgumentParser.PropertySetter> parameterParser) {
        super(numOfParams, parameterParser);
        this.argString = argString;
    }
    
    @Override
    public String getArgKey() {
        return argString;
    }
}
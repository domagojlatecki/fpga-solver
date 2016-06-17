/* 
 * The MIT License (MIT)
 * 
 * Copyright © 2016 Domagoj Latečki
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package hr.dlatecki.clb.args;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import hr.dlatecki.clb.args.abstracts.AbstractArgument;
import hr.dlatecki.clb.args.exceptions.MissingPropertyException;
import hr.dlatecki.clb.args.exceptions.ParameterException;
import hr.dlatecki.clb.args.exceptions.UnknownArgumentException;

public class ArgumentParser {
    
    private String[] remainingArgs;
    private final PropertySetter propertySetter;
    private final Map<String, AbstractArgument> arguments;
    private final Map<String, Object> properties;
    
    public ArgumentParser(Set<FieldKey<?>> defaultProperties, AbstractArgument... arguments) {
        this.arguments = new HashMap<>();
        
        properties = defaultProperties.stream()
                .collect(Collectors.toMap(FieldKey::getName, FieldKey::getDefaultValue));
        propertySetter = new PropertySetter();
        
        for (AbstractArgument argument : arguments) {
            this.arguments.put(argument.getArgKey(), argument);
        }
    }
    
    public void parse(String[] args) {
        if (args.length == 0) {
            remainingArgs = args;
        }
        
        int index = 0;
        int currentParam = 0;
        int numOfParams = 0;
        boolean continueParsing = true;
        
        List<String> params = new ArrayList<>();
        List<AbstractArgument> activeArguments = new ArrayList<>();
        
        while (continueParsing && index < args.length) {
            String currentArg = args[index++];
            
            if (currentArg.startsWith("--")) {
                parseActiveArguments(activeArguments, params);
                
                activeArguments.clear();
                params.clear();
                currentParam = 0;
                numOfParams = 0;
                
                parseLongArgument(currentArg.substring(2));
            } else if (currentArg.startsWith("-")) {
                parseActiveArguments(activeArguments, params);
                
                List<AbstractArgument> shortArgs = getShortArguments(currentArg.substring(1));
                
                int expectedParams = -1;
                
                for (AbstractArgument shortArg : shortArgs) {
                    if (expectedParams == -1) {
                        expectedParams = shortArg.getNumOfParams();
                    } else if (expectedParams != shortArg.getNumOfParams()) {
                        throw new ParameterException("Arguments " + argsToString(shortArgs)
                                + " don't expect same number of parameters.");
                    }
                }
                
                if (expectedParams == 0) {
                    String[] emptyArray = new String[0];
                    
                    shortArgs.forEach(shortArg -> shortArg.parseParameters(emptyArray, propertySetter));
                    activeArguments.clear();
                    numOfParams = 0;
                } else {
                    numOfParams = expectedParams;
                    activeArguments = shortArgs;
                }
                
                params.clear();
                currentParam = 0;
            } else if (currentParam < numOfParams) {
                params.add(currentArg);
                currentParam++;
            } else {
                parseActiveArguments(activeArguments, params);
                
                index--;
                continueParsing = false;
            }
        }
        
        if (!activeArguments.isEmpty()) {
            parseActiveArguments(activeArguments, params);
        }
        
        remainingArgs = Arrays.copyOfRange(args, index, args.length);
    }
    
    private static String argsToString(List<AbstractArgument> args) {
        StringBuilder builder = new StringBuilder();
        
        for (AbstractArgument arg : args) {
            builder.append("-").append(arg.getArgKey()).append(", ");
        }
        
        return builder.substring(0, builder.length() - 2);
    }
    
    private void parseActiveArguments(List<AbstractArgument> activeArgs, List<String> parameters) {
        String[] params = parameters.toArray(new String[parameters.size()]);
        
        activeArgs.forEach(activeArg -> activeArg.parseParameters(params, propertySetter));
    }
    
    private List<AbstractArgument> getShortArguments(String argument) {
        List<AbstractArgument> arguments = new ArrayList<>();
        
        for (char c : argument.toCharArray()) {
            AbstractArgument arg = this.arguments.get(Character.toString(c));
            
            if (arg == null) {
                throw new UnknownArgumentException(Character.toString(c));
            }
            
            arguments.add(arg);
        }
        
        return arguments;
    }
    
    private void parseLongArgument(String argument) {
        String argName = argument;
        String[] argParams = new String[0];
        
        if (argument.contains("=")) {
            String[] split = argument.split("=", 2);
            
            argName = split[0];
            argParams = split[1].split(",");
        }
        
        AbstractArgument arg = arguments.get(argName);
        
        if (arg == null) {
            throw new UnknownArgumentException(argName);
        }
        
        arg.parseParameters(argParams, propertySetter);
    }
    
    public <T> T getProperty(FieldKey<T> key) {
        @SuppressWarnings("unchecked")
        T property = (T) properties.get(key.getName());
        
        if (property == null) {
            throw new MissingPropertyException(key.getName());
        }
        
        return property;
    }
    
    public String[] getRemainingArgs() {
        return remainingArgs;
    }
    
    public class PropertySetter {
        
        private PropertySetter() {}
        
        public <T> void setProperty(FieldKey<T> key, T value) {
            properties.put(key.getName(), value);
        }
    }
}

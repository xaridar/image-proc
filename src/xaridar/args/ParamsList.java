package xaridar.args;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ParamsList {
    public UnnamedArgsInfo unnamedArgsInfo;
    public List<Param> params;
    public Map<String[], Integer> reqSets = new HashMap<>();
    public Map<String[], Integer> conflictingSets = new HashMap<>();

    public ParamsList(List<Param> params) {
        this(null, params);
    }

    public ParamsList(Param... params) {
        this(null, Arrays.asList(params));
    }

    public ParamsList(List<Param> params, Map<String[], Integer> requiredSets) {
        this(null, params);
        this.reqSets = requiredSets;
    }

    public ParamsList(List<Param> params, Map<String[], Integer> requiredSets, Map<String[], Integer> conflictingSets) {
        this(null, params);
        this.reqSets = requiredSets;
        this.conflictingSets = conflictingSets;
    }

    public ParamsList(@Nullable UnnamedArgsInfo unnamedArgsInfo, List<Param> params) {
        this.unnamedArgsInfo = unnamedArgsInfo;
        this.params = params;
        for (Param p : params) {
            if (p.error != null) {
                p.error.printStackTrace();
                System.exit(-1);
            }
        }
    }

    public ParamsList(UnnamedArgsInfo unnamedArgsInfo, Param... params) {
        this(unnamedArgsInfo, Arrays.asList(params));
    }

    public ParamsList(UnnamedArgsInfo unnamedArgsInfo, List<Param> params, Map<String[], Integer> requiredSets) {
        this(unnamedArgsInfo, params);
        this.reqSets = requiredSets;
    }

    public ParamsList(UnnamedArgsInfo unnamedArgsInfo, List<Param> params, Map<String[], Integer> requiredSets, Map<String[], Integer> conflictingSets) {
        this(unnamedArgsInfo, params);
        this.reqSets = requiredSets;
        this.conflictingSets = conflictingSets;
    }

    public boolean hasUnnamed() {
        return unnamedArgsInfo != null;
    }

    public ArgsObj parse(String[] args, String op) throws ArgError {
        ArgsObj ret = new ArgsObj();
        boolean reachedNamedArgs = false;
        List unnamedArgs = new ArrayList<>();
        // Parses passed arguments
        for (int i = 0; i < args.length; i++) {
            String paramName = args[i];
            if (paramName.matches("-[^\\d.]\\S*")) reachedNamedArgs = true;
            if (!reachedNamedArgs && unnamedArgsInfo != null) {
                ParsedArg parsed = unnamedArgsInfo.type.validate(paramName);
                if (parsed.isError()) throw new ArgError(op + ": Bad input for '" + unnamedArgsInfo.name + "' input #" + (i + 1) + " - expected " + unnamedArgsInfo.type.getType());
                unnamedArgs.add(parsed.getValue());
            } else {
                Param param = findParam(paramName);
                if (param == null) {
                    if (!reachedNamedArgs) throw new ArgError("Operation " + op + " does not take unnamed parameters");
                    else throw new ArgError(op + ": Parameter not found: " + paramName);
                }
                List<?> argObjs = param.validate(Arrays.copyOfRange(args, i + 1, i + 1 + param.types.size()), op);
                i += param.types.size();
                if (argObjs.size() > 1) ret.put(param.fullName, argObjs);
                else ret.put(param.fullName, argObjs.get(0));
            }
        }

        // Sets unnamed parameter
        if (unnamedArgsInfo != null) {
            if (unnamedArgsInfo.max == unnamedArgsInfo.required && unnamedArgsInfo.required != unnamedArgs.size()) throw new ArgError("Exactly " + unnamedArgsInfo.required + " unnamed arguments are required for operation " + op);
            if (unnamedArgsInfo.required > unnamedArgs.size()) throw new ArgError("At least " + unnamedArgsInfo.required + " unnamed arguments are required for operation " + op);
            if (unnamedArgsInfo.max != -1 && unnamedArgsInfo.max < unnamedArgs.size()) throw new ArgError("At most " + unnamedArgsInfo.required + " unnamed arguments are allowed for operation " + op);
            if (unnamedArgs.size() > 0) {
                if (unnamedArgsInfo.required == 1 && unnamedArgsInfo.max == 1) ret.put(unnamedArgsInfo.name, unnamedArgs.get(0));
                else ret.put(unnamedArgsInfo.name, unnamedArgs);
            }
        }

        // Adds defaults and checks for missing required parameters
        for (Param param : params) {
            if (!ret.exists(param.fullName)) {
                if (param.defaultVals.size() != 0) {
                    if (param.defaultVals.size() > 1) ret.put(param.fullName, param.defaultVals);
                    else ret.put(param.fullName, param.defaultVals.get(0));
                }
                else if (!param.optional) throw new ArgError(op + ": Missing required parameter: " + param.fullName);
            }
        }

        // Checks that limitations in reqSets are met
        List<String[]> reqFiltered = reqSets.keySet().stream().filter((set) -> {
            int numUsed = (int) Arrays.stream(set).filter(ret::exists).count();
            int count = reqSets.get(set);
            return numUsed < count;
        }).collect(Collectors.toList());
        if (reqFiltered.size() > 0) {
            String[] set = reqFiltered.get(0);
            int count = reqSets.get(set);
            throw new ArgError(op + ": Missing required parameter; Must provide at least " + count + " of the following: " + String.join(", ", set));
        }

        // Checks that limitations in conflictingSets are met
        List<String[]> confFiltered = conflictingSets.keySet().stream().filter((set) -> {
            int numUsed = (int) Arrays.stream(set).filter(ret::exists).count();
            int count = conflictingSets.get(set);
            return numUsed > count;
        }).collect(Collectors.toList());
        if (confFiltered.size() > 0) {
            String[] set = confFiltered.get(0);
            int count = conflictingSets.get(set);
            throw new ArgError(op + ": Can only use " + count + " of: " + String.join(", ", set));
        }
        return ret;
    }

    public Param findParam(String paramName) {
        for (Param p : params) {
            if (("--" + p.fullName).equals(paramName) || (p.shortName.length() > 0 && ("-" + p.shortName).equals(paramName))) return p;
        }
        return null;
    }

    public static class Param {
        public final List<ParamType<?>> types;
        public final String fullName;
        public final String shortName;
        public ArgError error = null;
        public List<Object> defaultVals = new ArrayList<>();
        public final boolean optional;
        public final String description;

        public Param(String fullName, String shortName, List<ParamType<?>> types, boolean optional, String description) {
            this.fullName = fullName;
            this.shortName = shortName;
            this.types = types;
            this.optional = optional;
            this.description = description;
        }

        public Param(String fullName, String shortName, List<ParamType<?>> types, String description) {
            this(fullName, shortName, types, false, description);
        }

        public Param(String fullName, String shortName, List<ParamType<?>> types, List<Object> defaults, String description) {
            this(fullName, shortName, types, false, description);

            if (defaults.size() != types.size()) error = new ArgError("Argument " + fullName + " requires " + types.size() + " inputs, but " + defaults.size() + " default parameters were provided");
            for (int i = 0; i < defaults.size(); i++) {
                Object defaultVal = defaults.get(i);
                ParsedArg<?> parsed = types.get(i).validate(String.valueOf(defaultVal));
                if (parsed.isError()) error = new ArgError("Bad default argument for input #" + (i + 1) + " of parameter " + fullName + "; should be of type " + types.get(i).getType());
            }
            defaultVals = defaults;
        }

        public List<Object> validate(String[] args, String op) throws ArgError {
            String[] nonNull = Arrays.stream(args).filter(Objects::nonNull).toArray(String[]::new);
            List<Object> parsedValues = new ArrayList<>(types.size());
            if (nonNull.length != types.size()) throw new ArgError(op + ":Argument " + fullName + " requires " + types.size() + " inputs, but " + nonNull.length + " were provided");
            if (nonNull.length == 0) return List.of(true);
            for (int i = 0; i < types.size(); i++) {
                ParamType<?> param = types.get(i);

                ParsedArg<?> parsed = param.validate(nonNull[i]);
                if (parsed.isError()) throw new ArgError(op + ":Bad input for input #" + (i + 1) + " to argument '" + fullName + "' - expected " + types.get(i).getType());
                parsedValues.add(parsed.getValue());
            }
            return parsedValues;
        }
    }
}
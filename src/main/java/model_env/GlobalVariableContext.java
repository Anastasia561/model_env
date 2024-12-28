package model_env;

import java.util.HashMap;
import java.util.Map;

public class GlobalVariableContext {
    private static final Map<String, Object> context = new HashMap<>();

    public static Map<String, Object> getGlobalContext() {
        return context;
    }

    public static void setVariable(String name, Object value) {
        context.put(name, value);
    }

    public static Object getVariable(String name) {
        return context.get(name);
    }
}

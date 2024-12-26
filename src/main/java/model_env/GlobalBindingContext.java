package model_env;

import groovy.lang.Binding;

public class GlobalBindingContext {
    private static Binding globalBinding = new Binding();

    public static Binding getGlobalBinding() {
        return globalBinding;
    }

    public static void setVariable(String name, Object value) {
        globalBinding.setVariable(name, value);
    }

    public static Object getVariable(String name) {
        return globalBinding.getVariable(name);
    }
}

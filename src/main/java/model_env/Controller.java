package model_env;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import model_env.model.Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Controller {
    private static final String PACKAGE_NAME = "model_env.model.";
    private static final String SCRIPTS_DIR_NAME = "src/main/resources/scripts/";
    private static final String DATA_DIR_NAME = "src/main/resources/data/";
    private final Model model;
    private String[] years;
    private GroovyShell groovyShell;
    private Binding binding;

    public Controller(String modelName) {
        try {
            model = (Model) Class.forName(PACKAGE_NAME + modelName).getDeclaredConstructors()[0].newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Controller readDataFrom(String fname) {
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_DIR_NAME + fname))) {
            String line;
            int ll = 1;
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("\\s+", " ").trim();

                if (line.startsWith("LATA")) {
                    ll = initLLField(line);
                } else {
                    initModelField(line, ll);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        saveVariablesFromModel();
        return this;
    }

    public Controller runModel() {
        addVariablesToModel();
        model.run();
        saveVariablesFromModel();
        return this;
    }

    public Controller runScriptFromFile(String fname) {
        try {
            addVariablesToScript();
            groovyShell = new GroovyShell(binding);
            groovyShell.evaluate(new File(SCRIPTS_DIR_NAME + fname));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        saveVariablesFromScript();
        return this;
    }

    public Controller runScript(String script) {
        try {
            addVariablesToScript();
            groovyShell = new GroovyShell(binding);
            groovyShell.evaluate(script);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        saveVariablesFromScript();
        return this;
    }

    public String getResultsAsTsv() {
        StringBuilder builder = new StringBuilder();
        for (String year : years) {
            builder.append(year).append("\t");
        }
        builder.append("\n");

        addModelFieldsToOutput(builder);

        if (groovyShell != null) {
            addScriptFieldsToOutput(builder);
        }
        return builder.toString();
    }

    private void initModelField(String line, int ll) {
        double[] values = parseValues(line);
        if (values.length < ll) {
            values = addValues(ll, values);
        }

        Field field = getField(model.getClass(), (line.split(" ")[0]));
        if (field != null && field.isAnnotationPresent(Bind.class)) {
            field.setAccessible(true);
            try {
                field.set(model, values);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private int initLLField(String line) {
        int ll;
        years = line.split(" ");
        try {
            Field LLfield = model.getClass().getDeclaredField("LL");
            LLfield.setAccessible(true);
            ll = years.length - 1;
            LLfield.set(model, ll);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return ll;
    }


    private void addScriptFieldsToOutput(StringBuilder builder) {
        Map<String, double[]> scriptResults = getResultsFromScript();
        for (String name : scriptResults.keySet()) {
            if (!builder.toString().contains(name)) {
                builder.append(name).append("\t");
                for (double value : scriptResults.get(name)) {
                    builder.append(value).append("\t");
                }
                builder.append("\n");
            }
        }
    }

    private void addModelFieldsToOutput(StringBuilder builder) {
        for (Field field : model.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (!field.getName().equals("LL") && field.isAnnotationPresent(Bind.class)) {
                builder.append(field.getName()).append("\t");
                try {
                    double[] values = (double[]) field.get(model);
                    for (double value : values) {
                        builder.append(value).append("\t");
                    }

                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                builder.append("\n");
            }
        }
    }

    private Map<String, double[]> getResultsFromScript() {
        Map<String, double[]> varNamesToValues = new HashMap<>();

        Map variables = groovyShell.getContext().getVariables();
        for (Object name : variables.keySet()) {
            String varName = (String) name;
            if (!varName.matches("[a-z]")) {
                if (variables.get(varName) instanceof double[]) {
                    varNamesToValues.put(varName, (double[]) variables.get(varName));
                }
            }
        }
        return varNamesToValues;
    }

    private double[] parseValues(String line) {
        String[] parts = line.split(" ");
        double[] values = new double[parts.length - 1];
        for (int i = 1; i < parts.length; i++) {
            values[i - 1] = Double.parseDouble(parts[i]);
        }
        return values;
    }

    private double[] addValues(int ll, double[] values) {
        double[] newValues = new double[ll];
        System.arraycopy(values, 0, newValues, 0, values.length);
        for (int i = values.length; i < newValues.length; i++) {
            newValues[i] = values[values.length - 1];
        }
        return newValues;
    }

    private void saveVariablesFromModel() {
        Field[] fields = model.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Bind.class)) {
                field.setAccessible(true);
                try {
                    Object value = field.get(model);
                    if (value != null) {
                        GlobalVariableContext.setVariable(field.getName(), field.get(model));
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void addVariablesToModel() {
        for (String varName : GlobalVariableContext.getGlobalContext().keySet()) {
            Field field = getField(model.getClass(), varName);
            if (field != null) {
                field.setAccessible(true);
                try {
                    Object value = GlobalVariableContext.getVariable(varName);
                    if (value != null) {
                        field.set(model, value);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void saveVariablesFromScript() {
        for (Object name : binding.getVariables().keySet()) {
            String varName = (String) name;
            if (!varName.matches("[a-z]")) {
                GlobalVariableContext.setVariable(varName, binding.getVariable(varName));
            }
        }
    }

    private void addVariablesToScript() {
        binding = new Binding();
        for (String varName : GlobalVariableContext.getGlobalContext().keySet()) {
            binding.setVariable(varName, GlobalVariableContext.getVariable(varName));
        }
    }

    public Field getField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
}

package model_env;

import groovy.lang.GroovyShell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Controller {
    private final Model model;
    private String[] years;
    private GroovyShell groovyShell;
    //private Binding binding;

    public Controller(String modelName) {
        try {
            model = (Model) Class.forName("model_env." + modelName).getDeclaredConstructors()[0].newInstance();
            //binding = new Binding();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void readDataFrom(String fname) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fname))) {
            String line;
            int ll = 1;
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("\\s+", " ").trim();
                if (line.startsWith("LATA")) {
                    years = line.split(" ");
                    Field LLfield = model.getClass().getDeclaredField("LL");
                    LLfield.setAccessible(true);
                    ll = years.length - 1;
                    LLfield.set(model, ll);

                    //GlobalBindingContext.setVariable(LLfield.getName(), LLfield.get(model));
                } else {
                    double[] values = parseValues(line);
                    if (values.length < ll) {
                        values = addValues(ll, values);
                    }

                    Field field = getField(model.getClass(), (line.split(" ")[0]));
                    if (field != null && field.isAnnotationPresent(Bind.class)) {
                        field.setAccessible(true);
                        field.set(model, values);

                        //GlobalBindingContext.setVariable(field.getName(), field.get(model));
                    }
                }
            }
        } catch (IOException | IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        saveVariablesFromModel(model);
    }

    public void runModel() {
        addVariablesToModel(model);
        model.run();
        saveVariablesFromModel(model);
    }

    public void runScriptFromFile(String fname) {
        try {
//            for (Field field : model.getClass().getDeclaredFields()) {
//                if (field.isAnnotationPresent(Bind.class)) {
//                    field.setAccessible(true);
//                    //binding.setVariable(field.getName(), field.get(model));
//                }
//            }

            groovyShell = new GroovyShell(GlobalBindingContext.getGlobalBinding());
            groovyShell.evaluate(new File("src\\main\\java\\model_env\\" + fname));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void runScript(String script) {
        try {
//            for (Field field : model.getClass().getDeclaredFields()) {
//                if (field.isAnnotationPresent(Bind.class)) {
//                    field.setAccessible(true);
//                    binding.setVariable(field.getName(), field.get(model));
//                }
//            }

            groovyShell = new GroovyShell(GlobalBindingContext.getGlobalBinding());
            groovyShell.evaluate(script);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getResultsAsTsv() {
        StringBuilder builder = new StringBuilder();
        for (String year : years) {
            builder.append(year).append("\t");
        }
        builder.append("\n");

        for (Field field : model.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (!field.getName().equals("LL") && field.isAnnotationPresent(Bind.class)) {
                builder.append(field.getName()).append("\t");
                try {
                    double[] values = (double[]) field.get(model);
                    for (double value : values) {
                        builder.append(value).append("\t");
                    }

                    // GlobalBindingContext.setVariable(field.getName(), field.get(model));

                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                builder.append("\n");
            }
        }
        Map<String, double[]> scriptResults = getResultsFromScript();
        for (String name : scriptResults.keySet()) {
            if (!builder.toString().contains(name)) {
                builder.append(name).append("\t");

                GlobalBindingContext.setVariable(name, scriptResults.get(name));

                for (double value : scriptResults.get(name)) {
                    builder.append(value).append("\t");
                }
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    private Map<String, double[]> getResultsFromScript() {
        Map<String, double[]> varNamesToValues = new HashMap<>();
        Map variables = GlobalBindingContext.getGlobalBinding().getVariables();
        for (Object name : GlobalBindingContext.getGlobalBinding().getVariables().keySet()) {
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

    private void saveVariablesFromModel(Model model) {
        Field[] fields = model.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Bind.class)) {
                field.setAccessible(true);
                try {
                    GlobalBindingContext.setVariable(field.getName(), field.get(model));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void addVariablesToModel(Model model) {
        for (Object name : GlobalBindingContext.getGlobalBinding().getVariables().keySet()) {
            String varName = (String) name;
            Field field = getField(model.getClass(), varName);
            if (field != null) {
                field.setAccessible(true);
                try {
                    field.set(model, GlobalBindingContext.getVariable(varName));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

            }
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

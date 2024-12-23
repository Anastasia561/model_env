package model_env;

import groovy.lang.GroovyShell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class Controller {
    private Object model;
    private List<Field> fields;
    private String[] years;

    public Controller(String modelName) {
        try {
            this.model = Class.forName("model_env." + modelName).getDeclaredConstructors()[0].newInstance();
            fields = Arrays.stream(model.getClass().getDeclaredFields()).toList();
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
                } else {
                    double[] values = parseValues(line);
                    if (values.length < ll) {
                        values = addValues(ll, values);
                    }
                    Field field = model.getClass().getDeclaredField(line.split(" ")[0]);
                    field.setAccessible(true);
                    field.set(model, values);
                }
            }
        } catch (IOException | IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public void runModel() {
        if (model instanceof Model1) {
            ((Model1) model).run();
        }
    }

    public void runScriptFromFile(String fname) {
        try {
            GroovyShell shell = new GroovyShell();
            shell.evaluate(new File("src\\main\\java\\model_env\\" + fname));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void runScript(String script) {
        try {
            GroovyShell shell = new GroovyShell();
            shell.evaluate(script);
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
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Bind.class) && !field.getName().equals("LL")) {
                builder.append(field.getName()).append("\t");
                try {
                    if (field.get(model) instanceof double[]) {
                        double[] values = (double[]) field.get(model);
                        for (double value : values) {
                            builder.append(value).append("\t");
                        }
                    }
                    System.out.println();
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                builder.append("\n");
            }
        }
        return builder.toString();
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
}
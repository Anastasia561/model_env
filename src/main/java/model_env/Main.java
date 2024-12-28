package model_env;

public class Main {
    public static void main(String[] args) {
        String results = new Controller("Model1")
                .readDataFrom("src/main/resources/data/data1.txt")
                .runModel()
                .runScriptFromFile("script1.groovy")
                .getResultsAsTsv();

        System.out.println(results);
    }
}

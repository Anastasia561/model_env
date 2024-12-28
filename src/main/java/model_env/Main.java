package model_env;

public class Main {
    public static void main(String[] args) {
//        String results = new Controller("Model1")
//                .readDataFrom("data1.txt")
//                .runModel()
//                .runScriptFromFile("script1.groovy")
//                .getResultsAsTsv();
//
//        System.out.println(results);
        Runner runner = new Runner();
        runner.run();
    }
}

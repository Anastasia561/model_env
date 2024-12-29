package model_env;

public class Main {
    public static void main(String[] args) {
        String results = new Controller("Model1")
                .readDataFrom("data1.txt")
                .runModel()
                .runScriptFromFile("script1.groovy")
                .getResultsAsTsv();


        String results2 = new Controller("Model2")
                .readDataFrom("data1.txt")
                .runModel()
                .getResultsAsTsv();

        String results3 = new Controller("Model1")
                .readDataFrom("data1.txt")
                .runModel()
                .runScriptFromFile("script1.groovy")
                .getResultsAsTsv();

        System.out.println(results3);
//        Runner runner = new Runner();
//        runner.run();
    }
}

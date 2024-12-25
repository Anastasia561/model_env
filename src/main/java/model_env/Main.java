package model_env;

public class Main {
    public static void main(String[] args) {
        Controller controller = new Controller("Model1");
        controller.readDataFrom("C:\\Users\\User\\JavaProjects\\model_env\\src\\main\\resources\\data2.txt");
        controller.runModel();

        controller.runScriptFromFile("script1.groovy");

        String results = controller.getResultsAsTsv();
        System.out.println(results);
    }
}

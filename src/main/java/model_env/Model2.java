package model_env;

public class Model2 extends Model {
    @Bind
    private int LL; // number of years
    @Bind
    private double[] PKB; // GDP from Model1
    @Bind
    private double[] EKS; // export data from Model1
    @Bind
    private double[] IMP; // import data from Model1
    @Bind
    private double[] KI; // private consumption from Model1
    @Bind
    private double[] KS; // public consumption from Model1
    @Bind
    private double[] ZDEKS; // export-to-GDP ratio from the script
    @Bind
    private double[] ANI; // adjusted net impact

    public Model2() {
    }

    @Override
    public void run() {
        ANI = new double[LL];
        for (int t = 0; t < LL; t++) {
            if (PKB[t] != 0) {
                ANI[t] = (EKS[t] - IMP[t]) * ZDEKS[t] + (KI[t] + KS[t]) * 0.05;
            } else {
                ANI[t] = 0;
            }
        }
    }
}

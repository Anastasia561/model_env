package model_env;

import javax.swing.*;

public class Runner {
    public void run() {
        SwingUtilities.invokeLater(SimulationFrame::new);
    }
}

import javafx.application.Application;
import javafx.stage.Stage;
import view.GraphTemp;
import view.SimulationConfigView;

public class SimulationConfigMain extends Application {

	@Override
	public void start(Stage primaryStage) {
		SimulationConfigView mainView = new SimulationConfigView();

		mainView.show(primaryStage);
	}

	public static void main(String[] args) {
		launch(args);
	}
}

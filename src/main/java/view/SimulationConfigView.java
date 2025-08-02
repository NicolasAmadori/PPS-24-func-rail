package view;

import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.railway.Rail;
import model.railway.Station;

public class SimulationConfigView {

	private SmartGraphPanel<Station, Rail> graphPanel;

	public SimulationConfigView() {
		Graph<Station, Rail> g = GraphTemp.createGraph();
		SmartPlacementStrategy strategy = new SmartCircularSortedPlacementStrategy();
		SmartGraphPanel<Station, Rail> graphView = new SmartGraphPanel<>(g, strategy);
		graphPanel = graphView;
	}

	public Parent getRoot() {
		BorderPane bp = new BorderPane();
		bp.setCenter(graphPanel);

		CheckBox autoLayout = new CheckBox();
		Label auto = new Label("Automatic Layout");
		HBox hBox = new HBox(autoLayout, auto);
		hBox.setAlignment(Pos.CENTER);

		autoLayout.selectedProperty().bindBidirectional(graphPanel.automaticLayoutProperty());

		bp.setBottom(hBox);
		return bp;
	}

	public void show(Stage primaryStage) {
		Scene scene = new Scene(getRoot(), 400, 300);
		primaryStage.setTitle("JavaFX MVC App");
		primaryStage.setScene(scene);
		primaryStage.show();

		graphPanel.init();

		graphPanel.setAutomaticLayout(true);

	}
}

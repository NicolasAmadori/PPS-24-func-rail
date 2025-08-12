package view;

import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.GraphEdgeList;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.SmartGraphEdge;
import model.railway.*;
import scala.jdk.javaapi.CollectionConverters;
import utils.Converters;
import view.simconfig.RailView;
import view.simconfig.StationView;

import java.util.List;

public class GraphUtil {
	public static Graph<StationView, RailView> createGraph(Railway railway) {
		Graph<StationView, RailView> graph = new GraphEdgeList<>();

		railway.stations().map(StationView::new).foreach(graph::insertVertex);
		var stations = graph.vertices().stream().map(Vertex::element).toList();
		railway.rails().distinctBy(Rail::code).foreach(r -> {
			StationView stationA = stations.stream()
					.filter(s -> s.stationCode().equals(r.stationA())).findFirst().orElseThrow();
			StationView stationB = stations.stream()
					.filter(s -> s.stationCode().equals(r.stationB())).findFirst().orElseThrow();
			return graph.insertEdge(stationA, stationB, new RailView(r));
		});

		return graph;
	}
}

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

	public static Railway createRailway() {

		List<Station> stations = List.of(
			new SmallStation("Station A"),
			new BigStation("Station B"),
			new SmallStation("Station C"),
			new BigStation("Station D"),
			new BigStation("Station E")
		);

		List<Rail> rails = List.of(
			new MetalRail(1, 100, stations.get(0).code(), stations.get(1).code()),
			new TitaniumRail(2, 150, stations.get(1).code(), stations.get(2).code()),
			new MetalRail(3, 200, stations.get(2).code(), stations.get(3).code()),
			new TitaniumRail(4, 120, stations.get(0).code(), stations.get(3).code()),
			new MetalRail(5, 120, stations.get(4).code(), stations.get(3).code())
		);

		var scalaStations = Converters.toImmutableList(stations);
		var scalaRails = Converters.toImmutableList(rails);

		return new RailwayImpl(scalaStations, scalaRails);
	}

	public static Graph<StationView, RailView> createGraph(Railway railway) {
		Graph<StationView, RailView> graph = new GraphEdgeList<>();

		railway.stations().map(StationView::new).foreach(graph::insertVertex);
		var stations = graph.vertices().stream().map(Vertex::element).toList();
		railway.rails().foreach(r -> {
			StationView stationA = stations.stream()
					.filter(s -> s.stationCode().equals(r.stationA())).findFirst().orElseThrow();
			StationView stationB = stations.stream()
					.filter(s -> s.stationCode().equals(r.stationB())).findFirst().orElseThrow();
			return graph.insertEdge(stationA, stationB, new RailView(r));
		});

		return graph;
	}
}

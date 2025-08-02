package view;

import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.GraphEdgeList;
import model.railway.*;
import scala.jdk.javaapi.CollectionConverters;
import utils.Converters;

import java.util.List;

public class GraphTemp {
	
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
			new MetalRail(2, 150, stations.get(1).code(), stations.get(2).code()),
			new MetalRail(3, 200, stations.get(2).code(), stations.get(3).code()),
			new MetalRail(4, 120, stations.get(0).code(), stations.get(3).code()),
			new MetalRail(5, 120, stations.get(4).code(), stations.get(3).code())
		);
		
		var scalaStations = Converters.toImmutableList(CollectionConverters.asScala(stations));
		var scalaRails = Converters.toImmutableList(CollectionConverters.asScala(rails));
		
		return new RailwayImpl(scalaStations, scalaRails);
	}
	
	public static Graph<Station, Rail> createGraph() {
		Railway railway = createRailway();
		Graph<Station, Rail> graph = new GraphEdgeList<>();
		
		railway.stations().foreach(graph::insertVertex);
		railway.rails().foreach(r -> {
			Station stationA = railway.stations().find(s -> s.code().equals(r.stationA())).get();
			Station stationB = railway.stations().find(s -> s.code().equals(r.stationB())).get();
			return graph.insertEdge(stationA, stationB, r);
		});
		
		return graph;
	}
}

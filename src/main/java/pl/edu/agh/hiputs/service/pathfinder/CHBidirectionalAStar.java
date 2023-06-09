package pl.edu.agh.hiputs.service.pathfinder;

import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.BidirectionalAStarShortestPath;
import org.jgrapht.alg.util.Pair;
import pl.edu.agh.hiputs.model.id.LaneId;
import pl.edu.agh.hiputs.model.map.mapfragment.MapFragment;
import pl.edu.agh.hiputs.model.map.roadstructure.*;
import pl.edu.agh.hiputs.service.pathfinder.astar.ContractionHierarchyAStar;
import pl.edu.agh.hiputs.service.pathfinder.astar.AStarContractionHierarchyPrecomputation;
import pl.edu.agh.hiputs.service.worker.usecase.MapRepository;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class CHBidirectionalAStar extends CHPathFinder implements Serializable {
    public CHBidirectionalAStar(MapRepository mapRepository, ThreadPoolExecutor executor) {
        super(mapRepository, executor);
    }

    public CHBidirectionalAStar(MapFragment mapFragment, ThreadPoolExecutor executor) {
        super(mapFragment, executor);
    }

    private AStarContractionHierarchyPrecomputation.ContractionHierarchy<JunctionReadable, LaneReadable> createCHBidirectionalAStar(
            Graph<JunctionReadable, LaneReadable> graph,
            ThreadPoolExecutor executor) {
        AStarContractionHierarchyPrecomputation<JunctionReadable, LaneReadable> precomputation =
                new AStarContractionHierarchyPrecomputation<>(graph, executor);

        return precomputation.computeContractionHierarchy();
    }

    @Override
    ShortestPathAlgorithm<JunctionReadable, LaneReadable> initShortestPathAlgorithm(Graph<JunctionReadable, LaneReadable> graph, ThreadPoolExecutor executor) {
        AStarContractionHierarchyPrecomputation.ContractionHierarchy<JunctionReadable, LaneReadable> ch = createCHBidirectionalAStar(graph, executor);
        return new ContractionHierarchyAStar<>(ch);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(laneIds);
        out.writeObject(this.laneToJunctionsMapping);
        ContractionHierarchyAStar<JunctionReadable, LaneReadable> a = (ContractionHierarchyAStar<JunctionReadable, LaneReadable>) shortestPathAlgorithm;
        out.writeObject(a);
    }


    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
       laneIds = (ArrayList<LaneId>) in.readObject();
       laneToJunctionsMapping = (Map<LaneId, Pair<JunctionReadable, JunctionReadable>>) in.readObject();

    }
}
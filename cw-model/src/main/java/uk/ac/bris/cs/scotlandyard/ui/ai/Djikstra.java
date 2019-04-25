package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.*;



public class Djikstra {
    private Graph<Integer, Transport> graph;


    public Djikstra(ScotlandYardView view) {
        this.graph = view.getGraph();
    }


    //uses dijkstra's algorithm to calculate min distances from MrX to any node
    public HashMap<Node<Integer>, Integer> getMinDistancesToNodes(int location) {
        Set<Node<Integer>> visitedNodes = new HashSet<>();
        Set<Node<Integer>> unvisitedNodes = new HashSet<>();
        List<Node<Integer>> nodeArrayList = new ArrayList<>();
        List<Node<Integer>> nodesToAdd = new ArrayList<>();
        List<Node<Integer>> nodesToRemove = new ArrayList<>();
        HashMap<Node<Integer>, Integer> distanceFromMrXMap = new HashMap<>();

        Node<Integer> mrXNode = graph.getNode(location);
        unvisitedNodes.add(mrXNode);
        distanceFromMrXMap.put(mrXNode, 0);

        while (unvisitedNodes.size() > 0) {
            for (Node<Integer> n : unvisitedNodes) {
                visitedNodes.add(n);
                //get adjacent nodes of MrX
                Collection<Edge<Integer, Transport>> nodeEdges = graph.getEdgesFrom(n);

                for (Edge<Integer, Transport> e : nodeEdges) {
                    nodeArrayList.add(e.destination());
                }
                nodesToRemove.add(n);
                //for each of the adjacent Nodes, put the distance from Mrx into the distanceFromMrXMap map
                for (Node<Integer> nextNode : nodeArrayList) {
                    nodesToAdd.add(nextNode);
                    distanceFromMrXMap.putIfAbsent(nextNode, distanceFromMrXMap.get(n) + 1);
                }
            }
            unvisitedNodes.addAll(nodesToAdd);
            unvisitedNodes.removeAll(nodesToRemove);
        }
        return distanceFromMrXMap;
    }
}





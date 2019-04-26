package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;



public class Djikstra {

    /** Calculate Shortest path from Source Location to end point
     *
     *
     *
     *
     *
     */

    public Integer ShortestPath(Graph<Integer, Transport> graph, int location, int endLocation) {
        Integer p = -1;
        Integer minDistance =Integer.MAX_VALUE;
        List<Node<Integer>> unvisited = new ArrayList<>();
        unvisited.addAll(graph.getNodes());
        Integer[] dist = new Integer[200];
        for (Node<Integer> node : unvisited) {
            dist[node.value()] = minDistance;
        }
        dist[location] = 0;
        while (unvisited.size()!=0) {
            for (Node<Integer> n : unvisited) {
                if (dist[n.value()] < minDistance) {
                    p = n.value();
                }
            }

            Collection<Edge<Integer, Transport>> edges = graph.getEdgesFrom(graph.getNode(p));

            for (Edge<Integer, Transport> e : edges) {
                int l = e.destination().value();
                if (dist[l] > dist[p] + Weight(e.data()))  dist[l] = dist[p] + Weight(e.data());
            }
               unvisited.remove(graph.getNode(p));
                if (p == endLocation) return dist[endLocation] - dist[location];
                if (unvisited.size()==0) return dist[endLocation] - dist[location];
        }
        return dist[endLocation] - dist[location];
    }


    // Weight Each edge Using the cost of Transportation as Integer value
    private int Weight(Transport t) {
         int a=-1;
        switch (t) {
            case TAXI:
                a=2;
                break;
            case BUS:
                a= 4;
                break;
            case UNDERGROUND:
                a= 6;
                break;
            case FERRY:
                a= 8;
        }
        return a;
    }
}



























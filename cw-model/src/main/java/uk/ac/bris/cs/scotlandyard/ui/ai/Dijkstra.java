package uk.ac.bris.cs.scotlandyard.ui.ai;


import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.*;



class Dijkstra {
    private Graph<Integer, Transport> graph;


    Dijkstra(ScotlandYardView view) {
        this.graph = view.getGraph();
    }
    HashMap<Node<Integer>,Integer> shortestPath(int location){
        int Nodes= graph.size() + 1;
        int[] distance = new int[Nodes];
        boolean[] visited= new boolean[Nodes];

        for (int i = 0; i < distance.length; i++) {
            distance[i] = Integer.MAX_VALUE;
            visited[i] = false;
        }

        distance[location] = 0;

        while(location != -1){
            visited[location] = true;
            Collection<Edge<Integer, Transport>> edges = graph.getEdgesFrom(graph.getNode(location));
            for (Edge<Integer, Transport> e : edges) {
                if (distance[e.destination().value()] > distance[location] + 1) {
                    distance[e.destination().value()] = distance[location] + 1;
                }
            }
            int x = Integer.MAX_VALUE;
            int newDestination = -1;
            for (int i=0; i < distance.length; i++) {
                if (!visited[i] && distance[i ]< x) {
                    newDestination=i;
                    x = distance[i];}
            }
            location = newDestination;
        }
        HashMap<Node<Integer>,Integer> nodeIntegerHashMap = new HashMap<>();
        for (int i = 1; i < distance.length; i++) {
            nodeIntegerHashMap.put(graph.getNode(i),distance[i]);
        }
        System.out.println(nodeIntegerHashMap);
        return nodeIntegerHashMap;

    }

}





package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.AiHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static uk.ac.bris.cs.scotlandyard.model.Ticket.*;


import static uk.ac.bris.cs.scotlandyard.model.Ticket.fromTransport;

public class Djikstra  {
         private ValidMoves v;
         private AiHelper Ai;

    public void  ShortestPath(ScotlandYardView view, int location){
          v= new ValidMoves();
          Ai= new AiHelper(view,location);
        Graph<Integer, Transport> graph = view.getGraph();
        v.setGraph(graph);
        v.setAiHelper(view,location);
        int Nodes= graph.size();
        int[] dis= new int[Nodes];
        boolean[] visited= new boolean[Nodes];
        List<Colour> colours= view.getPlayers();





        for(int i=0; i<Nodes; i++){
            dis[i] = Integer.MAX_VALUE;
            visited[i]= false;
        }
        visited[location]=true;
        dis[location]= 0;
    }






    private void minEdge() {

    }

    private int EdgeWeight(Edge<Integer, Transport> edge){
            if(fromTransport(edge.data())==TAXI)  return 2;
            if(fromTransport(edge.data())==BUS)   return 4;
            if(fromTransport(edge.data())==UNDERGROUND) return 4;
            if(fromTransport(edge.data())==SECRET) return  6;
            else  return 8;
    }













}

package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.function.Consumer;

import com.sun.jdi.IntegerValue;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;
import uk.ac.bris.cs.scotlandyard.ai.ManagedAI;
import uk.ac.bris.cs.scotlandyard.ai.PlayerFactory;
import  uk.ac.bris.cs.scotlandyard.model.*;
import static uk.ac.bris.cs.scotlandyard.model.Colour.BLACK;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardPlayer;


import static uk.ac.bris.cs.scotlandyard.model.Ticket.*;


@SuppressWarnings("SpellCheckingInspection")
@ManagedAI("NIGNOG")
public class MyAI implements PlayerFactory {

    // TODO create a new player here
    @Override
    public Player createPlayer(Colour colour) {
        return new MyPlayer();
    }



    // TODO A sample player that selects a random move
    private static class MyPlayer implements Player {
        private Score score;
      private final Random random = new Random();

        @Override
            public void makeMove (ScotlandYardView view,int location, Set<Move > moves,
                    Consumer < Move > callback){
                // TODO do something interesting here; find the best move
                // picks a random move4
                Graph<Integer, Transport> graph = view.getGraph();

                //make List of players

            score = new Score(view,location);
            score.FreedomOfMovement(location);
            //score.GoodMove();
                //System.out.println("COMMON : " + commonMoves());
                callback.accept( score.GetBestMove());


            }
        }










        //private Edge<Integer, Transport> findNodeHelper(ScotlandYardPlayer player, Collection<Edge<Integer,Transport>> edges, Edge<Integer,
          //      Transport> edge, int numNodes){

            //for (Edge<Integer, Transport> edge1 : edges) {
              //  int destinationValue = edge1.destination().value();
                //if (validMoves.filterMoves(validMoves.PossibleMoves(destinationValue), player,
                  //      aiHelper.detective()).size() > numNodes && !commonMoves().contains(edge1)) {
                    //numNodes = validMoves.filterMoves(validMoves.PossibleMoves(destinationValue), player,
                      //      aiHelper.detective()).size();

                    //edge = edge1;
                //}

            //}
            //return edge;

        //}

       // private Move findNodes(Collection<Edge<Integer, Transport>> edges, ScotlandYardPlayer player) {
         //   Collection<Edge<Integer, Transport>> edge = validMoves.filterMoves(edges, player, aiHelper.detective());
           // Optional<Edge<Integer, Transport>> eps = validMoves.filterMoves(edge, player, aiHelper.detective()).stream().findFirst();

            //int availableNodes = 0;
            //int availableNodes1 = 0;

            //Edge<Integer, Transport> locationNode = eps.orElseThrow();
            //Edge<Integer, Transport> locationNode1 = eps.orElseThrow();

            //locationNode = findNodeHelper(player,edge,locationNode,availableNodes);


            //if(aiHelper.rounds.get(aiHelper.getCurrentRound()) && player.hasTickets(DOUBLE)){
              //  Collection<Edge<Integer, Transport>> newEdges = validMoves.filterMoves
                //        (validMoves.PossibleMoves(locationNode.destination().value()),player,aiHelper.detective());

                //locationNode1 = findNodeHelper(player,newEdges,locationNode1,availableNodes1);

                //System.out.println("Edge " + edge);
                //System.out.println(locationNode);



                //return new DoubleMove(aiHelper.getCurrentPlayer().colour(),fromTransport(locationNode.data()),locationNode.destination().value(),
                  //      fromTransport(locationNode1.data()),locationNode1.destination().value());
            //}
            //else{
              //  return new TicketMove(aiHelper.getCurrentPlayer().colour(), fromTransport(locationNode.data()),
                //        locationNode.destination().value());
            //}

        //}

//        private Move move(ScotlandYardView view){
//           Optional < Edge<Integer, Transport>> edge = findNodes(validMoves.PossibleMoves(aiHelper.getPlayerLocation(view.getCurrentPlayer()))
//                    , aiHelper.getCurrentPlayer()).stream().findFirst();
//           Edge<Integer,Transport> e = edge.orElseThrow();
//
//           if (aiHelper.rounds.get(aiHelper.getCurrentRound())){
//               Optional < Edge<Integer, Transport>> edgesss = findNodes(validMoves.PossibleMoves(aiHelper.getPlayerLocation(view.getCurrentPlayer()))
//                       , aiHelper.getCurrentPlayer()).stream().skip(1).findFirst();
//               Edge<Integer,Transport> ee = edgesss.orElseThrow();
//               return new DoubleMove(view.getCurrentPlayer(),fromTransport(e.data()),e.destination().value(),fromTransport(ee.data()),ee.destination().value());
//           }
//
//                return new TicketMove(view.getCurrentPlayer(), fromTransport(e.data()),
//                    e.destination().value());
//
//        }


//        private int ticket (Edge<Integer, Transport> edges){
//            Ticket ticket = fromTransport(edges.data());
//            List<Integer> ticketwight = ticketWeight(aiHelper.getCurrentPlayer().colour());
//
//            int taxi = ticketwight.get(0);
//            int bus = ticketwight.get(1);
//            int underground = ticketwight.get(2);
//            int doublem = ticketwight.get(3);
//            int secert = ticketwight.get(4);
//
//            if (ticket == TAXI) return taxi;
//            if (ticket == BUS) return bus;
//            if (ticket == UNDERGROUND) return underground;
//            if (ticket == SECRET) return secert;
//            if (ticket == DOUBLE) return doublem;
//
//            return 0;
//        }
//
//      private List<Integer> ticketWeight( Colour colour) {
//            List<Integer> ticketScore = new ArrayList<>();
//          int taxiWeight = 2;
//          int busWeight = 4;
//          int unaWeight = 4;
//          int secWeight = 8;
//          int doubtWeigh = 10;
//
//          for (Ticket t : Ticket.values()){
//              if (t == TAXI)ticketScore.add(aiHelper.getPlayerTickets(colour,TAXI) * taxiWeight);
//              if (t == BUS) ticketScore.add(aiHelper.getPlayerTickets(colour,BUS)* busWeight);
//              if (t == UNDERGROUND) ticketScore.add(aiHelper.getPlayerTickets(colour,UNDERGROUND) * unaWeight);
//              if (t == SECRET) ticketScore.add(aiHelper.getPlayerTickets(colour,SECRET) * secWeight);
//              if (t == DOUBLE) ticketScore.add(aiHelper.getPlayerTickets(colour,DOUBLE)* doubtWeigh);
//          }
//          return ticketScore;
//      }
//
//    }
    }



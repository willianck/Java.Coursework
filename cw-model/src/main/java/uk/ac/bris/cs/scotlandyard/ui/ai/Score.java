package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.scotlandyard.model.*;
import static uk.ac.bris.cs.scotlandyard.model.Colour.BLACK;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.*;

import java.util.*;



public class Score implements  MoveVisitor {

    private final List<PlayerConfiguration> players;
    private final Graph<Integer, Transport> graph;
    private final int currentRound;
    private final List<Boolean> rounds;
    private Dijkstra dijkstra;

    /** Score class that encapsulate the Model Functions and the AI one move Ahead methods with Dijkstra Algorithm
     *
     *
     *
     */

    Score(ScotlandYardView view, int location) {
        dijkstra = new Dijkstra();
        players = ListOfPlayers(view, location);
        graph = view.getGraph();
        currentRound=view.getCurrentRound();
        rounds=view.getRounds();
    }

    // Map of MrX Moves and their corresponding scores
    private  HashMap<Move,Integer > bestMoves = new HashMap<>();

    private  List<Ticket> Types = new ArrayList<>(Arrays.asList(TAXI, BUS , UNDERGROUND, SECRET, DOUBLE));


// Gets a List of players from class PlayerConfiguration
    private  List<PlayerConfiguration> ListOfPlayers(ScotlandYardView view, int location) {
        List<PlayerConfiguration> players = new ArrayList<>();
        for (Colour c : view.getPlayers()) {
            Map<Ticket, Integer> tickets = new HashMap<>();
            for(Ticket t: Types) tickets.put(t, view.getPlayerTickets(c, t).orElse(0));
            PlayerConfiguration player = new PlayerConfiguration(c, view.getPlayerLocation(c).orElse(0), tickets);
            players.add(player);
        }
        players.get(0).location(location);
        return players;
    }


    /**  Methods From Model Called via Score
     *  Returns type PlayerConfiguration
     *
     *
     */


    //List of  the Detective Locations
    private  Set<Integer> PlayerLocation() {
        Set<Integer> PlayerLocation = new HashSet<>();
        for (PlayerConfiguration p : detective()) {
             PlayerLocation.add(p.location());
        }
        return PlayerLocation;
    }

  // List of Detectives
    public List<PlayerConfiguration> detective() {
        List<PlayerConfiguration> detectives = new ArrayList<>();
        for (PlayerConfiguration player : players) {
            if (player.colour() != BLACK) {
                detectives.add(player);
            }
        }
        return detectives;
    }


// All edges from a source point
    private Collection<Edge<Integer, Transport>> PossibleMoves(int location) {
        return graph.getEdgesFrom(graph.getNode(location));
    }


  // All edges accessible if player is not  located on node
    private Collection<Edge<Integer, Transport>> filterLocation(Collection<Edge<Integer, Transport>> edges) {
        Set<Integer> location = PlayerLocation();
        Collection<Edge<Integer, Transport>> filter_moves = new HashSet<>();
        for (Edge<Integer, Transport> edge : edges) {
            if (!location.contains(edge.destination().value())) {
                filter_moves.add(edge);
            }
        }
        return filter_moves;
    }

    // Collection of edges accessible only if the player has the tickets
    private Collection<Edge<Integer, Transport>> filterTicket(Collection<Edge<Integer, Transport>> edges,
                                                              PlayerConfiguration player) {
        Collection<Edge<Integer, Transport>> filter_ticket = new HashSet<>();
        for (Edge<Integer, Transport> e : edges) {
            if (player.hasTickets(fromTransport(e.data())))
                filter_ticket.add(e);
        }
        return filter_ticket;
    }


    // Collection of edges only  accessible by the player during a round
    private  Collection<Edge<Integer, Transport>> filterMoves(Collection<Edge<Integer, Transport>> edges,
                                                            PlayerConfiguration player) {
        Collection<Edge<Integer, Transport>> filter_moves = filterLocation(edges);
        filter_moves = filterTicket(filter_moves, player);
        return filter_moves;
    }


    // Special function to get the moves of MrX including double moves . Called on MrX to get his valid moves
    private Set<Move> MrXMoves(PlayerConfiguration player) {
        Set<Move> firstMoves = getMoves(players.get(0).location());
        Set<Move> DoubleMoves = new HashSet<>();
        if ((player.hasTickets(DOUBLE)) && currentRound != rounds.size() - 1) {
            for (Move init_move : firstMoves) {
                TicketMove m = (TicketMove) init_move;
                Set<Move> second_moves = getMoves(m.destination());
                for (Move init_move1 : second_moves) {
                    TicketMove m1 = (TicketMove) init_move1;
                    if (m.ticket() != m1.ticket() || player.hasTickets(m.ticket(), 2))
                        DoubleMoves.add(new DoubleMove(player.colour(), m, m1));
                }
            }
        }
        firstMoves.addAll(DoubleMoves);
        return firstMoves;
    }


    // Function that gets  valid Moves from a source point
    private  Set<Move> getMoves(int location) {
        Set<Move> moves = new HashSet<>();
        Collection<Edge<Integer, Transport>> possible_moves = PossibleMoves(location);

        Collection<Edge<Integer, Transport>> possible_location = filterLocation(possible_moves);//
        Collection<Edge<Integer, Transport>> player_moves = filterMoves(possible_moves, players.get(0));
        for (Edge<Integer, Transport> e : possible_location) {
            if ((players.get(0).hasTickets(SECRET))) {
                moves.add(new TicketMove(players.get(0).colour(), Ticket.SECRET, e.destination().value()));
            }
        }
        for (Edge<Integer, Transport> e : player_moves) {
            moves.add(new TicketMove(players.get(0).colour(), fromTransport(e.data()),
                    e.destination().value()));
        }
        return moves;
    }

    /**
     * Scoring Class Implementation Starts here !
     */
// Scoring function for the Moves which opens the most possible moves and Gets MrX away from Detectives

     void ScoreMoves() {
        Set<Move> MrxMove = MrXMoves(players.get(0));
        for (Move m : MrxMove) {
            m.visit(this);
        }
    }

    // Retrieves the Max Score
private int Max(){
        return Collections.max(bestMoves.values());
}

// During Rounds , call Best  Normal Ticket  Move if It is not a Reveal Round.
// Calls Secret Move if No normal Ticket can be used otherwise

 private Move SingleMove(){
        List<Move> maxKeys= new ArrayList<>();
        for(Map.Entry<Move,Integer> entry : bestMoves.entrySet()){
            if(entry.getValue()==Max()) maxKeys.add(entry.getKey());
        }

       if(maxKeys.size()>1) {
            for (Move m : maxKeys) {
                if(m.getClass()== DoubleMove.class){ continue; }
                    TicketMove m1 = (TicketMove) m;
                    if (m1.ticket() != SECRET) return m1;
                }
            }
        return maxKeys.get(0);
 }

// Best Secret Ticket Used when After Reveal Round
     private Move SelectSecret(){
         Move dummyMove=null;
         Integer largestValue=0;
         for(Map.Entry<Move,Integer> map : bestMoves.entrySet()){
             if(map.getKey().getClass()== TicketMove.class){
                 TicketMove m = (TicketMove) map.getKey();
                 if(m.ticket()==SECRET){
                     if(map.getValue()>largestValue) largestValue=map.getValue();
                 }
             }
         }
         for(Map.Entry<Move,Integer> entry : bestMoves.entrySet()) {
             if (entry.getKey().getClass() == TicketMove.class) {
                 TicketMove m = (TicketMove) entry.getKey();
                 if (Objects.equals(largestValue, entry.getValue()) && m.ticket() == SECRET)
                 dummyMove = entry.getKey();
             }
         }
         return dummyMove;
         }



// Best Double Ticket Used when After Reveal Round
      private Move SelectDouble(){
        Move dummyMove= null;
        Integer largestValue= 0;
          for (Map.Entry<Move, Integer> map : bestMoves.entrySet()) {
              if(map.getKey().getClass()==DoubleMove.class){
                  if(map.getValue()>largestValue)  largestValue=map.getValue();
              }
          }
          for( Map.Entry<Move,Integer> entry : bestMoves.entrySet()){
              if( Objects.equals(largestValue,entry.getValue()) && entry.getKey().getClass()==DoubleMove.class)
                  dummyMove= entry.getKey();
          }
        return dummyMove;
      }


      // Checks if player has Secret and it is after a  Reveal Round
         private boolean SecretAfterRevealRound(){
             PlayerConfiguration player= players.get(0);
             return (player.hasTickets(SECRET, 1)  && rounds.get(currentRound -1));
         }

       // Check if player has Double and it is after a Reveal Round
         private boolean DoubleAfterRevealRound(){
           PlayerConfiguration player = players.get(0);
           return (player.hasTickets(DOUBLE,1) &&  rounds.get(currentRound-1));
         }


// Global Function which Select the Best Move to be played by MrX
         Move ChooseAMove() {
            if(currentRound!=0) {
                if (SecretAfterRevealRound()) { return SelectSecret(); }
                if(DoubleAfterRevealRound()) {return SelectDouble(); }
            }
            return SingleMove();
         }


// Visit Ticket Move to add and  compute their  scores
    @Override
    public void visit(TicketMove move) {
        int score=0;
        Set<Move> NextMove = getMoves(move.destination());
        bestMoves.put(move, NextMove.size());
        for(PlayerConfiguration p : detective()){
            int distance = dijkstra.ShortestPath(graph,move.destination(),p.location());
            // Negatively scores the move as It is close to detectives
            if(distance<=4) score -= 50;
            else score += distance;
        }
        int a= score;
        bestMoves.compute(move, (key, val) -> (val == null) ? 1 : val +  a);

    }

    // Visit Double Move to compute their  scores
    @Override
    public void visit(DoubleMove move) {
        int score = 0;
       // Default Score for DoubleMove as these are Special ticket which use is not base
        // on the amount of possible nodes they open up for  a  Move
       final int def = 1;
         bestMoves.put(move,def);
        for(PlayerConfiguration p : detective()){
            int distance = (dijkstra.ShortestPath(graph,move.finalDestination(),p.location()));
            if(distance<=4) score -=50;
            else score +=distance;
        }
        int a = score;
        bestMoves.compute(move, (key, val) -> (val == null) ? 1 : val + a);


    }
}














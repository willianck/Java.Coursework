package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.scotlandyard.model.*;
import static  java.util.Objects.requireNonNull;
import java.util.*;

import static uk.ac.bris.cs.scotlandyard.model.Colour.BLACK;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.*;

public class Score {

    private final List<PlayerConfiguration> players;
    private final Graph<Integer, Transport> graph;
    private final int currentRound;
    private final List<Boolean> rounds;
// Score class that encapsulate the Model Functions and the AI one move Ahead methods

    public Score(ScotlandYardView view, int location) {
        players = ListOfPlayers(view, location);
        graph = view.getGraph();
        currentRound=view.getCurrentRound();
        rounds=view.getRounds();
    }

    // Map of edges and their corresponding scores
    private HashMap<Move,Integer > bestMoves = new HashMap<>();

    private static List<Ticket> Types = new ArrayList<>(Arrays.asList(TAXI, BUS , UNDERGROUND, SECRET, DOUBLE));

// Gets a List of players from class PlayerConfiguration
    private static List<PlayerConfiguration> ListOfPlayers(ScotlandYardView view, int location) {
        List<PlayerConfiguration> players = new ArrayList<>();
        for (Colour c : view.getPlayers()) {
            Map<Ticket, Integer> tickets = new HashMap<>();
            for(Ticket t: Types) tickets.put(t, view.getPlayerTickets(c, t).get());
            PlayerConfiguration player = new PlayerConfiguration(c, view.getPlayerLocation(c).get(), tickets);
            players.add(player);
        }
        players.get(0).location(location);
        return players;
    }


    // Same Methods as in Model now used via Score

    private Set<Integer> PlayerLocation() {
        Set<Integer> PlayerLocation = new HashSet<>();
        for (PlayerConfiguration p : players) {
            if (p.colour() != BLACK) PlayerLocation.add(p.location());

        }
        return PlayerLocation;
    }

    public List<PlayerConfiguration> detective() {
        List<PlayerConfiguration> detectives = new ArrayList<>();
        for (PlayerConfiguration player : players) {
            if (player.colour() != BLACK) {
                detectives.add(player);
            }
        }
        return detectives;
    }


    private Collection<Edge<Integer, Transport>> PossibleMoves(int location) {
        return graph.getEdgesFrom(graph.getNode(location));
    }

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

    // Collection of edges accessible by the player
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

    private Set<Move> getMoves(int location) {
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

// Assign scores to edges which opens the most possible moves

    public void FreedomOfMovement() {
        Set<Move> MrxMove = MrXMoves(players.get(0));
        for (Move m : MrxMove) {
            if(m!=null) {
                if(m instanceof TicketMove) {
                    TicketMove move = (TicketMove) m;
                    Set<Move> NextMove = getMoves(move.destination());
                    bestMoves.put(m, NextMove.size());
                }
                else if(m instanceof DoubleMove){
                    DoubleMove  Doublemove= (DoubleMove) m;
                    Set<Move> NextMove= getMoves(Doublemove.finalDestination());
                    bestMoves.put(m,NextMove.size());
                }
            }
        }
    }



    // Set of Detective destination in one or two moves ahead at a point in the Game
    private Set<Integer> DetectiveDestination() {
        Set<Integer> Destination = new HashSet<>();
        for (PlayerConfiguration player : detective()) {
            Set<Move> moves = getMoves(player.location());
            for (Move m : moves) {
                TicketMove init_move = (TicketMove) m;
                Destination.add(init_move.destination());
                Set<Move> moves1 = getMoves(init_move.destination());
                for (Move m1 : moves1) {
                    TicketMove init_move2 = (TicketMove) m1;
                    Destination.add(init_move2.destination());
                }
            }
        }
        return Destination;
    }


//Assign Score to Edges that move MrX to a point where  A detective can not reach in one / two moves
    public void  EscapeMove(){
        Set<Integer> set = DetectiveDestination();
        Set<Move> moves = MrXMoves(players.get(0));
        for( Move m : moves){
            if(m!=null){
                if(m instanceof TicketMove) {
                    TicketMove move = (TicketMove) m;
                    if (!set.contains(move.destination()))
                        bestMoves.compute(m, (key, val) -> (val == null) ? 1 : val + 2);
                }
                if(m instanceof DoubleMove){
                    DoubleMove move = (DoubleMove) m;
                    if(!set.contains(move.finalDestination()))
                        bestMoves.compute(m, (key, val) -> (val == null) ? 1 : val + 2);
                }
            }
        }

    }

    public Move GetBestMove(){
        int largestValue = -1;
        Move largestKey = null;
        for(Map.Entry<Move,Integer> entry : bestMoves.entrySet()){
            int value= entry.getValue();
            Move key = entry.getKey();
            if(value>largestValue){
                largestValue=value;
                largestKey=key;
            }
        }
        System.out.println(bestMoves.size());
        if(largestKey==null){
            throw new IllegalArgumentException("LARGEST KEY IS NULL");
        }
        return largestKey;
    }


// Weight the Edges
    private int EdgeWeight(Edge<Integer, Transport> edge){
        if(fromTransport(edge.data())==TAXI)  return 2;
        if(fromTransport(edge.data())==BUS)   return 4;
        if(fromTransport(edge.data())==UNDERGROUND) return 4;
        if(fromTransport(edge.data())==SECRET) return  6;
        else  return 8;
    }





}







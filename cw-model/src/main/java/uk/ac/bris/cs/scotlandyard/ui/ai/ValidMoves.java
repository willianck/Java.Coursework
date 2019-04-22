package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.*;

import static uk.ac.bris.cs.scotlandyard.model.Ticket.*;

class ValidMoves{

    private static Graph<Integer, Transport> graph;
    private AiHelper aiHelper;

    void setAiHelper(ScotlandYardView view, int location) {
        this.aiHelper = new AiHelper(view,location);
    }

    //Initialize class variables
    void setGraph(Graph<Integer, Transport> g) {
        graph = g;
    }



    //Checks whether a player has enough tickets to make the move
    private static Set<Integer> PlayerLocation(List<ScotlandYardPlayer> detective) {
        Set<Integer> PlayerLocation = new HashSet<>();
        for (ScotlandYardPlayer player : detective) {
            PlayerLocation.add(player.location());
        }
        return PlayerLocation;
    }

    // Collection of possible  edges from  a given player location
    Collection<Edge<Integer, Transport>> PossibleMoves(int location) {
        return graph.getEdgesFrom(graph.getNode(location));
    }

    // Collection of edges accessible only if there is no player on it
    private Collection<Edge<Integer, Transport>> filterLocation(Collection<Edge<Integer, Transport>> edges, List<ScotlandYardPlayer> detective) {
        Set<Integer> location = PlayerLocation(detective);
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
                                                              ScotlandYardPlayer player) {
        Collection<Edge<Integer, Transport>> filter_ticket = new HashSet<>();
        for (Edge<Integer, Transport> e : edges) {
            if (player.hasTickets(fromTransport(e.data())))
                filter_ticket.add(e);
        }
        return filter_ticket;
    }
//    public Collection<Edge<Integer, Transport>> tomove(Edge<Integer, Transport> edges,
//                                                        ScotlandYardPlayer player){
//        Collection<Edge<Integer, Transport>> moves = new HashSet<>();
//
//        for (Edge<Integer,Transport> e : PossibleMoves(player.location())){
//            if (player.hasTickets(fromTransport(edges.data()))){
//                moves.add(e);
//            }
//        }
//        return moves;
//    }

    // Collection of edges accessible by the player
    Collection<Edge<Integer, Transport>> filterMoves(Collection<Edge<Integer, Transport>> edges,
                                                     ScotlandYardPlayer player, List<ScotlandYardPlayer> detective) {
        Collection<Edge<Integer, Transport>> filter_moves = filterLocation(edges,detective);
        filter_moves = filterTicket(filter_moves, player);
        return filter_moves;
    }

    private Set<Move> getMoves(int location) {
        Set<Move> moves = new HashSet<>();
        Collection<Edge<Integer, Transport>> possible_moves = PossibleMoves(location);

        Collection<Edge<Integer, Transport>> possible_location = filterLocation(possible_moves,aiHelper.detective());//
        Collection<Edge<Integer, Transport>> player_moves = filterMoves(possible_moves,aiHelper.getCurrentPlayer(),aiHelper.detective());
        for (Edge<Integer, Transport> e : possible_location) {
            if ((aiHelper.getCurrentPlayer().hasTickets(SECRET))) {
                moves.add(new TicketMove(aiHelper.getCurrentPlayer().colour(), Ticket.SECRET, e.destination().value()));
            }
        }
        for (Edge<Integer, Transport> e : player_moves) {
            moves.add(new TicketMove(aiHelper.getCurrentPlayer().colour(), fromTransport(e.data()),
                    e.destination().value()));
        }
        return moves;
    }

    // Special function to get the moves of MrX including double moves . Called on MrX to get his valid moves
    Set<Move> MrXMoves(ScotlandYardPlayer player) {
        Set<Move> firstMoves = getMoves(aiHelper.getPlayers().get(0).location());
        Set<Move> DoubleMoves = new HashSet<>();
        if ((player.hasTickets(DOUBLE)) && aiHelper.getCurrentRound() != aiHelper.rounds.size() - 1) {
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
        return DoubleMoves;
    }

}
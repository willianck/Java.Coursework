package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardPlayer;
import uk.ac.bris.cs.scotlandyard.model.Transport;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static uk.ac.bris.cs.scotlandyard.model.Ticket.fromTransport;

class ValidMoves{

    private static Graph<Integer, Transport> graph;

    //Initialize class variables
    void initialize(Graph<Integer, Transport> g) {
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

    // Collection of edges accessible by the player
    Collection<Edge<Integer, Transport>> filterMoves(Collection<Edge<Integer, Transport>> edges,
                                                     ScotlandYardPlayer player, List<ScotlandYardPlayer> detective) {
        Collection<Edge<Integer, Transport>> filter_moves = filterLocation(edges,detective);
        filter_moves = filterTicket(filter_moves, player);
        return filter_moves;
    }
}
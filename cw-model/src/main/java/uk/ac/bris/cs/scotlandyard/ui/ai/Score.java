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
// Score class that encapsulate the Model Functions and the AI one move Ahead methods

    public Score(ScotlandYardView view, int location) {
        players = ListOfPlayers(view, location);
        graph = view.getGraph();
    }

    // Map of edges and their corresponding scores
    private HashMap<Edge<Integer, Transport>,Integer > BestMoves = new HashMap<>();

    // Method to get the player tickets
    private static Map<Ticket, Integer> getTickets(ScotlandYardView view, Colour colour) {
        Map<Ticket, Integer> ticketColourMap = new HashMap<>();
        ticketColourMap.put(Ticket.BUS, view.getPlayerTickets(colour, Ticket.BUS).orElse(null));
        ticketColourMap.put(Ticket.DOUBLE, view.getPlayerTickets(colour, Ticket.DOUBLE).orElse(null));
        ticketColourMap.put(Ticket.SECRET, view.getPlayerTickets(colour, Ticket.SECRET).orElse(null));
        ticketColourMap.put(Ticket.TAXI, view.getPlayerTickets(colour, Ticket.TAXI).orElse(null));
        ticketColourMap.put(Ticket.UNDERGROUND, view.getPlayerTickets(colour, Ticket.UNDERGROUND).orElse(null));
        return ticketColourMap;
    }
// Gets a List of players from class PlayerConfiguration
    private static List<PlayerConfiguration> ListOfPlayers(ScotlandYardView view, int location) {
        List<PlayerConfiguration> players = new ArrayList<>();
        for (Colour c : view.getPlayers()) {
            PlayerConfiguration player = new PlayerConfiguration(c, view.getPlayerLocation(c).orElse(0), getTickets(view, c));
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

// Assign scores to edges which opens the most possible moves

    public void FreedomOfMovement(int location){
        Collection<Edge<Integer, Transport>> possiblemoves=PossibleMoves(location);
        Collection<Edge<Integer, Transport>> FirstNodes = filterMoves(possiblemoves,players.get(0));
        for(Edge<Integer, Transport> e : FirstNodes) {
            Collection<Edge<Integer, Transport>> Sub = filterMoves(PossibleMoves(e.destination().value()), players.get(0));
            if (Sub.size() >= 3) {
                BestMoves.put(e, 2);
            }
            if (Sub.size() == 2) {
                BestMoves.put(e,1 );
            } else BestMoves.put(e, 0);
        }
    }


    // All Edges accesible by Detectives at a point in the Game
    private Collection<Edge<Integer,Transport>> DetectiveMoves(){
        Collection<Edge<Integer,Transport>> Edges = new HashSet<>();
        Collection<Edge<Integer, Transport>> edges;
        Collection<Edge<Integer, Transport>> subedges= new HashSet<>();
        for(PlayerConfiguration player : detective()){
            edges = filterMoves(PossibleMoves(player.location()), player);
            for(Edge<Integer, Transport> e : edges)
                subedges= filterMoves(PossibleMoves(e.destination().value()),player);
            Edges.addAll(subedges);
        }
        for(PlayerConfiguration player : detective()){
            Edges.addAll(filterMoves(PossibleMoves(player.location()),player));
        }
        return Edges;
    }

// Assign Score to Edges that move MrX to a point where  A detective can not reach in one / two moves
    public void  GoodMove(){
        Collection<Edge<Integer, Transport>> detectiveNodes = DetectiveMoves();
        Collection<Edge<Integer,Transport>> moves= filterMoves(PossibleMoves(players.get(0).location()),players.get(0));
        for(Edge<Integer, Transport> e : moves){
            if(!detectiveNodes.contains(e)) BestMoves.compute(e,(key,val)->(val==null) ? 1:val + 2);
        }
    }

 // Get the Max score from the Map
    private int Max(Collection<Integer> v){
        Iterator<Integer> iterator = v.iterator();
        int i=0;
        while(iterator.hasNext()){
            if(i<=iterator.next()) {
                i = iterator.next();
            }
        }
        return i;
    }

    // get the Edge with Max Score from map
    private Edge<Integer,Transport> GetEdgeFromScore(HashMap<Edge<Integer, Transport>,Integer > map ){
        Integer max = Max(BestMoves.values());
        for(Edge<Integer,Transport> e : BestMoves.keySet()){
            if(BestMoves.get(e).equals(max))
                return e;
        }
        return null;

    }

// Create BestMove with Edge
    public Move GetBestMove(){
        Edge<Integer, Transport> edge= GetEdgeFromScore(BestMoves);
        return  new TicketMove(players.get(0).colour(),fromTransport(requireNonNull(edge.data())),edge.destination().value());
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







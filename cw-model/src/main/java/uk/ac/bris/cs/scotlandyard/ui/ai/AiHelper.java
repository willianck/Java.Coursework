package uk.ac.bris.cs.scotlandyard.ui.ai;


import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.*;



public class AiHelper {

    public  List<Boolean> rounds;
    private final List<ScotlandYardPlayer> players;
    private final Map <Colour, ScotlandYardPlayer> colourToPlayer;



    private final int currentRound;
    private ScotlandYardPlayer currentPlayer;


    AiHelper(ScotlandYardView view, int mrXLocation){
        players = getPlayerFromView (view, mrXLocation);
        colourToPlayer = setColoursToPlayersMap();
        currentRound = view.getCurrentRound();
        currentPlayer = colourToPlayer.get(view.getCurrentPlayer());
        rounds = view.getRounds();

    }

    public AiHelper(AiHelper aiHelper) {
        this.players = copyPlayers (aiHelper.players);
        this.colourToPlayer = this.setColoursToPlayersMap();
        this.currentRound = aiHelper.currentRound;
        this.currentPlayer = this.players.get(aiHelper.players.lastIndexOf(aiHelper.currentPlayer));
        this.rounds = getRounds();
    }

    //Returns a list of detectives
    public List<ScotlandYardPlayer> detective() {
        List <ScotlandYardPlayer> detectives = new ArrayList <> ( );
        for (ScotlandYardPlayer player : players) {
            if (player.isDetective ( )) {
                detectives.add (player);
            }
        }
        return detectives;
    }
    public int getCurrentRound() {
        return currentRound;
    }
    public List<Boolean> getRounds() {
        return rounds;
    }
    private List<ScotlandYardPlayer> copyPlayers(List<ScotlandYardPlayer> players) {
        return new ArrayList<>(players);
    }

    public List<ScotlandYardPlayer> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    private Map<Colour, ScotlandYardPlayer> setColoursToPlayersMap() {
        Map<Colour, ScotlandYardPlayer> colourScotlandYardPlayerMap = new HashMap<>();
        for (ScotlandYardPlayer player : players) {
            colourScotlandYardPlayerMap.put (player.colour (),player);
        }
        return colourScotlandYardPlayerMap;
    }
    private Map<Ticket, Integer> getTickets(ScotlandYardView view, Colour colour){
        Map<Ticket, Integer> ticketColourMap = new HashMap<>();
        ticketColourMap.put(Ticket.BUS, view.getPlayerTickets(colour, Ticket.BUS).orElse(null));
        ticketColourMap.put(Ticket.DOUBLE, view.getPlayerTickets(colour, Ticket.DOUBLE).orElse(null));
        ticketColourMap.put(Ticket.SECRET, view.getPlayerTickets(colour, Ticket.SECRET).orElse(null));
        ticketColourMap.put(Ticket.TAXI, view.getPlayerTickets(colour, Ticket.TAXI).orElse(null));
        ticketColourMap.put(Ticket.UNDERGROUND, view.getPlayerTickets(colour, Ticket.UNDERGROUND).orElse(null));
        return ticketColourMap;
    }

    int getPlayerTickets(Colour colour, Ticket ticket) {
        return colourToPlayer.get(colour).tickets().get(ticket);
    }


    int getPlayerLocation(Colour colour) {
        return colourToPlayer.get(colour).location();
    }

    public ScotlandYardPlayer getCurrentPlayer() {
        return currentPlayer;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private List<ScotlandYardPlayer> getPlayerFromView(ScotlandYardView view, int mrXLocation) {
        List<ScotlandYardPlayer> scotlandYardPlayerList = new ArrayList<>();
        List<Colour> players = view.getPlayers ();
        for (Colour c : players){
            int location;
            if (c.isMrX ()){
                location = mrXLocation;
            }
            else{
                location = view.getPlayerLocation (c).get ();
            }
            scotlandYardPlayerList.add(new ScotlandYardPlayer(null, c, location, getTickets (view, c)));
        }
        return scotlandYardPlayerList;
    }

}

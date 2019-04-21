package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.*;


@SuppressWarnings("CanBeFinal")
public class AiHelper {
    private final List<ScotlandYardPlayer> players;
    private final Map <Colour, ScotlandYardPlayer> colourToPlayer;
    private final int currentRound;
    private static List <Boolean> rounds;
    private final ScotlandYardPlayer currentPlayer;

    AiHelper(ScotlandYardView view, int mrXLocation){
        players = getPlayerFromView (view, mrXLocation);
        colourToPlayer = setColoursToPlayersMap();
        currentRound = view.getCurrentRound();
        currentPlayer = colourToPlayer.get(view.getCurrentPlayer());

    }

    public AiHelper(AiHelper aiHelper) {
        this.players = getPlayers (aiHelper.players);
        this.colourToPlayer = this.setColoursToPlayersMap();
        this.currentRound = aiHelper.currentRound;
        this.currentPlayer = this.players.get(aiHelper.players.lastIndexOf(aiHelper.currentPlayer));
    }

    public  List <Boolean> getRounds() {
        return rounds;
    }

    public static void setRounds(List <Boolean> rounds) {
        AiHelper.rounds = rounds;
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

    private List<ScotlandYardPlayer> getPlayers(List<ScotlandYardPlayer> players) {
        return new ArrayList<>(players);
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

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    List<ScotlandYardPlayer> getPlayerFromView(ScotlandYardView view, int mrXLocation) {
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



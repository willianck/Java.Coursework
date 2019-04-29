package uk.ac.bris.cs.scotlandyard.model;

import java.util.*;

import static java.util.Objects.requireNonNull;

public class NotifySpectators {

    private ArrayList <Spectator> spectators = new ArrayList <> ();

    Collection<Spectator> getSpectators() {
        return Collections.unmodifiableCollection(spectators);
    }

    public void registerSpectator(Spectator spectator) {
        if (spectators.contains(requireNonNull(spectator))){
            throw new IllegalArgumentException("A spectator is already registered. ");
        }
        else {
            spectators.add(spectator);
        }
    }

    void unregisterSpectator(Spectator spectator) {
        if (spectators.contains(requireNonNull(spectator))){
            spectators.remove(spectator);
        }
        else{
            throw new IllegalArgumentException("No spectator is registered");
        }
    }
    /** notify  game is over **/
    void gameIsOver(ScotlandYardView view, Set<Colour> win){
        for (Spectator spectator : spectators){
            spectator.onGameOver(view, win);
        }
    }

    /** notify  round has started **/
    void roundHasStarted(ScotlandYardView view, int currentRound){
        for (Spectator spectator : spectators){
            spectator.onRoundStarted(view, currentRound);
        }
    }

    /** notify  move is made**/
    void moveIsMade(ScotlandYardView view, Move move){
        for (Spectator spectator : spectators){
            spectator.onMoveMade(view, move);
        }
    }

    /** notify rotation is complete **/
    void rotationIsComplete(ScotlandYardView view){
        for (Spectator spectator : spectators){
            spectator.onRotationComplete(view);
        }
    }
}
package uk.ac.bris.cs.scotlandyard.SpectatorHelper;

import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;
import uk.ac.bris.cs.scotlandyard.model.Spectator;

import java.util.*;

import static java.util.Objects.requireNonNull;

public class NotifySpectator {

    private ArrayList <Spectator> spectators = new ArrayList <> ();

    public Collection<Spectator> getSpectators() {

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

    public void unregisterSpectator(Spectator spectator) {
        if (spectators.contains(requireNonNull(spectator))){
            spectators.remove(spectator);
        }
        else{
            throw new IllegalArgumentException("No spectator is registered");
        }
    }

    public void GameisOver(ScotlandYardView view, Set<Colour> win){
        for (Spectator spectator : spectators){
            spectator.onGameOver(view, win);
        }
    }

    //A helper method to notify all the spectators that a round has started
    public void RoundhasStarted(ScotlandYardView view, int currentRound){
        for (Spectator spectator : spectators){
            spectator.onRoundStarted(view, currentRound);
        }
    }

    //A helper method to notify all the spectators that a move has been made
    public void MoveisMade(ScotlandYardView view, Move move){
        for (Spectator spectator : spectators){
            spectator.onMoveMade(view, move);
        }
    }

    //A helper method to notify all the spectators that a round has ended
    public void RotationisComplete(ScotlandYardView view){
        for (Spectator spectator : spectators){
            spectator.onRotationComplete(view);
        }
    }
}

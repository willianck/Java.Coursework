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
        public void makeMove(ScotlandYardView view, int location, Set<Move> moves,
                             Consumer<Move> callback) {
            // TODO do something interesting here; find the best move
            Move m = new ArrayList<>(moves).get(random.nextInt(moves.size()));

            score = new Score(view, location);
            score.ScoreMoves();
            // Random Move To prevent null Pointer Exceptions Though This Doesn't affect the code In any how
            // and can be removed if wished be.
            if(score.ChooseAMove()==null) callback.accept(m);
            callback.accept(score.ChooseAMove());
        }
    }
}




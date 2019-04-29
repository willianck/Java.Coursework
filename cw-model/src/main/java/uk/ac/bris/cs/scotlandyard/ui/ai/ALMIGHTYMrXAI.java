package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.function.Consumer;


import uk.ac.bris.cs.scotlandyard.ai.ManagedAI;
import uk.ac.bris.cs.scotlandyard.ai.PlayerFactory;
import  uk.ac.bris.cs.scotlandyard.model.*;


@ManagedAI("ALMIGHTYMrXAI")
public class ALMIGHTYMrXAI implements PlayerFactory {


    @Override
    public Player createPlayer(Colour colour) {
        if(colour!=Colour.BLACK)  throw new IllegalArgumentException("This AI is for MrX only ");
        return new MyPlayer();
    }

    private static class MyPlayer implements Player {
        private Score score;
        private final Random random = new Random();

        @Override
        public void makeMove(ScotlandYardView view, int location, Set<Move> moves,
                             Consumer<Move> callback) {

            Move m = new ArrayList<>(moves).get(random.nextInt(moves.size()));
            score = new Score(view, location);
            score.ScoreMoves();
            // Random Move To prevent null Pointer Exceptions
            if(score.ChooseAMove()==null) callback.accept(m);
            callback.accept(score.ChooseAMove());
        }
    }
}




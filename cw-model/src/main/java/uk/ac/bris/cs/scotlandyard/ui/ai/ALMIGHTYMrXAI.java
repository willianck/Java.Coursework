package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.ai.ManagedAI;
import uk.ac.bris.cs.scotlandyard.ai.PlayerFactory;
import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYardView;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;


@ManagedAI("ALMIGHTYMrXAI")
public class ALMIGHTYMrXAI implements PlayerFactory {


    @Override
    public Player createPlayer(Colour colour) {
        if(colour!=Colour.BLACK)  throw new IllegalArgumentException("This AI is for MrX only ");
        return new MyPlayer();
    }

    private static class MyPlayer implements Player {
        private final Random random = new Random();

        @Override
        public void makeMove(ScotlandYardView view, int location, Set<Move> moves,
                             Consumer<Move> callback) {

            Move m = new ArrayList<>(moves).get(random.nextInt(moves.size()));
            Score score = new Score(view, location);
            score.scoreMoves();
            // Random Move To prevent null Pointer Exceptions
            if(score.chooseAMove()==null) callback.accept(m);
            callback.accept(score.chooseAMove());
        }
    }
}




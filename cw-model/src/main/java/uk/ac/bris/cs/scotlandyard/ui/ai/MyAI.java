package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.function.Consumer;

import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.Node;
import uk.ac.bris.cs.scotlandyard.ai.ManagedAI;
import uk.ac.bris.cs.scotlandyard.ai.PlayerFactory;
import uk.ac.bris.cs.scotlandyard.model.*;

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


        private AiHelper aiHelper;
        private ValidMoves validMoves;
        private Dijkstra dijkstra;


        void MyPlayers(ScotlandYardView view, int location) {
            aiHelper = new AiHelper(view, location);
            Graph<Integer, Transport> graph = view.getGraph();
            validMoves = new ValidMoves();
            validMoves.setGraph(graph);
            validMoves.setAiHelper(view, location);
            dijkstra = new Dijkstra(view);

        }
        private final Random random = new Random();

        @Override
        public void makeMove(ScotlandYardView view, int location, Set<Move> moves,
                             Consumer<Move> callback) {
            // TODO do something interesting here; find the best move
            // picks a random move4

            MyPlayers(view, location);
            findNodess(view,location);
//            System.out.println(location);
////            System.out.println(validMoves.getEdges(location,view));

           // System.out.println( "I dont know "+ findNodeHelperList(aiHelper.getCurrentPlayer(),validMoves.PossibleMoves(location)));
            //System.out.println(getMinDistancesToNodes(view,location));
            //System.out.println("EDGE USED" + findNodes(aiHelper.getCurrentPlayer()));
            //System.out.println("EDGEMAP"
//           System.out.println(Arrays.toString(dijkstra.ShortestPath(location)));
//           System.out.println(dijkstra.ShortestPath(location));
//            int[] dis = dijkstra.ShortestPath(location);
//            System.out.println(Arrays.toString(dis));
//            System.out.println(dis.length);



            callback.accept(move(moves,view,location));
        }

        private Move move (Set<Move> moves,ScotlandYardView view, int location){
            Edge<Integer,Transport>edge = edgeIntegerHashMap(view,location);
            Set<DoubleMove> douMoves = new HashSet<>();

            if(aiHelper.rounds.get(aiHelper.getCurrentRound()) && aiHelper.getCurrentPlayer().hasTickets(DOUBLE)){
                for(Move m : moves){
                    if(m.getClass() == DoubleMove.class){
                        douMoves.add((DoubleMove)m);
                    }
                }
                if(!douMoves.isEmpty()){
                    for(DoubleMove m : douMoves){
                        if(m.finalDestination() == Objects.requireNonNull(edgeIntegerHashMap(view, m.secondMove().destination())).destination().value()){
                            return m;
                        }
                    }
                }

            }

            if (edgeIntegerHashMap(view,location) == null){
                System.out.println("EMPTY");
                return new ArrayList<>(moves).get(random.nextInt(moves.size()));
            }


            assert edge != null;
            return new TicketMove(view.getCurrentPlayer(),fromTransport(edge.data()),edge.destination().value());

        }

        private Edge<Integer,Transport> edgeIntegerHashMap(ScotlandYardView view, int location){
            HashMap<Edge<Integer,Transport>, Integer> distanceFromMrXMap = findNodes(view,location);


            distanceFromMrXMap.putAll(findNodess(view,location));
            if(distanceFromMrXMap.isEmpty()){
                return null;
            }

          //  System.out.println("SHOUYLD HAVE SECERT" + distanceFromMrXMap);
            //System.out.println(distanceFromMrXMap.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey());

            return distanceFromMrXMap.entrySet().stream().max((entry1, entry2) -> {
                if (entry1.getValue() > entry2.getValue()) return 1;
                else return 0;
            }).get().getKey();
        }

        private HashMap<Edge<Integer,Transport>,Integer> findNodes( ScotlandYardView view, int location) {
            ScotlandYardPlayer player = aiHelper.getCurrentPlayer();

            Collection<Edge<Integer, Transport>> edge = validMoves.filterMoves(validMoves.PossibleMoves(player.location()), player, aiHelper.detective());
            edge.addAll(validMoves.getEdges(location,view));

            HashMap<Edge<Integer,Transport>, Integer> edgeIntegerHashMap = new HashMap<>();

            int availableNodes = 1;

            for (Edge<Integer, Transport> edge1 : edge) {
                int destinationValue = edge1.destination().value();
                int n = (int) Math.floor(validMoves.filterMoves(validMoves.PossibleMoves(destinationValue), player, aiHelper.detective()).size() / 2);
                if (validMoves.filterMoves(validMoves.PossibleMoves(destinationValue), player,
                        aiHelper.detective()).size() > availableNodes) {
                    edgeIntegerHashMap.put(edge1,n);
                    availableNodes = 1;
                }
            }
            return edgeIntegerHashMap;
        }


        private HashMap<Edge<Integer,Transport>,Integer> findNodess(ScotlandYardView view, int location) {
            int DD = 0;
            List<Node<Integer>> dectiveNodeList = new ArrayList<>();

            for (ScotlandYardPlayer player : aiHelper.detective()){
                dectiveNodeList.add(view.getGraph().getNode(player.location()));
            }

            HashMap<Node<Integer>, Integer> distancesToNodes = dijkstra.shortestPath(location);
            HashMap<Edge<Integer,Transport>,Integer> eddd = new HashMap<>();
            Collection<Edge<Integer,Transport>> edgeCollection =  validMoves.filterMoves(validMoves.PossibleMoves(aiHelper.getCurrentPlayer().location()),aiHelper.getCurrentPlayer(),aiHelper.detective());
            edgeCollection.addAll(validMoves.getEdges(location,view));
            for (Edge<Integer,Transport> e : edgeCollection) {
                for (Node<Integer> node : dectiveNodeList) {
                    if (distancesToNodes.get(node) == 1)  eddd.put(e,1);

                    if (distancesToNodes.get(node) == 2) eddd.put(e, 2);
                    if (distancesToNodes.get(node) == 3) eddd.put(e, 3);
                    if (distancesToNodes.get(node) > 3) eddd.put(e, 5);
                }
            }


            System.out.println("This is largest node with disatnce found" + DD);
            return eddd;
        }
    }
}




package uk.ac.bris.cs.scotlandyard.model;
import java.util.*;

import com.sun.nio.sctp.SctpChannel;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.ImmutableGraph;
import uk.ac.bris.cs.gamekit.graph.Node;

import java.util.concurrent.CopyOnWriteArrayList;
import static java.util.Objects.requireNonNull;
import static uk.ac.bris.cs.scotlandyard.model.Colour.BLACK;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableCollection;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.*;


public   class ScotlandYardModel implements ScotlandYardGame , Consumer<Move> , MoveVisitor{
	private List<Boolean> rounds;
	private Graph<Integer, Transport> graph;
	private ArrayList<PlayerConfiguration> configurations = new ArrayList<>();
	private List<ScotlandYardPlayer> players = new ArrayList<>();
	private int currentPlayer=0;
	private int currentRound = ScotlandYardView.NOT_STARTED;
	private Collection<Spectator> spectators = new CopyOnWriteArrayList<>();
	private int lastLocation = 0;


	public ScotlandYardModel(List<Boolean> rounds, Graph<Integer, Transport> graph,
							 PlayerConfiguration mrX, PlayerConfiguration firstDetective,
							 PlayerConfiguration... restOfTheDetectives) {


		this.rounds = requireNonNull(rounds);
		if (rounds.isEmpty()) {
			throw new IllegalArgumentException("Empty rounds");
		}

		this.graph = requireNonNull(graph);
		if (graph.isEmpty()) {
			throw new IllegalArgumentException("Empty graph");
		}


		if (mrX.colour != BLACK) { // or mr.colour.isDetective()
			throw new IllegalArgumentException("MrX should be Black");
		}

		//List<PlayerConfiguration> configurations = new ArrayList<>();
		configurations.add(0, mrX);
		configurations.add(0,firstDetective);

		for (PlayerConfiguration configuration : restOfTheDetectives) {
			configurations.add(requireNonNull(configuration));
		}

		Set<Integer> set = new HashSet<>();
		for (PlayerConfiguration configuration : configurations) {
			if (set.contains(configuration.location)) {
				throw new IllegalArgumentException("Duplicate location");
			}
			set.add(configuration.location);
		}

		Set<Colour> set1 = new HashSet<>();
		for (PlayerConfiguration configuration : configurations) {
			if (set1.contains(configuration.colour))
				throw new IllegalArgumentException("Duplicate colour");
			set1.add(configuration.colour);
		}

		for(PlayerConfiguration configuration : configurations) {
			if (configuration.tickets.get(Ticket.SECRET) == null)
				throw new IllegalArgumentException("ALL TYPE TICKET MUST EXIST");
			if (configuration.tickets.get(Ticket.DOUBLE) == null)
				throw new IllegalArgumentException("ALL TYPE TICKET MUST EXIST");
			if (configuration.tickets.get(Ticket.BUS) == null)
				throw new IllegalArgumentException("ALL TYPE TICKET MUST EXIST");
			if (configuration.tickets.get(Ticket.TAXI) == null)
				throw new IllegalArgumentException("ALL TYPE TICKET MUST EXIST");
			if (configuration.tickets.get(Ticket.UNDERGROUND) == null)
				throw new IllegalArgumentException("ALL TYPE TICKET MUST EXIST");
			if ((configuration.tickets.get(Ticket.SECRET) != 0) & configuration.colour != BLACK)
				throw new IllegalArgumentException("Detectives Do not have SECRET TICKETS ");
			if ((configuration.tickets.get(Ticket.DOUBLE) != 0) & configuration.colour != BLACK)
				throw new IllegalArgumentException("Detectives Do not have DOUBLE TICKETS");
		}
          //mutable list of players in ScotlandYard
		players.add(0,new ScotlandYardPlayer(mrX.player,mrX.colour,mrX.location,mrX.tickets));
		players.add(1,new ScotlandYardPlayer(firstDetective.player,firstDetective.colour,firstDetective.location,firstDetective.tickets));
		for(PlayerConfiguration configuration : restOfTheDetectives)
			players.add( new ScotlandYardPlayer(configuration.player,configuration.colour,configuration.location,configuration.tickets));


	}


	@Override
	public void registerSpectator(Spectator spectator) {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public void unregisterSpectator(Spectator spectator) {
		// TODO
		throw new RuntimeException("Implement me");
	}

	@Override
	public void startRotate() {
		    Set<Move> moves= requireNonNull(validMove(players.get(currentPlayer)));
			Player player= players.get(currentPlayer).player();
			player.makeMove(this, players.get(currentPlayer).location(), moves, this);
	}


	@Override
	public Collection<Spectator> getSpectators() {

		return Collections.unmodifiableCollection(spectators);
	}

	@Override
	public List<Colour> getPlayers() {
		List<Colour> colours = new ArrayList<>();
		for(ScotlandYardPlayer player : players){
			colours.add(player.colour());
		}
		return Collections.unmodifiableList(colours);
	}



	@Override
	public Set<Colour> getWinningPlayers() {
		Set<Colour> checkW = new HashSet<>();
		return Collections.unmodifiableSet(checkW);

	}

	@Override
	public Optional<Integer> getPlayerLocation(Colour colour) {
		for (ScotlandYardPlayer player : players){
			if (colour == player.colour()){
				if (player.isDetective()){
					return  Optional.of(player.location());
				}
				else{
					return Optional.of(lastLocation);
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<Integer> getPlayerTickets(Colour colour, Ticket ticket) {
		for (ScotlandYardPlayer SYP : players){
			if (colour == SYP.colour()){
				if (SYP.tickets().containsKey(ticket)){
					return Optional.of(SYP.tickets().get(ticket));
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public boolean isGameOver() {

		return false;
	}

	@Override
	public Colour getCurrentPlayer() {

		return getPlayers().get(currentPlayer);
	}

	@Override
	public int getCurrentRound() {

		return currentRound;
	}

	@Override
	public List<Boolean> getRounds() {

		return Collections.unmodifiableList(rounds);
	}

	@Override
	public Graph<Integer, Transport> getGraph() {

		return new ImmutableGraph<Integer, Transport>(graph);
	}



	// Checks if MrX is Cornered by any Detective . In this case no move can be Done and the Game has ended
	private  Boolean MrXCornered(ScotlandYardPlayer player){
		Set<Integer> PlayerLocation = new HashSet<>();
		for(ScotlandYardPlayer p : players){
			if(p.colour() !=BLACK) PlayerLocation.add(p.location());
		}
		Collection<Edge<Integer, Transport>> edges= graph.getEdgesFrom(graph.getNode(player.location()));
		boolean MrX_can_move=false;
		for( Edge<Integer, Transport> edge : edges)
			MrX_can_move= !PlayerLocation.contains(edge.destination().value()) || MrX_can_move;
		return MrX_can_move;
	}


	// Check if player has Double Tickets
	public boolean hasDouble(ScotlandYardPlayer player) {
		return player.hasTickets(Ticket.DOUBLE);
	}



	// Check if player has Secret Tickets
	public boolean hasSecret(ScotlandYardPlayer player){
		return player.hasTickets(Ticket.SECRET);
	}




	/// Players location not concealed
	public Set<Integer> PlayerLocation() {
		Set<Integer> PlayerLocation = new HashSet<>();
		for (ScotlandYardPlayer p : players) {
			if (p.colour() != BLACK) PlayerLocation.add(p.location());
		}
		return PlayerLocation;
	}
	// Collection of possible  edges from  a given player location
	public Collection<Edge<Integer, Transport>> PossibleMoves(int location){
		return graph.getEdgesFrom(graph.getNode(location));
	}



	// Collection of edges accessible only if there is no player on it
	public Collection<Edge<Integer, Transport>> filterLocation( Collection<Edge<Integer, Transport>> edges) {
		Set<Integer> location = PlayerLocation();
		Collection<Edge<Integer, Transport>> filter_moves = new HashSet<>();
		for (Edge<Integer, Transport> e : edges) {
			if (!location.contains(e.destination().value())) {
				filter_moves.add(e);
			}
		}
		return filter_moves;
	}



	// Collection of edges accessible only if the player has the tickets
	public Collection<Edge<Integer,Transport>> filterTicket(Collection<Edge<Integer, Transport>> edges, ScotlandYardPlayer p)	{
		Collection<Edge<Integer, Transport>> filter_ticket = new HashSet<>();
		for(Edge<Integer, Transport> e : edges) {
			if (p.hasTickets(fromTransport(e.data())))
				filter_ticket.add(e);
		}
		return filter_ticket;
	}



	// Collection of edges accessible by the player
	public Collection<Edge<Integer, Transport>> filterMoves(Collection<Edge<Integer, Transport>> edges, ScotlandYardPlayer p) {
		Collection<Edge<Integer, Transport>> filter_moves = filterLocation(edges);
		filter_moves = filterTicket(filter_moves, p);
		return filter_moves;
	}

    // gets the next player in the game
	private ScotlandYardPlayer NextPlayer(){
		ScotlandYardPlayer nextPlayer;
		currentPlayer= (currentPlayer+1) % players.size();
		nextPlayer= players.get(currentPlayer);
		return nextPlayer;

	}



	// Moves for ALL players , Include Secret Moves to MrX accordingly .
	// Called on all detectives to get their valid Moves
	private Set<Move> getMoves( int location){
		Set<Move> moves= new HashSet<>();
		Collection<Edge<Integer,Transport>> possible_moves= PossibleMoves(location);
		Collection<Edge<Integer,Transport>> possible_location= filterLocation(possible_moves);//
		Collection<Edge<Integer, Transport>> player_moves= filterMoves(possible_moves,players.get(currentPlayer));
		for(Edge<Integer, Transport> e : possible_location ){
			if(hasSecret(players.get(currentPlayer))) {
				moves.add(new TicketMove(players.get(currentPlayer).colour(), Ticket.SECRET, e.destination().value()));
			}
		}
		for(Edge<Integer, Transport> e : player_moves) {
			moves.add(new TicketMove(players.get(currentPlayer).colour(), fromTransport(e.data()), e.destination().value()));
		}
		return moves;
	}



	// Special function to get the moves of MrX including double moves . Called on MrX to get his valid moves
	private Set<Move> MrXMoves(ScotlandYardPlayer player){
		Set<Move> firstMoves= getMoves(players.get(0).location());
		Set<Move> DoubleMoves= new HashSet<>();
		if(hasDouble(player) && currentRound!= rounds.size()-1){
			for(Move init_move : firstMoves){
				TicketMove m = (TicketMove) init_move;
				Set<Move> second_moves = getMoves(m.destination());
				for(Move init_move1 : second_moves){
					TicketMove m1= (TicketMove) init_move1;
					if (m.ticket() != m1.ticket() || player.hasTickets(m.ticket(), 2))
						DoubleMoves.add(new DoubleMove(player.colour(), m, m1));
				}
			}
		}
		firstMoves.addAll(DoubleMoves);
		return firstMoves;
	}



	// Global function with comprises of get moves and MrX moves and calls them accordingly
	// to get the valid moves of the current player in game
	private Set<Move> validMove(ScotlandYardPlayer player) {
		Set<Move> myMoves=getMoves(player.location());
		if (player.colour().equals(BLACK) && myMoves.isEmpty()) {
			return myMoves;
		}
		else if(player.colour().equals(BLACK) && !myMoves.isEmpty()) {
			return MrXMoves(player);
		}
		else if(!player.colour().equals(BLACK) && myMoves.isEmpty()){
			myMoves.add(new PassMove(player.colour()));
			return myMoves;
		}
		else { return myMoves; }
	}




	@Override
	public void visit(PassMove move) {


	}

	@Override
	public void visit(TicketMove move) {


		

	}

	@Override
	public void visit(DoubleMove move) {

	}
	// THIS IS PROBABLY THE FUNCTION WHICH NEED A LOT OF WORK . THIS IS WHERE THE LOGIC OF THE MOVES WORK
	// AND WHERE I BELIEVE YOUR SPECTATOR FUNCTION WILL BE CALLED FROM HERE AS WELL
	// IF YOU HAVE LOOK INTO THIS FUNCTION I GOT ANY IDEA CALL ME
	@Override
	public void accept(Move move) {
		ScotlandYardPlayer player = players.get(currentPlayer);
		Set<Move> moves = validMove(player);
		requireNonNull(moves);
		if (!moves.contains(move)) {
			throw new IllegalArgumentException("It is Not a Valid Move ");

		} else {
			move.visit(this); // use visitor to see what ticket is used
			//if (players.get(currentPlayer).colour().equals(BLACK) ) {
				// Call on Rotation Complete
				// Can't implement until i start 6 as I need spectator
				//currentRound += 1;
			//}

			//Will implement currentRound incrementation once I have implemented the visitor pattern
			//As then I will be able to know  what ticket type is move (double, pass etc.)
			//As I need to know what the ticket type is in order to increment the round twice for a double move

			//Look at ALL the sample game sequences to understand how to update rounds

			player = NextPlayer();
			Set<Move> NextMove = requireNonNull(validMove(player));
			player.player().makeMove(this, player.location(), NextMove, this);

				player = NextPlayer();
				Set<Move> NMove = requireNonNull(validMove(player));
				player.player().makeMove(this, player.location(), NMove, this);

		}
	}


	@Override
	public Consumer<Move> andThen(Consumer<? super Move> after) {

		return null;
	}





}











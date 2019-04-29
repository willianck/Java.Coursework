package uk.ac.bris.cs.scotlandyard.model;

import java.util.*;

import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.ImmutableGraph;
import static java.util.Objects.requireNonNull;
import static uk.ac.bris.cs.scotlandyard.model.Colour.BLACK;
import java.util.function.Consumer;
import static uk.ac.bris.cs.scotlandyard.model.Ticket.*;


public class ScotlandYardModel implements ScotlandYardGame, Consumer<Move>, MoveVisitor, Spectator {
	private final List<Boolean> rounds;
	private Graph<Integer, Transport> graph;
	private final List<ScotlandYardPlayer> players = new ArrayList<>();
	private int currentPlayer = NOT_STARTED;
	private int currentRound = NOT_STARTED;
	private final NotifySpectators spectators = new NotifySpectators();
	private int lastLocation = NOT_STARTED;

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
		ArrayList<PlayerConfiguration> configurations = new ArrayList<>();
		configurations.add(0, mrX);
		configurations.add(1, firstDetective);

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


		for (PlayerConfiguration configuration : configurations) {
			for (Ticket tickets : Ticket.values()) {
				if (!configuration.tickets.containsKey(tickets)) {
					throw new IllegalArgumentException("ALL TYPE TICKET MUST EXIST");

				}
			}
			if (configuration.colour.isDetective()) {
				if ((configuration.tickets.get(Ticket.SECRET) != 0 || configuration.tickets.get(Ticket.DOUBLE) != 0))
					throw new IllegalArgumentException("Detectives Do not have MrX TICKETS ");
			}

		}

		//mutable list of players in ScotlandYard
		players.add(0, new ScotlandYardPlayer(mrX.player, mrX.colour, mrX.location, mrX.tickets));
		players.add(1, new ScotlandYardPlayer(firstDetective.player, firstDetective.colour, firstDetective.location,
				firstDetective.tickets));
		for (PlayerConfiguration configuration : restOfTheDetectives)
			players.add(new ScotlandYardPlayer(configuration.player, configuration.colour, configuration.location,
					configuration.tickets));

	}

	@Override
	public void startRotate() {
		if (isGameOver()) {
			throw new IllegalStateException("Game can not  end on start Round ");
		}
		Set<Move> moves = validMoves(players.get(currentPlayer));
		Player player = players.get(currentPlayer).player();
		player.makeMove(this, players.get(currentPlayer).location(), moves, this);
	}

	@Override
	public void registerSpectator(Spectator spectator) {
		spectators.registerSpectator(spectator);
	}

	@Override
	public void unregisterSpectator(Spectator spectator) {
		spectators.unregisterSpectator(spectator);
	}

	@Override
	public Collection<Spectator> getSpectators() {

		return spectators.getSpectators();
	}

	@Override
	public List<Colour> getPlayers() {
		List<Colour> colours = new ArrayList<>();
		for (ScotlandYardPlayer player : players) {
			colours.add(player.colour());
		}
		return Collections.unmodifiableList(colours);
	}

	//Returns the player for a colour
	private ScotlandYardPlayer playerNow(Colour colour) {
		ScotlandYardPlayer player = null;
		for (ScotlandYardPlayer p : players) {
			if (colour.equals(p.colour())) {
				player = p;
			}
		}
		return player;
	}

	@Override
	public Optional<Integer> getPlayerLocation(Colour colour) {
		for (ScotlandYardPlayer player : detective()) {
			if (colour == player.colour()) {
				return Optional.of(player.location());
			}
		}
		if (colour.isMrX()){
			return Optional.of(lastLocation);
		}
		return Optional.empty();
	}

	@Override
	public Optional<Integer> getPlayerTickets(Colour colour, Ticket ticket) {
		for (ScotlandYardPlayer player : players) {
			if (colour == player.colour()) {
				if (player.tickets().containsKey(ticket)) {
					return Optional.of(player.tickets().get(ticket));
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public  Colour getCurrentPlayer() { return getPlayers().get(currentPlayer); }

	@Override
	public int getCurrentRound() { return currentRound; }

	@Override
	public List<Boolean> getRounds() { return Collections.unmodifiableList(rounds); }

	@Override
	public Graph<Integer, Transport> getGraph() { return new ImmutableGraph<>(graph); }


	//Returns a set of winning players
	@Override
	public Set<Colour> getWinningPlayers() {
		Set<Colour> colourHashSet = new HashSet<>();
		if (isCaptured() || mrxCornered()) {
			colourHashSet.addAll(getPlayers());
			colourHashSet.remove(BLACK);
		}
		if (detectivesStuck() || isRoundsOver()) {
			colourHashSet.add(BLACK);
		}
		return Collections.unmodifiableSet(colourHashSet);
	}


	//Returns true if the game is over
	@Override
	public boolean isGameOver() {
		if (isCaptured()) return true;

		if (detectivesStuck()) return true;

		if (mrxCornered()) return true;

		return (isRoundsOver() );
	}


	//Checks if a detective is on the same space as Mrx
	private  boolean isCaptured() {
		for (ScotlandYardPlayer player : detective()) {
			if (players.get(0).location() == player.location()) {
				return true;
			}
		}
		return false;
	}


	//Checks if a boolean array is all true
	private boolean checkTrue(List<Boolean> array) {
		for (boolean check : array) {
			if (!check) {
				return false;
			}
		}
		return true;
	}

	//Checks if all the detectives are stuck
	private  boolean detectivesStuck() {
		List<Boolean> check = new ArrayList<>();
		for (ScotlandYardPlayer player : detective()) {
			if (!player.hasTickets(BUS) && !player.hasTickets(UNDERGROUND) && !player.hasTickets(TAXI)) {
				check.add(true);
			} else {
				check.add(false);
			}
		}
		return checkTrue(check);
	}


	//Checks if Mrx is cornered
	private boolean mrxCornered() {
		return players.get(currentPlayer).isMrX() && validMoves(players.get(currentPlayer)).isEmpty(); }


	//checks if all the rounds have been used up
	private boolean isRoundsOver() {
		return (currentRound == rounds.size() && players.get(currentPlayer).isMrX());
	}


	//Returns a list of detectives
	public List<ScotlandYardPlayer> detective() {
		List<ScotlandYardPlayer> detectives = new ArrayList<>();
		for (ScotlandYardPlayer player : players) {
			if (player.isDetective()) {
				detectives.add(player);
			}
		}
		return detectives;
	}


	//Removes ticket and makes the intended move
	private void doubleHelper(ScotlandYardPlayer player, TicketMove move) {
		player.removeTicket(move.ticket());
		visitHelper(move);
		player.location(move.destination());
	}


	//Helper method for TicketMove and DoubleMove
	private void visitHelper(TicketMove move) {
		currentRound++;
		spectators.roundHasStarted(this, currentRound);
		spectators.moveIsMade(this, move);
	}


	//If it's a reveal round updates lastLocation
	private int updateLastLocation(int storedLastLocation, int move, int round){
		if (rounds.get(round)) {
			  storedLastLocation = move ;
		}
		return storedLastLocation;
	}


	/// Set of Detectives  Location
	private  Set<Integer> playerLocation() {
		Set<Integer> playerLocation = new HashSet<>();
		for (ScotlandYardPlayer player : detective()) {
			playerLocation.add(player.location());
		}
		return playerLocation;
	}


	// Collection of  all  possible  edges from  a  node ( player's location)
	private Collection<Edge<Integer, Transport>> possibleMoves(int location) {
		return graph.getEdgesFrom(graph.getNode(location));
	}


	// Collection of edges accessible only if there is no player on the node
	private Collection<Edge<Integer, Transport>> filterLocation(Collection<Edge<Integer, Transport>> edges) {
		Set<Integer> location = playerLocation();
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
															  ScotlandYardPlayer player) {
		Collection<Edge<Integer, Transport>> filter_ticket = new HashSet<>();
		for (Edge<Integer, Transport> e : edges) {
			if (player.hasTickets(fromTransport(e.data())))
				filter_ticket.add(e);
		}
		return filter_ticket;
	}


	// Collection of edges  only  accessible by the player
	private Collection<Edge<Integer, Transport>> filterMoves(Collection<Edge<Integer, Transport>> edges,
															 ScotlandYardPlayer player) {
		Collection<Edge<Integer, Transport>> filter_moves = filterLocation(edges);
		return  filterTicket(filter_moves, player);
	}


	// gets the next player in the game
	private ScotlandYardPlayer nextPlayer() {
		ScotlandYardPlayer nextPlayer;
		currentPlayer = (currentPlayer + 1) % players.size();
		nextPlayer = players.get(currentPlayer);
		return nextPlayer;
	}



	// Moves for ALL players , Include Secret Moves to MrX accordingly .
	// Called on all detectives to get their valid Moves
	private Set<Move> getMoves(int location) {
		Set<Move> moves = new HashSet<>();
		Collection<Edge<Integer, Transport>> possible_moves = possibleMoves(location);

        Collection<Edge<Integer, Transport>> possible_location = filterLocation(possible_moves);//
		Collection<Edge<Integer, Transport>> player_moves = filterMoves(possible_moves, players.get(currentPlayer));
		for (Edge<Integer, Transport> e : possible_location) {
			if ((players.get(currentPlayer).hasTickets(SECRET))) {
				moves.add(new TicketMove(players.get(currentPlayer).colour(), Ticket.SECRET, e.destination().value()));
			}
		}
		for (Edge<Integer, Transport> e : player_moves) {
			moves.add(new TicketMove(players.get(currentPlayer).colour(), fromTransport(e.data()),
					e.destination().value()));
		}
		return moves;
	}



	// Special function to get the moves of MrX including double moves . Called on MrX to get his valid moves
	private Set<Move> mrXMoves(ScotlandYardPlayer player) {
		Set<Move> firstMoves = getMoves(players.get(0).location());
		Set<Move> DoubleMoves = new HashSet<>();
		if ((player.hasTickets(DOUBLE)) && currentRound != rounds.size() - 1) {
			for (Move init_move : firstMoves) {
				TicketMove m = (TicketMove) init_move;
				Set<Move> second_moves = getMoves(m.destination());
				for (Move init_move1 : second_moves) {
					TicketMove m1 = (TicketMove) init_move1;
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
	private Set<Move> validMoves(ScotlandYardPlayer player) {
		Set<Move> myMoves = getMoves(player.location());
 		 if (player.colour().equals(BLACK) && !myMoves.isEmpty()) {
			return mrXMoves(player);
		} else if (!player.colour().equals(BLACK) && myMoves.isEmpty()) {
			myMoves.add(new PassMove(player.colour()));
			return myMoves;
		} else {
			return myMoves;
		}
	}


	@Override
	public void accept(Move move) {
		requireNonNull(move);
		ScotlandYardPlayer player;
		Set<Move> moves = validMoves(players.get(currentPlayer));
		player = nextPlayer();
		if (!moves.contains(move)) {
			throw new IllegalArgumentException("It is Not a Valid Move ");
		}
		else {
			move.visit(this); // use visitor to see what ticket is used
		}
		if (!player.isMrX() && !isGameOver()) {
			Set<Move> NMove = (validMoves(player));
			player.player().makeMove(this, player.location(), NMove, this);

		}
		else {
			if (isGameOver()) {
				spectators.gameIsOver(this, getWinningPlayers());
			}
			else {
				spectators.rotationIsComplete(this);
			}
		}
	}


	@Override
	public void visit(PassMove move) {
		spectators.moveIsMade(this, move);
	}


	@Override
	public void visit(TicketMove move) {
		ScotlandYardPlayer player = playerNow(move.colour());
		player.location(move.destination());
		player.removeTicket(move.ticket());

		if (player.isDetective()) {
			players.get(0).addTicket(move.ticket());
			spectators.moveIsMade(this, move);
		}
		else {
			lastLocation = updateLastLocation (lastLocation, move.destination (),currentRound);
			TicketMove ticketMove = new TicketMove(move.colour(), move.ticket(), lastLocation);
			visitHelper(ticketMove);
		}
	}



	@Override
	public void visit(DoubleMove move) {
		ScotlandYardPlayer player = playerNow(move.colour());
		int firstMoveLastLocation;
		int storedLastLocation = lastLocation;

		firstMoveLastLocation = storedLastLocation =
				updateLastLocation (storedLastLocation,move.firstMove ().destination (), currentRound);

		TicketMove FMove = new TicketMove(player.colour(),move.firstMove().ticket(),storedLastLocation);

		storedLastLocation =
				updateLastLocation (storedLastLocation,move.secondMove ().destination (), currentRound + 1);

		TicketMove SMove = new TicketMove(player.colour(),move.secondMove().ticket(),storedLastLocation);

		player.removeTicket(DOUBLE);

		DoubleMove NotifyMove = new DoubleMove(player.colour(), FMove, SMove);

		spectators.moveIsMade(this, NotifyMove);

		lastLocation = firstMoveLastLocation;

		doubleHelper(player, FMove);
		player.location(move.firstMove().destination());

		lastLocation = storedLastLocation;

		doubleHelper(player, SMove);
		player.location(move.secondMove().destination());
	}

}
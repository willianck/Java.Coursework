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
import uk.ac.bris.cs.scotlandyard.SpectatorHelper.NotifySpectator;


public   class ScotlandYardModel implements ScotlandYardGame , Consumer<Move> , MoveVisitor,Spectator{
	private List<Boolean> rounds;
	private Graph<Integer, Transport> graph;
	private ArrayList<PlayerConfiguration> configurations = new ArrayList<>();
	private List<ScotlandYardPlayer> players = new ArrayList<>();
	private int currentPlayer=0;
	private int currentRound = ScotlandYardView.NOT_STARTED;
	private NotifySpectator spectators = new NotifySpectator ();
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
		spectators.registerSpectator (spectator);
	}

	@Override
	public void unregisterSpectator(Spectator spectator) {
		spectators.unregisterSpectator (spectator);
	}

	@Override
	public void startRotate() {
		if(isGameOver ()){

			throw new IllegalStateException ("Game can not  end on start Round ");
		}
		Set<Move> moves= validMove(players.get(currentPlayer));
		Player player= players.get(currentPlayer).player();
		player.makeMove(this, players.get(currentPlayer).location(), moves, this);

	}


	@Override
	public Collection<Spectator> getSpectators() {

		return spectators.getSpectators ();
	}

	@Override
	public List<Colour> getPlayers() {
		List<Colour> colours = new ArrayList<>();
		for(ScotlandYardPlayer player : players){
			colours.add(player.colour());
		}
		return Collections.unmodifiableList(colours);
	}

	private ScotlandYardPlayer playerNow(Colour c){
		ScotlandYardPlayer player=null;
		for(ScotlandYardPlayer p : players){
			if( c.equals(p.colour())) {
				player=p;
			}
		}
		return player;
	}



	@Override
	public Set<Colour> getWinningPlayers() {
		Set<Colour> checkW = new HashSet<>();
		if(isCaptured() || players.get(currentPlayer).isMrX() && validMove(players.get(currentPlayer)).isEmpty ()){
			checkW.addAll(getPlayers());
			checkW.remove(BLACK);
		}
		if(detectivesStuck() || isRoundsOver() && players.get(currentPlayer).isMrX()){
			checkW.add(BLACK);
		}
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

	//returns a list of detectives
	private List<ScotlandYardPlayer> detective(){
		List <ScotlandYardPlayer> detectives = new ArrayList<>();
		for(ScotlandYardPlayer player : players){
			if (player.isDetective()){
				detectives.add(player);

			}
		}
		return detectives;

	}

	private boolean areAllTrue(List<Boolean> array)
	{
		for(boolean b : array){
			if(!b){
				return false;
			}
		}
		return true;
	}


	private boolean isCaptured(){
		for (ScotlandYardPlayer player : detective()){
			if(players.get(0).location() == player.location()){
				return true;
			}
		}
		return false;
	}

	private boolean isRoundsOver(){ return (currentRound == rounds.size()); }

	private boolean detectivesStuck() {
		//Checks if all the detectives are stuck
		List<Boolean> check = new ArrayList<>();
		for (ScotlandYardPlayer player : detective()) {
			if (!player.hasTickets(BUS) && !player.hasTickets(UNDERGROUND) && !player.hasTickets(TAXI)) {
				check.add(true);
			} else {
				check.add(false);
			}
		}
		return areAllTrue(check);
	}


	@Override
	public boolean isGameOver() {
		if(isCaptured()) return true;

		if(detectivesStuck()) return true;

		if(players.get(currentPlayer).isMrX() && validMove(players.get(currentPlayer)).isEmpty ())
			return true;

		return (isRoundsOver() && players.get(currentPlayer).isMrX());
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

		return new ImmutableGraph <> (graph);
	}

	// Check if player has Double Tickets
	private boolean hasDouble(ScotlandYardPlayer player) {
		return player.hasTickets(Ticket.DOUBLE);
	}

	// Check if player has Secret Tickets
	private boolean hasSecret(ScotlandYardPlayer player){
		return player.hasTickets(Ticket.SECRET);
	}


	/// Players location not concealed
	private Set<Integer> PlayerLocation() {
		Set<Integer> PlayerLocation = new HashSet<>();
		for (ScotlandYardPlayer p : players) {
			if (p.colour() != BLACK) PlayerLocation.add(p.location());
		}
		return PlayerLocation;
	}
	// Collection of possible  edges from  a given player location
	private Collection<Edge<Integer, Transport>> PossibleMoves(int location){
		return graph.getEdgesFrom(graph.getNode(location));
	}



	// Collection of edges accessible only if there is no player on it
	private Collection<Edge<Integer, Transport>> filterLocation( Collection<Edge<Integer, Transport>> edges) {
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
	private Collection<Edge<Integer,Transport>> filterTicket(Collection<Edge<Integer, Transport>> edges, ScotlandYardPlayer p)	{
		Collection<Edge<Integer, Transport>> filter_ticket = new HashSet<>();
		for(Edge<Integer, Transport> e : edges) {
			if (p.hasTickets(fromTransport(e.data())))
				filter_ticket.add(e);
		}
		return filter_ticket;
	}



	// Collection of edges accessible by the player
	private Collection<Edge<Integer, Transport>> filterMoves(Collection<Edge<Integer, Transport>> edges, ScotlandYardPlayer p) {
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
		spectators.MoveisMade (this,move);
	}


	@Override
	public void visit(TicketMove move) {
		//ScotlandYardPlayer player = players.get(currentPlayer);
		ScotlandYardPlayer player= playerNow(move.colour());
		player.location(move.destination());
		player.removeTicket(move.ticket());



		if (player.isDetective()){
			players.get(0).addTicket(move.ticket());
			spectators.MoveisMade(this,move);
		}

		else{
			if (rounds.get(currentRound)){
				lastLocation = move.destination();
			}
			TicketMove move1 = new TicketMove (move.colour (),move.ticket (),lastLocation);

			currentRound++;
			spectators.RoundhasStarted (this,currentRound);
			spectators.MoveisMade(this,move1);


		}

	}



	@Override
	public void visit(DoubleMove move) {
		ScotlandYardPlayer player=playerNow(move.colour());
         int l = lastLocation;
         int firstL = lastLocation;


		player.removeTicket(DOUBLE);

		System.out.println ( "before" + "----------------------->" + lastLocation);
		if (rounds.get(currentRound)) {
			l = move.firstMove().destination ();
			firstL = l;



		}
		TicketMove FMove = new TicketMove(player.colour(),move.firstMove().ticket(),l);


		if (rounds.get(currentRound + 1)){
			l = move.secondMove().destination();
			
		}

		TicketMove SMove= new TicketMove(player.colour(),move.secondMove().ticket(),l);




		DoubleMove NotifyMove= new DoubleMove(player.colour(),FMove,SMove);

		System.out.println ("after" + "----------------------->" + l);





		spectators.MoveisMade(this,NotifyMove);



		lastLocation=firstL;


		player.removeTicket(move.firstMove().ticket());


		currentRound++;
		spectators.RoundhasStarted (this,currentRound);
		spectators.MoveisMade(this,FMove);
		player.location(move.firstMove().destination());

		lastLocation=l;

		player.removeTicket(move.secondMove().ticket());

		currentRound++;
		spectators.RoundhasStarted (this,currentRound);
		spectators.MoveisMade(this,SMove);
		player.location(move.secondMove().destination());

	}







	@Override
	public void accept(Move move) {
		requireNonNull(move);
		ScotlandYardPlayer player;
		Set<Move> moves = validMove(players.get(currentPlayer));
		player=NextPlayer();
		if (!moves.contains(move)) {
			throw new IllegalArgumentException("It is Not a Valid Move ");

		} else {

			move.visit(this); // use visitor to see what ticket is used

		}
			if(!player.isMrX() && !isGameOver()) {
				Set<Move> NMove = (validMove(player));
				player.player().makeMove(this, player.location(), NMove, this);

			}
			else{
				if(isGameOver ()){
					spectators.GameisOver (this,getWinningPlayers ());
				}
				else{
					spectators.RotationisComplete (this);
				}
			}

	}




	@Override
	public Consumer<Move> andThen(Consumer<? super Move> after) {

		return null;
	}





}

package uk.ac.bris.cs.scotlandyard.model;
import java.util.*;
import uk.ac.bris.cs.gamekit.graph.Graph;
import uk.ac.bris.cs.gamekit.graph.ImmutableGraph;
import java.util.concurrent.CopyOnWriteArrayList;
import static java.util.Objects.requireNonNull;
import static uk.ac.bris.cs.scotlandyard.model.Colour.BLACK;



public class ScotlandYardModel implements ScotlandYardGame {
	private List<Boolean> rounds;
	private Graph<Integer, Transport> graph;
	private ArrayList<PlayerConfiguration> configurations = new ArrayList<>();
	private List<ScotlandYardPlayer> players = new ArrayList<>();
	private int currentPlayer;
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

		List<PlayerConfiguration> configurations = new ArrayList<>();
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
		// TODO
		throw new RuntimeException("Implement me");
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

}

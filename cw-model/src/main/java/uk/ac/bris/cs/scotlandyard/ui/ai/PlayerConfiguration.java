package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.Ticket;
import java.util.HashMap;
import java.util.Map;



/**
 * A class that contains all the information about a particular player.
 */


public class PlayerConfiguration {


        private final Colour colour;
        private int location;
        private final Map<Ticket, Integer> tickets;

        /**
         * Constructs a new PlayerData object.
         *
         * @param colour the colour of the player.
         * @param location the location of the player.
         * @param tickets the tickets associated with the player.
         */
        public PlayerConfiguration(Colour colour, int location,
                          Map<Ticket, Integer> tickets) {
            this.colour = colour;
            this.location = location;
            this.tickets = new HashMap<>(tickets);

        }

        /**
         * @return the colour of the player
         */
        public PlayerConfiguration clone() {
            PlayerConfiguration player = new PlayerConfiguration(this.colour,this.location,this.tickets);
            return player;
        }

        /**
         * @return the colour of the player
         */
        public Colour colour() {
            return colour;
        }

        /**
         * Sets the player's current location.
         *
         * @param location the location to set
         */
        public void location(int location) {
            this.location = location;
        }

        /**
         * @return the player's current location.
         */
        public int location() {
            return location;
        }

        /**
         * @return the player's current tickets.
         */
        public Map<Ticket, Integer> tickets() {
            return tickets;
        }

        /**
         * Adds a ticket to the player's current tickets.
         *
         * @param ticket the ticket to be added.
         */
        public void addTicket(Ticket ticket) {
            adjustTicketCount(ticket, 1);
        }

        /**
         * Removes a ticket to the player's current tickets.
         *
         * @param ticket the ticket to be removed.
         */
        public void removeTicket(Ticket ticket) {
            adjustTicketCount(ticket, -1);
        }

        public void adjustTicketCount(Ticket ticket, int by) {
            Integer ticketCount = tickets.get(ticket);
            ticketCount += by;
            tickets.remove(ticket);
            tickets.put(ticket, ticketCount);
        }

        /**
         * Checks whether the player has the given ticket
         *
         * @param ticket the ticket to check for; not null
         * @return true if the player has the given ticket, false otherwise
         */
        public boolean hasTickets(Ticket ticket) {
            return tickets.get(ticket) != 0;
        }

        /**
         * Checks whether the player has the given ticket and quantity
         *
         * @param ticket the ticket to check for; not null
         * @param quantityInclusive whether the ticket count is greater than or
         *        equal to given quantity
         * @return true if the player has the quantity of the given ticket, false
         *         otherwise
         */
        public boolean hasTickets(Ticket ticket, int quantityInclusive) {
            return tickets.get(ticket) >= quantityInclusive;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ScotlandYardPlayer{");
            sb.append(", colour=").append(colour);
            sb.append(", location=").append(location);
            sb.append(", tickets=").append(tickets);
            sb.append('}');
            return sb.toString();
        }
    }



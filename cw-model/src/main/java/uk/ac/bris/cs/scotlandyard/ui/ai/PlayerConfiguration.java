package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Colour;
import uk.ac.bris.cs.scotlandyard.model.Ticket;

import java.util.HashMap;
import java.util.Map;



/**
 * A class that contains all the information about a particular player.
 */


class PlayerConfiguration {


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
         * Checks whether the player has the given ticket
         *
         * @param ticket the ticket to check for; not null
         * @return true if the player has the given ticket, false otherwise
         */
         boolean hasTickets(Ticket ticket) {
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
         boolean hasTickets(Ticket ticket, int quantityInclusive) {
            return tickets.get(ticket) >= quantityInclusive;
        }


        @Override
        public String toString() {
            return "ScotlandYardPlayer{" + ", colour=" + colour +
                    ", location=" + location +
                    ", tickets=" + tickets +
                    '}';
        }
    }



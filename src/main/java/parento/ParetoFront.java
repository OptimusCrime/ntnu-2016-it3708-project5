package parento;

import nsga.Individual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParetoFront {

    private ArrayList<Individual> members;
    private int index;

    /**
     * Constructor
     * @param index The layer id
     */

    public ParetoFront(int index) {
        // Create empty list of members
        this.members = new ArrayList<>();

        // The index
        this.index = index;
    }

    public int getIndex() {
        return this.index;
    }

    /**
     * Add member to the front
     * @param individual Individual to add
     */

    public void addMember(Individual individual) {
        // Add this individual to the front
        members.add(individual);
    }

    public void addAll(ArrayList<Individual> list) {
        members.addAll(list);
    }

    /**
     * Get member of this front by an id
     * @param index Index to fetch member of
     * @return The member with corresponding index
     */

    public Individual getMember(int index) {
        return this.members.get(index);
    }

    /**
     * Returns number of members of this front
     * @return Size of front
     */

    public int getSize() {
        return this.members.size();
    }

    /**
     * Return all members of this front
     * @return ArrayList with members
     */

    public ArrayList<Individual> getAllMembers() {
        return this.members;
    }
}

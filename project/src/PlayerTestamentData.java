package com.fallengod.testament.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores testament progress for a single player
 * Thread-safe implementation for concurrent access
 */
public class PlayerTestamentData {
    // godType -> set of fragment numbers found (e.g. 1-7)
    private final Map<String, Set<Integer>> fragmentsFound = new ConcurrentHashMap<>();
    // godTypes completed
    private final Set<String> completedTestaments = ConcurrentHashMap.newKeySet();

    /**
     * Returns the set of fragment numbers found for a god (never null)
     */
    public Set<Integer> getFragmentsFound(String godType) {
        return fragmentsFound.computeIfAbsent(godType, k -> ConcurrentHashMap.newKeySet());
    }

    /**
     * Sets the set of fragment numbers found for a god
     */
    public void setFragmentsFound(String godType, Set<Integer> fragmentNumbers) {
        fragmentsFound.put(godType, ConcurrentHashMap.newKeySet());
        fragmentsFound.get(godType).addAll(fragmentNumbers);
    }

    /**
     * Adds a fragment number for a god
     * @return true if this was a new fragment, false if already had it
     */
    public boolean addFragment(String godType, int fragmentNumber) {
        if (fragmentNumber < 1 || fragmentNumber > 7) {
            return false;
        }
        return getFragmentsFound(godType).add(fragmentNumber);
    }

    /**
     * Removes a fragment number for a god
     * @return true if the fragment was removed, false if it wasn't present
     */
    public boolean removeFragment(String godType, int fragmentNumber) {
        return getFragmentsFound(godType).remove(fragmentNumber);
    }
    
    /**
     * Clears all fragments for a god type
     */
    public void clearFragments(String godType) {
        fragmentsFound.remove(godType);
    }

    /**
     * Returns the number of unique fragments found for a god
     */
    public int getFragmentCount(String godType) {
        return getFragmentsFound(godType).size();
    }

    /**
     * Checks if a player has a specific fragment
     */
    public boolean hasFragment(String godType, int fragmentNumber) {
        return getFragmentsFound(godType).contains(fragmentNumber);
    }

    public boolean isTestamentCompleted(String godType) {
        return completedTestaments.contains(godType);
    }

    public void completeTestament(String godType) {
        completedTestaments.add(godType);
    }

    public Set<String> getCompletedTestaments() {
        return new HashSet<>(completedTestaments);
    }

    /**
     * Returns a copy of the fragments map (godType -> set of fragment numbers)
     */
    public Map<String, Set<Integer>> getFragmentsMap() {
        Map<String, Set<Integer>> copy = new HashMap<>();
        for (Map.Entry<String, Set<Integer>> entry : fragmentsFound.entrySet()) {
            copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return copy;
    }
    
    /**
     * Gets progress summary for a god type
     */
    public String getProgressSummary(String godType) {
        Set<Integer> fragments = getFragmentsFound(godType);
        if (isTestamentCompleted(godType)) {
            return "§a§lCOMPLETED";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("§7").append(fragments.size()).append("/7 §8[");
        
        for (int i = 1; i <= 7; i++) {
            if (fragments.contains(i)) {
                summary.append("§a").append(i);
            } else {
                summary.append("§c").append(i);
            }
            if (i < 7) summary.append("§8,");
        }
        
        summary.append("§8]");
        return summary.toString();
    }
}
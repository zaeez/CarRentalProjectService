package ch.unil.softarch.luxurycarrental.rest;

import java.time.LocalDateTime;
import java.util.List;

public class DebugRequest {
    private List<LocalDateTime> timeslots;

    // Getters and setters
    public List<LocalDateTime> getTimeslots() {
        return timeslots;
    }

    public void setTimeslots(List<LocalDateTime> timeslots) {
        this.timeslots = timeslots;
    }
}

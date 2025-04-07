package cit.edu.mmr.dto;

public class CountdownDTO {
    private long days;
    private long hours;
    private long minutes;
    private int seconds;
    private boolean isOpen;

    // Constructor
    public CountdownDTO(long days, long hours, long minutes, int seconds, boolean isOpen) {
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.isOpen = isOpen;
    }

    // Getters
    public long getDays() { return days; }
    public long getHours() { return hours; }
    public long getMinutes() { return minutes; }
    public int getSeconds() { return seconds; }
    public boolean isOpen() { return isOpen; }
}
package de.timmi6790.mineplex.stats.common.models;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class ParserResult {
    private final String[][] leaderboard;
    private final String[] header;
    private final ZonedDateTime highestTime;
}

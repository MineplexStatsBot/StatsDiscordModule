package de.timmi6790.mineplex_stats.statsapi.models.java;

import de.timmi6790.mineplex_stats.statsapi.models.ResponseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class FilteredPlayers extends ResponseModel {
    private final Map<String, Map<String, Map<String, List<Integer>>>> games;
}

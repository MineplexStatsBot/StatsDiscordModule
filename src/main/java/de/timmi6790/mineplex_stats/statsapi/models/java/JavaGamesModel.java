package de.timmi6790.mineplex_stats.statsapi.models.java;

import de.timmi6790.mineplex_stats.statsapi.models.ResponseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class JavaGamesModel extends ResponseModel {
    private final Map<String, JavaGame> games;
}

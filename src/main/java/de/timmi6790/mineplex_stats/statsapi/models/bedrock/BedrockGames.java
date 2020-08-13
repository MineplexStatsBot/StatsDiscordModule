package de.timmi6790.mineplex_stats.statsapi.models.bedrock;

import de.timmi6790.mineplex_stats.statsapi.models.ResponseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class BedrockGames extends ResponseModel {
    private final List<BedrockGame> games;
}

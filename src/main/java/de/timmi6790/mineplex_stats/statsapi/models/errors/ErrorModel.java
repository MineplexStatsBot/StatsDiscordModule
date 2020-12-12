package de.timmi6790.mineplex_stats.statsapi.models.errors;


import de.timmi6790.mineplex_stats.statsapi.models.ResponseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ErrorModel extends ResponseModel {
    private final int errorCode;
    private final String errorMessage;
}

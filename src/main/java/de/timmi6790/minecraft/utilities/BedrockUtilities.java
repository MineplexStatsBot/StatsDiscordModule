package de.timmi6790.minecraft.utilities;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public class BedrockUtilities {
    private final Pattern NAME_PATTERN = Pattern.compile("^.{3,32}$");

    public boolean isValidName(@NonNull final String playerName) {
        return NAME_PATTERN.matcher(playerName).find();
    }
}

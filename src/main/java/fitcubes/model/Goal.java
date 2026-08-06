package fitcubes.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Goal {
    WEIGHT_LOSS("Weight Loss"),
    MUSCLE_GAIN("Muscle Gain"),
    MAINTENANCE("Maintenance");

    private final String displayName;
}

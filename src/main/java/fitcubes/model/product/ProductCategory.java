package fitcubes.model.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductCategory {
    DAIRY_AND_CHEESE("Dairy & Cheese"),
    MEAT_AND_POULTRY("Meat & Poultry"),
    FISH_AND_SEAFOOD("Fish & Seafood"),
    VEGETABLES("Vegetables"),
    FRUITS("Fruits"),
    GRAINS_AND_CEREALS("Grains & Cereals"),
    NUTS_AND_SEEDS("Nuts & Seeds"),
    SWEETS_AND_SPREADS("Sweets & Spreads"),
    PREPARED_MEALS("Prepared Meals"),
    OTHER("Other");

    private final String displayName;
}

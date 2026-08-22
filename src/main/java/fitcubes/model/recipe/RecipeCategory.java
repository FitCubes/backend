package fitcubes.model.recipe;

public class RecipeCategory {
    public static final String BREAKFAST = "BREAKFAST";
    public static final String LUNCH = "LUNCH";
    public static final String DINNER = "DINNER";
    public static final String SNACK = "SNACK";
    public static final String DESSERT = "DESSERT";
    public static final String MY_MEALS = "MY_MEALS";

    public static final String SOUPS = "Soups";
    public static final String SALADS = "Salads";
    public static final String MAIN_DISHES = "Main Dishes";
    public static final String SNACKS = "Snacks";

    private RecipeCategory() {
        throw new UnsupportedOperationException("This is a utility "
                + "class and cannot be instantiated");
    }

}

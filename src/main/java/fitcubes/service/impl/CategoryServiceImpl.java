package fitcubes.service.impl;

import fitcubes.model.recipe.RecipeCategory;
import fitcubes.service.CategoryService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl implements CategoryService {

    public List<String> getAvailableCategories() {
        return List.of(
                RecipeCategory.BREAKFAST,
                RecipeCategory.LUNCH,
                RecipeCategory.DINNER,
                RecipeCategory.SNACK,
                RecipeCategory.DESSERT,
                RecipeCategory.MY_MEALS,
                RecipeCategory.SOUPS,
                RecipeCategory.SALADS,
                RecipeCategory.MAIN_DISHES,
                RecipeCategory.SNACKS
        );
    }
}

package fitcubes.controller;

import fitcubes.service.product.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class RecipeCategoryController {

    private final CategoryService categoryService;

    @GetMapping("/presets")
    public ResponseEntity<List<String>> getCategoryPresets() {
        return ResponseEntity.ok(categoryService.getAvailableCategories());
    }
}

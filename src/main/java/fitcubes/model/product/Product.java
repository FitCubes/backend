package fitcubes.model.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "calories", nullable = false)
    private int calories;

    @Column(name = "fat", nullable = false)
    private double fat;

    @Column(name = "protein", nullable = false)
    private double protein;

    @Column(name = "carbohydrates", nullable = false)
    private double carbohydrates;

}

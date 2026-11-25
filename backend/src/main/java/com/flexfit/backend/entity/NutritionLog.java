package com.flexfit.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "nutrition_logs", indexes = {
        @Index(name = "idx_nutrition_logs_user_id", columnList = "user_id"),
        @Index(name = "idx_nutrition_logs_logged_date", columnList = "logged_date")
})
public class NutritionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Size(max = 120)
    @Column(name = "meal_name", nullable = false, length = 120)
    private String mealName;

    @NotBlank
    @Size(max = 30)
    @Column(name = "meal_type", nullable = false, length = 30)
    private String mealType;

    @NotNull
    @Min(0)
    @Column(name = "calories", nullable = false)
    private Integer calories;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "protein_g", nullable = false, precision = 6, scale = 2)
    private BigDecimal proteinG = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "carbs_g", nullable = false, precision = 6, scale = 2)
    private BigDecimal carbsG = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "fats_g", nullable = false, precision = 6, scale = 2)
    private BigDecimal fatsG = BigDecimal.ZERO;

    @NotNull
    @Column(name = "logged_date", nullable = false)
    private LocalDate loggedDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public NutritionLog() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.loggedDate == null) {
            this.loggedDate = LocalDate.now();
        }
        if (this.proteinG == null) {
            this.proteinG = BigDecimal.ZERO;
        }
        if (this.carbsG == null) {
            this.carbsG = BigDecimal.ZERO;
        }
        if (this.fatsG == null) {
            this.fatsG = BigDecimal.ZERO;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMealName() {
        return mealName;
    }

    public void setMealName(String mealName) {
        this.mealName = mealName;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public Integer getCalories() {
        return calories;
    }

    public void setCalories(Integer calories) {
        this.calories = calories;
    }

    public BigDecimal getProteinG() {
        return proteinG;
    }

    public void setProteinG(BigDecimal proteinG) {
        this.proteinG = proteinG;
    }

    public BigDecimal getCarbsG() {
        return carbsG;
    }

    public void setCarbsG(BigDecimal carbsG) {
        this.carbsG = carbsG;
    }

    public BigDecimal getFatsG() {
        return fatsG;
    }

    public void setFatsG(BigDecimal fatsG) {
        this.fatsG = fatsG;
    }

    public LocalDate getLoggedDate() {
        return loggedDate;
    }

    public void setLoggedDate(LocalDate loggedDate) {
        this.loggedDate = loggedDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

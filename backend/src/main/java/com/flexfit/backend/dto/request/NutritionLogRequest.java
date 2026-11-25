package com.flexfit.backend.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class NutritionLogRequest {

    @NotBlank(message = "Meal name is required")
    @Size(max = 120, message = "Meal name must not exceed 120 characters")
    private String mealName;

    @NotBlank(message = "Meal category is required")
    @Size(max = 30, message = "Meal category must not exceed 30 characters")
    private String mealType;

    @NotNull(message = "Calories are required")
    @Min(value = 0, message = "Calories cannot be negative")
    @Max(value = 10000, message = "Calories must not exceed 10000")
    private Integer calories;

    @NotNull(message = "Protein is required")
    @DecimalMin(value = "0.00", message = "Protein cannot be negative")
    @DecimalMax(value = "1000.00", message = "Protein must not exceed 1000 g")
    private BigDecimal proteinG;

    @NotNull(message = "Carbohydrates are required")
    @DecimalMin(value = "0.00", message = "Carbohydrates cannot be negative")
    @DecimalMax(value = "1000.00", message = "Carbohydrates must not exceed 1000 g")
    private BigDecimal carbsG;

    @NotNull(message = "Fat is required")
    @DecimalMin(value = "0.00", message = "Fat cannot be negative")
    @DecimalMax(value = "1000.00", message = "Fat must not exceed 1000 g")
    private BigDecimal fatsG;

    @NotNull(message = "Logged date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate loggedDate = LocalDate.now();

    public NutritionLogRequest() {
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
}

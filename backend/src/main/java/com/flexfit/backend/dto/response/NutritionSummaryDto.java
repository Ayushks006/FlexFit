package com.flexfit.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class NutritionSummaryDto {
    private LocalDate selectedDate;
    private NutritionTargetDto target;
    private int caloriesConsumed;
    private int remainingCalories;
    private int calorieProgressPercent;
    private BigDecimal proteinConsumedG;
    private BigDecimal remainingProteinG;
    private int proteinProgressPercent;
    private BigDecimal carbsConsumedG;
    private BigDecimal remainingCarbsG;
    private int carbsProgressPercent;
    private BigDecimal fatsConsumedG;
    private BigDecimal remainingFatsG;
    private int fatsProgressPercent;
    private int mealsLoggedCount;
    private Map<String, List<NutritionLogDto>> mealsByType;

    public NutritionSummaryDto() {
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(LocalDate selectedDate) {
        this.selectedDate = selectedDate;
    }

    public NutritionTargetDto getTarget() {
        return target;
    }

    public void setTarget(NutritionTargetDto target) {
        this.target = target;
    }

    public int getCaloriesConsumed() {
        return caloriesConsumed;
    }

    public void setCaloriesConsumed(int caloriesConsumed) {
        this.caloriesConsumed = caloriesConsumed;
    }

    public int getRemainingCalories() {
        return remainingCalories;
    }

    public void setRemainingCalories(int remainingCalories) {
        this.remainingCalories = remainingCalories;
    }

    public int getCalorieProgressPercent() {
        return calorieProgressPercent;
    }

    public void setCalorieProgressPercent(int calorieProgressPercent) {
        this.calorieProgressPercent = calorieProgressPercent;
    }

    public BigDecimal getProteinConsumedG() {
        return proteinConsumedG;
    }

    public void setProteinConsumedG(BigDecimal proteinConsumedG) {
        this.proteinConsumedG = proteinConsumedG;
    }

    public BigDecimal getRemainingProteinG() {
        return remainingProteinG;
    }

    public void setRemainingProteinG(BigDecimal remainingProteinG) {
        this.remainingProteinG = remainingProteinG;
    }

    public int getProteinProgressPercent() {
        return proteinProgressPercent;
    }

    public void setProteinProgressPercent(int proteinProgressPercent) {
        this.proteinProgressPercent = proteinProgressPercent;
    }

    public BigDecimal getCarbsConsumedG() {
        return carbsConsumedG;
    }

    public void setCarbsConsumedG(BigDecimal carbsConsumedG) {
        this.carbsConsumedG = carbsConsumedG;
    }

    public BigDecimal getRemainingCarbsG() {
        return remainingCarbsG;
    }

    public void setRemainingCarbsG(BigDecimal remainingCarbsG) {
        this.remainingCarbsG = remainingCarbsG;
    }

    public int getCarbsProgressPercent() {
        return carbsProgressPercent;
    }

    public void setCarbsProgressPercent(int carbsProgressPercent) {
        this.carbsProgressPercent = carbsProgressPercent;
    }

    public BigDecimal getFatsConsumedG() {
        return fatsConsumedG;
    }

    public void setFatsConsumedG(BigDecimal fatsConsumedG) {
        this.fatsConsumedG = fatsConsumedG;
    }

    public BigDecimal getRemainingFatsG() {
        return remainingFatsG;
    }

    public void setRemainingFatsG(BigDecimal remainingFatsG) {
        this.remainingFatsG = remainingFatsG;
    }

    public int getFatsProgressPercent() {
        return fatsProgressPercent;
    }

    public void setFatsProgressPercent(int fatsProgressPercent) {
        this.fatsProgressPercent = fatsProgressPercent;
    }

    public int getMealsLoggedCount() {
        return mealsLoggedCount;
    }

    public void setMealsLoggedCount(int mealsLoggedCount) {
        this.mealsLoggedCount = mealsLoggedCount;
    }

    public Map<String, List<NutritionLogDto>> getMealsByType() {
        return mealsByType;
    }

    public void setMealsByType(Map<String, List<NutritionLogDto>> mealsByType) {
        this.mealsByType = mealsByType;
    }
}

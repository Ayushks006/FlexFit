package com.flexfit.backend.dto.response;

public class FoodRecommendationDto {
    private String name;
    private String category;
    private String mealSlot;
    private String servingSize;
    private int approxCalories;
    private int approxProteinG;
    private int approxCarbsG;
    private int approxFatsG;
    private String reason;

    public FoodRecommendationDto() {
    }

    public FoodRecommendationDto(String name, String category, String mealSlot, String servingSize,
                                 int approxCalories, int approxProteinG, int approxCarbsG, int approxFatsG,
                                 String reason) {
        this.name = name;
        this.category = category;
        this.mealSlot = mealSlot;
        this.servingSize = servingSize;
        this.approxCalories = approxCalories;
        this.approxProteinG = approxProteinG;
        this.approxCarbsG = approxCarbsG;
        this.approxFatsG = approxFatsG;
        this.reason = reason;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMealSlot() {
        return mealSlot;
    }

    public void setMealSlot(String mealSlot) {
        this.mealSlot = mealSlot;
    }

    public String getServingSize() {
        return servingSize;
    }

    public void setServingSize(String servingSize) {
        this.servingSize = servingSize;
    }

    public int getApproxCalories() {
        return approxCalories;
    }

    public void setApproxCalories(int approxCalories) {
        this.approxCalories = approxCalories;
    }

    public int getApproxProteinG() {
        return approxProteinG;
    }

    public void setApproxProteinG(int approxProteinG) {
        this.approxProteinG = approxProteinG;
    }

    public int getApproxCarbsG() {
        return approxCarbsG;
    }

    public void setApproxCarbsG(int approxCarbsG) {
        this.approxCarbsG = approxCarbsG;
    }

    public int getApproxFatsG() {
        return approxFatsG;
    }

    public void setApproxFatsG(int approxFatsG) {
        this.approxFatsG = approxFatsG;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

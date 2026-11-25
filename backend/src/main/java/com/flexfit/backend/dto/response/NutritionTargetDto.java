package com.flexfit.backend.dto.response;

public class NutritionTargetDto {
    private boolean calculable;
    private String missingProfileFieldsMessage;
    private Integer bmrCalories;
    private Integer estimatedTdeeCalories;
    private String activityAssumption;
    private Integer targetCalories;
    private Integer proteinTargetG;
    private Integer carbsTargetG;
    private Integer fatsTargetG;
    private String goalAdjustmentNote;
    private String disclaimer;

    public NutritionTargetDto() {
    }

    public boolean isCalculable() {
        return calculable;
    }

    public void setCalculable(boolean calculable) {
        this.calculable = calculable;
    }

    public String getMissingProfileFieldsMessage() {
        return missingProfileFieldsMessage;
    }

    public void setMissingProfileFieldsMessage(String missingProfileFieldsMessage) {
        this.missingProfileFieldsMessage = missingProfileFieldsMessage;
    }

    public Integer getBmrCalories() {
        return bmrCalories;
    }

    public void setBmrCalories(Integer bmrCalories) {
        this.bmrCalories = bmrCalories;
    }

    public Integer getEstimatedTdeeCalories() {
        return estimatedTdeeCalories;
    }

    public void setEstimatedTdeeCalories(Integer estimatedTdeeCalories) {
        this.estimatedTdeeCalories = estimatedTdeeCalories;
    }

    public String getActivityAssumption() {
        return activityAssumption;
    }

    public void setActivityAssumption(String activityAssumption) {
        this.activityAssumption = activityAssumption;
    }

    public Integer getTargetCalories() {
        return targetCalories;
    }

    public void setTargetCalories(Integer targetCalories) {
        this.targetCalories = targetCalories;
    }

    public Integer getProteinTargetG() {
        return proteinTargetG;
    }

    public void setProteinTargetG(Integer proteinTargetG) {
        this.proteinTargetG = proteinTargetG;
    }

    public Integer getCarbsTargetG() {
        return carbsTargetG;
    }

    public void setCarbsTargetG(Integer carbsTargetG) {
        this.carbsTargetG = carbsTargetG;
    }

    public Integer getFatsTargetG() {
        return fatsTargetG;
    }

    public void setFatsTargetG(Integer fatsTargetG) {
        this.fatsTargetG = fatsTargetG;
    }

    public String getGoalAdjustmentNote() {
        return goalAdjustmentNote;
    }

    public void setGoalAdjustmentNote(String goalAdjustmentNote) {
        this.goalAdjustmentNote = goalAdjustmentNote;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}

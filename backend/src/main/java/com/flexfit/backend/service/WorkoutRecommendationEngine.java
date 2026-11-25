package com.flexfit.backend.service;

import com.flexfit.backend.dto.response.RecommendedWorkoutDto;
import com.flexfit.backend.dto.response.WorkoutExerciseDto;
import com.flexfit.backend.entity.FitnessProfile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Deterministic, profile-aware rule-based workout recommendation engine.
 * Designed as an isolated component so an external ML service can augment or replace it later.
 */
@Component
public class WorkoutRecommendationEngine {

    public List<RecommendedWorkoutDto> recommend(FitnessProfile profile) {
        String goal = profile != null && profile.getFitnessGoal() != null
                ? profile.getFitnessGoal().trim()
                : "General fitness";
        String level = profile != null && profile.getExperienceLevel() != null
                ? profile.getExperienceLevel().trim()
                : "Beginner";
        String location = profile != null && profile.getWorkoutLocation() != null
                ? profile.getWorkoutLocation().trim()
                : "Home";
        int duration = profile != null && profile.getPreferredWorkoutDurationMin() != null
                ? Math.max(15, Math.min(120, profile.getPreferredWorkoutDurationMin()))
                : 45;
        double weightKg = profile != null && profile.getCurrentWeightKg() != null
                ? profile.getCurrentWeightKg().doubleValue()
                : 70.0;
        int age = profile != null && profile.getAge() != null ? profile.getAge() : 28;
        String injuriesRaw = profile != null && profile.getInjuriesLimitations() != null
                ? profile.getInjuriesLimitations().trim()
                : "";

        Set<String> equipment = parseEquipment(profile != null ? profile.getAvailableEquipment() : null, location);
        InjuryFlags injuryFlags = InjuryFlags.from(injuriesRaw);
        String safetyNote = buildSafetyNote(injuriesRaw, injuryFlags, age);

        String normalizedGoal = goal.toLowerCase(Locale.ROOT);
        List<RecommendedWorkoutDto> plans = new ArrayList<>();

        plans.add(buildPrimaryGoalPlan(normalizedGoal, level, location, duration, weightKg, equipment, injuryFlags, safetyNote));
        plans.add(buildSecondaryPlan(normalizedGoal, level, location, Math.max(20, duration - 10), weightKg, equipment, injuryFlags, safetyNote));
        plans.add(buildMobilityRecoveryPlan(level, Math.min(30, Math.max(15, duration / 2)), weightKg, injuryFlags, safetyNote));

        return plans;
    }

    public int estimateCalories(String workoutType, int durationMin, BigDecimal weightKg) {
        double weight = weightKg != null && weightKg.doubleValue() > 0 ? weightKg.doubleValue() : 70.0;
        double met = switch (workoutType == null ? "" : workoutType.trim().toLowerCase(Locale.ROOT)) {
            case "hiit", "cardio" -> 7.2;
            case "strength", "muscle gain" -> 5.5;
            case "full body" -> 6.0;
            case "mobility", "flexibility" -> 2.8;
            default -> 5.0;
        };
        double raw = met * weight * (durationMin / 60.0);
        return Math.max(10, (int) (Math.round(raw / 5.0) * 5));
    }

    private RecommendedWorkoutDto buildPrimaryGoalPlan(
            String goal, String level, String location, int duration, double weightKg,
            Set<String> equipment, InjuryFlags injuries, String safetyNote) {

        String setsReps = resolveSetsReps(level, goal);
        String rest = resolveRest(level, goal);

        if (goal.contains("weight loss") || goal.contains("fat")) {
            List<WorkoutExerciseDto> exercises = new ArrayList<>();
            exercises.add(selectLowerBodyExercise(equipment, injuries, setsReps, rest, "Controlled tempo"));
            exercises.add(selectPushExercise(equipment, injuries, setsReps, rest));
            exercises.add(selectPullExercise(equipment, injuries, setsReps, rest));
            exercises.add(selectConditioningFinisher(equipment, injuries, level));

            return new RecommendedWorkoutDto(
                    "Metabolic Conditioning Circuit (" + location + ")",
                    "HIIT & Conditioning",
                    "Low-waste full-body circuit calibrated for caloric expenditure while respecting your equipment and joint profile.",
                    duration,
                    level,
                    estimateCalories("HIIT", duration, BigDecimal.valueOf(weightKg)),
                    exercises,
                    safetyNote
            );
        }

        if (goal.contains("muscle") || goal.contains("hypertrophy")) {
            List<WorkoutExerciseDto> exercises = new ArrayList<>();
            exercises.add(selectPushExercise(equipment, injuries, setsReps, rest));
            exercises.add(selectPullExercise(equipment, injuries, setsReps, rest));
            exercises.add(selectLowerBodyExercise(equipment, injuries, setsReps, rest, "2-second eccentric"));
            exercises.add(selectCoreExercise(injuries, "3 x 30-40s", "45s"));

            return new RecommendedWorkoutDto(
                    "Hypertrophy Foundation (" + location + ")",
                    "Strength",
                    "Balanced compound and accessory volume focused on progressive muscular development.",
                    duration,
                    level,
                    estimateCalories("Strength", duration, BigDecimal.valueOf(weightKg)),
                    exercises,
                    safetyNote
            );
        }

        if (goal.contains("strength")) {
            String strengthReps = level.equalsIgnoreCase("Beginner") ? "3 x 8 reps" : "4 x 5-6 reps";
            String strengthRest = "90-120s";
            List<WorkoutExerciseDto> exercises = new ArrayList<>();
            exercises.add(selectLowerBodyExercise(equipment, injuries, strengthReps, strengthRest, "Brace core before each rep"));
            exercises.add(selectPushExercise(equipment, injuries, strengthReps, strengthRest));
            exercises.add(selectPullExercise(equipment, injuries, strengthReps, strengthRest));

            return new RecommendedWorkoutDto(
                    "Progressive Strength Builder",
                    "Strength",
                    "Lower-rep, higher-focus compound session designed to build foundational force output safely.",
                    duration,
                    level,
                    estimateCalories("Strength", duration, BigDecimal.valueOf(weightKg)),
                    exercises,
                    safetyNote
            );
        }

        if (goal.contains("endurance")) {
            List<WorkoutExerciseDto> exercises = new ArrayList<>();
            exercises.add(selectCardioBlock(equipment, injuries, "12-15 min steady pace", "60s"));
            exercises.add(selectLowerBodyExercise(equipment, injuries, "3 x 15 reps", "45s", "Rhythmic breathing"));
            exercises.add(selectCoreExercise(injuries, "3 x 40s", "30s"));

            return new RecommendedWorkoutDto(
                    "Aerobic Capacity & Stamina",
                    "Cardio",
                    "Sustained heart-rate zone session to elevate cardiovascular endurance without excessive joint impact.",
                    duration,
                    level,
                    estimateCalories("Cardio", duration, BigDecimal.valueOf(weightKg)),
                    exercises,
                    safetyNote
            );
        }

        // General fitness / Weight maintenance
        List<WorkoutExerciseDto> exercises = new ArrayList<>();
        exercises.add(selectLowerBodyExercise(equipment, injuries, setsReps, rest, "Full range of motion"));
        exercises.add(selectPushExercise(equipment, injuries, setsReps, rest));
        exercises.add(selectPullExercise(equipment, injuries, setsReps, rest));
        exercises.add(selectCoreExercise(injuries, "3 x 30s", "45s"));

        return new RecommendedWorkoutDto(
                "Balanced Full-Body Wellness",
                "Full Body",
                "Well-rounded strength, posture, and light conditioning session for steady week-to-week health.",
                duration,
                level,
                estimateCalories("Full Body", duration, BigDecimal.valueOf(weightKg)),
                exercises,
                safetyNote
        );
    }

    private RecommendedWorkoutDto buildSecondaryPlan(
            String goal, String level, String location, int duration, double weightKg,
            Set<String> equipment, InjuryFlags injuries, String safetyNote) {

        List<WorkoutExerciseDto> exercises = new ArrayList<>();
        exercises.add(selectCardioBlock(equipment, injuries, "10-15 min moderate effort", "60s"));
        exercises.add(selectCoreExercise(injuries, "3 x 30-45s", "45s"));
        exercises.add(new WorkoutExerciseDto("Farmer or Suitcase Carry / March", "3 x 30s", "45s",
                injuries.back ? "Use light load or bodyweight march to keep spine neutral" : "Keep ribs stacked over pelvis"));

        String category = goal.contains("endurance") ? "Full Body" : "Cardio";
        return new RecommendedWorkoutDto(
                "Zone-2 Cardio & Core Stability (" + location + ")",
                category,
                "Complementary aerobic and trunk-stability session to support recovery and cardiovascular health.",
                duration,
                level,
                estimateCalories(category, duration, BigDecimal.valueOf(weightKg)),
                exercises,
                safetyNote
        );
    }

    private RecommendedWorkoutDto buildMobilityRecoveryPlan(
            String level, int duration, double weightKg, InjuryFlags injuries, String safetyNote) {

        List<WorkoutExerciseDto> exercises = new ArrayList<>();
        exercises.add(new WorkoutExerciseDto("Cat-Cow Thoracic Flow", "2 x 8 slow reps", "30s", "Move smoothly with breath"));
        exercises.add(new WorkoutExerciseDto(
                injuries.knee ? "Supine Hamstring & Hip Opener" : "90/90 Hip Mobility Transitions",
                "2 x 8 per side", "30s", "Avoid forcing end-range stretch"));
        exercises.add(new WorkoutExerciseDto(
                injuries.shoulder ? "Scapular Wall Slides (Pain-Free Range)" : "Thoracic Open-Book Rotation",
                "2 x 8 per side", "30s", "Stop immediately if sharp discomfort occurs"));

        return new RecommendedWorkoutDto(
                "Guided Mobility & Active Recovery",
                "Mobility",
                "Gentle joint-health and posture reset designed for rest days or conservative movement.",
                duration,
                level.equalsIgnoreCase("Advanced") ? "All Levels" : level,
                estimateCalories("Mobility", duration, BigDecimal.valueOf(weightKg)),
                exercises,
                safetyNote
        );
    }

    private WorkoutExerciseDto selectLowerBodyExercise(Set<String> equipment, InjuryFlags injuries,
                                                       String setsReps, String rest, String note) {
        if (injuries.knee) {
            return new WorkoutExerciseDto("Glute Bridge (Floor)", setsReps, rest,
                    "Knee-friendly posterior chain movement; drive through heels.");
        }
        if (injuries.back) {
            return new WorkoutExerciseDto("Supported Split Squat or Wall Sit", setsReps, rest,
                    "Keeps torso upright with minimal lumbar shear.");
        }
        if (equipment.contains("barbell")) {
            return new WorkoutExerciseDto("Barbell Back Squat", setsReps, rest, note);
        }
        if (equipment.contains("dumbbells")) {
            return new WorkoutExerciseDto("Dumbbell Goblet Squat", setsReps, rest, note);
        }
        return new WorkoutExerciseDto("Bodyweight Squat", setsReps, rest, note);
    }

    private WorkoutExerciseDto selectPushExercise(Set<String> equipment, InjuryFlags injuries,
                                                  String setsReps, String rest) {
        if (injuries.shoulder || injuries.wrist) {
            return new WorkoutExerciseDto("Incline Wall Push-Up or Light Band Chest Press", setsReps, rest,
                    "Conservative range of motion to protect shoulder and wrist joints.");
        }
        if (equipment.contains("dumbbells") && equipment.contains("bench")) {
            return new WorkoutExerciseDto("Dumbbell Bench Press", setsReps, rest, "Keep shoulder blades tucked.");
        }
        if (equipment.contains("dumbbells")) {
            return new WorkoutExerciseDto("Dumbbell Floor Press", setsReps, rest, "Controlled descent to floor.");
        }
        return new WorkoutExerciseDto("Incline or Standard Push-Up", setsReps, rest, "Maintain a rigid plank line.");
    }

    private WorkoutExerciseDto selectPullExercise(Set<String> equipment, InjuryFlags injuries,
                                                  String setsReps, String rest) {
        if (injuries.back) {
            return new WorkoutExerciseDto("Seated Resistance Band Row", setsReps, rest,
                    "Supported upright posture to avoid lower-back strain.");
        }
        if (equipment.contains("pull-up bar") && !injuries.shoulder) {
            return new WorkoutExerciseDto("Assisted or Full Pull-Ups", setsReps, rest, "Full control at the bottom.");
        }
        if (equipment.contains("dumbbells")) {
            return new WorkoutExerciseDto("Single-Arm Dumbbell Row", setsReps, rest, "Support torso on bench or thigh.");
        }
        if (equipment.contains("resistance bands")) {
            return new WorkoutExerciseDto("Resistance Band Row", setsReps, rest, "Squeeze shoulder blades together.");
        }
        return new WorkoutExerciseDto("Prone Y-T-W Back Extensions", setsReps, rest, "Bodyweight upper-back activation.");
    }

    private WorkoutExerciseDto selectCoreExercise(InjuryFlags injuries, String setsReps, String rest) {
        if (injuries.wrist || injuries.shoulder) {
            return new WorkoutExerciseDto("Dead Bug Core Control", setsReps, rest,
                    "Zero wrist/shoulder load; press lower back gently into floor.");
        }
        if (injuries.back) {
            return new WorkoutExerciseDto("Bird-Dog Stability Hold", setsReps, rest,
                    "Neutral spine anti-rotation drill.");
        }
        return new WorkoutExerciseDto("Forearm Plank", setsReps, rest, "Brace glutes and abdominals evenly.");
    }

    private WorkoutExerciseDto selectConditioningFinisher(Set<String> equipment, InjuryFlags injuries, String level) {
        if (injuries.knee || injuries.ankle || injuries.unclearMedical) {
            return new WorkoutExerciseDto("Low-Impact Shadow Boxing or Brisk Marching", "3 x 45s", "30s",
                    "Joint-friendly conditioning without jumping impact.");
        }
        if (equipment.contains("cardio machine")) {
            return new WorkoutExerciseDto("Cardio Machine Intervals", "4 x 60s brisk / 60s easy", "60s",
                    "Adjust resistance to match " + level + " effort.");
        }
        return new WorkoutExerciseDto("Step-Back Jacks or Tempo Mountain Climbers", "3 x 40s", "30s",
                "Keep breathing steady.");
    }

    private WorkoutExerciseDto selectCardioBlock(Set<String> equipment, InjuryFlags injuries, String setsReps, String rest) {
        if (equipment.contains("cardio machine")) {
            return new WorkoutExerciseDto("Stationary Bike or Elliptical", setsReps, rest,
                    injuries.knee ? "Use low resistance and smooth cadence" : "Maintain conversational Zone-2 pace");
        }
        return new WorkoutExerciseDto("Brisk Incline Walk or Low-Impact Step-Ups", setsReps, rest,
                "Maintain steady aerobic effort.");
    }

    private String resolveSetsReps(String level, String goal) {
        if (goal.contains("weight loss")) {
            return switch (level.toLowerCase(Locale.ROOT)) {
                case "advanced" -> "4 x 15 reps";
                case "intermediate" -> "3 x 12-15 reps";
                default -> "3 x 10-12 reps";
            };
        }
        return switch (level.toLowerCase(Locale.ROOT)) {
            case "advanced" -> "4 x 8-12 reps";
            case "intermediate" -> "3 x 10-12 reps";
            default -> "3 x 8-10 reps";
        };
    }

    private String resolveRest(String level, String goal) {
        if (goal.contains("weight loss")) {
            return level.equalsIgnoreCase("Beginner") ? "60s" : "45s";
        }
        return level.equalsIgnoreCase("Beginner") ? "75-90s" : "60s";
    }

    private Set<String> parseEquipment(String raw, String location) {
        Set<String> set = (raw == null || raw.isBlank())
                ? Set.of()
                : Arrays.stream(raw.split(","))
                        .map(s -> s.trim().toLowerCase(Locale.ROOT))
                        .filter(s -> !s.isEmpty() && !s.equals("none"))
                        .collect(Collectors.toSet());
        if (set.isEmpty() && "Gym".equalsIgnoreCase(location)) {
            return Set.of("dumbbells", "barbell", "bench", "cardio machine");
        }
        return set;
    }

    private String buildSafetyNote(String injuriesRaw, InjuryFlags flags, int age) {
        if (!injuriesRaw.isBlank()) {
            if (flags.unclearMedical) {
                return "Recorded health note (\"" + injuriesRaw + "\"): Conservative, low-impact defaults applied. "
                        + "Please consult a qualified healthcare or fitness professional before high-intensity training.";
            }
            return "Exercises were adjusted for your recorded limitation (\"" + injuriesRaw + "\"). "
                    + "Stop if you feel pain and consult a qualified professional when needed.";
        }
        if (age >= 60) {
            return "Pacing and rest intervals are kept steady for joint safety. Warm up for 5 minutes before starting.";
        }
        return "Rule-based plan tailored to your profile. Warm up 5 minutes prior and scale loads conservatively.";
    }

    private record InjuryFlags(boolean knee, boolean back, boolean shoulder, boolean wrist, boolean ankle, boolean unclearMedical) {
        static InjuryFlags from(String raw) {
            if (raw == null || raw.isBlank()) {
                return new InjuryFlags(false, false, false, false, false, false);
            }
            String lower = raw.toLowerCase(Locale.ROOT);
            boolean knee = lower.contains("knee");
            boolean back = lower.contains("back") || lower.contains("spine") || lower.contains("lumbar") || lower.contains("disc");
            boolean shoulder = lower.contains("shoulder") || lower.contains("rotator");
            boolean wrist = lower.contains("wrist") || lower.contains("carpal");
            boolean ankle = lower.contains("ankle") || lower.contains("achilles") || lower.contains("foot");
            boolean known = knee || back || shoulder || wrist || ankle;
            boolean unclear = !known || lower.contains("heart") || lower.contains("cardiac")
                    || lower.contains("pregnan") || lower.contains("surgery") || lower.contains("pain");
            return new InjuryFlags(knee, back, shoulder, wrist, ankle, unclear);
        }
    }
}

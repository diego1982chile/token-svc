package cl.dsoto.onboarding.model;

public record OnboardingTrainStepView(
        OnboardingTrainStep key,
        String label,
        OnboardingTrainStepStatus status
) {
}

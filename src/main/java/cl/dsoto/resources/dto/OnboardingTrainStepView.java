package cl.dsoto.resources.dto;

public record OnboardingTrainStepView(
        OnboardingTrainStep key,
        String label,
        OnboardingTrainStepStatus status
) {
}

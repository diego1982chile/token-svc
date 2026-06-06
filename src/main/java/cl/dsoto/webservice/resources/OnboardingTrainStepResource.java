package cl.dsoto.webservice.resources;

public record OnboardingTrainStepResource(
        OnboardingTrainStep key,
        String label,
        OnboardingTrainStepStatus status
) {
}

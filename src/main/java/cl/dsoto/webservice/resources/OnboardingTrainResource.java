package cl.dsoto.webservice.resources;

import cl.dsoto.model.OnboardingState;

import java.util.List;

public record OnboardingTrainResource(
        String username,
        OnboardingState currentState,
        OnboardingTrainStep currentStep,
        List<OnboardingTrainStepResource> steps
) {
}

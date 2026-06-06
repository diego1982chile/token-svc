package cl.dsoto.resources.dto;

import cl.dsoto.model.OnboardingState;

import java.util.List;

public record OnboardingTrainView(
        String username,
        OnboardingState currentState,
        OnboardingTrainStep currentStep,
        List<OnboardingTrainStepView> steps
) {
}

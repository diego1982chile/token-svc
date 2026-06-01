package cl.dsoto.onboarding.model;

import java.util.List;

public record OnboardingTrainView(
        String username,
        OnboardingState currentState,
        OnboardingTrainStep currentStep,
        List<OnboardingTrainStepView> steps
) {
}

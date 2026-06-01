package cl.dsoto.onboarding;

import cl.dsoto.onboarding.model.OnboardingTrainView;

import java.util.Optional;

public interface OnboardingTrainViewService {

    Optional<OnboardingTrainView> getTrainView(String username);
}

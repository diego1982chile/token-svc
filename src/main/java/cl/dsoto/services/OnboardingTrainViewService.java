package cl.dsoto.services;

import cl.dsoto.resources.dto.OnboardingTrainView;

import java.util.Optional;

public interface OnboardingTrainViewService {

    OnboardingTrainView getPublicTrainView(String stage);

    Optional<OnboardingTrainView> getTrainView(String username);

    Optional<OnboardingTrainView> getTrainViewByRegistrationId(String registrationId);
}

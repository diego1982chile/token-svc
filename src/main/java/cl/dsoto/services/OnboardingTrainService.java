package cl.dsoto.services;

import cl.dsoto.webservice.resources.OnboardingTrainResource;

import java.util.Optional;

public interface OnboardingTrainService {

    OnboardingTrainResource getPublicTrain(String stage);

    Optional<OnboardingTrainResource> getTrain(String username);

    Optional<OnboardingTrainResource> getTrainByRegistrationId(String registrationId);
}

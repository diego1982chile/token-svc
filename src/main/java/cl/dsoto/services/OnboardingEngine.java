package cl.dsoto.services;

import cl.dsoto.events.OnboardingEvent;
import cl.dsoto.model.OnboardingState;

public interface OnboardingEngine {

    void applyEvent(OnboardingEvent event);

    OnboardingState getCurrentState(String username);
}

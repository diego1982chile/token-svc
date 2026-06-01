package cl.dsoto.onboarding;

import cl.dsoto.onboarding.model.OnboardingEvent;
import cl.dsoto.onboarding.model.OnboardingState;
import cl.dsoto.onboarding.model.OnboardingTrainStep;
import cl.dsoto.onboarding.model.OnboardingTrainStepStatus;
import cl.dsoto.onboarding.model.OnboardingTrainView;
import cl.dsoto.onboarding.repositories.OnboardingProcessRepository;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
class OnboardingTrainViewServiceTest {

    @Inject
    OnboardingEngine onboardingEngine;

    @Inject
    OnboardingTrainViewService trainViewService;

    @Inject
    OnboardingProcessRepository repository;

    @BeforeEach
    void cleanUp() {
        repository.deleteAll();
    }

    @Test
    void shouldShowRegistrationAsCurrentWhenUserIsRegistered() {
        String username = "registered.user@example.com";

        onboardingEngine.applyEvent(OnboardingEvent.userRegistered(username));

        OnboardingTrainView view = trainViewService.getTrainView(username).orElseThrow();

        assertThat(view.currentState(), is(OnboardingState.REGISTERED));
        assertThat(view.currentStep(), is(OnboardingTrainStep.REGISTRATION));
        assertThat(view.steps().get(0).status(), is(OnboardingTrainStepStatus.CURRENT));
        assertThat(view.steps().get(1).status(), is(OnboardingTrainStepStatus.PENDING));
        assertThat(view.steps().get(2).status(), is(OnboardingTrainStepStatus.PENDING));
    }

    @Test
    void shouldShowIdentityCheckAsCurrentWhenEmailIsVerified() {
        String username = "email.verified.user@example.com";

        onboardingEngine.applyEvent(OnboardingEvent.userRegistered(username));
        onboardingEngine.applyEvent(OnboardingEvent.emailVerified(username));

        OnboardingTrainView view = trainViewService.getTrainView(username).orElseThrow();

        assertThat(view.currentState(), is(OnboardingState.EMAIL_VERIFIED));
        assertThat(view.currentStep(), is(OnboardingTrainStep.IDENTITY_CHECK));
        assertThat(view.steps().get(0).status(), is(OnboardingTrainStepStatus.COMPLETED));
        assertThat(view.steps().get(1).status(), is(OnboardingTrainStepStatus.CURRENT));
        assertThat(view.steps().get(2).status(), is(OnboardingTrainStepStatus.PENDING));
    }

    @Test
    void shouldReturnEmptyWhenUserHasNoOnboardingProcess() {
        Optional<OnboardingTrainView> view = trainViewService.getTrainView("missing.user@example.com");

        assertThat(view.isEmpty(), is(true));
    }
}

package cl.dsoto.services;

import cl.dsoto.events.OnboardingEvent;
import cl.dsoto.model.OnboardingState;
import cl.dsoto.webservice.resources.OnboardingTrainStep;
import cl.dsoto.webservice.resources.OnboardingTrainStepStatus;
import cl.dsoto.webservice.resources.OnboardingTrainResource;
import cl.dsoto.repositories.OnboardingProcessRepository;
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
class OnboardingTrainServiceTest {

    @Inject
    OnboardingEngine onboardingEngine;

    @Inject
    OnboardingTrainService trainService;

    @Inject
    OnboardingProcessRepository repository;

    @BeforeEach
    void cleanUp() {
        repository.deleteAll();
    }

    @Test
    void shouldShowPublicRegistrationTrain() {
        OnboardingTrainResource view = trainService.getPublicTrain(null);

        assertThat(view.username(), is((String) null));
        assertThat(view.currentState(), is((OnboardingState) null));
        assertThat(view.currentStep(), is(OnboardingTrainStep.REGISTRATION));
        assertThat(view.steps().get(0).status(), is(OnboardingTrainStepStatus.CURRENT));
        assertThat(view.steps().get(1).status(), is(OnboardingTrainStepStatus.PENDING));
        assertThat(view.steps().get(2).status(), is(OnboardingTrainStepStatus.PENDING));
    }

    @Test
    void shouldShowIdentityCheckForPublicEmailConfirmedTrain() {
        OnboardingTrainResource view = trainService.getPublicTrain("email-confirmed");

        assertThat(view.username(), is((String) null));
        assertThat(view.currentState(), is((OnboardingState) null));
        assertThat(view.currentStep(), is(OnboardingTrainStep.IDENTITY_CHECK));
        assertThat(view.steps().get(0).status(), is(OnboardingTrainStepStatus.COMPLETED));
        assertThat(view.steps().get(1).status(), is(OnboardingTrainStepStatus.CURRENT));
        assertThat(view.steps().get(2).status(), is(OnboardingTrainStepStatus.PENDING));
    }

    @Test
    void shouldShowRegistrationAsCurrentWhenUserIsRegistered() {
        String username = "registered.user@example.com";

        onboardingEngine.applyEvent(OnboardingEvent.userRegistered(username));

        OnboardingTrainResource view = trainService.getTrain(username).orElseThrow();

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

        OnboardingTrainResource view = trainService.getTrain(username).orElseThrow();

        assertThat(view.currentState(), is(OnboardingState.EMAIL_VERIFIED));
        assertThat(view.currentStep(), is(OnboardingTrainStep.IDENTITY_CHECK));
        assertThat(view.steps().get(0).status(), is(OnboardingTrainStepStatus.COMPLETED));
        assertThat(view.steps().get(1).status(), is(OnboardingTrainStepStatus.CURRENT));
        assertThat(view.steps().get(2).status(), is(OnboardingTrainStepStatus.PENDING));
    }

    @Test
    void shouldReturnEmptyWhenUserHasNoOnboardingProcess() {
        Optional<OnboardingTrainResource> view = trainService.getTrain("missing.user@example.com");

        assertThat(view.isEmpty(), is(true));
    }
}

package cl.dsoto.services.impl;

import cl.dsoto.services.OnboardingEngine;
import cl.dsoto.entities.OnboardingProcessEntity;
import cl.dsoto.repositories.OnboardingProcessRepository;
import cl.dsoto.services.OnboardingTrainService;
import cl.dsoto.model.OnboardingState;
import cl.dsoto.webservice.resources.OnboardingTrainStep;
import cl.dsoto.webservice.resources.OnboardingTrainStepStatus;
import cl.dsoto.webservice.resources.OnboardingTrainStepResource;
import cl.dsoto.webservice.resources.OnboardingTrainResource;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

@RequestScoped
public class DefaultOnboardingTrainService implements OnboardingTrainService {

    @Inject
    OnboardingEngine onboardingEngine;

    @Inject
    OnboardingProcessRepository onboardingProcessRepository;

    @Override
    public OnboardingTrainResource getPublicTrain(String stage) {
        return trainView(null, null, publicStepFor(stage));
    }

    @Override
    public Optional<OnboardingTrainResource> getTrain(String username) {
        OnboardingState currentState = onboardingEngine.getCurrentState(username);
        if (currentState == null) {
            return Optional.empty();
        }

        OnboardingTrainStep currentStep = currentStepFor(currentState);
        return Optional.of(trainView(username, currentState, currentStep));
    }

    @Override
    public Optional<OnboardingTrainResource> getTrainByRegistrationId(String registrationId) {
        if (registrationId == null || registrationId.isBlank()) {
            return Optional.empty();
        }

        return onboardingProcessRepository.findByRegistrationId(registrationId)
                .map(this::trainView);
    }

    private OnboardingTrainResource trainView(OnboardingProcessEntity process) {
        OnboardingState currentState = process.getCurrentState();
        return trainView(null, currentState, currentStepFor(currentState));
    }

    private OnboardingTrainResource trainView(
            String username,
            OnboardingState currentState,
            OnboardingTrainStep currentStep
    ) {
        return new OnboardingTrainResource(
                username,
                currentState,
                currentStep,
                List.of(
                        step(OnboardingTrainStep.REGISTRATION, "Registro", statusFor(OnboardingTrainStep.REGISTRATION, currentStep, currentState)),
                        step(OnboardingTrainStep.IDENTITY_CHECK, "Comprueba tu identidad", statusFor(OnboardingTrainStep.IDENTITY_CHECK, currentStep, currentState)),
                        step(OnboardingTrainStep.PLAN_SELECTION, "Elige tu plan", statusFor(OnboardingTrainStep.PLAN_SELECTION, currentStep, currentState))
                )
        );
    }

    private OnboardingTrainStep currentStepFor(OnboardingState currentState) {
        return switch (currentState) {
            case REGISTERED -> OnboardingTrainStep.REGISTRATION;
            case EMAIL_VERIFIED -> OnboardingTrainStep.IDENTITY_CHECK;
            case KYC_APPROVED, PLAN_SELECTED, PROFILE_COMPLETED, READY_TO_PUBLISH -> OnboardingTrainStep.PLAN_SELECTION;
        };
    }

    private OnboardingTrainStep publicStepFor(String stage) {
        if ("email-confirmed".equalsIgnoreCase(stage) || "EMAIL_CONFIRMED".equalsIgnoreCase(stage)) {
            return OnboardingTrainStep.IDENTITY_CHECK;
        }

        return OnboardingTrainStep.REGISTRATION;
    }

    private OnboardingTrainStepStatus statusFor(
            OnboardingTrainStep step,
            OnboardingTrainStep currentStep,
            OnboardingState currentState
    ) {
        if (currentState == OnboardingState.READY_TO_PUBLISH) {
            return OnboardingTrainStepStatus.COMPLETED;
        }

        if (step == currentStep) {
            return OnboardingTrainStepStatus.CURRENT;
        }

        return step.ordinal() < currentStep.ordinal()
                ? OnboardingTrainStepStatus.COMPLETED
                : OnboardingTrainStepStatus.PENDING;
    }

    private OnboardingTrainStepResource step(
            OnboardingTrainStep step,
            String label,
            OnboardingTrainStepStatus status
    ) {
        return new OnboardingTrainStepResource(step, label, status);
    }
}

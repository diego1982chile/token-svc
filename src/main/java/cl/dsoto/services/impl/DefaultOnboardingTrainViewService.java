package cl.dsoto.services.impl;

import cl.dsoto.services.OnboardingEngine;
import cl.dsoto.entities.OnboardingProcess;
import cl.dsoto.repositories.OnboardingProcessRepository;
import cl.dsoto.services.OnboardingTrainViewService;
import cl.dsoto.model.OnboardingState;
import cl.dsoto.resources.dto.OnboardingTrainStep;
import cl.dsoto.resources.dto.OnboardingTrainStepStatus;
import cl.dsoto.resources.dto.OnboardingTrainStepView;
import cl.dsoto.resources.dto.OnboardingTrainView;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

@RequestScoped
public class DefaultOnboardingTrainViewService implements OnboardingTrainViewService {

    @Inject
    OnboardingEngine onboardingEngine;

    @Inject
    OnboardingProcessRepository onboardingProcessRepository;

    @Override
    public OnboardingTrainView getPublicTrainView(String stage) {
        return trainView(null, null, publicStepFor(stage));
    }

    @Override
    public Optional<OnboardingTrainView> getTrainView(String username) {
        OnboardingState currentState = onboardingEngine.getCurrentState(username);
        if (currentState == null) {
            return Optional.empty();
        }

        OnboardingTrainStep currentStep = currentStepFor(currentState);
        return Optional.of(trainView(username, currentState, currentStep));
    }

    @Override
    public Optional<OnboardingTrainView> getTrainViewByRegistrationId(String registrationId) {
        if (registrationId == null || registrationId.isBlank()) {
            return Optional.empty();
        }

        return onboardingProcessRepository.findByRegistrationId(registrationId)
                .map(this::trainView);
    }

    private OnboardingTrainView trainView(OnboardingProcess process) {
        OnboardingState currentState = process.getCurrentState();
        return trainView(null, currentState, currentStepFor(currentState));
    }

    private OnboardingTrainView trainView(
            String username,
            OnboardingState currentState,
            OnboardingTrainStep currentStep
    ) {
        return new OnboardingTrainView(
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

    private OnboardingTrainStepView step(
            OnboardingTrainStep step,
            String label,
            OnboardingTrainStepStatus status
    ) {
        return new OnboardingTrainStepView(step, label, status);
    }
}

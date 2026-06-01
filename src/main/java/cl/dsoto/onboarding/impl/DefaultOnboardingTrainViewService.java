package cl.dsoto.onboarding.impl;

import cl.dsoto.onboarding.OnboardingEngine;
import cl.dsoto.onboarding.OnboardingTrainViewService;
import cl.dsoto.onboarding.model.OnboardingState;
import cl.dsoto.onboarding.model.OnboardingTrainStep;
import cl.dsoto.onboarding.model.OnboardingTrainStepStatus;
import cl.dsoto.onboarding.model.OnboardingTrainStepView;
import cl.dsoto.onboarding.model.OnboardingTrainView;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

@RequestScoped
public class DefaultOnboardingTrainViewService implements OnboardingTrainViewService {

    @Inject
    OnboardingEngine onboardingEngine;

    @Override
    public Optional<OnboardingTrainView> getTrainView(String username) {
        OnboardingState currentState = onboardingEngine.getCurrentState(username);
        if (currentState == null) {
            return Optional.empty();
        }

        OnboardingTrainStep currentStep = currentStepFor(currentState);
        return Optional.of(new OnboardingTrainView(
                username,
                currentState,
                currentStep,
                List.of(
                        step(OnboardingTrainStep.REGISTRATION, "Registro", statusFor(OnboardingTrainStep.REGISTRATION, currentStep, currentState)),
                        step(OnboardingTrainStep.IDENTITY_CHECK, "Comprueba tu identidad", statusFor(OnboardingTrainStep.IDENTITY_CHECK, currentStep, currentState)),
                        step(OnboardingTrainStep.PLAN_SELECTION, "Elige tu plan", statusFor(OnboardingTrainStep.PLAN_SELECTION, currentStep, currentState))
                )
        ));
    }

    private OnboardingTrainStep currentStepFor(OnboardingState currentState) {
        return switch (currentState) {
            case REGISTERED -> OnboardingTrainStep.REGISTRATION;
            case EMAIL_VERIFIED -> OnboardingTrainStep.IDENTITY_CHECK;
            case KYC_APPROVED, PLAN_SELECTED, PROFILE_COMPLETED, READY_TO_PUBLISH -> OnboardingTrainStep.PLAN_SELECTION;
        };
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

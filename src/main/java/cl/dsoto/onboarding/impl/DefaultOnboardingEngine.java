package cl.dsoto.onboarding.impl;

import cl.dsoto.onboarding.OnboardingEngine;
import cl.dsoto.onboarding.entities.OnboardingProcess;
import cl.dsoto.onboarding.model.OnboardingEvent;
import cl.dsoto.onboarding.model.OnboardingState;
import cl.dsoto.onboarding.repositories.OnboardingProcessRepository;
import cl.dsoto.onboarding.rules.EmailVerifiedRule;
import cl.dsoto.onboarding.rules.IdempotentOnboardingRule;
import cl.dsoto.onboarding.rules.UserRegisteredRule;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;

import java.time.Instant;

@RequestScoped
public class DefaultOnboardingEngine implements OnboardingEngine {

    private static final String INITIAL_STATE = "NONE";

    @Inject
    OnboardingProcessRepository repository;

    @Override
    @Transactional
    public void applyEvent(OnboardingEvent event) {
        OnboardingProcess process = repository.findById(event.username()).orElse(null);
        OnboardingState currentState = process == null ? null : process.getCurrentState();

        Facts facts = new Facts();
        facts.put("processExists", process != null);
        facts.put("currentState", currentState == null ? INITIAL_STATE : currentState.name());
        if (currentState != null) {
            facts.put("nextState", currentState);
        }
        facts.put("eventType", event.type());
        facts.put("applied", false);

        rulesEngine().fire(rules(), facts);

        if (!Boolean.TRUE.equals(facts.get("applied"))) {
            throw new IllegalStateException("Invalid onboarding event " + event.type()
                    + " for current state " + currentState);
        }

        OnboardingState nextState = facts.get("nextState");
        if (process == null) {
            process = OnboardingProcess.builder()
                    .username(event.username())
                    .currentState(nextState)
                    .createdAt(event.occurredAt())
                    .updatedAt(event.occurredAt())
                    .build();
        } else {
            process.setCurrentState(nextState);
            process.setUpdatedAt(Instant.now());
        }

        repository.save(process);
    }

    @Override
    public OnboardingState getCurrentState(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }

        return repository.findById(username)
                .map(OnboardingProcess::getCurrentState)
                .orElse(null);
    }

    private Rules rules() {
        Rules rules = new Rules();
        rules.register(new IdempotentOnboardingRule());
        rules.register(new UserRegisteredRule());
        rules.register(new EmailVerifiedRule());
        return rules;
    }

    private RulesEngine rulesEngine() {
        return new DefaultRulesEngine();
    }
}

package cl.dsoto.onboarding.repositories;

import cl.dsoto.onboarding.entities.OnboardingProcess;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnboardingProcessRepository extends JpaRepository<OnboardingProcess, String> {
}

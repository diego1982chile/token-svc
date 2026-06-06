package cl.dsoto.repositories;

import cl.dsoto.entities.OnboardingProcess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OnboardingProcessRepository extends JpaRepository<OnboardingProcess, String> {

    Optional<OnboardingProcess> findByRegistrationId(String registrationId);
}

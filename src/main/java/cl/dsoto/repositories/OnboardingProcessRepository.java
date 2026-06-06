package cl.dsoto.repositories;

import cl.dsoto.entities.OnboardingProcessEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OnboardingProcessRepository extends JpaRepository<OnboardingProcessEntity, String> {

    Optional<OnboardingProcessEntity> findByRegistrationId(String registrationId);
}

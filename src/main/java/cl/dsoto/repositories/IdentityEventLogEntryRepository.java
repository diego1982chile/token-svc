package cl.dsoto.repositories;

import cl.dsoto.entities.IdentityEventLogEntryEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IdentityEventLogEntryRepository extends JpaRepository<IdentityEventLogEntryEntity, Long> {

    List<IdentityEventLogEntryEntity> findByIdGreaterThanOrderByIdAsc(Long id, Pageable pageable);
}

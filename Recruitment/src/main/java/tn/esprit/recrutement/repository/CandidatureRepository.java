package tn.esprit.recrutement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.recrutement.entity.Candidature;

@Repository
public interface CandidatureRepository extends JpaRepository<Candidature,Long> {

}

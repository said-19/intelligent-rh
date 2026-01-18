package tn.esprit.employees.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.employees.entity.Candidat;

import java.util.Optional;

@Repository
public interface CandidatRepository extends JpaRepository<Candidat, Long> {
    Optional<Candidat> findByEmail(String email);


    Optional<Candidat> findByPrenomIgnoreCaseAndNomIgnoreCase(String prenom, String nom);
}
package tn.esprit.employees.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.employees.entity.Candidat;
import tn.esprit.employees.repository.CandidatRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CandidatService {

    private final CandidatRepository repository;


    @Transactional
    public Candidat create(Candidat candidat) {
        candidat.setDateCreation(LocalDateTime.now());
        if (candidat.getCompetences() != null) {
            candidat.getCompetences().forEach(comp -> comp.setCandidat(candidat));
        }
        return repository.save(candidat);
    }

    @Transactional(readOnly = true)
    public Candidat getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Candidat non trouvé avec l'id " + id));
    }


    @Transactional(readOnly = true)
    public Optional<Candidat> findByPrenomAndNom(String prenom, String nom) {
        return repository.findByPrenomIgnoreCaseAndNomIgnoreCase ( prenom, nom );
    }



    @Transactional(readOnly = true)
    public List<Candidat> getAll() {
        return repository.findAll();
    }

    @Transactional
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Candidat impossible à supprimer : id " + id);
        }
        repository.deleteById(id);
    }

    @Transactional
    public Candidat update(Long id, Candidat updatedCandidat) {
        Candidat existingCandidat = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Candidat non trouvé avec l'id " + id));

        existingCandidat.setNom(updatedCandidat.getNom());
        existingCandidat.setPrenom(updatedCandidat.getPrenom());
        existingCandidat.setEmail(updatedCandidat.getEmail());
        existingCandidat.setTelephone(updatedCandidat.getTelephone());
        existingCandidat.setPosteActuel ( updatedCandidat.getPosteActuel () );
        existingCandidat.setLienCV(updatedCandidat.getLienCV());
        existingCandidat.setLienGitHub ( updatedCandidat.getLienGitHub () );
        existingCandidat.setLienLinkedin ( updatedCandidat.getLienLinkedin () );
        existingCandidat.setAnneesExperience(updatedCandidat.getAnneesExperience());
        existingCandidat.setConsentementDonnees(updatedCandidat.getConsentementDonnees());

        if (updatedCandidat.getCompetences() != null) {
            existingCandidat.getCompetences().clear();
            updatedCandidat.getCompetences().forEach(comp -> {
                comp.setCandidat(existingCandidat);
                existingCandidat.getCompetences().add(comp);
            });
        }

        return repository.save(existingCandidat);
    }
    public Page<Candidat> getAllCandidats(int page, int size) {
        return repository.findAll( PageRequest.of(page, size, Sort.by("dateCreation").descending()));
    }
}
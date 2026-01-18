package tn.esprit.recrutement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.recrutement.DTO.CandidatDTO;
import tn.esprit.recrutement.client.CandidatClient;
import tn.esprit.recrutement.entity.*;
import tn.esprit.recrutement.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CandidatureService {

    @Autowired private CandidatureRepository candidatureRepository;
    @Autowired private OffreEmploiRepository offreEmploiRepository;
    @Autowired private EntretienRepository entretienRepository;
    @Autowired private CandidatClient candidatClient;

    // --- 1. GESTION DES OFFRES (ADMIN) ---

    @Transactional
    public OffreEmploi creerOffre(OffreEmploi offre) {
        // Initialisation automatique pour respecter le workflow
        offre.setDatePublication(LocalDate.now());
        offre.setStatut(StatutOffre.PUBLIEE);
        return offreEmploiRepository.save(offre);
    }

    public List<OffreEmploi> listerOffresActives() {
        return offreEmploiRepository.findByStatut(StatutOffre.PUBLIEE);
    }

    // --- 2. GESTION DES CANDIDATURES (CANDIDAT) ---

    @Transactional
    public Candidature postuler(Long offreId, String prenom, String nom) {
        // 1. Vérification Offre
        OffreEmploi offre = offreEmploiRepository.findById(offreId)
                .orElseThrow(() -> new NoSuchElementException("L'offre #" + offreId + " n'existe plus."));

        // 2. Appel Feign avec gestion d'erreur précise
        CandidatDTO candidat;
        try {
            candidat = candidatClient.getByFullIdentity(prenom, nom);
        } catch (feign.FeignException.NotFound e) {
            throw new RuntimeException("Candidat inconnu : " + prenom + " " + nom + ". Veuillez vérifier l'orthographe.");
        } catch (Exception e) {
            throw new RuntimeException("Le service Talent est indisponible actuellement.");
        }

        // 3. Création Candidature
        Candidature candidature = new Candidature();
        candidature.setOffre(offre);
        candidature.setCandidatId(candidat.getId()); // On lie l'ID récupéré
        candidature.setEtat(EtatCandidature.NOUVELLE);
        candidature.setDatePostulation(LocalDateTime.now());

        return candidatureRepository.save(candidature);
    }
    // --- 3. WORKFLOW DE RECRUTEMENT (RH/ADMIN) ---

    @Transactional
    public Entretien planifierEntretien(Long candidatureId, Entretien entretien) {
        Candidature cand = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new NoSuchElementException("Candidature introuvable"));

        // Passage automatique à l'étape suivante du workflow
        cand.setEtat(EtatCandidature.ENTRETIEN_EN_COURS);

        entretien.setCandidature(cand);
        candidatureRepository.save(cand);
        return entretienRepository.save(entretien);
    }

    @Transactional
    public void mettreAJourStatutCandidature(Long candidatureId, EtatCandidature nouvelEtat) {
        Candidature cand = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new NoSuchElementException("Candidature introuvable"));

        cand.setEtat(nouvelEtat);

        // Si le candidat est accepté, on clôture l'offre automatiquement
        if (nouvelEtat == EtatCandidature.ACCEPTEE) {
            OffreEmploi offre = cand.getOffre();
            offre.setStatut(StatutOffre.CLOTUREE);
            offreEmploiRepository.save(offre);
        }
        candidatureRepository.save(cand);
    }

    // --- 4. CONSULTATION 360° ---

    @Transactional(readOnly = true)
    public Map<String, Object> consulterDossierComplet(Long candidatureId) {
        Candidature cand = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new NoSuchElementException("Candidature introuvable"));

        Map<String, Object> dossier = new LinkedHashMap<>();
        dossier.put("candidature_id", cand.getId());
        dossier.put("etat_actuel", cand.getEtat());
        dossier.put("offre_concernee", cand.getOffre().getTitre());

        // Récupération des données distantes via Feign
        try {
            dossier.put("infos_candidat", candidatClient.getById(cand.getCandidatId()));
        } catch (Exception e) {
            dossier.put("infos_candidat", "Données candidat indisponibles (Service Talent)");
        }

        dossier.put("historique_entretiens", cand.getEntretiens());
        return dossier;
    }
    @Transactional(readOnly = true)
    public List<Map<String, Object>> consulterTousLesDossiersComplets() {
        // Récupération de toutes les candidatures
        List<Candidature> candidatures = candidatureRepository.findAll();

        return candidatures.stream().map(cand -> {
            Map<String, Object> dossier = new LinkedHashMap<>();

            // Données locales de la candidature
            dossier.put("candidature_id", cand.getId());
            dossier.put("etat_actuel", cand.getEtat());

            // PROTECTION contre le NullPointerException (getOffre)
            String titreOffre = Optional.ofNullable(cand.getOffre())
                    .map(offre -> offre.getTitre())
                    .orElse("Offre non définie ou supprimée");
            dossier.put("offre_concernee", titreOffre);

            // Données distantes via Feign (Service Talent)
            if (cand.getCandidatId() != null) {
                try {
                    Object infosCandidat = candidatClient.getById(cand.getCandidatId());
                    dossier.put("infos_candidat", infosCandidat);
                } catch (Exception e) {
                    // Si le service Talent est injoignable ou l'ID n'existe pas
                    dossier.put("infos_candidat", "Détails candidat indisponibles (ID: " + cand.getCandidatId() + ")");
                }
            } else {
                dossier.put("infos_candidat", "ID Candidat manquant");
            }

            dossier.put("historique_entretiens", cand.getEntretiens());

            return dossier;
        }).collect(Collectors.toList());
    }
}
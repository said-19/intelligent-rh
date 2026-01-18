package tn.esprit.recrutement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.recrutement.entity.*;
import tn.esprit.recrutement.service.CandidatureService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recrutement")
@CrossOrigin(origins = "http://localhost:4200") // Autorise explicitement votre Angular
public class CandidatureController {

    @Autowired
    private CandidatureService candidatureService;

    // 1. Créer une offre (ADMIN)
    @PostMapping("/offres")
    public ResponseEntity<OffreEmploi> creerOffre(@RequestBody OffreEmploi offre) {
        return new ResponseEntity<>(candidatureService.creerOffre(offre), HttpStatus.CREATED);
    }

    // 2. Lister les offres pour les candidats
    @GetMapping("/offres/actives")
    public ResponseEntity<List<OffreEmploi>> listerOffres() {
        return ResponseEntity.ok(candidatureService.listerOffresActives());
    }

    @PostMapping("/postuler")
    public ResponseEntity<?> postuler(
            @RequestParam Long offreId,
            @RequestParam String prenom,
            @RequestParam String nom) {
        try {
            Candidature candidature = candidatureService.postuler(offreId, prenom, nom);
            return new ResponseEntity<>(candidature, HttpStatus.CREATED);
        } catch (java.util.NoSuchElementException e) {
            // Cas où l'offre n'existe pas
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            // Cas où le candidat n'est pas trouvé ou service Feign indisponible
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Erreur générique
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Une erreur inattendue est survenue."));
        }
    }

    // 4. Planifier un entretien (RH)
    @PostMapping("/candidatures/{id}/entretiens")
    public ResponseEntity<Entretien> planifierEntretien(
            @PathVariable Long id,
            @RequestBody Entretien entretien) {
        return ResponseEntity.ok(candidatureService.planifierEntretien(id, entretien));
    }

    // 5. Accepter ou Refuser (RH/ADMIN)
    @PatchMapping("/statut/{candidatureId}") // <--- Vérifiez ce chemin
    public void mettreAJourStatutCandidature(
            @PathVariable Long candidatureId,
            @RequestParam EtatCandidature etat) { // Spring cherche le paramètre ?etat=...

        candidatureService.mettreAJourStatutCandidature (candidatureId, etat);
    }

    // 6. Voir le dossier complet (Vue 360°)
    @GetMapping("/candidatures/{id}/dossier")
    public ResponseEntity<Map<String, Object>> voirDossier(@PathVariable Long id) {
        return ResponseEntity.ok(candidatureService.consulterDossierComplet(id));
    }
    @GetMapping("/dossiers-complets")
    public ResponseEntity<List<Map<String, Object>>> getAllDossiers() {
        List<Map<String, Object>> dossiers = candidatureService.consulterTousLesDossiersComplets();

        if (dossiers.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(dossiers);
    }
}
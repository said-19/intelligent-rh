package tn.esprit.employees.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.employees.entity.Candidat;
import tn.esprit.employees.repository.CandidatRepository;
import tn.esprit.employees.service.CandidatService;
import tn.esprit.employees.service.FileService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/candidats")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class CandidatController {

    private final CandidatService service;
    private final FileService fileService;
    private final CandidatRepository repository;


    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Candidat> create(
            @RequestPart("candidat") Candidat candidat,
            @RequestPart("image") MultipartFile image,
            @RequestPart("cv") MultipartFile cv) {

        try {
            // 1. Sauvegarde de la Photo de profil
            if (image != null && !image.isEmpty()) {
                String imagePath = fileService.save(image);
                candidat.setFileName(imagePath); // Stocke le nom de l'image
            }

            // 2. Sauvegarde du CV
            if (cv != null && !cv.isEmpty()) {
                String cvPath = fileService.save(cv);
                candidat.setLienCV(cvPath); // Stocke le nom du CV
            }

            // 3. Enregistrement final
            candidat.setDateCreation( LocalDateTime.now());
            Candidat saved = service.create(candidat);

            return new ResponseEntity<>(saved, HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<Candidat> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Candidat> getByFullIdentity(
            @RequestParam String prenom,
            @RequestParam String nom) {

        return service.findByPrenomAndNom(prenom.trim(), nom.trim())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping
    public ResponseEntity<List<Candidat>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }


    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Candidat> update(
            @PathVariable Long id,
            @RequestPart("candidat") Candidat candidat,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        try {
            // Si un nouveau fichier est fourni, on remplace l'ancien
            if (file != null && !file.isEmpty()) {
                String fileName = fileService.save(file);
                candidat.setFileName(fileName);
                candidat.setLienCV("/files/get/" + fileName);
            }

            return ResponseEntity.ok(service.update(id, candidat));
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour : {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Suppression du candidat.
     * Note: Idéalement, il faudrait aussi supprimer le fichier sur le disque ici.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        // Optionnel: Récupérer le candidat avant suppression pour avoir le nom du fichier
        // Candidat c = service.getById(id);
        // fileService.deletePhysicalFile(c.getFileName());

        service.deleteById(id);
        log.info("Candidat avec ID {} supprimé", id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/all")
    public ResponseEntity<Page<Candidat>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size) {
        return ResponseEntity.ok(service.getAllCandidats(page, size));
    }
    @GetMapping("/check/{id}")
    public ResponseEntity<Boolean> checkCandidatExists(@PathVariable Long id) {
        return ResponseEntity.ok(repository.existsById(id));
    }
}
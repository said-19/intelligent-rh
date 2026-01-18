package tn.esprit.recrutement.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import tn.esprit.recrutement.DTO.CandidatDTO;

import java.util.Optional;

@FeignClient(name = "talent-management-service", url = "${app.services.url}")
public interface CandidatClient {

    @GetMapping("/api/candidats/{id}")
    CandidatDTO getById(@PathVariable("id") Long id);

    @GetMapping("/api/candidats/search")
    CandidatDTO getByFullIdentity(
            @RequestParam("prenom") String prenom,
            @RequestParam("nom") String nom);
}
package tn.esprit.recrutement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication
@EnableFeignClients
@CrossOrigin(origins = "http://localhost:4200") // Autorise explicitement votre Angular
public class RecrutementApplication {

    public static void main(String[] args) {
        SpringApplication.run ( RecrutementApplication.class, args );
    }

}

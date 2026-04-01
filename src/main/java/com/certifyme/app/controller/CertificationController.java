package com.certifyme.app.controller;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.certifyme.app.Certification;
import com.certifyme.app.service.CertificationService;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/certifications")
@CrossOrigin(origins = "*")
public class CertificationController {

    private final CertificationService service;

    public CertificationController(CertificationService service) {
        this.service = service;
    }

    @GetMapping
    public List<Certification> getAll() {
        return service.getAllCertifications();
    }

    // Combined the duplicate POST methods into one clean action
    @PostMapping("/user/{userId}")
    public Certification create(@PathVariable Long userId, @RequestBody Certification cert) {
        return service.saveCertification(userId, cert);
    }

    @GetMapping("/user/{userId}")
    public List<Certification> getByUser(@PathVariable Long userId) {
        return service.getByUser(userId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteCertification(id);
        return ResponseEntity.noContent().build(); // Better REST practice to return 204
    }

    @PutMapping("/{id}")
    public Certification update(@PathVariable Long id, @RequestBody Certification cert) {
        // Ensure the ID from the URL is the one being updated
        return service.updateCertification(id, cert);
    }
    
    @GetMapping("/all")
    public List<Certification> getAllCertifications() {
        return service.getAll();
    }
}
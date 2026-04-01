package com.certifyme.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

import com.certifyme.app.Certification;
import com.certifyme.app.User;
import com.certifyme.app.repository.CertificationRepository;
import com.certifyme.app.repository.UserRepository;

@Service
public class CertificationService {

    private final CertificationRepository repo;

    @Autowired
    private UserRepository userRepo;

    public CertificationService(CertificationRepository repo) {
        this.repo = repo;
    }

    // 🔹 GET ALL
    public List<Certification> getAllCertifications() {
        return repo.findAll();
    }

    public List<Certification> getAll() {
        return repo.findAll();
    }

    // 🔹 CREATE WITH USER
    public Certification saveCertification(Long userId, Certification cert) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        cert.setUser(user);
        return repo.save(cert);
    }

    // 🔹 GET BY USER
    public List<Certification> getByUser(Long userId) {
        return repo.findByUserId(userId);
    }

    // 🔥 UPDATE (ONLY ONE METHOD)
    public Certification updateCertification(Long id, Certification updatedCert) {
        Certification cert = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Certification not found"));

        cert.setTitle(updatedCert.getTitle());
        cert.setIssuer(updatedCert.getIssuer());
        cert.setIssueDate(updatedCert.getIssueDate());
        cert.setExpiryDate(updatedCert.getExpiryDate());
        cert.setCredentialId(updatedCert.getCredentialId());
        cert.setUrl(updatedCert.getUrl());

        return repo.save(cert);
    }

    // 🔥 DELETE
    public void deleteCertification(Long id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("Certification not found");
        }
        repo.deleteById(id);
    }
}
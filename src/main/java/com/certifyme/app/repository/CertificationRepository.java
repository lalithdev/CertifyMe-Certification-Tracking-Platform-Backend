package com.certifyme.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.certifyme.app.Certification;
import java.util.List;
public interface CertificationRepository extends JpaRepository<Certification, Long> {
	List<Certification> findByUserId(Long userId);
}
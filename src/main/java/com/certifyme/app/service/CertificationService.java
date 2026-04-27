package com.certifyme.app.service;

import com.certifyme.app.dto.*;
import com.certifyme.app.exception.ResourceNotFoundException;
import com.certifyme.app.exception.UnauthorizedException;
import com.certifyme.app.mapper.CertificationMapper;
import com.certifyme.app.model.*;
import com.certifyme.app.repository.CertificationRepository;
import com.certifyme.app.repository.UserRepository;
import com.certifyme.app.util.DateUtil;
import com.certifyme.app.util.ExcelExportUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CertificationService {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CertificationService.class);
    private final CertificationRepository repo;
    private final UserRepository userRepo;
    private final CertificationMapper mapper;
    private final NotificationService notificationService;

    public CertificationService(CertificationRepository repo, UserRepository userRepo, CertificationMapper mapper, NotificationService notificationService) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.mapper = mapper;
        this.notificationService = notificationService;
    }

    public PagedResponseDTO<CertificationResponseDTO> getAll(Pageable pageable) {
        Page<Certification> page = repo.findAll(pageable);
        List<CertificationResponseDTO> dtos = mapper.toResponseDTOList(page.getContent());
        return new PagedResponseDTO<>(dtos, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    public List<CertificationResponseDTO> getAllForAdmin() {
        return mapper.toResponseDTOList(repo.findAllWithUser());
    }

    public CertificationResponseDTO saveCertification(Long userId, CertificationRequestDTO request) {
        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Certification cert = mapper.toEntity(request);
        cert.setUser(user);
        cert = repo.save(cert);
        notificationService.createNotification(user, cert, "New Certification", "Certification '" + cert.getTitle() + "' was successfully added.", "SYSTEM");
        return mapper.toResponseDTO(cert);
    }

    public PagedResponseDTO<CertificationResponseDTO> getByUser(Long userId, Pageable pageable) {
        Page<Certification> page = repo.findByUserId(userId, pageable);
        List<CertificationResponseDTO> dtos = mapper.toResponseDTOList(page.getContent());
        return new PagedResponseDTO<>(dtos, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    public List<CertificationResponseDTO> getByUser(Long userId) {
        return mapper.toResponseDTOList(repo.findByUserId(userId));
    }

    public CertificationResponseDTO updateCertification(Long id, CertificationRequestDTO request) {
        Certification cert = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Certification not found"));
        cert.setTitle(request.getTitle());
        cert.setIssuer(request.getIssuer());
        cert.setIssueDate(request.getIssueDate());
        cert.setExpiryDate(request.getExpiryDate());
        cert.setCredentialId(request.getCredentialId());
        cert.setUrl(request.getUrl());
        cert.setRemarks(request.getRemarks());
        return mapper.toResponseDTO(repo.save(cert));
    }

    public void deleteCertification(Long id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Certification not found");
        }
        repo.deleteById(id);
    }

    public CertificationResponseDTO updateRenewalStatus(Long id, CertificationRenewalDTO dto, User user) {
        Certification cert = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Certification not found"));
        LocalDateTime now = LocalDateTime.now();
        
        if (user.getRole() == Role.STUDENT) {
            // Students can ONLY request renewal
            if (dto.getAction() != RenewalAction.REQUEST) {
                throw new UnauthorizedException("Students can only request renewals.");
            }
            // Ownership check
            if (!cert.getUser().getId().equals(user.getId())) {
                throw new UnauthorizedException("You can only request renewal for your own certifications.");
            }
            
            cert.setRenewalStatus(RenewalStatus.PENDING);
            cert.setRequestedOn(now);
            notificationService.createNotification(cert.getUser(), cert, "Renewal Requested", 
                "Renewal requested for '" + cert.getTitle() + "'. Awaiting admin approval.", "RENEWAL_UPDATE");
                
        } else if (user.getRole() == Role.ADMIN) {
            // Admins can ONLY approve or reject
            if (dto.getAction() == RenewalAction.REQUEST) {
                throw new UnauthorizedException("Admins cannot request renewals.");
            }
            
            if (dto.getAction() == RenewalAction.APPROVE) {
                cert.setRenewalStatus(RenewalStatus.APPROVED);
                cert.setApprovedOn(now);
                cert.setRemarks(null); // Clear any previous rejection remarks
                notificationService.createNotification(cert.getUser(), cert, "Renewal Approved", 
                    "Your renewal for '" + cert.getTitle() + "' has been approved.", "RENEWAL_UPDATE");
                    
            } else if (dto.getAction() == RenewalAction.REJECT) {
                if (dto.getRemarks() == null || dto.getRemarks().isBlank()) {
                    throw new IllegalArgumentException("Remarks are required for rejection.");
                }
                cert.setRenewalStatus(RenewalStatus.REJECTED);
                cert.setRejectedOn(now);
                cert.setRemarks(dto.getRemarks());
                notificationService.createNotification(cert.getUser(), cert, "Renewal Rejected", 
                    "Your renewal for '" + cert.getTitle() + "' was rejected. Remarks: " + dto.getRemarks(), "RENEWAL_UPDATE");
            }
        }
        
        return mapper.toResponseDTO(repo.save(cert));
    }

    public CertificationResponseDTO sendReminder(Long id) {
        Certification cert = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Certification not found"));
        cert.setLastReminderSent(LocalDateTime.now());
        cert = repo.save(cert);
        notificationService.createNotification(cert.getUser(), cert, "Action Required: Expiring Certification", "Your certification '" + cert.getTitle() + "' is expiring soon. Please renew it.", "REMINDER");
        return mapper.toResponseDTO(cert);
    }

    public List<CertificationResponseDTO> getExpiringSoon() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusDays(30);
        return mapper.toResponseDTOList(repo.findExpiringSoon(now, threshold));
    }

    public List<CertificationResponseDTO> getExpired() {
        return mapper.toResponseDTOList(repo.findExpired(LocalDateTime.now()));
    }

    public List<CertificationResponseDTO> getPendingRenewals() {
        return mapper.toResponseDTOList(repo.findByRenewalStatus(RenewalStatus.PENDING));
    }

    public DashboardStatsDTO getDashboardStats(Long userId) {
        List<Certification> certs = repo.findByUserId(userId);
        long total = certs.size();
        long active = 0;
        long expiringSoon = 0;
        long expired = 0;
        long pendingRenewals = 0;
        for (Certification c : certs) {
            CertificationStatus status = DateUtil.computeCertificationStatus(c.getExpiryDate());
            if (status == CertificationStatus.ACTIVE) active++;
             else if (status == CertificationStatus.EXPIRING_SOON) expiringSoon++;
             else expired++;
            if (c.getRenewalStatus() == RenewalStatus.PENDING) pendingRenewals++;
        }
        double completion = 0.0;
        if (total > 0) {
            completion = ((double) active / total) * 100.0;
        }
        return DashboardStatsDTO.builder().totalCertifications(total).activeCertifications(active).expiringSoon(expiringSoon).expiredCertifications(expired).pendingRenewals(pendingRenewals).completionPercentage(completion).build();
    }

    public ByteArrayInputStream exportToExcel(Long userId) {
        List<CertificationResponseDTO> certs;
        if (userId == null) {
            certs = mapper.toResponseDTOList(repo.findAll());
        } else {
            certs = mapper.toResponseDTOList(repo.findByUserId(userId));
        }
        return ExcelExportUtil.generateCertificationReport(certs);
    }
}

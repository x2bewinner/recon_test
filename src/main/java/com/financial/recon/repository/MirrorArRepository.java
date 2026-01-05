package com.financial.recon.repository;

import com.financial.recon.entity.MirrorAr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MirrorArRepository extends JpaRepository<MirrorAr, String> {
}


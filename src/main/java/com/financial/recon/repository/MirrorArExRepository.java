package com.financial.recon.repository;

import com.financial.recon.entity.MirrorArEx;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MirrorArExRepository extends JpaRepository<MirrorArEx, String> {
}


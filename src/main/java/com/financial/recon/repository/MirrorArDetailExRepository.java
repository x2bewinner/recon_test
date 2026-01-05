package com.financial.recon.repository;

import com.financial.recon.entity.MirrorArDetailEx;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MirrorArDetailExRepository extends JpaRepository<MirrorArDetailEx, String> {
}


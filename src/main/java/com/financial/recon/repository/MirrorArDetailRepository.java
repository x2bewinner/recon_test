package com.financial.recon.repository;

import com.financial.recon.entity.MirrorArDetail;
import com.financial.recon.entity.MirrorArDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MirrorArDetailRepository extends JpaRepository<MirrorArDetail, MirrorArDetailId> {
}


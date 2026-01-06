package com.xxcards.xbtx.udar.repository;

import com.xxcards.xbtx.udar.entity.MirrorArDetail;
import com.xxcards.xbtx.udar.entity.MirrorArDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MirrorArDetailRepository extends JpaRepository<MirrorArDetail, MirrorArDetailId> {
}


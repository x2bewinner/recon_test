package com.xxcards.xbtx.udar.repository;

import com.xxcards.xbtx.udar.entity.MirrorAr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MirrorArRepository extends JpaRepository<MirrorAr, String> {
}


package com.xxcards.xbtx.udar.repository;

import com.xxcards.xbtx.udar.entity.MirrorArEx;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MirrorArExRepository extends JpaRepository<MirrorArEx, String> {
}


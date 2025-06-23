package com.challkathon.momento.global.infrastructure.repository

import com.challkathon.momento.domain.s3.entity.Uuid
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UuidRepository : JpaRepository<Uuid, Long>

package com.challkathon.momento.domain.family.repository

import com.challkathon.momento.domain.family.entity.Family

interface FamilyRepositoryCustom {
    fun findActiveFamilies(): List<Family>
}
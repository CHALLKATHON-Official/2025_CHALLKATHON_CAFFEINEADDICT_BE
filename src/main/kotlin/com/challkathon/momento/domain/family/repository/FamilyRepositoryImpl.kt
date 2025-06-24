package com.challkathon.momento.domain.family.repository

import com.challkathon.momento.domain.family.entity.Family
import com.challkathon.momento.domain.family.entity.QFamily.family
import com.challkathon.momento.domain.user.entity.QUser
import com.challkathon.momento.domain.user.entity.QUser.user
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class FamilyRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : FamilyRepositoryCustom {

    override fun findActiveFamilies(): List<Family> {
        val subUser = QUser("u2")

        return queryFactory
            .selectDistinct(family)
            .from(family)
            .join(family.users, user).fetchJoin()
            .where(
                family.count.gt(0),
                queryFactory.selectOne()
                    .from(subUser)
                    .where(
                        subUser.family.eq(family),
                        subUser.lastActiveAt.gt(LocalDateTime.now().minusDays(30))
                    )
                    .exists()
            )
            .fetch()
    }
}
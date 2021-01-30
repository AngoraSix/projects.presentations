package com.angorasix.contributors.application

import com.angorasix.contributors.domain.contributor.Contributor
import io.smallrye.mutiny.Uni
import javax.enterprise.context.ApplicationScoped




/**
 *
 *
 * @author rozagerardo
 */
@ApplicationScoped
class ContributorsService {

    fun findSingleContributor(id: String): Uni<Contributor>? {
        return Uni.createFrom()
            .item(id)
            .onItem()
            .transform { i: String? ->
                Contributor(id,"newName")
            }
    }
}

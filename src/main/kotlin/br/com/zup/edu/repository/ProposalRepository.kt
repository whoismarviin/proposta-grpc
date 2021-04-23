package br.com.zup.edu.repository

import br.com.zup.edu.Proposal
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ProposalRepository : JpaRepository<Proposal,UUID>{
    fun existsByDocument(document: String?): Boolean

}
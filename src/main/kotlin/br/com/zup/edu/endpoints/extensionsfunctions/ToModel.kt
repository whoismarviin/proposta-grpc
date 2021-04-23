package br.com.zup.edu.endpoints.extensionsfunctions

import br.com.zup.edu.CreateProposalRequest
import br.com.zup.edu.Proposal
import java.math.BigDecimal


fun CreateProposalRequest.toModel(): Proposal {
    return Proposal(

        document = document,
        name = name,
        email = email,
        adress = address,
        salary = BigDecimal(salary)

    )

}
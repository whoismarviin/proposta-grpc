package br.com.zup.edu.endpoints

import br.com.zup.edu.CreateProposalRequest
import br.com.zup.edu.CreateProposalResponse
import br.com.zup.edu.PropostasGrpcServiceGrpc
import br.com.zup.edu.endpoints.extensionsfunctions.toModel
import br.com.zup.edu.repository.ProposalRepository
import com.google.protobuf.Any
import com.google.protobuf.Timestamp
import com.google.rpc.BadRequest
import com.google.rpc.Code
import io.grpc.Status
import io.grpc.stub.StreamObserver
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.ConstraintViolationException

@Singleton // indica que o micronaut administra esse objeto.
open class CreateProposalEndpoint(@Inject val repository: ProposalRepository) :
    PropostasGrpcServiceGrpc.PropostasGrpcServiceImplBase() {

    @Transactional
    override fun create(request: CreateProposalRequest, responseObserver: StreamObserver<CreateProposalResponse>?) {


        val proposal = request.toModel()

        if (repository.existsByDocument(request.document)) {
            Status.ALREADY_EXISTS
                .withDescription("O documento já existe dentro do nosso banco de dados")
                .asRuntimeException()

        }


        try {
            repository.save(proposal)

        } catch (e: ConstraintViolationException) {
            val badRequest = BadRequest.newBuilder()
                .addAllFieldViolations(e.constraintViolations.map {
                    BadRequest.FieldViolation.newBuilder()
                        .setField(it.propertyPath.last().toString())
                        .setDescription(it.message)
                        .build()
                })
                .build();

            val statusProto = com.google.rpc.Status.newBuilder()
                .setCode(Code.ALREADY_EXISTS_VALUE)
                .setMessage("Invalid Arguments")
                .addDetails(Any.pack(badRequest))
                .build()


            responseObserver!!.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("Os parametros passados são invalidos")
                    .withCause(e)
                    .asRuntimeException()
            )
            return
        }

        responseObserver?.onNext(
            CreateProposalResponse.newBuilder()
                .setPropostaId(proposal.id.toString())
                .setCreatedAt(proposal.createdAt.let {
                    val instante = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder().setSeconds(instante.epochSecond)
                        .setNanos(instante.nano)
                        .build()
                })
                .build()
        )


    }


}
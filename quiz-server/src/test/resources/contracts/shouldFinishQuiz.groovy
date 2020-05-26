package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should finish a quiz"
    request {
        url "/api/quiz/123e4567-e89b-12d3-a456-426655440000"
        method POST()
    }
    response {
        status 200
    }
}

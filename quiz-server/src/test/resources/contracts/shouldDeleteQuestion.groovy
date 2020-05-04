package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should create a new quiz"
    request {
        url "/api/quiz/123e4567-e89b-12d3-a456-426655440000/questions/123e4567-e89b-12d3-a456-426655440001"
        method DELETE()
    }
    response {
        status OK()
    }
}

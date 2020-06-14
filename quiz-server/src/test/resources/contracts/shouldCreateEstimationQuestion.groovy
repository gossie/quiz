package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should create a new estimation question"
    request {
        url "/api/quiz/123e4567-e89b-12d3-a456-426655440000/questions"
        method POST()
        headers {
            contentType applicationJson()
        }
        body([question: "Schätzfrage?", publicVisible: true, estimation: true])
    }
    response {
        status 201
    }
}

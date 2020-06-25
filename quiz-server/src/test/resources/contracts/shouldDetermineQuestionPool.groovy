package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should determine question pool"
    request {
        url "/api/questionPool?category=category1"
        method GET()
    }
    response {
        status OK()
    }
}

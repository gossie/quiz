package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should determine question pool"
    request {
        url "/api/questionPool"
        method GET()
    }
    response {
        status OK()
    }
}

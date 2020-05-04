package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should create a new quiz"
    request {
        url "/api/quiz"
        method POST()
        headers {
            accept textPlain()
            contentType applicationJson()
        }
        body([name: "Awesome Quiz"])
    }
    response {
        status 201
        body anyUuid()
    }
}

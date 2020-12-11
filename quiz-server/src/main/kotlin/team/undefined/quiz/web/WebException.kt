package team.undefined.quiz.web

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class WebException(status: HttpStatus, message: String?) : ResponseStatusException(status, message)
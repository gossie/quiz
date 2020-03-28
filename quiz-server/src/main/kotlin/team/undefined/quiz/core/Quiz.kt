package team.undefined.quiz.core

data class Quiz(val id: Long? = null, val name: String, val participants: List<Participant> = ArrayList(), val questions: List<String> = ArrayList(), var turn: String? = null) {

    fun nobodyHasBuzzered(): Boolean {
        return turn == null
    }

    fun select(participantName: String): Quiz {
        turn = participantName
        return this
    }

    fun addParticipant(participantName: Participant): Quiz {
        (participants as MutableList).add(participantName)
        return this;
    }

    fun startQuestion(): Quiz {
        turn = null
        return this
    }

}

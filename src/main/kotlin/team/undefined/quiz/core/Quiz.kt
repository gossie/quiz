package team.undefined.quiz.core

data class Quiz(val id: Long? = null, val name: String, val participants: List<String> = ArrayList(), var turn: String? = null) {

    fun nobodyHasBuzzered(): Boolean {
        return turn == null
    }

    fun select(participantName: String): Quiz {
        turn = participantName
        return this
    }

    fun addParticipant(participantName: String): Quiz {
        (participants as MutableList).add(participantName)
        return this;
    }

}

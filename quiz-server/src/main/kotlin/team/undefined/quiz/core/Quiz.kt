package team.undefined.quiz.core

data class Quiz(val id: Long? = null, val name: String, val participants: List<Participant> = ArrayList(), val questions: List<Question> = ArrayList()) {

    fun nobodyHasBuzzered(): Boolean {
        return participants
                .none { it.turn }
    }

    fun select(participantId: Long): Quiz {
        participants
                .find { it.id == participantId }
                ?.turn = true
        return this
    }

    fun addParticipant(participantName: Participant): Quiz {
        (participants as MutableList).add(participantName)
        return this;
    }

    fun startQuestion(question: Question): Quiz {
        participants.forEach { it.turn = false }
        (questions as MutableList).add(question)
        return this
    }

    fun answeredCorrect(): Quiz {
        participants
                .filter { it.turn }
                .forEach {
                    it.points = it.points + 1
                    it.turn = false
                }

        questions.forEach { it.pending = false }
        return this;
    }

    fun answeredInorrect(): Quiz {
        participants
                .filter { it.turn }
                .forEach { it.turn = false }

        questions.forEach { it.pending = false }
        return this;
    }

}

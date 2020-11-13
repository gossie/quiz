package team.undefined.quiz.web;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

public class ParticipantDTOAssert extends AbstractAssert<ParticipantDTOAssert, ParticipantDTO> {

    private ParticipantDTOAssert(ParticipantDTO participantDTO) {
        super(participantDTO, ParticipantDTOAssert.class);
    }

    public static ParticipantDTOAssert assertThat(ParticipantDTO participant) {
        return new ParticipantDTOAssert(participant);
    }

    public ParticipantDTOAssert hasName(String name) {
        Assertions.assertThat(actual.getName()).isEqualTo(name);
        return this;
    }

    public ParticipantDTOAssert isTurn() {
        Assertions.assertThat(actual.getTurn()).isTrue();
        return this;
    }

    public ParticipantDTOAssert isNotTurn() {
        Assertions.assertThat(actual.getTurn()).isFalse();
        return this;
    }

    public ParticipantDTOAssert hasPoints(int points) {
        Assertions.assertThat(actual.getPoints()).isEqualTo(points);
        return this;
    }

    public ParticipantDTOAssert allowsReveal() {
        Assertions.assertThat(actual.getRevealAllowed()).isTrue();
        return this;
    }

    public ParticipantDTOAssert doesNotAllowReveal() {
        Assertions.assertThat(actual.getRevealAllowed()).isFalse();
        return this;
    }
}

package team.undefined.quiz.core;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import team.undefined.quiz.web.ParticipantDTO;

public class ParticipantAssert extends AbstractAssert<ParticipantAssert, Participant> {

    private ParticipantAssert(Participant participant) {
        super(participant, ParticipantAssert.class);
    }

    public static ParticipantAssert assertThat(Participant participant) {
        return new ParticipantAssert(participant);
    }

    public ParticipantAssert hasName(String name) {
        Assertions.assertThat(actual.getName()).isEqualTo(name);
        return this;
    }

    public ParticipantAssert isTurn() {
        Assertions.assertThat(actual.getTurn()).isTrue();
        return this;
    }

    public ParticipantAssert isNotTurn() {
        Assertions.assertThat(actual.getTurn()).isFalse();
        return this;
    }

    public ParticipantAssert hasPoints(int points) {
        Assertions.assertThat(actual.getPoints()).isEqualTo(points);
        return this;
    }

    public ParticipantAssert allowsReveal() {
        Assertions.assertThat(actual.getRevealAllowed()).isTrue();
        return this;
    }

    public ParticipantAssert doesNotAllowReveal() {
        Assertions.assertThat(actual.getRevealAllowed()).isFalse();
        return this;
    }
}

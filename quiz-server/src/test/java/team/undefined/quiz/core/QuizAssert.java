package team.undefined.quiz.core;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import java.util.UUID;

public class QuizAssert extends AbstractAssert<QuizAssert, Quiz> {

    private QuizAssert(Quiz actual) {
        super(actual, QuizAssert.class);
    }

    public static QuizAssert assertThat(Quiz actual) {
        return new QuizAssert(actual);
    }

    public QuizAssert hasId(UUID id) {
        Assertions.assertThat(actual.getId()).isEqualTo(id);
        return this;
    }

    public QuizAssert hasNoParticipants() {
        Assertions.assertThat(actual.getParticipants()).isEmpty();
        return this;
    }

    public QuizAssert hasNoQuestions() {
        Assertions.assertThat(actual.getQuestions()).isEmpty();
        return this;
    }

    public QuizAssert isFinished() {
        Assertions.assertThat(actual.getFinished()).isTrue();
        return this;
    }

    public QuizAssert isNotFinished() {
        Assertions.assertThat(actual.getFinished()).isFalse();
        return this;
    }

}

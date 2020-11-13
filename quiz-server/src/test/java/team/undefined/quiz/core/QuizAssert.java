package team.undefined.quiz.core;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import team.undefined.quiz.web.QuestionDTOAssert;
import team.undefined.quiz.web.QuizDTOAssert;

import java.util.UUID;
import java.util.function.Consumer;

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

    public QuizAssert particpantSizeIs(int size) {
        Assertions.assertThat(actual.getParticipants()).hasSize(size);
        return this;
    }

    public QuizAssert hasParticipant(int index, Consumer<ParticipantAssert> consumer) {
        consumer.accept(ParticipantAssert.assertThat(actual.getParticipants().get(index)));
        return this;
    }

    public QuizAssert hasNoQuestions() {
        Assertions.assertThat(actual.getQuestions()).isEmpty();
        return this;
    }

    public QuizAssert questionSizeIs(int size) {
        Assertions.assertThat(actual.getQuestions()).hasSize(size);
        return this;
    }

    public QuizAssert hasQuestion(int index, Consumer<QuestionAssert> consumer) {
        consumer.accept(QuestionAssert.assertThat(actual.getQuestions().get(index)));
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

    public QuizAssert hasQuizStatistics(Consumer<QuizStatisticsAssert> consumer) {
        consumer.accept(QuizStatisticsAssert.assertThat(actual.getQuizStatistics()));
        return this;
    }

}

package team.undefined.quiz.core;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import java.util.Map;
import java.util.UUID;

public class QuestionAssert extends AbstractAssert<QuestionAssert, Question> {

    public QuestionAssert(Question question) {
        super(question, QuestionAssert.class);
    }

    public static QuestionAssert assertThat(Question question) {
        return new QuestionAssert(question);
    }

    public QuestionAssert hasId(UUID id) {
        Assertions.assertThat(actual.getId()).isEqualTo(id);
        return this;
    }

    public QuestionAssert hasQuestion(String question) {
        Assertions.assertThat(actual.getQuestion()).isEqualTo(question);
        return this;
    }

    public QuestionAssert isPending() {
        Assertions.assertThat(actual.getPending()).isTrue();
        return this;
    }

    public QuestionAssert isNotPending() {
        Assertions.assertThat(actual.getPending()).isFalse();
        return this;
    }

    public QuestionAssert initialTimeToAnswerIs(int seconds) {
        Assertions.assertThat(actual.getInitialTimeToAnswer()).isEqualTo(seconds);
        return this;
    }

    public QuestionAssert secondsLeftIs(int seconds) {
        Assertions.assertThat(actual.getSecondsLeft()).isEqualTo(seconds);
        return this;
    }

    public QuestionAssert isBuzzerQuestion() {
        Assertions.assertThat(actual.getEstimates()).isNull();
        return this;
    }

    public QuestionAssert isEstimationQuestion() {
        Assertions.assertThat(actual.getEstimates()).isNotNull();
        return this;
    }

    public QuestionAssert hasEstimates(Map<UUID, String> estimates) {
        Assertions.assertThat(actual.getEstimates()).isEqualTo(estimates);
        return this;
    }

    public QuestionAssert hasPreviousQuestionId(UUID previousQuestionId) {
        Assertions.assertThat(actual.getPreviousQuestionId()).isEqualTo(previousQuestionId);
        return this;
    }

    public QuestionAssert isRevealed() {
        Assertions.assertThat(actual.getRevealed()).isTrue();
        return this;
    }

    public QuestionAssert isNotRevealed() {
        Assertions.assertThat(actual.getRevealed()).isFalse();
        return this;
    }
}

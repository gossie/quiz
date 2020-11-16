package team.undefined.quiz.web;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import java.util.function.Consumer;

public class QuizDTOAssert extends AbstractAssert<QuizDTOAssert, QuizDTO> {

    private QuizDTOAssert(QuizDTO actual) {
        super(actual, QuizDTOAssert.class);
    }

    public static QuizDTOAssert assertThat(QuizDTO actual) {
        return new QuizDTOAssert(actual);
    }

    public QuizDTOAssert openQuestionSizeIs(int size) {
        Assertions.assertThat(actual.getOpenQuestions()).hasSize(size);
        return this;
    }

    public QuizDTOAssert playedQuestionSizeIs(int size) {
        Assertions.assertThat(actual.getPlayedQuestions()).hasSize(size);
        return this;
    }

    public QuizDTOAssert hasOpenQuestion(int index, Consumer<QuestionDTOAssert> consumer) {
        consumer.accept(QuestionDTOAssert.assertThat(actual.getOpenQuestions().get(index)));
        return this;
    }

    public QuizDTOAssert hasPlayedQuestion(int index, Consumer<QuestionDTOAssert> consumer) {
        consumer.accept(QuestionDTOAssert.assertThat(actual.getPlayedQuestions().get(index)));
        return this;
    }

    public QuizDTOAssert particpantSizeIs(int size) {
        Assertions.assertThat(actual.getParticipants()).hasSize(size);
        return this;
    }

    public QuizDTOAssert hasParticipant(int index, Consumer<ParticipantDTOAssert> consumer) {
        consumer.accept(ParticipantDTOAssert.assertThat(actual.getParticipants().get(index)));
        return this;
    }

    public QuizDTOAssert undoIsNotPossible() {
        Assertions.assertThat(actual.getUndoPossible()).isFalse();
        return this;
    }

    public QuizDTOAssert undoIsPossible() {
        Assertions.assertThat(actual.getUndoPossible()).isTrue();
        return this;
    }

    public QuizDTOAssert redoIsNotPossible() {
        Assertions.assertThat(actual.getRedoPossible()).isFalse();
        return this;
    }

    public QuizDTOAssert redoIsPossible() {
        Assertions.assertThat(actual.getRedoPossible()).isTrue();
        return this;
    }

    public QuizDTOAssert isNotFinished() {
        Assertions.assertThat(actual.getFinished()).isFalse();
        return this;
    }

    public QuizDTOAssert isFinished() {
        Assertions.assertThat(actual.getFinished()).isTrue();
        return this;
    }


}

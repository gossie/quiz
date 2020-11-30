package team.undefined.quiz.web;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class QuestionDTOAssert extends AbstractAssert<QuestionDTOAssert, QuestionDTO> {

    public QuestionDTOAssert(QuestionDTO questionDTO) {
        super(questionDTO, QuestionDTOAssert.class);
    }

    public static QuestionDTOAssert assertThat(QuestionDTO question) {
        return new QuestionDTOAssert(question);
    }

    public QuestionDTOAssert hasQuestion(String question) {
        Assertions.assertThat(actual.getQuestion()).isEqualTo(question);
        return this;
    }

    public QuestionDTOAssert isPending() {
        Assertions.assertThat(actual.getPending()).isTrue();
        return this;
    }

    public QuestionDTOAssert isNotPending() {
        Assertions.assertThat(actual.getPending()).isFalse();
        return this;
    }

    public QuestionDTOAssert isBuzzerQuestion() {
        Assertions.assertThat(actual.getEstimates()).isNull();
        return this;
    }

    public QuestionDTOAssert isEstimationQuestion() {
        Assertions.assertThat(actual.getEstimates()).isNotNull();
        return this;
    }

    public QuestionDTOAssert hasEstimates(Map<UUID, String> estimates) {
        Assertions.assertThat(actual.getEstimates()).isEqualTo(estimates);
        return this;
    }

    public QuestionDTOAssert isMultipleChoiceQuestion() {
        Assertions.assertThat(actual.getChoices()).isNotEmpty();
        return this;
    }

    public QuestionDTOAssert hasChoice(int index, Consumer<ChoiceDTOAssert> consumer) {
        consumer.accept(ChoiceDTOAssert.assertThat(actual.getChoices().get(index)));
        return this;
    }
}

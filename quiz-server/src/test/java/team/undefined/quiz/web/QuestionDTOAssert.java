package team.undefined.quiz.web;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

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
}

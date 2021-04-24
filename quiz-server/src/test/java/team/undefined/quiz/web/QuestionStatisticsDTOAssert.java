package team.undefined.quiz.web;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import java.util.function.Consumer;

public class QuestionStatisticsDTOAssert extends AbstractAssert<QuestionStatisticsDTOAssert, QuestionStatisticsDTO> {

    private QuestionStatisticsDTOAssert(QuestionStatisticsDTO questionStatistics) {
        super(questionStatistics, QuestionStatisticsDTOAssert.class);
    }

    public static QuestionStatisticsDTOAssert assertThat(QuestionStatisticsDTO actual) {
        return new QuestionStatisticsDTOAssert(actual);
    }
/*
    public QuestionStatisticsDTOAssert hasQuestion(Consumer<QuestionDTOAssert> consumer) {
        consumer.accept(QuestionDTOAssert.assertThat(actual.getQuestion()));
        return this;
    }

    public QuestionStatisticsDTOAssert answerStatisticsSizeIs(int size) {
        Assertions.assertThat(actual.getAnswerStatistics()).hasSize(size);
        return this;
    }

    public QuestionStatisticsDTOAssert hasAnswerStatistics(int index, Consumer<AnswerStatisticsDTOAssert> consumer) {
        consumer.accept(AnswerStatisticsDTOAssert.assertThat(actual.getAnswerStatistics().get(index)));
        return this;
    }
 */
}

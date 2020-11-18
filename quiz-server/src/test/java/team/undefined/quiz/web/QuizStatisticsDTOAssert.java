package team.undefined.quiz.web;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import team.undefined.quiz.core.QuizStatistics;

import java.util.function.Consumer;

public class QuizStatisticsDTOAssert extends AbstractAssert<QuizStatisticsDTOAssert, QuizStatisticsDTO> {

    private QuizStatisticsDTOAssert(QuizStatisticsDTO quizStatistics) {
        super(quizStatistics, QuizStatisticsDTOAssert.class);
    }

    public static QuizStatisticsDTOAssert assertThat(QuizStatisticsDTO actual) {
        return new QuizStatisticsDTOAssert(actual);
    }

    public QuizStatisticsDTOAssert questionStatisticsSizeIs(int size) {
        Assertions.assertThat(actual.getQuestionStatistics()).hasSize(size);
        return this;
    }

    public QuizStatisticsDTOAssert hasQuestionStatistics(int index, Consumer<QuestionStatisticsDTOAssert> consumer) {
        consumer.accept(QuestionStatisticsDTOAssert.assertThat(actual.getQuestionStatistics().get(index)));
        return this;
    }
}

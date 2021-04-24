package team.undefined.quiz.core;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import java.util.function.Consumer;

public class QuizStatisticsAssert extends AbstractAssert<QuizStatisticsAssert, QuizStatistics> {

    private QuizStatisticsAssert(QuizStatistics quizStatistics) {
        super(quizStatistics, QuizStatisticsAssert.class);
    }

    public static QuizStatisticsAssert assertThat(QuizStatistics actual) {
        return new QuizStatisticsAssert(actual);
    }
/*
    public QuizStatisticsAssert questionStatisticsSizeIs(int size) {
        Assertions.assertThat(actual.getQuestionStatistics()).hasSize(size);
        return this;
    }

    public QuizStatisticsAssert hasQuestionStatistics(int index, Consumer<QuestionStatisticsAssert> consumer) {
        consumer.accept(QuestionStatisticsAssert.assertThat(actual.getQuestionStatistics().get(index)));
        return this;
    }
 */
}

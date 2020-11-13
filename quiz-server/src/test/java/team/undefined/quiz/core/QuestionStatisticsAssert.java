package team.undefined.quiz.core;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import java.util.UUID;
import java.util.function.Consumer;

public class QuestionStatisticsAssert extends AbstractAssert<QuestionStatisticsAssert, QuestionStatistics> {

    private QuestionStatisticsAssert(QuestionStatistics questionStatistics) {
        super(questionStatistics, QuestionStatisticsAssert.class);
    }

    public static QuestionStatisticsAssert assertThat(QuestionStatistics actual) {
        return new QuestionStatisticsAssert(actual);
    }

    public QuestionStatisticsAssert hasQuestionId(UUID id) {
        Assertions.assertThat(actual.getQuestionId()).isEqualTo(id);
        return this;
    }

    public QuestionStatisticsAssert buzzerStatisticsSizeIs(int size) {
        Assertions.assertThat(actual.getBuzzerStatistics()).hasSize(size);
        return this;
    }

    public QuestionStatisticsAssert hasBuzzerStatistics(int index, Consumer<BuzzerStatisticsAssert> consumer) {
        consumer.accept(BuzzerStatisticsAssert.assertThat(actual.getBuzzerStatistics().get(index)));
        return this;
    }
}

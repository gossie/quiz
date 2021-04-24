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

    public QuestionStatisticsAssert ratingSizeIs(int size) {
        Assertions.assertThat(actual.getRatings()).hasSize(size);
        return this;
    }

    public QuestionStatisticsAssert hasRating(int index, AnswerCommand.Answer raiting) {
        Assertions.assertThat(actual.getRatings().get(index)).isEqualTo(raiting);
        return this;
    }

}

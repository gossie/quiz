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

    public QuizStatisticsAssert participantStatisticsSizeIs(int size) {
        Assertions.assertThat(actual.getParticipantStatistics()).hasSize(size);
        return this;
    }

    public QuizStatisticsAssert hasParticipantStatistics(int index, Consumer<ParticipantStatisticAssert> consumer) {
        consumer.accept(ParticipantStatisticAssert.assertThat(actual.getParticipantStatistics().get(index)));
        return this;
    }

}

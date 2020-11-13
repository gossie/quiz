package team.undefined.quiz.core;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import java.util.UUID;

public class BuzzerStatisticsAssert extends AbstractAssert<BuzzerStatisticsAssert, BuzzerStatistics> {

    private BuzzerStatisticsAssert(BuzzerStatistics buzzerStatistics) {
        super(buzzerStatistics, BuzzerStatisticsAssert.class);
    }

    public static BuzzerStatisticsAssert assertThat(BuzzerStatistics actual) {
        return new BuzzerStatisticsAssert(actual);
    }

    public BuzzerStatisticsAssert hasDuration(long duration) {
        Assertions.assertThat(actual.getDuration()).isEqualTo(duration);
        return this;
    }

    public BuzzerStatisticsAssert hasParticipantId(UUID participantId) {
        Assertions.assertThat(actual.getParticipantId()).isEqualTo(participantId);
        return this;
    }

    public BuzzerStatisticsAssert isCorrect() {
        Assertions.assertThat(actual.getAnswer()).isEqualTo(AnswerCommand.Answer.CORRECT);
        return this;
    }

    public BuzzerStatisticsAssert isIncorrect() {
        Assertions.assertThat(actual.getAnswer()).isEqualTo(AnswerCommand.Answer.INCORRECT);
        return this;
    }

}

package team.undefined.quiz.core;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import java.util.UUID;

public class AnswerStatisticsAssert extends AbstractAssert<AnswerStatisticsAssert, AnswerStatistics> {

    private AnswerStatisticsAssert(AnswerStatistics answerStatistics) {
        super(answerStatistics, AnswerStatisticsAssert.class);
    }

    public static AnswerStatisticsAssert assertThat(AnswerStatistics actual) {
        return new AnswerStatisticsAssert(actual);
    }

    public AnswerStatisticsAssert hasDuration(long duration) {
        Assertions.assertThat(actual.getDuration()).isEqualTo(duration);
        return this;
    }

    public AnswerStatisticsAssert hasParticipantId(UUID participantId) {
        Assertions.assertThat(actual.getParticipantId()).isEqualTo(participantId);
        return this;
    }

    public AnswerStatisticsAssert isCorrect() {
        Assertions.assertThat(actual.getRating()).isEqualTo(AnswerCommand.Answer.CORRECT);
        return this;
    }

    public AnswerStatisticsAssert isIncorrect() {
        Assertions.assertThat(actual.getRating()).isEqualTo(AnswerCommand.Answer.INCORRECT);
        return this;
    }

}

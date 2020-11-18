package team.undefined.quiz.web;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import team.undefined.quiz.core.AnswerCommand;

import java.util.function.Consumer;

public class AnswerStatisticsDTOAssert extends AbstractAssert<AnswerStatisticsDTOAssert, AnswerStatisticsDTO> {

    private AnswerStatisticsDTOAssert(AnswerStatisticsDTO answerStatistics) {
        super(answerStatistics, AnswerStatisticsDTOAssert.class);
    }

    public static AnswerStatisticsDTOAssert assertThat(AnswerStatisticsDTO actual) {
        return new AnswerStatisticsDTOAssert(actual);
    }

    public AnswerStatisticsDTOAssert hasDuration(long duration) {
        Assertions.assertThat(actual.getDuration()).isEqualTo(duration);
        return this;
    }

    public AnswerStatisticsDTOAssert hasParticipantId(Consumer<ParticipantDTOAssert> consumer) {
        consumer.accept(ParticipantDTOAssert.assertThat(actual.getParticipant()));
        return this;
    }

    public AnswerStatisticsDTOAssert hasNoAnswer() {
        Assertions.assertThat(actual.getAnswer()).isNull();
        return this;
    }

    public AnswerStatisticsDTOAssert hasAnswer(String answer) {
        Assertions.assertThat(actual.getAnswer()).isEqualTo(answer);
        return this;
    }

    public AnswerStatisticsDTOAssert isCorrect() {
        Assertions.assertThat(actual.getRating()).isEqualTo(AnswerCommand.Answer.CORRECT);
        return this;
    }

    public AnswerStatisticsDTOAssert isIncorrect() {
        Assertions.assertThat(actual.getRating()).isEqualTo(AnswerCommand.Answer.INCORRECT);
        return this;
    }

}

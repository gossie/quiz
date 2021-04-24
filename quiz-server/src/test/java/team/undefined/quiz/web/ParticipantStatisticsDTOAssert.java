package team.undefined.quiz.web;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import team.undefined.quiz.core.ParticipantStatisticAssert;
import team.undefined.quiz.core.ParticipantStatistics;
import team.undefined.quiz.core.QuestionStatisticsAssert;

import java.util.UUID;
import java.util.function.Consumer;

public class ParticipantStatisticsDTOAssert extends AbstractAssert<ParticipantStatisticsDTOAssert, ParticipantStatisticsDTO> {

    private ParticipantStatisticsDTOAssert(ParticipantStatisticsDTO participantStatistics) {
        super(participantStatistics, ParticipantStatisticAssert.class);
    }

    public static ParticipantStatisticsDTOAssert assertThat(ParticipantStatisticsDTO participantStatistics) {
        return new ParticipantStatisticsDTOAssert(participantStatistics);
    }

    public ParticipantStatisticsDTOAssert questionStatisticsSizeIs(int size) {
        Assertions.assertThat(actual.getQuestionStatistics()).hasSize(size);
        return this;
    }

    public ParticipantStatisticsDTOAssert hasQuestionStatistics(int index, Consumer<QuestionStatisticsDTOAssert> consumer) {
        consumer.accept(QuestionStatisticsDTOAssert.assertThat(actual.getQuestionStatistics().get(index)));
        return this;
    }

}

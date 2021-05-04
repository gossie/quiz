package team.undefined.quiz.web;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import team.undefined.quiz.core.ParticipantStatisticAssert;
import team.undefined.quiz.core.ParticipantStatistics;
import team.undefined.quiz.core.QuestionStatisticsAssert;

import java.util.UUID;
import java.util.function.Consumer;

public class ParticipantStatisticsDTOAssert extends AbstractAssert<ParticipantStatisticsDTOAssert, ParticipantStatisticsDTO> {

    private ParticipantStatisticsDTOAssert(ParticipantStatisticsDTO participantStatisticsDTO) {
        super(participantStatisticsDTO, ParticipantStatisticsDTOAssert.class);
    }

    public static ParticipantStatisticsDTOAssert assertThat(ParticipantStatisticsDTO participantStatisticsDTO) {
        return new ParticipantStatisticsDTOAssert(participantStatisticsDTO);
    }

    public ParticipantStatisticsDTOAssert hasParticipant(ParticipantDTO participant) {
        Assertions.assertThat(actual.getParticipant()).isEqualTo(participant);
        return this;
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

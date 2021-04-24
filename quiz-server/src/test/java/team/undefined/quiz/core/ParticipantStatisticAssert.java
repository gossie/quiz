package team.undefined.quiz.core;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import java.util.UUID;
import java.util.function.Consumer;

public class ParticipantStatisticAssert extends AbstractAssert<ParticipantStatisticAssert, ParticipantStatistics> {

    private ParticipantStatisticAssert(ParticipantStatistics participantStatistics) {
        super(participantStatistics, ParticipantStatisticAssert.class);
    }

    public static ParticipantStatisticAssert assertThat(ParticipantStatistics participantStatistics) {
        return new ParticipantStatisticAssert(participantStatistics);
    }

    public ParticipantStatisticAssert hasParticipantId(UUID participantId) {
        Assertions.assertThat(actual.getParticipantId()).isEqualTo(participantId);
        return this;
    }

    public ParticipantStatisticAssert questionStatisticsSizeIs(int size) {
        Assertions.assertThat(actual.getQuestionStatistics()).hasSize(size);
        return this;
    }

    public ParticipantStatisticAssert hasQuestionStatistics(int index, Consumer<QuestionStatisticsAssert> consumer) {
        consumer.accept(QuestionStatisticsAssert.assertThat(actual.getQuestionStatistics().get(index)));
        return this;
    }

}

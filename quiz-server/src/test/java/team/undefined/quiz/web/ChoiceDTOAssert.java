package team.undefined.quiz.web;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

public class ChoiceDTOAssert extends AbstractAssert<ChoiceDTOAssert, ChoiceDTO> {

    private ChoiceDTOAssert(ChoiceDTO choiceDTO) {
        super(choiceDTO, ChoiceDTOAssert.class);
    }

    public static ChoiceDTOAssert assertThat(ChoiceDTO actual) {
        return new ChoiceDTOAssert(actual);
    }

    public ChoiceDTOAssert hasChoice(String choice) {
        Assertions.assertThat(actual.getChoice()).isEqualTo(choice);
        return this;
    }

}

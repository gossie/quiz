import React from 'react';
import { useTranslation } from 'react-i18next';
import { connect } from 'react-redux';
import Quiz, { Participant } from "../quiz";
import Answers from '../../Answers/Answers';
import './ParticipantItem.scss';
import { showError } from '../../redux/actions';

interface StateProps {}

interface DispatchProps {
    showError: (errorMessage: string) => void;
}

interface OwnProps {
    quiz: Quiz;
    participant: Participant;
    pointsAfterLastQuestion: number;
}

type ParticipantProps = StateProps & DispatchProps & OwnProps;

const ParticipantItem: React.FC<ParticipantProps> = (props: ParticipantProps) => {

    const { t } = useTranslation();

    const pointDifference = () => {
        if (props.pointsAfterLastQuestion !== props.participant.points) {
            return (props.participant.points - props.pointsAfterLastQuestion > 0 ? ' +': ' ') + (props.participant.points - props.pointsAfterLastQuestion);
        }
    }

    const isEstimationQuestion = () => {
        const pendingQuestion = props.quiz.openQuestions.find(q => q.pending);
        return pendingQuestion !== undefined && pendingQuestion.estimates !== null;
    }

    const getEstimatedValue = () => {
        const pendingQuestion = props.quiz.openQuestions.find(q => q.pending);
        return pendingQuestion != null && pendingQuestion.estimates != null && pendingQuestion.estimates[props.participant.id] != null
                ? pendingQuestion.estimates[props.participant.id]
                : '';
    }

    const deleteParticipant = () => {
        const deleteUrl = props.participant.links.find(link => link.rel === 'delete').href;
        fetch(`${process.env.REACT_APP_BASE_URL}${deleteUrl}`, {
            method: 'DELETE'
        })
        .then(response => {
            if (response.status === 409) {
                response.json().then(json => props.showError(t(json.message)));
            }
        });
    };

    return <div data-testid="participant-wrapper" className="participant" >
                <div className={"participant-header"}>
                    <span data-testid="participant-name" className="participant-name">{props.participant.name}</span>
                    <div className="points">({props.pointsAfterLastQuestion}{pointDifference()})</div>
                    <div className="participant-actions">
                        { !props.participant.revealAllowed && 
                            <span data-testid="reveal-not-allowed" className="icon" title={t('titleRevealNotAllowed', { name: props.participant.name })}><i className="fas fa-eye-slash"></i></span> 
                        }
                        <span data-testid="delete" className="icon clickable has-text-danger" title={t('titleDeleteParticipant')} onClick={() => deleteParticipant()}><i className="fa fa-trash"></i></span>   
                    </div>
                </div>
                <div className={'participant-answer' + (props.participant.turn || getEstimatedValue() ? ' visible': '')}>
                    <div className="bubble">
                        {props.participant.turn ? t('buzzerHint') : getEstimatedValue()}
                    </div>
                    <div className={"answer-actions"}>
                        {(props.participant.turn || isEstimationQuestion()) && <Answers quiz={props.quiz} participant={props.participant}></Answers>}
                    </div>
                </div>
                                
            </div>
}

export default connect<StateProps, DispatchProps, OwnProps>(
    null,
    {showError}
)(ParticipantItem);
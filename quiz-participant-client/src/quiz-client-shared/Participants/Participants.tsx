import React, { useEffect, useState, useCallback } from 'react';
import Quiz, { Participant } from "../quiz";
import FlipMove from "react-flip-move"
import './Participants.css';
import ParticipantItem from './ParticipantItem';

interface ParticipantsProps {
    quiz: Quiz;
}

interface ParticipantState {
    id: string;
    points: number;
}

const Participants: React.FC<ParticipantsProps> = (props: ParticipantsProps) => {
    const pendingQuestion = props.quiz.openQuestions.find(question => question.pending);
    const [stateAfterLastQuestion, setStateAfterLastQuestion] = useState(new Array<ParticipantState>()); 
    const [currentQuestionId, setCurrentQuestionId] = useState('');
 
    const comparePoints = (a: Participant, b: Participant) => {
        return b.points - a.points;
    }
 
    const getPointsAfterLastQuestionForParticipant = (participant: Participant) => {
        const participantStateAfterLastQuestion = stateAfterLastQuestion.find(p => p.id === participant.id);
        if (participantStateAfterLastQuestion) {
            return participantStateAfterLastQuestion.points;
        } else {
            const participantCurrentState = props.quiz.participants.find(p => p.id === participant.id);
            return participantCurrentState ? participantCurrentState.points : 0;
        }
    }

    const elements = props.quiz.participants?.sort(comparePoints).map(p => <div key={p.name}><ParticipantItem participant={p} pointsAfterLastQuestion={getPointsAfterLastQuestionForParticipant(p)}></ParticipantItem></div>)

    const updateStateAfterLastQuestion = useCallback(() => {
        setCurrentQuestionId(pendingQuestion ? pendingQuestion.id : '');
        setStateAfterLastQuestion(props.quiz.participants.map(p => { return {id: p.id, points: p.points}}));
    }, [props.quiz.participants, pendingQuestion]);


    const isNewQuestion = useCallback(() => {
        return pendingQuestion && currentQuestionId !== pendingQuestion.id;
    }, [pendingQuestion, currentQuestionId]); 


    useEffect(() => {
        if (isNewQuestion()) {
            updateStateAfterLastQuestion();
        } 
    }, [isNewQuestion, updateStateAfterLastQuestion]);

    return (
        <div>
            <h4 className="title is-4">Participants</h4>
            <div data-testid="participants" className="participants-list">
                <FlipMove>
                    {elements}
                </FlipMove>     
            </div>
        </div>
    )
};

export default Participants;

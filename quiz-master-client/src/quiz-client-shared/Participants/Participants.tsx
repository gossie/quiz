import React, { useState, useEffect, useCallback, useRef } from 'react';
import Quiz, { Participant } from "../quiz";
import FlipMove from "react-flip-move"

import './Participants.css';
import ParticipantItem from './ParticipantItem';
import InviteButton from '../../InviteButton/InviteButton';
import { useTranslation } from 'react-i18next';

interface ParticipantsProps {
    quiz: Quiz;
}

interface ParticipantState {
    id: string;
    points: number;
}

const Participants: React.FC<ParticipantsProps> = (props: ParticipantsProps) => {
    const buzzerAudio = useRef(null);
    const pendingQuestion = props.quiz.openQuestions.find(question => question.pending);
    const itsAPlayersTurn = props.quiz.participants.some(p => p.turn);
    const [stateAfterLastQuestion, setStateAfterLastQuestion] = useState(new Array<ParticipantState>()); 
    const [currentQuestionId, setCurrentQuestionId] = useState('');
    const [wasAPlayersTurnBefore, setWasAPlayersTurnBefore] = useState(false);
    const [revealButtonCssClasses, setRevealButtonCssClasses] = useState('button is-link');
    const [answersRevealed, setAnswersRevealed] = useState(false);

    const { t } = useTranslation();

    const getPointsAfterLastQuestionForParticipant = (participant: Participant) => {
        const participantStateAfterLastQuestion = stateAfterLastQuestion.find(p => p.id === participant.id);
        if (participantStateAfterLastQuestion) {
            return participantStateAfterLastQuestion.points;
        } else {
            const participantCurrentState = props.quiz.participants.find(p => p.id === participant.id);
            return participantCurrentState ? participantCurrentState.points : 0;
        }
    }
    
    const updateStateAfterLastQuestion = useCallback(() => {
        setCurrentQuestionId(pendingQuestion ? pendingQuestion.id : '');
        setStateAfterLastQuestion(props.quiz.participants.map(p => { return {id: p.id, points: p.points}}));
    }, [props.quiz.participants, pendingQuestion]);

    const isNewQuestion = useCallback(() => {
        return pendingQuestion && currentQuestionId !== pendingQuestion.id;
    }, [pendingQuestion, currentQuestionId]); 

    const playerHasBuzzered = useCallback(() => {
        return !wasAPlayersTurnBefore && itsAPlayersTurn; 
    }, [wasAPlayersTurnBefore, itsAPlayersTurn]);

    useEffect(() => {
        if (playerHasBuzzered()) {
            buzzerAudio.current.muted = false;
            buzzerAudio.current.play();
        }
        setWasAPlayersTurnBefore(itsAPlayersTurn)
    }, [playerHasBuzzered, itsAPlayersTurn, setWasAPlayersTurnBefore]);

    useEffect(() => {
        if (isNewQuestion()) {
            updateStateAfterLastQuestion();
            setAnswersRevealed(false);
        } 
    }, [isNewQuestion, updateStateAfterLastQuestion]);
    
    useEffect(() => {
        // Trigger preloading of audio to prevent delays when buzzer is played
        buzzerAudio.current.muted = true;
        buzzerAudio.current.play();
    }, [])

    const comparePoints = (a: Participant, b: Participant) => {
        if (b.points === a.points) {
            return a.name.localeCompare(b.name);
        } else {
            return b.points - a.points;
        }  
    }

    const elements = props.quiz.participants
        ?.sort(comparePoints)
        .map((p, index) => 
            <div key={p.name}>
                <ParticipantItem quiz={props.quiz} participant={p} pointsAfterLastQuestion={getPointsAfterLastQuestionForParticipant(p)} />
            </div>
        )

    const revealAnswers = async () => {
        setRevealButtonCssClasses('button is-link is-loading');
        const href = props.quiz.links.find(link => link.rel === 'reopenQuestion')?.href;
        fetch(`${process.env.REACT_APP_BASE_URL}${href}`, {
            method: 'PATCH'
        })
        .finally(() => {
            setRevealButtonCssClasses('button is-link');
            setAnswersRevealed(true);
        });
    };

    return (
        <div>
            <h4 className="title is-4">{t('headlineParticipants')}</h4>
            <audio src='./assets/buzzer.mp3' ref={buzzerAudio} preload='auto'></audio>
            { props.quiz.participants.length === 0 && 
                <InviteButton quizId={props.quiz.id}></InviteButton>
            }
            <div data-testid="participants" className="participants-list">
                <FlipMove>
                    {elements}
                </FlipMove>     
            </div>
            <div>
                { pendingQuestion && pendingQuestion.secondsLeft != null && <span data-testid="question-counter">{t('secondsLeft', { seconds: pendingQuestion.secondsLeft })}</span> }
            </div>
            <div>
                { (pendingQuestion && pendingQuestion.estimates) &&
                    <div>
                        {
                            answersRevealed ? <span>{t('hintRevealed')}</span> : <button onClick={revealAnswers} className={revealButtonCssClasses}>{t('buttonReveal')}</button>
                        }
                    </div>
                }
            </div>
        </div>
    )
};

export default Participants;
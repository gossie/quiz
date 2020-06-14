import React, { useCallback, useEffect, useRef } from 'react';
import './Buzzer.css';
import Quiz from '../../quiz-client-shared/quiz';

const buzzerfile = require('./../../assets/buzzer.mp3');
interface BuzzerProps {
    quiz: Quiz;
    participantId: string;
}

const Buzzer: React.FC<BuzzerProps> = (props: BuzzerProps) => {
    const buzzerAudio = useRef(null);

    const isParticipantActive = props.quiz.participants.some(p => p.turn && p.id === props.participantId);

    const isCurrentQuestionOpen = !props.quiz.participants.some(p => p.turn);

    const buzzer = useCallback(() => {
        buzzerAudio.current.muted = false;
        buzzerAudio.current.play();
        const buzzerHref = props.quiz.participants
                .find(p => p.id === props.participantId)
                .links
                .find(link => link.rel === 'buzzer')
                .href;

        fetch(`${process.env.REACT_APP_BASE_URL}${buzzerHref}`, {
            method: 'PUT',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'text/plain'
            }
        })
        .then(response => {
            if (response.status !== 200) {
                throw Error('error when hitting buzzer');
            }
        })
        .catch(e => console.error(e));
    }, [props]);

    useEffect(() => {
        // Trigger preloading of audio to prevent delays when buzzer is pressed
        buzzerAudio.current.muted = true;
        buzzerAudio.current.play();
    }, [])

    useEffect(() => {
        console.log("add key listener for buzzer if question is open")
        if (isCurrentQuestionOpen) {
            console.log("question is open")
            const buzzerOnKeydown = (event) => {
                if ((event.keyCode === 32 || event.keyCode === 13)) {
                    buzzer();
                }
            }
            document.addEventListener('keydown', buzzerOnKeydown);

            return () => {
                console.log("clean buzzer keylistener");
                document.removeEventListener('keydown', buzzerOnKeydown);
            }
        }
    }, [isCurrentQuestionOpen, buzzer]);

    return (
        <span>
            <audio src={buzzerfile} ref={buzzerAudio} preload='auto'></audio>
            <button data-testid="buzzer" disabled={!isCurrentQuestionOpen} className={isParticipantActive ? 'buzzer-button active' : 'buzzer-button'} onMouseDown={buzzer}>
                {isCurrentQuestionOpen ? 
                    'I know it!' :
                    (!isParticipantActive ?
                    'Too late!'
                    :
                    'Your turn!')
                }    
            </button>
        </span>
    )
}

export default Buzzer;
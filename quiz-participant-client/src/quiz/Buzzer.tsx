import React, { useCallback, useEffect, useRef } from 'react';
import './Buzzer.css';

const buzzerfile = require('./../assets/buzzer.mp3');
interface BuzzerProps {
    isParticipantActive: boolean;
    isCurrentQuestionOpen: boolean;
    onBuzzer: Function;
}

const Buzzer: React.FC<BuzzerProps> = (props: BuzzerProps) => {
    const buzzerAudio = useRef(null);

    const buzzer = useCallback(() => {
        buzzerAudio.current.muted = false;
        buzzerAudio.current.play();
        props.onBuzzer();
    }, [props]);

    useEffect(() => {
        // Trigger preloading of audio to prevent delays when buzzer is pressed
        buzzerAudio.current.muted = true;
        buzzerAudio.current.play();
    }, [])

    useEffect(() => {
        console.log("add key listener for buzzer if question is open")
        if (props.isCurrentQuestionOpen) {
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
    }, [props.isCurrentQuestionOpen, buzzer]);

    return (
        <span>
            <audio src={buzzerfile} ref={buzzerAudio} preload='auto'></audio>
            <button disabled={!props.isCurrentQuestionOpen} className={props.isParticipantActive ? 'buzzer-button active' : 'buzzer-button'} onMouseDown={buzzer}>
                {props.isCurrentQuestionOpen ? 
                    "I know it!" :
                    (!props.isParticipantActive ?
                    "Too late!"
                    :
                    "Your turn!")
                }    
            </button>
        </span>
    )
}

export default Buzzer;
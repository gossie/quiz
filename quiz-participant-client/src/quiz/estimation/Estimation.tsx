import React, { useState } from 'react';
import Quiz from '../../quiz-client-shared/quiz';

interface EstimationProps {
    quiz: Quiz;
    participantId: string;
}

const Estimation: React.FC<EstimationProps> = (props: EstimationProps) => {
    const [estimation, setEstimation] = useState('');
    const [sendButtonCssClasses, setSendButtonCssClasses] = useState('button is-primary');

    const sendEstimation = () => {
        setSendButtonCssClasses('button is-primary is-loading');

        const buzzerHref = props.quiz.participants
            .find(p => p.id === props.participantId)
            .links
            .find(link => link.rel === 'buzzer')
            .href;

        fetch(`${process.env.REACT_APP_BASE_URL}${buzzerHref}`, {
            method: 'PUT',
            body: `${estimation}`,
            headers: {
                'Content-Type': 'text/plain'
            }
        })
        .then(response => {
            if (response.status !== 200) {
                throw Error('error when estimating');
            }
        })
        .catch(e => console.error(e))
        .finally(() => {
            setEstimation('');
            setSendButtonCssClasses('button is-primary')
        });
    }

    return (
        <span>
            <div className="field">
                <div className="control">
                    <input data-testid="estimation" value={estimation} onChange={ev => setEstimation(ev.target.value)} onKeyUp={ev => {if (ev.keyCode === 13) sendEstimation()}} className="input" type="text" placeholder="Answer" />
                </div>
            </div>
            <div className="field is-grouped">
                <div className="control">
                    <button data-testid="send" onClick={sendEstimation} className={sendButtonCssClasses}>
                        Answer
                    </button>
                </div>
            </div>
        </span>
    )
}

export default Estimation;
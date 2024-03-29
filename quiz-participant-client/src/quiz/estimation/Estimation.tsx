import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import Quiz from '../../quiz-client-shared/quiz';

interface EstimationProps {
    quiz: Quiz;
    participantId: string;
}

const Estimation: React.FC<EstimationProps> = (props: EstimationProps) => {
    const { t } = useTranslation();

    const [estimation, setEstimation] = useState('');
    const [currentAnswer, setCurrentAnswer] = useState(t('placeholderAnswer') as string);
    const [sendButtonCssClasses, setSendButtonCssClasses] = useState('button is-primary');
    const [errorMessage, setErrorMessage] = useState('');

    const pendingQuestion = props.quiz.openQuestions.find(q => q.pending);

    const disabled = pendingQuestion && (pendingQuestion.revealed || (pendingQuestion.secondsLeft != null && pendingQuestion.secondsLeft <= 0));

    useEffect(() => {
        setCurrentAnswer('');
    }, [pendingQuestion.id]);

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
            setCurrentAnswer(estimation);
            setEstimation('');
            setSendButtonCssClasses('button is-primary');
        })
        .catch(e => {
            console.error(e);
            setErrorMessage(t('errorEstimation'));
        });
    }

    const loadAnswer = () => setEstimation(currentAnswer);

    return (
        <span>
            <div className="field">
                <div className="control">
                    <div className="columns">
                        <input data-testid="estimation"
                            value={estimation}
                            onChange={ev => setEstimation(ev.target.value)}
                            onKeyUp={ev => {if (ev.keyCode === 13) sendEstimation()}}
                            className="input"
                            type="text"
                            placeholder={currentAnswer}
                            disabled={disabled} />
                        { 
                            currentAnswer &&
                            <button data-testid="load" onClick={loadAnswer} className="button" disabled={disabled}>
                                {t('buttonLoadAnswer')}
                            </button>
                        }
                    </div>
                </div>
            </div>
            <div className="field is-grouped">
                <div className="control">
                    <button data-testid="send" onClick={sendEstimation} className={sendButtonCssClasses} disabled={disabled}>
                        {t('buttonAnswer')}
                    </button>
                </div>
            </div>
            { 
                errorMessage.length > 0 &&
                <div data-testid="error-message" className="has-text-danger">
                    { errorMessage }
                </div>
            }
        </span>
    )
}

export default Estimation;
import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { connect } from "react-redux";
import Questions from './Questions/Questions';
import Participants from './quiz-client-shared/Participants/Participants';
import Quiz from './quiz-client-shared/quiz';
import { resetError } from './redux/actions';

import './QuizMaster.css';

interface OwnProps {
    quizId: string;
}

interface StateProps {
    errorMessage?: string;
}

interface DispatchProps {
    resetError: () => void; 
}

type QuizMasterProps = OwnProps & StateProps & DispatchProps

const QuizMaster: React.FC<QuizMasterProps> = (props: QuizMasterProps) => {
    const [quiz, setQuiz] = useState({} as Quiz);

    
    const { t } = useTranslation();

    useEffect(() => {
        console.debug('register for server sent events');
        const evtSource = new EventSource(`${process.env.REACT_APP_BASE_URL}/api/quiz/${props.quizId}/quiz-master`);
        
        evtSource.onerror = (e) => console.error('sse error', e);

        evtSource.addEventListener("ping", (ev: any) => console.debug('received heartbeat', ev));

        evtSource.addEventListener("quiz", (ev: any) => {
            console.debug('event', ev);
            setQuiz(JSON.parse(ev['data']));
        });

        return () => {
            console.debug('closing event stream');
            evtSource.close();
        };
    }, [props.quizId]);

    const lastChanged = () => new Date(quiz.timestamp).toDateString();
    const expires = () => {
        const expirationDate = new Date(quiz.expirationDate);
        return `${expirationDate.getDate()}.${expirationDate.getMonth() + 1}.${expirationDate.getFullYear()}`
    };


    const undo = () => {
        if (quiz.undoPossible) {
            const url = quiz.links.find(link => link.rel === 'undo').href;
            fetch(`${process.env.REACT_APP_BASE_URL}${url}`, {
                method: 'DELETE'
            });
        }
    }

    const redo = () => {
        if (quiz.redoPossible) {
            const url = quiz.links.find(link => link.rel === 'redo').href;
            fetch(`${process.env.REACT_APP_BASE_URL}${url}`, {
                method: 'POST'
            });
        }
    }

    return (
        <div className="Quiz-dashboard">
            <div>
                { props.errorMessage && 
                    <div className="modal is-active">
                        <div className="modal-background"></div>
                        <div className="modal-content">
                            <div className="box">
                                {props.errorMessage}
                            </div>
                        </div>
                        { <button data-testid="close-button" className="modal-close is-large" aria-label="close" onClick={() => props.resetError()}></button> }
                    </div>
                }
            </div>
            <div>
                { Object.keys(quiz).length > 0
                ?
                    <div>
                        <div id="timestamp">{lastChanged()}</div>
                        <div>
                            <h4 className="title is-4">{quiz.name}</h4>
                            <span className={`icon ${quiz.undoPossible ? "clickable has-text-link" : "has-text-grey-light"}`} onClick={() => undo()} title={t('titleUndo')}><i className="fas fa-undo"></i></span>
                            <span className={`icon ${quiz.redoPossible ? "clickable has-text-link" : "has-text-grey-light"}`} onClick={() => redo()} title={t('titleRedo')}><i className="fas fa-redo"></i></span>
                        </div>
                        <div className="columns">
                            <div className="column participants box">
                                <Participants quiz={quiz}></Participants>
                                
                                
                                <div id="expiration-date">
                                    {t('expirationNote', { expirationDate: expires() })}
                                </div> 
                            </div>
                            <div className="column question">
                                <Questions quiz={quiz}></Questions>
                            </div>
                        </div>
                    </div>
                :
                    <div>
                        The quiz is being loaded. This might take a moment if the application has to be woken up.
                    </div>
                }
            </div>
        </div>
    )
}

const mapStateToProps = state => {
    return { errorMessage: state.errorMessage };
};

export default connect<StateProps, DispatchProps, OwnProps>(
    mapStateToProps,
    { resetError }
)(QuizMaster);

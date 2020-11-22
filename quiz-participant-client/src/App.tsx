import React, { useState } from 'react';
import './App.scss';
import QuizDashboard from './quiz/QuizDashboard';
import LoginPageWidget, { InputInformation } from './quiz-client-shared/LoginPageWidget/LoginPageWidget';
import AppHeader from './quiz-client-shared/AppHeader/AppHeader';
import { useTranslation } from 'react-i18next';

function App() {
    const { t } = useTranslation();

    const quizIdLabel: string = t('quizIdLabel');
    const playerNameLabel: string = t('playerNameLabel');

    const getQuizIdFromUrl = () => {
        const query = new URLSearchParams(window.location.search);
        const id = query.get('quiz_id');
        return id;
    };

    const [quizId, setQuizId] = useState('');
    const [participantName, setParticipantName] = useState('');
    const [inputInformation, setInputInformation] = useState([{label: playerNameLabel, value: ''}, {label: quizIdLabel, value: getQuizIdFromUrl()}] as Array<InputInformation>);
    const [errorMessage, setErrorMessage] = useState('');

    const setQuizIDInURL = (quizId) => {
        if(window.history && window.history.pushState) {
            const query = new URLSearchParams(window.location.search);
            query.set('quiz_id', quizId);
            var newUrl = window.location.protocol + "//" + window.location.host + window.location.pathname + '?' + query.toString();
            window.history.pushState(null, document.title, newUrl);
        }
    }

    const joinQuiz = async (value) => {
        setQuizId(value[quizIdLabel]);
        setQuizIDInURL(value[quizIdLabel]);
        setParticipantName(value[playerNameLabel]);
        setInputInformation([
            { ...inputInformation[0], value: value[playerNameLabel] },
            { ...inputInformation[1], value: value[quizIdLabel] }
        ]);
    };

    const onError = (errorMessage: string) => {
        setErrorMessage(errorMessage);
        inputInformation[1].cssClass = 'is-danger';
        setInputInformation([
            inputInformation[0],
            { ...inputInformation[1], cssClass: inputInformation[1].cssClass }
        ]);
        setQuizId('');
    }

    return (
        <div className="App">
            <AppHeader title={t('title')}></AppHeader>
            <div className="App-content">
            {quizId.length > 0 && participantName.length > 0 ? 
                <QuizDashboard quizId={quizId} participantName={participantName} errorHandler={onError}></QuizDashboard> :
                <div className="container Login-page">
                    <div>
                        <LoginPageWidget title={t('headlineJoinQuiz')} inputInformation={inputInformation} buttonLabel={t('buttonJoin')} onSubmit={joinQuiz}></LoginPageWidget> 
                    </div>
                    <div className="error-container has-text-danger">
                        { errorMessage.length > 0 && <div>{errorMessage}</div> }
                    </div>
                </div>
            }
            </div>
        </div>
    );
}

export default App;

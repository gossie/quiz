import React, { useState } from 'react';
import './App.scss';
import QuizMaster from './QuizMaster';
import LoginPageWidget, { InputInformation } from './quiz-client-shared/LoginPageWidget/LoginPageWidget' 
import AppHeader from './quiz-client-shared/AppHeader/AppHeader';
import { useTranslation } from "react-i18next";


function App() {
    const [quizId, setQuizId] = useState('');
    const { t } = useTranslation();

    const quizNameLabel = t('quizNameLabel');
    const quizIdLabel = t('quizIdLabel');

    const getQuizIdFromUrl = () => {
        const query = new URLSearchParams(window.location.search);
        const id = query.get('quiz_id')
        return id;
    };

    const [joinInputInformation, setJoinInputInformation] = useState([{label: quizIdLabel, value: getQuizIdFromUrl(), focus: true}] as Array<InputInformation>);
    const [createInputInformation, setCreateInputInformation] = useState([{label: quizNameLabel, value: ''}] as Array<InputInformation>);

    const setQuizIDInURL = (quizId) => {
        if(window.history && window.history.pushState) {
            const query = new URLSearchParams(window.location.search);
            query.set('quiz_id', quizId);
            var newUrl = window.location.protocol + "//" + window.location.host + window.location.pathname + '?' + query.toString();
            window.history.pushState(null, document.title, newUrl);
        }
    }

    const startQuiz = async (value: any) => {
        const quizResponse = await fetch(`${process.env.REACT_APP_BASE_URL}/api/quiz/`, {
            method: 'POST',
            body: JSON.stringify({
                name: value[quizNameLabel]
            }),
            headers: {
                'Content-Type': 'application/json',
                Accept: 'text/plain'
            }
        });
        const responseStatus = await quizResponse.status;
        if (responseStatus === 201) {
            const responseBody = await quizResponse.text();
            setQuizId(responseBody);
            setQuizIDInURL(responseBody);
        }  
    };

    const joinQuiz = (value: any) => {  
        setQuizIDInURL(value[quizIdLabel]);
        return Promise.resolve(setQuizId(value[quizIdLabel]));
    }
    

    return (
        <div className="App">
            <AppHeader quizId={quizId} title={t('title')}></AppHeader>
            <div className="App-content">
                { quizId.length > 0
                    ?
                    <QuizMaster quizId={quizId}></QuizMaster>
                    :
                        <div className="container Login-page">
                            <LoginPageWidget title={t('headlineCreate')} inputInformation={createInputInformation} buttonLabel={t('buttonStart')} onSubmit={startQuiz}></LoginPageWidget>
                            <LoginPageWidget title={t('headlineOpen')} inputInformation={joinInputInformation} onSubmit={joinQuiz} buttonLabel={t('buttonJoin')} ></LoginPageWidget>
                        </div>
                }
            </div>
        </div>
    );
}

export default App;

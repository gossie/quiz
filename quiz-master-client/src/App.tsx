import React, { useState } from 'react';
import './App.scss';
import QuizMaster from './QuizMaster';
import LoginPageWidget from './quiz-client-shared/LoginPageWidget/LoginPageWidget' 
import AppHeader from './quiz-client-shared/AppHeader/AppHeader';
import i18n from "i18next";
import { useTranslation, initReactI18next } from "react-i18next";
import LanguageDetector from 'i18next-browser-languagedetector';


i18n
    .use(initReactI18next) // passes i18n down to react-i18next
    .use(LanguageDetector)
    .init({
        resources: {
            en: {
                translation: {
                    title: "Quiz Master",
                    headlineCreate: "Create a quiz",
                    headlineOpen: "Open a quiz",
                    buttonStart: "Start!",
                    buttonJoin: "Join!"
                }
            },
            de: {
                translation: {
                    title: "Quiz Master",
                    headlineCreate: "Neues Quiz erstellen",
                    headlineOpen: "Quiz öffnen",
                    buttonStart: "Start!",
                    buttonJoin: "Beitreten!"
                }
            }
        },
        fallbackLng: "en",
        interpolation: {
            escapeValue: false
        }
    });


function App() {
    const [quizId, setQuizId] = useState('');
    const { t } = useTranslation();

    const quizNameLabel = 'Quiz Name';
    const quizIdLabel = 'Quiz Id';

    const getQuizIdFromUrl = () => {
        const query = new URLSearchParams(window.location.search);
        const id = query.get('quiz_id')
        return id;
    };

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
                            <LoginPageWidget title={t('headlineCreate')} inputLabels={[quizNameLabel]} buttonLabel={t('buttonStart')} onSubmit={startQuiz}></LoginPageWidget>
                            <LoginPageWidget title={t('headlineOpen')} inputValues={{[quizIdLabel]: getQuizIdFromUrl()}} inputLabels={[quizIdLabel]} buttonLabel={t('buttonJoin')} onSubmit={joinQuiz}></LoginPageWidget>
                        </div>
                }
            </div>
        </div>
    );
}

export default App;

import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import Quiz, { Question } from '../../quiz-client-shared/quiz';

import './QuestionForm.css';

interface QuestionFormProps {
    quiz: Quiz;
    questionToChange?: Question;
    onSubmit?: () => void;
}

const QuestionForm: React.FC<QuestionFormProps> = (props: QuestionFormProps) => {
    const [newQuestion, setNewQuestion] = useState(props.questionToChange?.question);
    const [category, setCategory] = useState(props.questionToChange ? props.questionToChange.category : 'other');
    const [imagePath, setImagePath] = useState(props.questionToChange?.imagePath);
    const [timeToAnswer, setTimeToAnswer] = useState(props.questionToChange?.timeToAnswer ? `${props.questionToChange?.timeToAnswer}` : '');
    const [questionButtonCssClasses, setQuestionButtonCssClasses] = useState('button is-link');
    const [visibility, setVisibility] = useState(props.questionToChange ? props.questionToChange.publicVisible : false);
    const [estimation, setEstimation] = useState(props.questionToChange ? (props.questionToChange.estimates !== null && props.questionToChange.estimates !== undefined) : false);

    const { t } = useTranslation();
    
    const createQuestion = async () => {
        setQuestionButtonCssClasses('button is-link is-loading');
        let questionLink: string;
        let method: string;
        if (props.questionToChange) {
            questionLink = props.questionToChange.links.find(link => link.rel === 'self')?.href;
            method = 'PUT';
        } else {
            questionLink = props.quiz.links.find(link => link.rel === 'createQuestion')?.href;
            method = 'POST';
        }

        await fetch(`${process.env.REACT_APP_BASE_URL}${questionLink}`, {
            method: method,
            body: JSON.stringify({
                question: newQuestion,
                category: category,
                timeToAnswer: parseInt(timeToAnswer),
                imagePath: imagePath,
                publicVisible: visibility,
                estimates: estimation ? {} : undefined,
                previousQuestionId: props.questionToChange?.previousQuestionId
            }),
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json'
            }
        });
        
        setNewQuestion('');
        setCategory('other');
        setTimeToAnswer('');
        setImagePath('');
        setQuestionButtonCssClasses('button is-link');

        props.onSubmit && props.onSubmit();
    };

    return (
        <div>
            <div className="field">
                <label className="label">{t('labelQuestion')}</label>
                <div className="control">
                    <input data-testid={props.questionToChange ? 'question-to-edit' : 'new-question'} value={newQuestion || ''} onChange={ev => setNewQuestion(ev.target.value)} className="input" type="text" />
                </div>
            </div>
            
            <div className="field">
                <label className="label">{t('labelCategory')}</label>
                <div className="control">
                    <div className="select">
                        <select data-testid={props.questionToChange ? 'category-to-edit' : 'category'} value={category || null} onChange={ev => setCategory(ev.target.value)}>
                            <option value="other">{t('categoryOther')}</option>
                            <option value="history">{t('categoryHistory')}</option>
                            <option value="science">{t('categoryScience')}</option>
                            <option value="politics">{t('categoryPolitics')}</option>
                            <option value="geography">{t('categoryGeography')}</option>
                            <option value="literature">{t('categoryLiterature')}</option>
                            <option value="music">{t('categoryMusic')}</option>
                            <option value="movies">{t('categoryMovies')}</option>
                            <option value="sport">{t('categorySport')}</option>
                        </select>
                    </div>
                </div>
            </div>

            <div className="field">
                <label className="label">{t('labelSecondsToAnswer')}</label>
                <div className="control">
                    <input data-testid={props.questionToChange ? 'time-to-answer-to-edit' : 'time-to-answer'} value={timeToAnswer  || ''} onChange={ev => setTimeToAnswer(ev.target.value)} className="input" type="text" pattern="[0-9]*"/>
                </div>
            </div>

            <div className="field">
                <label className="label">{t('labelImagePath')}</label>
                <div className="control">
                    <input data-testid={props.questionToChange ? 'image-path-to-edit' : 'image-path'} value={imagePath} onChange={ev => setImagePath(ev.target.value)} className="input" type="text" />
                </div>
            </div>
            <div className="field">
                <label className="label">{t('labelQuestionType')}</label>
                <div className="control">
                    <label className="radio">
                        <input data-testid={props.questionToChange ? 'type-buzzer-to-edit' : 'type-buzzer'} type="radio" name="answer" checked={!estimation} onChange={ev => setEstimation(!ev.target.checked)}/>
                        &nbsp;&nbsp;{t('radioBuzzer')}
                    </label>
                    <label className="radio">
                        <input data-testid={props.questionToChange ? 'type-estimation-to-edit' : 'type-estimation'} type="radio" name="answer" checked={estimation} onChange={ev => setEstimation(ev.target.checked)}/>
                        &nbsp;&nbsp;{t('radioFreetext')}
                    </label>
                </div>
            </div>
            <div className="field">
                <div className="control">
                    <label className="checkbox">
                        <input data-testid={props.questionToChange ? 'visibility-to-edit' : 'visibility'} type="checkbox" checked={visibility} onChange={ev => setVisibility(ev.target.checked)} />
                        &nbsp;{t('questionPoolHint')}
                    </label>
                </div>
            </div>
            <div className="field is-grouped">
                <div className="control">
                    <button data-testid={props.questionToChange ? 'edit-question-button' : 'create-question-button'} onClick={createQuestion} className={questionButtonCssClasses}>
                        {props.questionToChange ? t('buttonEditQuestion') : t('buttonAddQuestion')}
                    </button>
                </div>
            </div>
        </div>
    )
};

export default QuestionForm;

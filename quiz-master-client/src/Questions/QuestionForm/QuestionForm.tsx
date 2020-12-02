import React, { useState } from 'react';
import { connect } from 'react-redux';
import { useTranslation } from 'react-i18next';
import Quiz, { Question } from '../../quiz-client-shared/quiz';
import { showError } from '../../redux/actions';

import './QuestionForm.css';
import { setUncaughtExceptionCaptureCallback } from 'process';

enum QuestionType {
    BUZZER,
    FREETEXT,
    MULTIPLE_CHOICE
}

interface StateProps {}

interface DispatchProps {
    showError: (errorMessage: string) => void;
}

interface OwnProps {
    quiz: Quiz;
    questionToChange?: Question;
    onSubmit?: () => void;
}

type QuestionFormProps = StateProps & DispatchProps & OwnProps;

const QuestionForm: React.FC<QuestionFormProps> = (props: QuestionFormProps) => {
    const [newQuestion, setNewQuestion] = useState(props.questionToChange?.question);
    const [newAnswer, setNewAnswer] = useState(props.questionToChange?.correctAnswer);
    const [category, setCategory] = useState(props.questionToChange ? props.questionToChange.category : 'other');
    const [imagePath, setImagePath] = useState(props.questionToChange?.imagePath);
    const [timeToAnswer, setTimeToAnswer] = useState(props.questionToChange?.timeToAnswer ? `${props.questionToChange?.timeToAnswer}` : '');
    const [questionButtonCssClasses, setQuestionButtonCssClasses] = useState('button is-link');
    const [visibility, setVisibility] = useState(props.questionToChange ? props.questionToChange.publicVisible : false);
    const [questionType, setQuestionType] = useState(props.questionToChange?.choices != null ? QuestionType.MULTIPLE_CHOICE : props.questionToChange?.estimates != null ? QuestionType.FREETEXT : QuestionType.BUZZER);
    const [newChoice, setNewChoice] = useState('');
    const [choices, setChoices] = useState(props.questionToChange?.choices?.map(c => c.choice) ?? []);

    const { t } = useTranslation();

    const addOptionToChoices = () => {
        setChoices(oldChoices => [...oldChoices, newChoice]);
        setNewChoice('');
    };

    const deleteOptionFromChoices = (index: number) => {
        setChoices(oldChoices => {
            const copy = [...oldChoices];
            copy.splice(index, 1)
            return copy
        });
    }

    const choiceElements = choices.map(
        (choice, index) => 
            <div className="multiple-choice-option">
                <span className="text">{choice}</span>
                <span data-testid={`delete-multiple-choice-option-${index}`} className="icon clickable has-text-danger" title={t('titleDeleteMultipleChoiceOption')} onClick={() => deleteOptionFromChoices(index)}><i className="fa fa-trash"></i></span>
            </div>
    );

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

        fetch(`${process.env.REACT_APP_BASE_URL}${questionLink}`, {
            method: method,
            body: JSON.stringify({
                question: newQuestion,
                correctAnswer: newAnswer,
                category: category,
                timeToAnswer: parseInt(timeToAnswer),
                imagePath: imagePath,
                publicVisible: visibility,
                estimates: questionType === QuestionType.FREETEXT || questionType === QuestionType.MULTIPLE_CHOICE ? {} : undefined,
                choices: questionType === QuestionType.MULTIPLE_CHOICE ? choices.map(c => ({ choice: c })) : undefined,
                previousQuestionId: props.questionToChange?.previousQuestionId
            }),
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json'
            }
        })
        .then(response => {
            if (response.status === 409) {
                props.showError(t('errorMessageConflict'));
            }

            setNewQuestion('');
            setNewAnswer('');
            setCategory('other');
            setTimeToAnswer('');
            setImagePath('');
            setQuestionButtonCssClasses('button is-link');
            setChoices([]);

            props.onSubmit && props.onSubmit();
        });
    };

    return (
        <div>
            <div className="field">
                <label className="label">{t('labelQuestion')}</label>
                <div className="control">
                    <input data-testid={props.questionToChange ? 'question-to-edit' : 'new-question'} value={newQuestion ?? ''} onChange={ev => setNewQuestion(ev.target.value)} className="input" type="text" />
                </div>
            </div>

            <div className="field">
                <label className="label">{t('labelAnswer')}</label>
                <div className="control">
                    <input data-testid={props.questionToChange ? 'answer-to-edit' : 'new-correct-answer'} value={newAnswer ?? ''} onChange={ev => setNewAnswer(ev.target.value)} className="input" type="text" />
                </div>
            </div>
            
            <div className="field">
                <label className="label">{t('labelCategory')}</label>
                <div className="control">
                    <div className="select">
                        <select data-testid={props.questionToChange ? 'category-to-edit' : 'category'} value={category} onChange={ev => setCategory(ev.target.value)}>
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
                    <input data-testid={props.questionToChange ? 'time-to-answer-to-edit' : 'time-to-answer'} value={timeToAnswer ?? ''} onChange={ev => setTimeToAnswer(ev.target.value)} className="input" type="text" pattern="[0-9]*"/>
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
                        <input data-testid={props.questionToChange ? 'type-buzzer-to-edit' : 'type-buzzer'} type="radio" name="answer" checked={questionType == QuestionType.BUZZER} onChange={ev => setQuestionType(QuestionType.BUZZER)}/>
                        &nbsp;&nbsp;{t('radioBuzzer')}
                    </label>
                    <label className="radio">
                        <input data-testid={props.questionToChange ? 'type-estimation-to-edit' : 'type-estimation'} type="radio" name="answer" checked={questionType == QuestionType.FREETEXT} onChange={ev => setQuestionType(QuestionType.FREETEXT)}/>
                        &nbsp;&nbsp;{t('radioFreetext')}
                    </label>
                    <label className="radio">
                        <input data-testid={props.questionToChange ? 'type-multiple-choice-to-edit' : 'type-multiple-choice'} type="radio" name="answer" checked={questionType == QuestionType.MULTIPLE_CHOICE} onChange={ev => setQuestionType(QuestionType.MULTIPLE_CHOICE)}/>
                        &nbsp;&nbsp;{t('radioMultipleChoice')}
                    </label>
                </div>
            </div>
            { questionType == QuestionType.MULTIPLE_CHOICE &&
                <div data-testid="choices" className="field">
                    <div className="field">
                        <label className="label">{t('labelOption')}</label>
                        <div className="multiple-choice-options">
                            {choiceElements}
                        </div>
                        <div className="control">
                            <input data-testid="new-choice" value={newChoice} onChange={ev => setNewChoice(ev.target.value)} onKeyUp={ev => {if (ev.keyCode === 13) addOptionToChoices()}} className="input new-multiple-choice-option" type="text" />
                            <span data-testid="add-option" className="icon has-text-secondary clickable multiple-choice-add-option" title={t('titleAddMultipleChoiceOption')} onClick={addOptionToChoices}><i className="fas fa-plus"></i></span>
                        </div>
                    </div>
                </div>
            }
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

export default connect<StateProps, DispatchProps, OwnProps>(
    null,
    {showError}
)(QuestionForm);

import React, { useState } from 'react';
import Quiz from "../../quiz-client-shared/quiz";
import QuizStatistics from "../../quiz-client-shared/QuizStatistics/QuizStatistics";
import { useTranslation } from 'react-i18next';

export interface FinishQuizProps {
    quiz: Quiz;
}

const FinishQuiz: React.FC<FinishQuizProps> = (props: FinishQuizProps) => {
    const [forceStatistics, setForceStatistics] = useState(false);
    const [finishButtonCssClasses, setFinishButtonCssClasses] = useState('button is-primary level-right');
    const [finishQuizModalOpen, setFinishQuizModalOpen] = useState(false);

    const { t } = useTranslation();
    
    const finishQuiz = () => {
        setFinishQuizModalOpen(false);
        setFinishButtonCssClasses('button is-primary is-loading');
        fetch(`${process.env.REACT_APP_BASE_URL}/api/quiz/${props.quiz.id}`, {
            method: 'POST'
        })
        .finally(() => setFinishButtonCssClasses('button is-primary level-right'));
    }

    return (
        <div>   
            <div className="level not-responsive">
                <div></div>
                { props.quiz.quizStatistics
                ?
                    <button className={finishButtonCssClasses} onClick={() => setForceStatistics(true)}>{t('buttonShowStatistics')}</button>
                :
                    <button className={finishButtonCssClasses} onClick={() => setFinishQuizModalOpen(true)}>{t('buttonFinishQuiz')}</button>
                }
            </div>
            {finishQuizModalOpen &&
                <div data-testid="finish-quiz-warning" className="modal is-active">
                        <div className="modal-background"></div>
                        <div className="modal-card">
                            <section className="modal-card-body">
                                {t('finishQuizNote')}
                            </section>
                            <footer className="modal-card-foot">
                                <button className="button is-primary" onClick={finishQuiz}>{t('buttonFinishQuiz')}</button>
                                <button className="button" onClick={() => setFinishQuizModalOpen(false)}>{t('buttonCancel')}</button>
                            </footer>
                        </div>
                </div>
            }
            <QuizStatistics quiz={props.quiz} closeable={true} forceOpen={forceStatistics} onClose={() => setForceStatistics(false)}></QuizStatistics>
        </div>
    );
}


export default FinishQuiz;

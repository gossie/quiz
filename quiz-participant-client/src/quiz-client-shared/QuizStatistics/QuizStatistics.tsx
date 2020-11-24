import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import Quiz, { AnswerStatistics } from "../quiz";
import './QuizStatistics.scss';

interface QuizStatisticsProps {
    quiz: Quiz;
    closeable: boolean;
    forceOpen?: boolean
    onClose?: () => void;
}

const QuizStatistics: React.FC<QuizStatisticsProps> = (props: QuizStatisticsProps) => {

    const [closed, setClosed] = useState(false);

    const { t } = useTranslation();

    const determineRows = () => {
        const buzzers = (answerStatistics: Array<AnswerStatistics>) => answerStatistics.map((answerStatistic, index) => {
            if (answerStatistic.answer) {
                if (answerStatistic.participant.revealAllowed) {
                    return <li data-testid={`answer-statistic-${index}`} key={index} className="answer-statistic">{t('answerStatisticAllowedAnswer', { participantName: answerStatistic.participant.name, answer: answerStatistic.answer, time: answerStatistic.duration / 1000, rating: answerStatistic.rating })}</li>
                } else {
                    return <li data-testid={`answer-statistic-${index}`} key={index} className="answer-statistic">{t('answerStatisticWithoutAnswer', { participantName: answerStatistic.participant.name, time: answerStatistic.duration / 1000, rating: answerStatistic.rating })}</li>
                }
            } else {
                return <li data-testid={`answer-statistic-${index}`} key={index} className="answer-statistic">{t('buzzerStatistic', { participantName: answerStatistic.participant.name, time: answerStatistic.duration / 1000, rating: answerStatistic.rating })}</li>
            }
        });

        return props.quiz.quizStatistics?.questionStatistics.map(questionStatistic => 
                <tr key={questionStatistic.question.id}>
                    <td>{questionStatistic.question.question}</td>
                    <td>
                        <ul>{buzzers(questionStatistic.answerStatistics)}</ul>
                    </td>
                    
                </tr>
        );
    }

    const close = () => {
        setClosed(true);
        if (props.onClose) {
            props.onClose();
        }
    };

    return (
        <div>
            { props.quiz.quizStatistics && (props.forceOpen || !closed) && 
                <div data-testid="quiz-statistics" className="modal is-active">
                    <div className="modal-background"></div>
                    <div className="modal-content">
                        <table className="table box">
                            <thead>
                                <tr>
                                    <th>{t('columnHeadlineQuestions')}</th>
                                    <th>{t('columnHeadlineAnswers')}</th>
                                </tr>
                            </thead>
                            <tbody>
                                {determineRows()}
                            </tbody>
                        </table>
                    </div>
                    { props.closeable && <button data-testid="close-button" className="modal-close is-large" aria-label="close" onClick={close}></button> }
                </div>
            }
        </div>
    );

}

export default QuizStatistics;

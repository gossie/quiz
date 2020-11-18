import React, { useState } from 'react';
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

    const determineRows = () => {
        const buzzers = (answerStatistics: Array<AnswerStatistics>) => answerStatistics.map((answerStatistic, index) => {
            if (answerStatistic.answer) {
                return <li data-testid={`answer-statistic-${index}`} key={index} className="answer-statistic">{answerStatistic.participant.name} has answered "{answerStatistic.answer}" after {answerStatistic.duration / 1000} seconds and it was {answerStatistic.rating}</li>
            } else {
                return <li data-testid={`answer-statistic-${index}`} key={index} className="answer-statistic">{answerStatistic.participant.name} has answered after {answerStatistic.duration / 1000} seconds and it was {answerStatistic.rating}</li>
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
                                    <th>Question</th>
                                    <th>Answers</th>
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

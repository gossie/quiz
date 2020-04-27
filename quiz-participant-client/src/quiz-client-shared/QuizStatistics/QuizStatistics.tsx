import React from 'react';
import Quiz, { BuzzerStatistics } from "../quiz";

interface QuizStatisticsProps {
    quiz: Quiz;
}

const QuizStatistics: React.FC<QuizStatisticsProps> = (props: QuizStatisticsProps) => {

    const determineRows = () => {
        const buzzers = (buzzerStatistics: Array<BuzzerStatistics>) => buzzerStatistics.map((buzzerStatistic, index) => 
                <li key={index}>{buzzerStatistic.participant.name} has buzzered after {buzzerStatistic.duration / 1000} seconds and the answer was {buzzerStatistic.answer}</li>
        );

        return props.quiz.quizStatistics?.questionStatistics.map(questionStatistic => 
                <tr key={questionStatistic.question.id}>
                    <td>{questionStatistic.question.question}</td>
                    <td>
                        <ul>{buzzers(questionStatistic.buzzerStatistics)}</ul>
                    </td>
                </tr>
        );
    }

    return (
        <div>
            { props.quiz.quizStatistics && 
                <div data-testid="quiz-statistics" className="modal is-active">
                    <div className="modal-background"></div>
                    <div className="modal-content">
                        <table className="table box">
                            <thead>
                                <tr>
                                    <th>Question</th>
                                    <th>Buzzers</th>
                                </tr>
                            </thead>
                            <tbody>
                                {determineRows()}
                            </tbody>
                        </table>
                    </div>
                </div>
            }
        </div>
    );

}

export default QuizStatistics;

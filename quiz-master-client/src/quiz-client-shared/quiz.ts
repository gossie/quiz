interface Link {
    rel: string;
    href: string;
}

export interface Participant {
    id: string;
    name: string;
    turn: boolean;
    points: number;
    revealAllowed: boolean;
    links: Array<Link>;
}

export interface Choice {
    choice: string;
}

export interface Question {
    id: string;
    question: string;
    correctAnswer?: string;
    category: string;
    timeToAnswer?: number;
    secondsLeft?: number;
    imagePath?: string;
    publicVisible: boolean;
    estimates?: object;
    choices?: Array<Choice>;
    pending: boolean;
    links: Array<Link>;
    previousQuestionId?: string;
    points?: number;
}

export interface AnswerStatistics {
    participant: Participant;
    duration: number;
    rating: string;
    answer?: string;
}

export interface QuestionStatistics {
    question: Question;
    answerStatistics: Array<AnswerStatistics>;
}

export interface QuizStatistics {
    questionStatistics: Array<QuestionStatistics>
}

export default interface Quiz {
    id: string;
    name: string;
    points: number;
    participants: Array<Participant>;
    playedQuestions: Array<Question>;
    openQuestions: Array<Question>;
    quizStatistics?: QuizStatistics;
    undoPossible?: boolean;
    redoPossible?: boolean;
    timestamp: number;
    expirationDate: number;
    links: Array<Link>;
}

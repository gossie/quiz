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

export interface Question {
    id: string;
    question: string;
    category: string;
    timeToAnswer?: number;
    secondsLeft?: number;
    imagePath?: string;
    publicVisible: boolean;
    estimates?: object;
    pending: boolean;
    links: Array<Link>;
}

export interface BuzzerStatistics {
    participant: Participant;
    duration: number;
    answer: string;
}

export interface QuestionStatistics {
    question: Question;
    buzzerStatistics: Array<BuzzerStatistics>;
}

export interface QuizStatistics {
    questionStatistics: Array<QuestionStatistics>
}

export default interface Quiz {
    id: string;
    name: string;
    participants: Array<Participant>;
    playedQuestions: Array<Question>;
    openQuestions: Array<Question>;
    quizStatistics?: QuizStatistics;
    undoPossible?: boolean;
    redoPossible?: boolean;
    timestamp: number;
    links: Array<Link>;
}

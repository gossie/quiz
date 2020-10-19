interface Link {
    rel: string;
    href: string;
}

export interface Participant {
    id: string;
    name: string;
    turn: boolean;
    points: number;
    links: Array<Link>;
}

export interface Question {
    id: string;
    question: string;
    imagePath?: string;
    estimates?: object,
    secondsLeft?: number;
    pending: boolean;
    revealed: boolean;
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
    turn?: string;
    participants: Array<Participant>;
    openQuestions: Array<Question>;
    quizStatistics?: QuizStatistics;
    links: Array<Link>;
}

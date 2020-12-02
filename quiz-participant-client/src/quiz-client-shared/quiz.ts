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
    links: Array<Link>;
}

export interface Question {
    id: string;
    question: string;
    imagePath?: string;
    estimates?: object,
    choices?: Array<Choice>;
    secondsLeft?: number;
    pending: boolean;
    revealed: boolean;
    links: Array<Link>;
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
    turn?: string;
    participants: Array<Participant>;
    openQuestions: Array<Question>;
    quizStatistics?: QuizStatistics;
    links: Array<Link>;
}

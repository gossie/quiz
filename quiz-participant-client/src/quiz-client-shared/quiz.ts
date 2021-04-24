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
    question: string
    imagePath?: string;
    estimates?: object,
    choices?: Array<Choice>;
    points?: number;
    timeToAnswer?: number;
    secondsLeft?: number;
    pending: boolean;
    revealed: boolean;
    links: Array<Link>;
}

export interface QuestionStatistics {
    question: Question;
    ratings: Array<string>;
}

export interface ParticipantStatistics {
    participant?: Participant;
    questionStatistics: Array<QuestionStatistics>;
}

export interface QuizStatistics {
    participantStatistics: Array<ParticipantStatistics>
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

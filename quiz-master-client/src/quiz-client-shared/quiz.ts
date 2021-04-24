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

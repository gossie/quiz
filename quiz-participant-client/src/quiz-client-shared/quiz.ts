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
    id: number;
    question: string;
    imagePath: string;
    pending: boolean;
    links: Array<Link>;
}

export default interface Quiz {
    id: string;
    name: string;
    turn?: string;
    participants: Array<Participant>;
    openQuestions: Array<Question>;
    links: Array<Link>;
}

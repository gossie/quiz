interface Link {
    rel: string;
    href: string;
}

export interface Participant {
    id: number;
    name: string;
    turn: boolean;
    points: number;
    links: Array<Link>;
}

interface Question {
    question: string;
    imagePath?: string;
    pending: boolean;
    links: Array<Link>;
}

export default interface Quiz {
    id: number;
    name: string;
    participants: Array<Participant>;
    questions: Array<Question>;
    links: Array<Link>;
}
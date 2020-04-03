interface Link {
    rel: string;
    href: string;
}

interface Participant {
    id: number;
    name: string;
    turn: boolean;
    points: number;
    links: Array<Link>;
}

interface Question {
    question: string;
    pending: boolean;
    links: Array<Link>;
}

export default interface Quiz {
    id: number;
    name: string;
    turn?: string;
    participants: Array<Participant>;
    questions: Array<Question>;
    links: Array<Link>;
}

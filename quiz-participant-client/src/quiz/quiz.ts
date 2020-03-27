interface Link {
    rel: string;
    href: string;
}

export default interface Quiz {
    turn?: string;
    participants: Array<string>;
    links: Array<Link>;
}

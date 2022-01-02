import { Component, OnDestroy, OnInit } from '@angular/core';
import { DateUtils } from '@aws-amplify/core';
import { Subscription } from 'rxjs';
import { APIService, CreateEventInput, Event } from 'src/app/API.service';

@Component({
    selector: 'quiz-login-page',
    templateUrl: './login-page.component.html',
    styleUrls: ['./login-page.component.css']
})
export class LoginPageComponent implements OnInit, OnDestroy {

    public createInputInformation = {label: 'Quiz Name', value: '', focus: true};
    public joinInputInformation = {label: 'Quiz ID', value: '', focus: false};

    public events: Array<string> = []

    private subscription: Subscription | null = null;

    constructor(private api: APIService) { }

    ngOnInit(): void {
        this.getEvents();
        this.subscription = <Subscription> this.api.OnCreateEventListener.subscribe((ev: any) => this.events.push(ev.value.data.onCreateEvent.id));
    }

    ngOnDestroy(): void {
        if (this.subscription) {
            this.subscription.unsubscribe();
        }
    }

    public onCreate(quizName: string): void {
        const quizId = this.createUUID();
        const ev: CreateEventInput = {
            aggregateId: quizId,
            type: "quizCreatedEvent",
            sequenceNumber: 1,
            domainEvent: JSON.stringify({
                quizId: quizId,
                quizName: quizName
            }),
            createdAt: new Date().toISOString()
        };

        console.log('Create Quiz', quizName);
        this.api
            .CreateEvent(ev)
            .then(() => console.log("item created!"));
    }

    private getEvents(): void {
        this.api.ListEvents()
            .then(result => this.events = result.items.map(item => item!!.id))
    }

    public onJoin(quizId: string): void {
        console.log('join quiz', quizId);
    }


    private createUUID(): string {
        let dt = new Date().getTime();
        const uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            const r = (dt + Math.random()*16)%16 | 0;
            dt = Math.floor(dt/16);
            return (c=='x' ? r :(r&0x3|0x8)).toString(16);
        });
        return uuid;
    }
}

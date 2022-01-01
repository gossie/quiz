import { Component, OnInit } from '@angular/core';
import { InputInformation } from 'src/app/common-elements/login-control/input-information';

@Component({
    selector: 'quiz-login-page',
    templateUrl: './login-page.component.html',
    styleUrls: ['./login-page.component.css']
})
export class LoginPageComponent implements OnInit {

    public createInputInformation = {label: 'Quiz Name', value: '', focus: true};
    public joinInputInformation = {label: 'Quiz ID', value: '', focus: false};

    constructor() { }

    ngOnInit(): void {
    }

    public onCreate(quizName: string): void {
        console.log('Create Quiz', quizName);
    }

    public onJoin(quizId: string): void {
        console.log('join quiz', quizId);
    }
}

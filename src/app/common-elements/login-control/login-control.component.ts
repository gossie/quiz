import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { InputInformation } from './input-information';

@Component({
    selector: 'quiz-login-control',
    templateUrl: './login-control.component.html',
    styleUrls: ['./login-control.component.css']
})
export class LoginControlComponent implements OnInit {

    @Input()
    public inputInformation: InputInformation = {label: 'placeholder', focus: false};

    @Input()
    public buttonLabel = '';

    @Output()
    public buttonClick = new EventEmitter<string>();

    public value = '';

    constructor() { }

    ngOnInit(): void {
    }

    public handleButtonClick(): void {
        this.buttonClick.emit(this.value);
    }

}

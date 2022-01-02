import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { API } from 'aws-amplify';
import {APIService, Event} from "./API.service";

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent {

    public title = 'quiz';
    public createForm: FormGroup;

    constructor(private api: APIService, private fb: FormBuilder) {
        this.createForm = this.fb.group({
            name: ["", Validators.required],
        });
    }

    public createEvent(ev: Event): void {
        this.api
            .CreateEvent(ev)
            .then(() => {
                console.log("item created!");
                this.createForm.reset();
            })
    }

}

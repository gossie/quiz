import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { InputInformation } from 'src/app/common-elements/login-control/input-information';

import { LoginPageComponent } from './login-page.component';

@Component({
    selector: [
        'quiz-login-control'
    ].join(','),
    template: '',
})
class MockComponent{
    @Input()
    public inputInformation?: InputInformation
    @Input()
    public buttonLabel?: string;
    @Output()
    public buttonClick = new EventEmitter<string>();
}

describe('LoginPageComponent', () => {
    let component: LoginPageComponent;
    let fixture: ComponentFixture<LoginPageComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [ LoginPageComponent, MockComponent ]
        })
        .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(LoginPageComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});

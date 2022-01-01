import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';

import { LoginControlComponent } from './login-control.component';

describe('LoginControlComponent', () => {
    let component: LoginControlComponent;
    let fixture: ComponentFixture<LoginControlComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [ FormsModule ],
            declarations: [ LoginControlComponent ]
        })
        .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(LoginControlComponent);
        
    });

    it('should emit input value', (done) => {
        component = fixture.componentInstance;
        component.buttonLabel = 'Button Label';
        component.inputInformation = {label: 'Input Label', focus: false}
        component.buttonClick.subscribe(v => {
            expect(v).toEqual('QUIZ_ID')
            done();
        });
        fixture.detectChanges();

        fixture.debugElement.query(By.css('#login-control-input')).nativeElement.value = 'QUIZ_ID';
        const event = new Event('input', {bubbles: true, cancelable: true});
        fixture.debugElement.query(By.css('#login-control-input')).nativeElement.dispatchEvent(event);
        fixture.debugElement.query(By.css('#login-control-button')).nativeElement.click();
    });
});

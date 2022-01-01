import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginControlComponent } from './login-control/login-control.component';
import { FormsModule } from '@angular/forms';

@NgModule({
    imports: [
        CommonModule,
        FormsModule
    ],
    declarations: [
        LoginControlComponent
    ],
    exports: [
        LoginControlComponent
    ]
})
export class CommonElementsModule { }
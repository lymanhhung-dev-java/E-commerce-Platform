import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './shared/layouts/header/header.component'; // <--- Import cái này

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent], // <--- Khai báo ở đây
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {}
import {Component, Input} from '@angular/core';
import {CommonModule, NgClass} from '@angular/common';

@Component({
  selector: 'app-content-header',
  imports: [
    NgClass,
    CommonModule
  ],
  templateUrl: './content-header.html',
  styleUrl: './content-header.css',
})
export class ContentHeader {
  @Input() title: string = '';
  @Input() breadcrumb: string[] = [];

}

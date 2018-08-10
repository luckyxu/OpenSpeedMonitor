import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {MeasuredEvent} from "../../models/measured-event.model";
import {Threshold} from "../../models/threshold.model";
import {Measurand} from "../../models/measurand.model";
import {ActualMeasurandsService} from "../../services/actual-measurands.service";

@Component({
  selector: 'osm-measured-event',
  templateUrl: './measured-event.component.html',
  styleUrls: ['./measured-event.component.css']
})

export class MeasuredEventComponent implements OnInit {
  @Input() measuredEvent: MeasuredEvent;
  @Input() thresholds: Threshold[] = [];
  @Input() measuredEventList: MeasuredEvent[];
  @Output() addedMeasure = new EventEmitter();
  @Output() removeEvent = new EventEmitter();
  @Output() removeOldMeasuredEvent = new EventEmitter();
  newThreshold: Threshold;
  addThresholdDisabled: boolean = false;
  actualMeasurandList: Measurand[];


  constructor(private actualMeasurandsService: ActualMeasurandsService) {
  }

  ngOnInit() {
    if (this.thresholds) {
      this.actualMeasurandList = this.actualMeasurandsService.getActualMeasurands(this.thresholds);
      this.addThresholdDisabled = this.actualMeasurandList.length < 1;
    }

  }

  addThreshold() {
    if (!this.thresholds) {
      return;
    }
    this.addThresholdDisabled = true;
    this.actualMeasurandList = this.actualMeasurandsService.getActualMeasurands(this.thresholds);
    this.newThreshold = {} as Threshold;
    let newMeasurand = {} as Measurand;
    let newMeasuredEvent = {} as MeasuredEvent;
    let newThresholdName: string;
    newThresholdName = this.actualMeasurandList[0].name;
    newMeasuredEvent = this.measuredEvent;
    if (this.measuredEvent) {
      if (this.measuredEvent.state == "new") {
        newMeasuredEvent.state = 'new';
      } else {
        newMeasuredEvent.state = 'normal'
      }
    }
    this.newThreshold.measurand = newMeasurand;
    this.newThreshold.measurand.name = newThresholdName;
    this.newThreshold.lowerBoundary = 0;
    this.newThreshold.upperBoundary = 0;
    this.newThreshold.state = "new";
    this.newThreshold.measuredEvent = newMeasuredEvent;
    this.thresholds.push(this.newThreshold);


  }

  addedMeasuredEvent() {
    this.addedMeasure.emit()
  }
  addedThreshold(){
    this.actualMeasurandList.length < 2 ? this.addThresholdDisabled = true : this.addThresholdDisabled = false;
  }

  removeThreshold() {
    if (this.thresholds.length == 1) {
      this.removeOldMeasuredEvent.emit();
    }
    this.addThresholdDisabled = false;
  }

  cancelNewThreshold(){
    this.thresholds.pop();
    this.addThresholdDisabled = false;

  }

  cancelNewMeasuredEvent() {
    this.removeEvent.emit();
  }
}
